package io.github.akashkansara.modak.api

/**
 * Represents a correction that was applied to an object.
 */
interface AppliedCorrection<T> {
    /** The root object that was corrected */
    val root: T

    /** Path to the property that was corrected */
    val propertyPath: Path

    /** The original value before correction */
    val oldValue: Any?

    /** The corrected value after applying the correction */
    val newValue: Any?

    /** Description of the correction that was applied */
    val correctionDescriptor: CorrectionDescriptor
}
