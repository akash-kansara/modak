package io.github.akashkansara.modak.api

/**
 * Describes a correction annotation and its metadata.
 */
interface CorrectionDescriptor {
    /** The correction annotation instance */
    val annotation: Annotation

    /** Validation groups for which this correction applies */
    val groups: Set<Class<*>>

    /** Payload classes attached to this correction */
    val payload: Set<Class<*>>

    /** Constraint types that trigger this correction */
    val constraintFilter: Set<Class<*>>
}
