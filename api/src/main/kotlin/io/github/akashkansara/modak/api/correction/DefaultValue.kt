package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

@Correction(correctedBy = [])
annotation class DefaultValue(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    val strValue: String = "",
    val intValue: Int = 0,
    val longValue: Long = 0L,
    val doubleValue: Double = 0.0,
    val floatValue: Float = 0.0f,
    val booleanValue: Boolean = false,
    val charValue: Char = '\u0000',
    val byteValue: Byte = 0,
    val shortValue: Short = 0,
    val enumValueClass: KClass<out Enum<*>> = Nothing::class,
    val enumValueName: String = "",
)
