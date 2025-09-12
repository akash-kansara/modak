package io.github.akashkansara.modak.api

/**
 * Specifies what should be corrected by a correction annotation.
 */
enum class CorrectionTarget {
    /** Apply correction to the property value itself */
    PROPERTY,

    /** Apply correction to elements within containers (lists, maps, arrays) */
    CONTAINER_ELEMENT,
}
