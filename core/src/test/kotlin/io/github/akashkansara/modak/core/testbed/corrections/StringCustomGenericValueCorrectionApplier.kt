package io.github.akashkansara.modak.core.testbed.corrections

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.core.testbed.beans.StringCustomGenericValue

class StringCustomGenericValueCorrectionApplier : CorrectionApplier<CustomGenericValue1Correction, StringCustomGenericValue> {
    private lateinit var annotation: CustomGenericValue1Correction

    override fun initialize(correctionAnnotation: CustomGenericValue1Correction) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: StringCustomGenericValue?, context: CorrectionApplierContext?): CorrectionApplierResult<StringCustomGenericValue> {
        return if (value == null || (value.value.isBlank() || value.value.isEmpty())) {
            val correctedValue = StringCustomGenericValue(annotation.defaultValue)
            CorrectionApplierResult.Edited(oldValue = value, newValue = correctedValue)
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
