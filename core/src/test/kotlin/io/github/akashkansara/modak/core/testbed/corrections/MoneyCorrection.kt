package io.github.akashkansara.modak.core.testbed.corrections

import io.github.akashkansara.modak.api.Correction
import io.github.akashkansara.modak.api.CorrectionTarget
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Correction(correctedBy = [MoneyCorrectionApplier::class])
annotation class MoneyCorrection(
    val constraintFilter: Array<KClass<*>> = [],
    val groups: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    val defaultCurrency: String = "GBP",
)
