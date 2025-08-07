package io.github.akashkansara.modak.core.beanmetadata

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget

data class CorrectionMeta(
    val annotation: Annotation,
    val correction: Correction,
    val correctionTarget: CorrectionTarget? = null,
    val groups: Set<Class<*>> = emptySet(),
    val payload: Set<Class<*>> = emptySet(),
    val constraintFilter: Set<Class<*>> = emptySet(),
)
