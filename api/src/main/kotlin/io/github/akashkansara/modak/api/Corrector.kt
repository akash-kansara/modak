package io.github.akashkansara.modak.api

import jakarta.validation.ConstraintViolation

interface Corrector {
    fun <T> correct(
        obj: T,
        correctViolationsOnly: Boolean,
        constraintViolations: Set<ConstraintViolation<T>>?,
        vararg groups: Class<*>,
    ): CorrectionResult<T, ErrorLike>
}
