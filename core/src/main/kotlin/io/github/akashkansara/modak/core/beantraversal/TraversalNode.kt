package io.github.akashkansara.modak.core.beantraversal

import io.github.akashkansara.modak.api.Path
import io.github.akashkansara.modak.core.beanmetadata.BeanMetaData
import io.github.akashkansara.modak.core.beanmetadata.PropertyMetaData
import io.github.akashkansara.modak.core.util.ContainerType

sealed class TraversalNode<C>(
    open val value: Any?,
    open val path: Path,
    open val root: Any,
    open val clientContext: C,
) {
    data class Bean<C>(
        override val value: Any?,
        override val path: Path,
        override val root: Any,
        val parentNode: TraversalNode<C>? = null,
        val beanMetaData: BeanMetaData<*>,
        override val clientContext: C,
    ) : TraversalNode<C>(value, path, root, clientContext)

    data class Property<C>(
        val parent: Any,
        override val value: Any?,
        override val path: Path,
        override val root: Any,
        val propertyMetaData: PropertyMetaData,
        override val clientContext: C,
    ) : TraversalNode<C>(value, path, root, clientContext)

    data class ContainerElement<C>(
        val container: Any,
        val containerType: ContainerType,
        override val value: Any?,
        override val path: Path,
        override val root: Any,
        val propertyMetaData: PropertyMetaData,
        override val clientContext: C,
    ) : TraversalNode<C>(value, path, root, clientContext)
}
