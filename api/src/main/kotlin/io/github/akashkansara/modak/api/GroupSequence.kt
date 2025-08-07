package io.github.akashkansara.modak.api

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GroupSequence(
    val value: Array<KClass<*>>,
) 
