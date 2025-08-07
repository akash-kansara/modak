package io.github.akashkansara.modak.core.testbed.corrections

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.core.testbed.beans.DoubleCustomGenericValue

class DoubleCustomGenericValueCorrectionApplier : CorrectionApplier<CustomGenericValue2Correction, DoubleCustomGenericValue> {
    private lateinit var annotation: CustomGenericValue2Correction

    override fun initialize(correctionAnnotation: CustomGenericValue2Correction) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: DoubleCustomGenericValue?, context: CorrectionApplierContext?): CorrectionApplierResult<DoubleCustomGenericValue> {
        return if (value == null || (value.value.isNaN() || value.value.isInfinite())) {
            val correctedValue = DoubleCustomGenericValue(annotation.defaultValue)
            CorrectionApplierResult.Edited(oldValue = value, newValue = correctedValue)
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
