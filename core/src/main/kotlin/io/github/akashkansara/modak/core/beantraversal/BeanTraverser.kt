package io.github.akashkansara.modak.core.beantraversal

import arrow.core.Either
import arrow.core.raise.either
import com.google.common.annotations.VisibleForTesting
import io.github.akashkansara.modak.api.CorrectNested
import io.github.akashkansara.modak.api.ElementKind
import io.github.akashkansara.modak.api.Path
import io.github.akashkansara.modak.core.beanmetadata.BeanInspection
import io.github.akashkansara.modak.core.beanmetadata.PropertyMetaData
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.models.PathImpl
import io.github.akashkansara.modak.core.util.BeanUtil
import io.github.akashkansara.modak.core.util.ContainerType
import io.github.akashkansara.modak.core.util.TypeUtil
import java.lang.reflect.Type

class BeanTraverser(
    private val beanInspection: BeanInspection,
    private val typeUtil: TypeUtil,
    private val beanUtil: BeanUtil,
) {
    fun <T, C> traverse(
        rootBean: T,
        callback: TraversalCallback<C>,
        clientContext: C,
    ): Either<InternalError, Unit> {
        return either {
            val context: BeanTraversalContext<T, C> = BeanTraversalContext(rootBean, clientContext, callback)
            Either.catch {
                traverseBean(context, rootBean).bind()
                context.clear()
            }.mapLeft {
                context.clear()
                InternalError.BeanTraversalError(
                    it,
                    "Error during bean traversal ${rootBean?.let { b -> b::class.simpleName }}",
                )
            }.bind()
        }
    }

    @VisibleForTesting
    fun <C> traverseBean(
        context: BeanTraversalContext<*, C>,
        bean: Any?,
    ): Either<InternalError, Unit> {
        return either {
            if (bean == null || context.hasVisited(bean)) return@either
            val beanMetaData = beanInspection.inspect(bean).bind() ?: return@either
            traversalWrapper(
                context,
                { parentPath ->
                    val beanPath = parentPath.copy()
                    beanPath.addNode(PathImpl.NodeImpl(name = null, index = null, kind = ElementKind.BEAN))
                    beanPath
                },
                { _, prevParentNode ->
                    TraversalNode.Bean(
                        value = bean,
                        path = context.getCurrentPath(),
                        root = context.root as Any,
                        parentNode = prevParentNode,
                        beanMetaData = beanMetaData,
                        clientContext = context.clientContext,
                    )
                },
                { node ->
                    either {
                        context.callback.onBeanEntered(node as TraversalNode.Bean<C>).bind()
                        beanMetaData.properties.forEach {
                            traverseBeanProperty(context, bean, it).bind()
                        }
                        context.callback.onBeanExited(node).bind()
                        context.markVisited(bean)
                    }
                },
            ).bind()
        }
    }

    @VisibleForTesting
    fun <C> traverseBeanProperty(
        context: BeanTraversalContext<*, C>,
        bean: Any,
        property: PropertyMetaData,
    ): Either<InternalError, Unit> {
        return either {
            traversalWrapper(
                context,
                { parentPath ->
                    val propertyPath = parentPath.copy()
                    propertyPath.addNode(generateNodeFromPropertyMetaData(property, ElementKind.PROPERTY))
                    propertyPath
                },
                { path, _ ->
                    TraversalNode.Property(
                        parent = bean,
                        value = getValue(bean, property).bind(),
                        path = path,
                        root = context.root as Any,
                        propertyMetaData = property,
                        clientContext = context.clientContext,
                    )
                },
                { node ->
                    either {
                        val shouldTraverseNested = shouldTraverseNested(property)
                        val containerType = getContainerType(property.genericType).bind()
                        context.callback.onPropertyEntered(node as TraversalNode.Property).bind()
                        if (containerType != null) {
                            val value = Either.catch {
                                beanUtil.getPropertyValue(bean, property)
                            }.mapLeft {
                                InternalError.BeanTraversalError(
                                    it,
                                    "Failed to get property value '${property.name}' from ${bean::class.simpleName}",
                                )
                            }.bind()
                            if (value != null) {
                                traverseContainer(context, value, property).bind()
                            }
                        } else if (shouldTraverseNested) {
                            val value = Either.catch {
                                beanUtil.getPropertyValue(bean, property)
                            }.mapLeft {
                                InternalError.BeanTraversalError(
                                    it,
                                    "Failed to get value for property '${property.name}' from ${bean::class.simpleName}",
                                )
                            }.bind()
                            value?.let { traverseBean(context, value).bind() }
                        }
                        context.callback.onPropertyExited(node).bind()
                    }
                },
            ).bind()
        }
    }

    private fun <C> traverseContainer(
        context: BeanTraversalContext<*, C>,
        value: Any,
        property: PropertyMetaData,
    ): Either<InternalError, Unit> {
        return either {
            val containerType = getContainerType(property.genericType).bind()
            when (containerType) {
                ContainerType.ARRAY -> traverseArray(context, value as Array<*>, property).bind()
                ContainerType.LIST -> traverseList(context, value as List<*>, property).bind()
                ContainerType.MAP -> traverseMap(context, value as Map<*, *>, property).bind()
                null -> {
                    // Do nothing
                }
            }
        }
    }

    @VisibleForTesting
    fun <C> traverseArray(
        context: BeanTraversalContext<*, C>,
        value: Array<*>,
        property: PropertyMetaData,
    ): Either<InternalError, Unit> {
        return either {
            traversalContainerWrapper(
                context,
                value.withIndex().map { Pair(it.index, it.value) }.iterator(),
                { parentPath, idx: Int, _: Any? ->
                    val elementPath = parentPath.copy()
                    elementPath.addNode(generateNodeFromPropertyMetaData(property, ElementKind.CONTAINER_ELEMENT, idx))
                    elementPath
                },
                { path, _, valueAtIndex ->
                    TraversalNode.ContainerElement(
                        container = value,
                        containerType = ContainerType.ARRAY,
                        value = valueAtIndex,
                        path = path,
                        root = context.root as Any,
                        propertyMetaData = property,
                        clientContext = context.clientContext,
                    )
                },
                { node, valueAtIndex ->
                    containerTraverseNodeCallback(
                        context,
                        node as TraversalNode.ContainerElement<C>,
                        valueAtIndex,
                    )
                },
            ).bind()
        }
    }

    @VisibleForTesting
    fun <C> traverseMap(
        context: BeanTraversalContext<*, C>,
        value: Map<*, *>,
        property: PropertyMetaData,
    ): Either<InternalError, Unit> {
        return either {
            traversalContainerWrapper(
                context,
                value.map { Pair(it.key, it.value) }.iterator(),
                { parentPath, key: Any?, _: Any? ->
                    val elementPath = parentPath.copy()
                    elementPath.addNode(generateNodeFromPropertyMetaData(property, ElementKind.CONTAINER_ELEMENT, key))
                    elementPath
                },
                { path, _, valueAtIndex ->
                    TraversalNode.ContainerElement(
                        container = value,
                        containerType = ContainerType.MAP,
                        value = valueAtIndex,
                        path = path,
                        root = context.root as Any,
                        propertyMetaData = property,
                        clientContext = context.clientContext,
                    )
                },
                { node, valueAtIndex ->
                    containerTraverseNodeCallback(
                        context,
                        node as TraversalNode.ContainerElement<C>,
                        valueAtIndex,
                    )
                },
            ).bind()
        }
    }

    @VisibleForTesting
    fun <C> traverseList(
        context: BeanTraversalContext<*, C>,
        value: List<*>,
        property: PropertyMetaData,
    ): Either<InternalError, Unit> {
        return either {
            traversalContainerWrapper(
                context,
                value.withIndex().map { Pair(it.index, it.value) }.iterator(),
                { parentPath, idx: Int, _: Any? ->
                    val elementPath = parentPath.copy()
                    elementPath.addNode(generateNodeFromPropertyMetaData(property, ElementKind.CONTAINER_ELEMENT, idx))
                    elementPath
                },
                { path, _, valueAtKey ->
                    TraversalNode.ContainerElement(
                        container = value,
                        containerType = ContainerType.LIST,
                        value = valueAtKey,
                        path = path,
                        root = context.root as Any,
                        propertyMetaData = property,
                        clientContext = context.clientContext,
                    )
                },
                { node, valueAtKey ->
                    containerTraverseNodeCallback(
                        context,
                        node as TraversalNode.ContainerElement<C>,
                        valueAtKey,
                    )
                },
            ).bind()
        }
    }

    private fun <C> containerTraverseNodeCallback(
        context: BeanTraversalContext<*, C>,
        node: TraversalNode.ContainerElement<C>,
        value: Any?,
    ): Either<InternalError, Unit> {
        return either {
            val shouldTraverseNested = shouldTraverseNested(node.propertyMetaData)
            context.callback.onContainerElementEntered(node).bind()
            if (shouldTraverseNested && value != null) {
                traverseBean(context, value).bind()
            }
            context.callback.onContainerElementExited(node).bind()
        }
    }

    private fun <C> traversalWrapper(
        context: BeanTraversalContext<*, C>,
        generatePath: (parentPath: Path) -> Path,
        generateTraversalNode: (path: Path, prevParentNode: TraversalNode<C>?) -> TraversalNode<C>,
        traverseNode: (node: TraversalNode<C>) -> Either<InternalError, Unit>,
    ): Either<InternalError, Unit> {
        return either {
            val parentPath = context.getCurrentPath()
            val parentNode = context.getParentNode()
            val currentPath = generatePath(parentPath)
            val currentNode = generateTraversalNode(currentPath, parentNode)
            context.setCurrentPath(currentPath)
            context.setParentNode(currentNode)
            traverseNode(currentNode).bind()
            context.setCurrentPath(parentPath)
            context.setParentNode(parentNode)
        }
    }

    private fun <C, K, V> traversalContainerWrapper(
        context: BeanTraversalContext<*, C>,
        iterator: Iterator<Pair<K, V>>,
        generatePath: (parentPath: Path, key: K, value: V) -> Path,
        generateTraversalNode: (path: Path, key: K, value: V) -> TraversalNode<C>,
        traverseNode: (node: TraversalNode<C>, value: V) -> Either<InternalError, Unit>,
    ): Either<InternalError, Unit> {
        return either {
            val parentPath = context.getCurrentPath()
            val parentNode = context.getParentNode()
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                val currentPath = generatePath(parentPath, key, value)
                val currentNode = generateTraversalNode(currentPath, key, value)
                context.setCurrentPath(currentPath)
                context.setParentNode(currentNode)
                traverseNode(currentNode, value).bind()
            }
            context.setCurrentPath(parentPath)
            context.setParentNode(parentNode)
        }
    }

    private fun shouldTraverseNested(property: PropertyMetaData) =
        property.correctionModifiers.any { it.annotationClass.java == CorrectNested::class.java }

    private fun getContainerType(type: Type): Either<InternalError, ContainerType?> = Either.catch {
        typeUtil.getContainerType(type)
    }.mapLeft { InternalError.BeanTraversalError(it, "Failed to determine container type for '${type.typeName}'") }

    private fun generateNodeFromPropertyMetaData(
        property: PropertyMetaData,
        kind: ElementKind,
        index: Any? = null,
    ): PathImpl.NodeImpl {
        return PathImpl.NodeImpl(
            name = property.name,
            index = index,
            kind = kind,
        )
    }

    private fun getValue(bean: Any, property: PropertyMetaData) = Either.catch {
        beanUtil.getPropertyValue(bean, property)
    }.mapLeft {
        InternalError.BeanTraversalError(it, "Failed to get value for property '${property.name}' from ${bean::class.simpleName}")
    }
}
