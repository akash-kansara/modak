package io.github.akashkansara.modak.api

sealed class CorrectionResult<out T, out E : ErrorLike> {
    val isSuccess: Boolean
        get() = this is Success
    class Success<T> (
        val appliedCorrections: List<AppliedCorrection<T>>,
    ) : CorrectionResult<T, Nothing>()

    class Failure<E : ErrorLike> (
        val error: E,
    ) : CorrectionResult<Nothing, E>()
}
