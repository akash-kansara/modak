package io.github.akashkansara.modak.core.beanmodification

import arrow.core.Either
import arrow.core.raise.either
import com.google.common.annotations.VisibleForTesting
import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.CorrectionTarget
import io.github.akashkansara.modak.api.DefaultGroup
import io.github.akashkansara.modak.api.Path
import io.github.akashkansara.modak.core.beanmetadata.BeanInspection
import io.github.akashkansara.modak.core.beanmetadata.CorrectionMeta
import io.github.akashkansara.modak.core.beanmetadata.PropertyMetaData
import io.github.akashkansara.modak.core.beantraversal.BeanTraverser
import io.github.akashkansara.modak.core.beantraversal.TraversalCallback
import io.github.akashkansara.modak.core.beantraversal.TraversalNode
import io.github.akashkansara.modak.core.group.GroupSequenceGenerator
import io.github.akashkansara.modak.core.group.GroupSequenceIterator
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.util.BeanUtil
import io.github.akashkansara.modak.core.util.ContainerType
import io.github.akashkansara.modak.core.util.TypeUtil
import jakarta.validation.ConstraintViolation
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class BeanModifier(
    private val groupSequenceGenerator: GroupSequenceGenerator,
    private val beanInspection: BeanInspection,
    private val typeUtil: TypeUtil,
    private val beanTraverser: BeanTraverser,
    private val valueCorrector: ValueCorrector,
    private val beanUtil: BeanUtil,
) : TraversalCallback<BeanModificationContext<*>> {
    fun <T> modifyBean(
        bean: T,
        correctViolationsOnly: Boolean,
        constraintViolations: Set<ConstraintViolation<T>>? = null,
        groups: List<Class<*>>? = null,
    ): Either<InternalError, List<AppliedCorrection<T>>> {
        if (bean == null) {
            return Either.Right(emptyList())
        }
        return either {
            val sequence = groupSequenceGenerator.generateGroupSequence(groups).bind()
            val beanModificationContext = BeanModificationContext(
                bean,
                GroupSequenceIterator(sequence),
                constraintViolations ?: emptySet(),
                correctViolationsOnly,
            )
            val corrections = modifyBeanInContext(beanModificationContext).bind()
            beanModificationContext.clear()
            corrections
        }
    }

    private fun <T> modifyBeanInContext(
        beanModificationContext: BeanModificationContext<T>,
    ): Either<InternalError.BeanModificationError, List<AppliedCorrection<T>>> {
        return either {
            while (beanModificationContext.hasNextGroup()) {
                beanModificationContext.nextGroup()
                applyBeanCorrection(beanModificationContext)
                    .mapLeft {
                        InternalError.BeanModificationError(
                            it.cause,
                            it.message,
                            beanModificationContext.getCorrections(),
                        )
                    }.bind()
            }
            beanModificationContext.getCorrections()
        }
    }

    private fun <T> applyBeanCorrection(
        beanModificationContext: BeanModificationContext<T>,
    ): Either<InternalError, Unit> {
        return either {
            beanTraverser.traverse(
                beanModificationContext.root,
                this@BeanModifier,
                beanModificationContext,
            ).bind()
        }
    }

    override fun onBeanEntered(
        node: TraversalNode.Bean<BeanModificationContext<*>>,
    ): Either<InternalError, Unit> = either {}

    override fun onBeanExited(node: TraversalNode.Bean<BeanModificationContext<*>>): Either<InternalError, Unit> {
        return either {
            if (node.value == null) return@either
            val beanMetaData = beanInspection.inspect(node.value).bind() ?: return@either
            beanMetaData.correctionMetas
                .filter { shouldCorrectionBeApplied(it, node.clientContext, node.path) }
                .forEach {
                    val appliedCorrection = applyCorrection(
                        node.clientContext,
                        node.value,
                        (node.value)::class.java,
                        it,
                    ).bind()
                    if (appliedCorrection.edited) {
                        if (node.parentNode is TraversalNode.ContainerElement<*>) {
                            updateBeanInContainer(
                                node,
                                node.parentNode as TraversalNode.ContainerElement<*>,
                                appliedCorrection as CorrectionApplierResult.Edited,
                                it,
                            ).bind()
                        } else if (node.parentNode is TraversalNode.Property<*>) {
                            updateBeanInProperty(
                                node,
                                node.parentNode as TraversalNode.Property<BeanModificationContext<*>>,
                                appliedCorrection as CorrectionApplierResult.Edited,
                                it,
                            ).bind()
                        } else {
                            node.clientContext.addCorrection(
                                propertyPath = node.path,
                                oldValue = (appliedCorrection as CorrectionApplierResult.Edited).oldValue,
                                newValue = appliedCorrection.newValue,
                                correctionMeta = it,
                            )
                        }
                    }
                }
        }
    }

    private fun updateBeanInContainer(
        node: TraversalNode.Bean<BeanModificationContext<*>>,
        parentNode: TraversalNode.ContainerElement<*>,
        appliedCorrection: CorrectionApplierResult.Edited<*>,
        correctionMeta: CorrectionMeta,
    ): Either<InternalError, Unit> {
        return either {
            val index = parentNode.path.lastOrNull()?.index ?: raise(
                InternalError.BeanModificationError(null, "Failed to find index in path"),
            )
            val edited = setContainerValue(
                parentNode.containerType,
                parentNode.container,
                index,
                appliedCorrection.newValue,
            ).bind()
            if (edited) {
                node.clientContext.addCorrection(
                    propertyPath = node.path,
                    oldValue = appliedCorrection.oldValue,
                    newValue = appliedCorrection.newValue,
                    correctionMeta = correctionMeta,
                )
            }
        }
    }

    private fun updateBeanInProperty(
        node: TraversalNode.Bean<BeanModificationContext<*>>,
        parentNode: TraversalNode.Property<BeanModificationContext<*>>,
        appliedCorrection: CorrectionApplierResult.Edited<*>,
        correctionMeta: CorrectionMeta,
    ): Either<InternalError, Unit> {
        return either {
            val edited = setValue(
                parentNode.parent,
                parentNode.propertyMetaData,
                appliedCorrection.newValue,
            ).bind()
            if (edited) {
                node.clientContext.addCorrection(
                    propertyPath = node.path,
                    oldValue = appliedCorrection.oldValue,
                    newValue = appliedCorrection.newValue,
                    correctionMeta = correctionMeta,
                )
            }
        }
    }

    override fun onPropertyEntered(
        node: TraversalNode.Property<BeanModificationContext<*>>,
    ): Either<InternalError, Unit> = either {}

    override fun onPropertyExited(
        node: TraversalNode.Property<BeanModificationContext<*>>,
    ): Either<InternalError, Unit> {
        return either {
            node.propertyMetaData.correctionMetas
                .filter {
                    filterPropertyCorrections(it) && shouldCorrectionBeApplied(it, node.clientContext, node.path)
                }
                .forEach {
                    val appliedCorrection = applyCorrection(
                        node.clientContext,
                        getValue(node.parent, node.propertyMetaData).bind(),
                        node.propertyMetaData.genericType,
                        it,
                    ).bind()
                    if (appliedCorrection.edited) {
                        val edited = setValue(
                            node.parent,
                            node.propertyMetaData,
                            (appliedCorrection as CorrectionApplierResult.Edited).newValue,
                        ).bind()
                        if (edited) {
                            node.clientContext.addCorrection(
                                propertyPath = node.path,
                                oldValue = appliedCorrection.oldValue,
                                newValue = appliedCorrection.newValue,
                                correctionMeta = it,
                            )
                        }
                    }
                }
        }
    }

    override fun onContainerElementEntered(
        node: TraversalNode.ContainerElement<BeanModificationContext<*>>,
    ): Either<InternalError, Unit> = either {}

    override fun onContainerElementExited(
        node: TraversalNode.ContainerElement<BeanModificationContext<*>>,
    ): Either<InternalError, Unit> {
        return either {
            node.propertyMetaData.correctionMetas
                .filter {
                    filterContainerCorrections(it) && shouldCorrectionBeApplied(it, node.clientContext, node.path)
                }
                .forEach {
                    val index = node.path.lastOrNull()?.index ?: raise(
                        InternalError.BeanModificationError(null, "Failed to find index in path"),
                    )
                    val type = getContainerValueType(node.value, node.propertyMetaData) ?: return@forEach
                    val appliedCorrection = applyCorrection(
                        node.clientContext,
                        getContainerValue(node.containerType, node.container, index).bind(),
                        type,
                        it,
                    ).bind()
                    if (appliedCorrection.edited) {
                        val edited = setContainerValue(
                            node.containerType,
                            node.container,
                            index,
                            (appliedCorrection as CorrectionApplierResult.Edited).newValue,
                        ).bind()
                        if (edited) {
                            node.clientContext.addCorrection(
                                propertyPath = node.path,
                                oldValue = appliedCorrection.oldValue,
                                newValue = appliedCorrection.newValue,
                                correctionMeta = it,
                            )
                        }
                    }
                }
        }
    }

    private fun applyCorrection(
        beanModificationContext: BeanModificationContext<*>,
        value: Any?,
        type: Type,
        correctionMeta: CorrectionMeta,
    ): Either<InternalError.CorrectionError, CorrectionApplierResult<*>> {
        return either {
            valueCorrector.applyCorrectionForValue(
                value,
                type,
                correctionMeta,
                beanModificationContext.root as Any,
            ).bind()
        }
    }

    private fun shouldCorrectionBeApplied(
        correctionMeta: CorrectionMeta,
        beanModificationContext: BeanModificationContext<*>,
        currentPath: Path,
    ): Boolean {
        val groups = correctionMeta.groups
        val isGroupInContext = (groups.isEmpty() && beanModificationContext.currentGroup() == DefaultGroup::class.java) ||
            (groups.contains(beanModificationContext.currentGroup()))
        if (!isGroupInContext) return false
        if (!beanModificationContext.correctViolationsOnly) return true
        val constraintFilter = correctionMeta.constraintFilter
        if (constraintFilter.isEmpty()) return false
        val constraints = beanModificationContext.constraintViolationsMap[currentPath.toString()]
        return constraints?.any {
            constraintFilter.contains(it.constraintDescriptor.annotation.annotationClass.java)
        } ?: false
    }

    private fun getValue(bean: Any, property: PropertyMetaData) = Either.catch {
        beanUtil.getPropertyValue(bean, property)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to get property '${property.name}' on ${bean::class.simpleName}")
    }

    private fun setValue(bean: Any, property: PropertyMetaData, newValue: Any?) = Either.catch {
        beanUtil.setPropertyValue(bean, property, newValue)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to set property '${property.name}' on ${bean::class.simpleName}")
    }

    @VisibleForTesting
    @Suppress("UNCHECKED_CAST")
    fun getContainerValue(
        containerType: ContainerType,
        container: Any,
        index: Any,
    ) = either {
        when (containerType) {
            ContainerType.MAP -> getMapValue(container as Map<Any?, Any?>, index).bind()
            ContainerType.LIST -> getListValue(container as List<Any?>, index as Int).bind()
            ContainerType.ARRAY -> getArrayValue(container as Array<Any?>, index as Int).bind()
        }
    }

    @VisibleForTesting
    @Suppress("UNCHECKED_CAST")
    fun setContainerValue(
        containerType: ContainerType,
        container: Any,
        index: Any,
        newValue: Any?,
    ) = either {
        when (containerType) {
            ContainerType.MAP -> setMapValue(container as Map<Any?, Any?>, index, newValue).bind()
            ContainerType.LIST -> setListValue(container as List<Any?>, index as Int, newValue).bind()
            ContainerType.ARRAY -> setArrayValue(container as Array<Any?>, index as Int, newValue).bind()
        }
    }

    private fun getMapValue(map: Map<Any?, Any?>, key: Any) = Either.catch {
        beanUtil.getMapValue(map, key)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to get value in Map for key '$key'")
    }

    private fun setMapValue(map: Map<Any?, Any?>, key: Any, newValue: Any?) = Either.catch {
        beanUtil.setMapValue(map, key, newValue)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to set value in Map for key '$key'")
    }

    private fun getListValue(list: List<Any?>, index: Int) = Either.catch {
        beanUtil.getListValue(list, index)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to get value from List at index $index")
    }

    private fun setListValue(list: List<Any?>, index: Int, newValue: Any?) = Either.catch {
        beanUtil.setListValue(list, index, newValue)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to set value in List at index $index")
    }

    private fun getArrayValue(array: Array<Any?>, index: Int) = Either.catch {
        beanUtil.getArrayValue(array, index)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to get value from Array at index $index")
    }

    private fun setArrayValue(array: Array<Any?>, index: Int, newValue: Any?) = Either.catch {
        beanUtil.setArrayValue(array, index, newValue)
    }.mapLeft {
        InternalError.BeanModificationError(it, "Failed to set value in Array at index $index")
    }

    private fun getContainerValueType(
        containerValue: Any?,
        propertyMetaData: PropertyMetaData,
    ): Type? = if (containerValue != null) {
        containerValue::class.java
    } else {
        (propertyMetaData.genericType as? ParameterizedType)?.let {
            typeUtil.getParameterizedTypeArgs(it).firstOrNull()
        }
    }

    private fun filterPropertyCorrections(correctionMeta: CorrectionMeta) =
        correctionMeta.correctionTarget?.let { ct -> ct == CorrectionTarget.PROPERTY } ?: true

    private fun filterContainerCorrections(correctionMeta: CorrectionMeta) =
        correctionMeta.correctionTarget?.let { ct -> ct == CorrectionTarget.CONTAINER_ELEMENT } ?: false
}
