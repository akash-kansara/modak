package io.github.akashkansara.modak.core.beanmetadata

import arrow.core.Either
import arrow.core.raise.either
import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import io.github.akashkansara.modak.core.models.InternalError
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

class AnnotationBeanInspector : BeanInspector {
    override val configurationSource = ConfigurationSource.ANNOTATION

    fun inspect(clazz: Class<*>): Either<InternalError, BeanMetaData<*>> {
        return buildBeanMetaData(clazz, mutableSetOf())
    }

    private fun buildBeanMetaData(clazz: Class<*>, processing: MutableSet<Class<*>>): Either<InternalError, BeanMetaData<*>> {
        return either {
            if (clazz in processing) {
                raise(
                    InternalError.BeanInspectionError(
                        configurationSource,
                        null,
                        "Failed to process class hierarchy due to circular inheritance: ${clazz.name}",
                    ),
                )
            }
            processing.add(clazz)
            val beanBuilder = BeanMetaData.Builder(clazz)
            val properties = getPropertyMetaData(clazz).bind()
            beanBuilder.addPropertyMetaData(properties)
            if (clazz.superclass != null && clazz.superclass != Any::class.java && clazz.superclass != Object::class.java) {
                val superBean = buildBeanMetaData(clazz.superclass, processing).bind()
                beanBuilder.addPropertyMetaData(superBean.properties)
            }
            beanBuilder.addBeanCorrectionMeta(getmodakAnnotations(clazz).bind())
            processing.remove(clazz)
            beanBuilder.toReflectedBean()
        }
    }

    private fun getPropertyDescriptors(clazz: Class<*>): Either<InternalError, List<PropertyDescriptor>> {
        return either {
            val descriptors = Either.catch { Introspector.getBeanInfo(clazz).propertyDescriptors }
                .mapLeft {
                    InternalError.BeanInspectionError(
                        configurationSource,
                        it,
                        "Failed to inspect bean for class: ${clazz.name}",
                    )
                }
                .bind()
            descriptors.filter { it.name != "class" }.toList()
        }
    }

    private fun getFields(clazz: Class<*>): Either<InternalError, List<Field>> {
        return either {
            val fields = Either.catch { clazz.declaredFields.toList() }
                .mapLeft {
                    InternalError.BeanInspectionError(
                        configurationSource,
                        it,
                        "Failed to inspect bean for class: ${clazz.name}",
                    )
                }
                .bind()
            fields
        }
    }

    private fun getBeanProperties(clazz: Class<*>): Either<InternalError, List<BeanProperty>> {
        return either {
            val descriptors = getPropertyDescriptors(clazz).bind()
            val seenProperties = descriptors.map { it.name }.toSet()
            val fields = getFields(clazz).bind()
                .asSequence()
                .filter { !seenProperties.contains(it.name) }
            val beanProperties = mutableListOf<BeanProperty>()
            descriptors.forEach {
                val field = getField(clazz, it).bind()
                val genericType = getGenericType(field, it.readMethod, it.writeMethod)
                genericType?.let { _ ->
                    beanProperties.add(
                        BeanProperty(
                            name = it.name,
                            genericType = genericType,
                            readMethod = getReadMethod(it).bind(),
                            writeMethod = getWriteMethod(it).bind(),
                            field = getField(clazz, it).bind(),
                        ),
                    )
                }
            }
            fields.forEach {
                val genericType = getGenericType(it)
                genericType?.let { _ ->
                    beanProperties.add(
                        BeanProperty(
                            name = it.name,
                            genericType = genericType,
                            readMethod = null,
                            writeMethod = null,
                            field = it,
                        ),
                    )
                }
            }
            beanProperties.toList()
        }
    }

    private fun getPropertyMetaData(clazz: Class<*>): Either<InternalError, List<PropertyMetaData>> {
        return either {
            val beanProperties = getBeanProperties(clazz).bind()
            val properties = beanProperties.mapNotNull { buildPropertyMetaData(it).bind() }
            properties.toList()
        }
    }

    private fun buildPropertyMetaData(property: BeanProperty): Either<InternalError, PropertyMetaData?> {
        return either {
            if (property.readMethod == null && property.field == null) {
                return@either null
            }
            val annotations = mutableListOf<CorrectionMeta>()
            val modifiers = mutableSetOf<Annotation>()
            property.readMethod?.let {
                annotations.addAll(getmodakAnnotations(property.readMethod).bind())
                modifiers.addAll(getCorrectionModifiers(it))
            }
            property.writeMethod?.let {
                annotations.addAll(getmodakAnnotations(property.writeMethod).bind())
                modifiers.addAll(getCorrectionModifiers(it))
            }
            property.field?.let {
                annotations.addAll(getmodakAnnotations(property.field).bind())
                modifiers.addAll(getCorrectionModifiers(it))
            }
            PropertyMetaData(
                name = property.name,
                genericType = property.genericType,
                correctionMetas = annotations.distinctBy { it.annotation.annotationClass },
                correctionModifiers = modifiers.toSet(),
                readMethod = property.readMethod,
                writeMethod = property.writeMethod,
                field = property.field,
            )
        }
    }

