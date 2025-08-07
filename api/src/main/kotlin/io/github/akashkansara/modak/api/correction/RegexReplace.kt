package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

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
