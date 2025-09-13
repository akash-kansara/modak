package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.Error

data class ErrorImpl(
    override val cause: Throwable? = null,
    override val message: String,
    override val appliedCorrections: List<AppliedCorrection<*>> = emptyList(),
) : Error
