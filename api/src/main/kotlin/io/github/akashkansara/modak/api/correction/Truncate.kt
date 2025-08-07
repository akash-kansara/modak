package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

@Correction(correctedBy = [])
annotation class Truncate(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    /**
     * The maximum allowed length for the field value.
     * Values exceeding this length will be truncated.
     * Must be a positive integer.
     */
    val length: Int,
    /**
     * Whether to truncate from the start or the end of the string.
     * If true, truncates from the end; if false, truncates from the start.
     * Defaults to true
     */
    val fromEnd: Boolean = true,
)
