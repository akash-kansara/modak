package io.github.akashkansara.modak.api

import kotlin.reflect.KClass

/**
 * Defines the sequence in which groups should be processed.
 *
 * @param value Array of group classes in the order they should be processed
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GroupSequence(
    val value: Array<KClass<*>>,
) 
