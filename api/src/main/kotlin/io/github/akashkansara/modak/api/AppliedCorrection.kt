package io.github.akashkansara.modak.api

interface AppliedCorrection<T> {
    val root: T
    val propertyPath: Path
    val oldValue: Any?
    val newValue: Any?
    val correctionDescriptor: CorrectionDescriptor
}
