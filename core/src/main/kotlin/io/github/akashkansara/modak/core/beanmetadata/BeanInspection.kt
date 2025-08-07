package io.github.akashkansara.modak.core.beanmetadata

import arrow.core.Either
import arrow.core.raise.either
import io.github.akashkansara.modak.core.models.InternalError
import java.util.concurrent.locks.ReentrantReadWriteLock

class BeanInspection(
    private val annotationBeanInspector: AnnotationBeanInspector,
) {
    private val store = mutableMapOf<Class<*>, BeanMetaData<*>>()
    private val lock = ReentrantReadWriteLock()

    fun <T> inspect(value: T): Either<InternalError, BeanMetaData<*>?> {
        return either {
            if (value == null) return@either null
            val clazz = value::class.java
            getMetaData(clazz)?.let { return@either it }
            val metaData = annotationBeanInspector.inspect(clazz).bind()
            storeMetaData(clazz, metaData)
            metaData
        }
    }

    private fun getMetaData(clazz: Class<*>): BeanMetaData<*>? {
        return store[clazz]
    }

    private fun storeMetaData(clazz: Class<*>, metaData: BeanMetaData<*>) {
        lock.writeLock().lock()
        store[clazz] = metaData
        lock.writeLock().unlock()
    }
}
