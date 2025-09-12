package io.github.akashkansara.modak.api

/**
 * Represents the kind of element in an object graph.
 */
enum class ElementKind {
    /** A bean/object instance */
    BEAN,

    /** A property/field of a bean */
    PROPERTY,

    /** An element within a container (list, map, array) */
    CONTAINER_ELEMENT,
}
