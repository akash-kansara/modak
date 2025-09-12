package io.github.akashkansara.modak.api

import jakarta.validation.ConstraintViolation

/**
 * Main interface for applying corrections to objects.
 */
interface Corrector {
    /**
     * Applies corrections to the given object based on annotation-based correction rules.
     *
     * @param obj The object to apply corrections to
     * @param groups Groups to consider for correction
     * @return CorrectionResult containing either the corrected object and applied corrections, or error information
     */
    fun <T> correct(
        obj: T,
        vararg groups: Class<*>,
    ): CorrectionResult<T, ErrorLike>

    /**
     * Applies corrections to the given object, considering existing constraint violations.
     *
     * @param obj The object to apply corrections to
     * @param constraintViolations Existing constraint violations from validation
     * @param groups Groups to consider for correction
     * @return CorrectionResult containing either the corrected object and applied corrections, or error information
     */
    fun <T> correct(
        obj: T,
        constraintViolations: Set<ConstraintViolation<T>>,
        vararg groups: Class<*>,
    ): CorrectionResult<T, ErrorLike>
}
