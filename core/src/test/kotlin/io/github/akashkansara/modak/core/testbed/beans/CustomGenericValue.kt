package io.github.akashkansara.modak.core.testbed.beans

interface CustomGenericValue1<T> {
    val value: T
}

class StringCustomGenericValue(override val value: String) : CustomGenericValue1<String>

class IntCustomGenericValue(override val value: Int) : CustomGenericValue1<Int>

abstract class CustomGenericValue2<T> {
    abstract val value: T
}

class LongCustomGenericValue(override val value: Long) : CustomGenericValue2<Long>()

class DoubleCustomGenericValue(override val value: Double) : CustomGenericValue2<Double>()
