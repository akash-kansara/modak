package io.github.akashkansara.modak.api

interface CorrectionApplier<A : Annotation, T> {
    fun initialize(correctionAnnotation: A) {
        // Default implementation does nothing
    }

    fun correct(value: T?, context: CorrectionApplierContext?): CorrectionApplierResult<T>
}
