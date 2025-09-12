package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

/**
 * Removes leading and trailing whitespace from string values
 *
 * @param groups Validation groups for which this correction applies
 * @param payload Payload classes attached to this correction
 * @param constraintFilter Constraint types that trigger this correction
 * @param correctionTarget What should be corrected (property or container elements)
 */
@Correction(correctedBy = [])
annotation class Trim(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
)
