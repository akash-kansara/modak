package io.github.akashkansara.modak.api

interface CorrectionDescriptor {
    val annotation: Annotation
    val groups: Set<Class<*>>
    val payload: Set<Class<*>>
    val constraintFilter: Set<Class<*>>
}
