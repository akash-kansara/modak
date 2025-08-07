package io.github.akashkansara.modak.core.testbed.corrections

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.core.testbed.beans.Money

class MoneyCorrectionApplier : CorrectionApplier<MoneyCorrection, Money> {
    private lateinit var annotation: MoneyCorrection

    override fun initialize(correctionAnnotation: MoneyCorrection) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(value: Money?, context: CorrectionApplierContext?): CorrectionApplierResult<Money> {
        return if (value != null && value.currencyCode == null) {
            val newValue = value.copy(currencyCode = annotation.defaultCurrency)
            CorrectionApplierResult.Edited(oldValue = value, newValue = newValue)
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
} 
