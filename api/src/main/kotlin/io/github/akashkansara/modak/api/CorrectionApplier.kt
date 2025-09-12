package io.github.akashkansara.modak.api

/**
 * Interface for implementing correction logic for specific annotation and type combinations.
 */
interface CorrectionApplier<A : Annotation, T> {
    /**
     * Initializes the correction applier with the annotation instance.
     *
     * @param correctionAnnotation The annotation instance containing correction parameters
     */
    fun initialize(correctionAnnotation: A) {
        // Default implementation does nothing
    }

    /**
     * Applies correction logic to the given value.
     *
     * @param value The current value to be corrected (may be null)
     * @param context Additional context information about the correction operation
     * @return CorrectionApplierResult indicating whether correction was applied and the new value
     */
    fun correct(value: T?, context: CorrectionApplierContext?): CorrectionApplierResult<T>
}
