package io.github.akashkansara.modak.api

/**
 * Result of a correction operation performed by a CorrectionApplier.
 */
sealed class CorrectionApplierResult<T> {
    /**
     * Indicates whether this result represents a change to the value.
     */
    val edited
        get() = this is Edited<T>

    /**
     * Indicates that no correction was applied to the value.
     */
    class NoChange<T>() : CorrectionApplierResult<T>()

    /**
     * Indicates that a correction was applied to the value.
     *
     * @param oldValue The original value before correction
     * @param newValue The corrected value after applying the correction
     */
    class Edited<T>(val oldValue: T?, val newValue: T?) : CorrectionApplierResult<T>()
}
