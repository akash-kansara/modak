package io.github.akashkansara.modak.api

import jakarta.validation.ConstraintViolation

interface Corrector {
    fun <T> correct(
        obj: T,
        vararg groups: Class<*>,
    ): CorrectionResult<T, ErrorLike>

    fun <T> correct(
        obj: T,
        constraintViolations: Set<ConstraintViolation<T>>,
        vararg groups: Class<*>,
    ): CorrectionResult<T, ErrorLike>
}
