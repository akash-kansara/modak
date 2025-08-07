package io.github.akashkansara.modak.core.correction.correctionapplier

import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.DefaultValue
import kotlin.reflect.KClass

class EnumDefaultValueCorrectionApplier : CorrectionApplier<DefaultValue, Enum<*>> {
    private lateinit var annotation: DefaultValue

    override fun initialize(correctionAnnotation: DefaultValue) {
        annotation = correctionAnnotation
        super.initialize(correctionAnnotation)
    }

    override fun correct(
        value: Enum<*>?,
        context: CorrectionApplierContext?,
    ): CorrectionApplierResult<Enum<*>> {
        return if (value == null) {
            val defaultEnumValue = getEnumValue(annotation.enumValueClass, annotation.enumValueName)
            if (defaultEnumValue != null) {
                CorrectionApplierResult.Edited(oldValue = value, newValue = defaultEnumValue)
            } else {
                CorrectionApplierResult.NoChange()
            }
        } else {
            CorrectionApplierResult.NoChange()
        }
    }

    private fun getEnumValue(enumClass: KClass<out Enum<*>>, enumValueName: String): Enum<*>? {
        return try {
            if (enumValueName.isNotBlank() && enumClass != Nothing::class) {
                val javaClass = enumClass.java
                // Check if the class is actually an enum
                if (javaClass.isEnum) {
                    val enumConstants = javaClass.enumConstants
                    enumConstants?.find { it.name == enumValueName }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }
}
