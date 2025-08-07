package io.github.akashkansara.modak.core.correction.correctionapplier

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.DefaultValue

class BooleanDefaultValueCorrectionApplier : CorrectionApplier<DefaultValue, Boolean> {
    private lateinit var annotation: DefaultValue

    override fun initialize(correctionAnnotation: DefaultValue) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: Boolean?, context: CorrectionApplierContext?): CorrectionApplierResult<Boolean> {
        return if (value == null) {
            val defaultValue = annotation.booleanValue
            CorrectionApplierResult.Edited(
                oldValue = value,
                newValue = defaultValue,
            )
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
