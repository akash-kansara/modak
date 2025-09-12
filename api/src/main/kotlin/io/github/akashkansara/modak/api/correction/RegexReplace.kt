package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

/**
 * Replaces text matching a regex pattern with a replacement string
 *
 * @param groups Validation groups for which this correction applies
 * @param payload Payload classes attached to this correction
 * @param constraintFilter Constraint types that trigger this correction
 * @param correctionTarget What should be corrected (property or container elements)
 * @param regexPattern The regex pattern to match against the string
 * @param replaceStr The string to replace the matched pattern with
 */
@Correction(correctedBy = [])
annotation class RegexReplace(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    /**
     * The regex pattern to match against the string
     */
    val regexPattern: String,
    /**
     * The string to replace the matched pattern with
     */
    val replaceStr: String,
)
