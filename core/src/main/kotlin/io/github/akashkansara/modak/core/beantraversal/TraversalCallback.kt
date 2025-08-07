package io.github.akashkansara.modak.core.beantraversal

import arrow.core.Either
import io.github.akashkansara.modak.core.models.InternalError

interface TraversalCallback<C> {
    fun onBeanEntered(node: TraversalNode.Bean<C>): Either<InternalError, Unit>

    fun onBeanExited(node: TraversalNode.Bean<C>): Either<InternalError, Unit>

    fun onPropertyEntered(node: TraversalNode.Property<C>): Either<InternalError, Unit>

    fun onPropertyExited(node: TraversalNode.Property<C>): Either<InternalError, Unit>

    fun onContainerElementEntered(node: TraversalNode.ContainerElement<C>): Either<InternalError, Unit>

    fun onContainerElementExited(node: TraversalNode.ContainerElement<C>): Either<InternalError, Unit>
}