    private fun getWriteMethod(propertyDescriptor: PropertyDescriptor) = Either.catch {
        propertyDescriptor.writeMethod?.apply { isAccessible = true }
    }.mapLeft {
        InternalError.BeanInspectionError(
            configurationSource,
            it,
            "Failed to inspect write method for property: ${propertyDescriptor.name}",
        )
    }

    private fun getReadMethod(propertyDescriptor: PropertyDescriptor) = Either.catch {
        propertyDescriptor.readMethod?.apply { isAccessible = true }
    }.mapLeft {
        InternalError.BeanInspectionError(
            configurationSource,
            it,
            "Failed to inspect read method for property: ${propertyDescriptor.name}",
        )
    }

    private fun getField(clazz: Class<*>, propertyDescriptor: PropertyDescriptor) = Either.catch {
        clazz.getDeclaredField(propertyDescriptor.name).apply { isAccessible = true }
    }.fold(
        ifLeft = {
            if (it is NoSuchFieldException) {
                Either.Right(null)
            } else {
                Either.Left(
                    InternalError.BeanInspectionError(
                        configurationSource,
                        it,
                        "Failed to inspect field for property: ${propertyDescriptor.name}",
                    ),
                )
            }
        },
        ifRight = {
            Either.Right(it)
        },
    )

    private fun getmodakAnnotations(annotated: AnnotatedElement) = Either.catch {
        annotated.annotations.mapNotNull {
            val dcAnnotation = findmodakAnnotation(it)
            if (dcAnnotation != null) {
                CorrectionMeta(
                    annotation = it,
                    correction = dcAnnotation as Correction,
                    correctionTarget = getCorrectionTargetMethod(it),
                    groups = getGroups(it) ?: emptySet(),
                    payload = getPayload(it) ?: emptySet(),
                    constraintFilter = getConstraintFilter(it) ?: emptySet(),
                )
            } else {
                null
            }
        }.distinctBy { it.annotation.annotationClass }
    }.mapLeft {
        InternalError.BeanInspectionError(
            configurationSource,
            it,
            "Failed to inspect data correction annotations for: ${annotated.javaClass.name}",
        )
    }

    private fun findmodakAnnotation(annotation: Annotation) =
        annotation.annotationClass.java.annotations.firstOrNull { x -> x.annotationClass.java == Correction::class.java }

    private fun getGenericType(
        field: Field? = null,
        readMethod: Method? = null,
        writeMethod: Method? = null,
    ) = when {
        field != null -> field.genericType
        readMethod != null -> readMethod.genericReturnType
        writeMethod != null -> {
            val paramTypes = writeMethod.genericParameterTypes
            if (paramTypes.isNotEmpty()) paramTypes[0] else null
        }
        else -> null
    }

    private fun getCorrectionModifiers(annotated: AnnotatedElement) =
        annotated.annotations
            ?.let {
                it.filter { x -> BeanMetaDataConstant.correctionModifiers.contains(x.annotationClass.java) }
            }
            ?: emptyList()

    private fun getGroups(annotation: Annotation) = try {
        val groupsMethod = annotation.annotationClass.java.getMethod("groups")
        val groups = groupsMethod.invoke(annotation) as? Array<*>
        val classes = mutableSetOf<Class<*>>()
        classes.addAll(groups?.filterIsInstance<Class<*>>() ?: emptyList())
        classes.addAll(groups?.filterIsInstance<KClass<*>>()?.map { it.java } ?: emptyList())
        classes.toSet()
    } catch (_: Exception) {
        null
    }

    private fun getPayload(annotation: Annotation) = try {
        val payloadMethod = annotation.annotationClass.java.getMethod("payload")
        val payload = payloadMethod.invoke(annotation) as? Array<*>
        val classes = mutableSetOf<Class<*>>()
        classes.addAll(payload?.filterIsInstance<Class<*>>() ?: emptyList())
        classes.addAll(payload?.filterIsInstance<KClass<*>>()?.map { it.java } ?: emptyList())
        classes.toSet()
    } catch (_: Exception) {
        null
    }

    private fun getConstraintFilter(annotation: Annotation) = try {
        val constraintFilterMethod = annotation.annotationClass.java.getMethod("constraintFilter")
        val constraintFilter = constraintFilterMethod.invoke(annotation) as? Array<*>
        val classes = mutableSetOf<Class<*>>()
        classes.addAll(constraintFilter?.filterIsInstance<Class<*>>() ?: emptyList())
        classes.addAll(constraintFilter?.filterIsInstance<KClass<*>>()?.map { it.java } ?: emptyList())
        classes.toSet()
    } catch (_: Exception) {
        null
    }

    private fun getCorrectionTargetMethod(annotation: Annotation) = try {
        val correctionTargetMethod = annotation.annotationClass.java.getMethod("correctionTarget")
        correctionTargetMethod.invoke(annotation) as? CorrectionTarget
    } catch (_: Exception) {
        null
    }
}
