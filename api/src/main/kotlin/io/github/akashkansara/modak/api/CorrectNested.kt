package io.github.akashkansara.modak.api

/**
 * Annotation that enables automatic traversal and correction of nested objects.
 *
 * When applied to a property, the correction engine will traverse into the nested object
 * and apply corrections to its properties based on their annotations.
 */
@Target(
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FIELD,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class CorrectNested()
