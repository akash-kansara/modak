package io.github.akashkansara.modak.api

import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Correction(
    val correctedBy: Array<KClass<out CorrectionApplier<*, *>>>,
)
