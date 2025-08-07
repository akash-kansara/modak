package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.CorrectionDescriptor
import io.github.akashkansara.modak.api.Path

data class AppliedCorrectionImpl<T>(
    override val root: T,
    override val propertyPath: Path,
    override val oldValue: Any?,
    override val newValue: Any?,
    override val correctionDescriptor: CorrectionDescriptor,
) : AppliedCorrection<T>
