package io.github.akashkansara.modak.api

interface ErrorLike {
    val message: String
    val cause: Throwable?
    val appliedCorrections: List<AppliedCorrection<*>>
}
