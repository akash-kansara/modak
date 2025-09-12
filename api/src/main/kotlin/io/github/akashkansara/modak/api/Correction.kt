package io.github.akashkansara.modak.api

import kotlin.reflect.KClass

/**
 * Meta-annotation that defines a correction annotation.
 *
 * @param correctedBy Array of correction applier classes that implement the correction logic
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Correction(
    val correctedBy: Array<KClass<out CorrectionApplier<*, *>>>,
)
