package io.github.akashkansara.modak.core.correction.correctionapplier

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.RegexReplace

class RegexReplaceCorrectionApplier : CorrectionApplier<RegexReplace, String> {
    private lateinit var annotation: RegexReplace

    override fun initialize(correctionAnnotation: RegexReplace) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: String?, context: CorrectionApplierContext?): CorrectionApplierResult<String> {
        if (value == null) {
            return CorrectionApplierResult.NoChange()
        }
        val regex = Regex(annotation.regexPattern)
        val replacement = annotation.replaceStr
        val replaced = value.replace(regex, replacement)
        return if (replaced != value) {
            CorrectionApplierResult.Edited(value, replaced)
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
