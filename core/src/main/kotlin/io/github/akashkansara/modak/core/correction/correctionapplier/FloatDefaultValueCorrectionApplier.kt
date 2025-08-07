package io.github.akashkansara.modak.core.correction.correctionapplier

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.DefaultValue

class FloatDefaultValueCorrectionApplier : CorrectionApplier<DefaultValue, Float> {
    private lateinit var annotation: DefaultValue

    override fun initialize(correctionAnnotation: DefaultValue) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: Float?, context: CorrectionApplierContext?): CorrectionApplierResult<Float> {
        return if (value == null) {
            CorrectionApplierResult.Edited(
                oldValue = value,
                newValue = annotation.floatValue,
            )
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
