package io.github.akashkansara.modak.core.correction.correctionapplier

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.Truncate

class TruncateCorrectionApplier : CorrectionApplier<Truncate, String> {
    private lateinit var annotation: Truncate

    override fun initialize(correctionAnnotation: Truncate) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: String?, context: CorrectionApplierContext?): CorrectionApplierResult<String> {
        if (value == null) {
            return CorrectionApplierResult.NoChange()
        }
        val maxLength = annotation.length
        return if (value.length > maxLength) {
            val truncated = if (annotation.fromEnd) {
                value.substring(0, maxLength)
            } else {
                value.substring(value.length - maxLength)
            }
            CorrectionApplierResult.Edited(value, truncated)
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
