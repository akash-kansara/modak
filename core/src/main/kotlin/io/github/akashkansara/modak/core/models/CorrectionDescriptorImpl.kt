package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.CorrectionDescriptor

data class CorrectionDescriptorImpl(
    override val annotation: Annotation,
    override val groups: Set<Class<*>> = emptySet(),
    override val payload: Set<Class<*>> = emptySet(),
    override val constraintFilter: Set<Class<*>> = emptySet(),
) : CorrectionDescriptor
