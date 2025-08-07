package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.core.beanmetadata.ConfigurationSource

sealed class InternalError(
    open val cause: Throwable? = null,
    open val message: String,
) {
    data class CorrectionError(
        override val cause: Throwable? = null,
        override val message: String,
    ) : InternalError(cause, message)

    data class BeanModificationError(
        override val cause: Throwable? = null,
        override val message: String,
        val appliedCorrections: List<AppliedCorrection<*>> = emptyList(),
    ) : InternalError(cause, message)

    data class BeanInspectionError(
        val configurationSource: ConfigurationSource,
        override val cause: Throwable? = null,
        override val message: String,
    ) : InternalError(cause, message)

    data class GroupSequenceError(
        override val cause: Throwable? = null,
        override val message: String,
    ) : InternalError(cause, message)

    data class BeanTraversalError(
        override val cause: Throwable? = null,
        override val message: String,
    ) : InternalError(cause, message)
}
