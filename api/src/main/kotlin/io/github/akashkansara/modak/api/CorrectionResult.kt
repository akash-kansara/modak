package io.github.akashkansara.modak.api

/**
 * Result of a correction operation performed by a Corrector.
 */
sealed class CorrectionResult<out T> {
    /**
     * Indicates whether the correction operation was successful.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Successful correction result.
     *
     * @param appliedCorrections List of corrections that were applied
     */
    class Success<T> (
        val appliedCorrections: List<AppliedCorrection<T>>,
    ) : CorrectionResult<T>()

    /**
     * Failed correction result.
     *
     * @param error The error that occurred during correction
     */
    class Failure(
        val error: Error,
    ) : CorrectionResult<Nothing>()
}
