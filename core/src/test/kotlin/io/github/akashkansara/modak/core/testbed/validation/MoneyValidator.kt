package io.github.akashkansara.modak.core.testbed.validation

import io.github.akashkansara.modak.core.testbed.beans.Money
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class MoneyValidator : ConstraintValidator<ValidMoney, Money> {
    override fun isValid(value: Money?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }
        return value.currencyCode != null
    }
}
