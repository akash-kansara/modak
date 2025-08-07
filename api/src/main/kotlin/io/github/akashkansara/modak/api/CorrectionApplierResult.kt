package io.github.akashkansara.modak.api

sealed class CorrectionApplierResult<T> {
    val edited
        get() = this is Edited<T>

    class NoChange<T>() : CorrectionApplierResult<T>()

    class Edited<T>(val oldValue: T?, val newValue: T?) : CorrectionApplierResult<T>()
}
