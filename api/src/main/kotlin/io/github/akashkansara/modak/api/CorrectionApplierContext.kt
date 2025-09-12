package io.github.akashkansara.modak.api

/**
 * Context information provided to correction appliers during correction operations.
 *
 * @param rootBean The root object that initiated the correction process
 * @param leafBean The current object being corrected
 */
class CorrectionApplierContext(
    val rootBean: Any? = null,
    val leafBean: Any? = null,
)
