package io.github.akashkansara.modak.core.correction.correctionapplier

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.Trim

class TrimCorrectionApplier : CorrectionApplier<Trim, String> {
    override fun correct(value: String?, context: CorrectionApplierContext?): CorrectionApplierResult<String> {
        if (value == null) {
            return CorrectionApplierResult.NoChange()
        }
        val newValue = value.trim()
        return if (newValue != value) {
            CorrectionApplierResult.Edited(value, newValue)
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
