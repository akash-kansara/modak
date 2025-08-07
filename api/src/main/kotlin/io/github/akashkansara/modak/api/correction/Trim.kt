package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

@Correction(correctedBy = [])
annotation class Trim(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
)
