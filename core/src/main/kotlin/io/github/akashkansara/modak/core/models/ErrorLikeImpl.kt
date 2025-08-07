package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.ErrorLike

data class ErrorLikeImpl(
    override val cause: Throwable? = null,
    override val message: String,
    override val appliedCorrections: List<AppliedCorrection<*>> = emptyList(),
) : ErrorLike
