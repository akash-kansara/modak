package io.github.akashkansara.modak.api

/**
 * Represents an error that occurred during correction operations.
 */
interface ErrorLike {
    /** Error message describing what went wrong */
    val message: String

    /** The underlying cause of the error */
    val cause: Throwable?

    /** Corrections that were applied before the error occurred */
    val appliedCorrections: List<AppliedCorrection<*>>
}
