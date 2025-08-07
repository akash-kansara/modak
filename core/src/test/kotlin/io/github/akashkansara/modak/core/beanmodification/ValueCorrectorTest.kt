package io.github.akashkansara.modak.core.beanmodification

import arrow.core.raise.either
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.beanmetadata.BeanInspection
import io.github.akashkansara.modak.core.correction.CorrectionApplierProvider
import io.github.akashkansara.modak.core.testbed.beans.Money
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValueCorrectorTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var beanInspection: BeanInspection
    private lateinit var valueCorrector: ValueCorrector
    private lateinit var correctionApplierProvider: CorrectionApplierProvider

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        beanInspection = testFactory.beanInspection
        valueCorrector = testFactory.valueCorrector
        correctionApplierProvider = testFactory.correctionApplierProvider
    }

    @Test
    fun `applyCorrectionForValue results in no change if correction applier is not found`() {
        every { correctionApplierProvider.provideCorrectionApplier(any(), any()) } returns either { null }
        val value = Money(100.0, "USD")
        val beanMetaData = beanInspection.inspect(value).getOrNull()!!
        val result = valueCorrector.applyCorrectionForValue(
            101,
            beanMetaData.properties.find { it.name == "amount" }!!.genericType,
            beanMetaData.properties.find { it.name == "amount" }!!.correctionMetas.first(),
            value,
        )
        assertTrue(result.isRight())
        val correctionResult = result.getOrNull()!!
        assertTrue(correctionResult is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `applyCorrectionForValue results in no change if correction applier is of different type`() {
        every { correctionApplierProvider.provideCorrectionApplier(any(), any()) } returns either { String::class.java }
        val value = Money(100.0, "USD")
        val beanMetaData = beanInspection.inspect(value).getOrNull()!!
        val result = valueCorrector.applyCorrectionForValue(
            101,
            beanMetaData.properties.find { it.name == "amount" }!!.genericType,
            beanMetaData.properties.find { it.name == "amount" }!!.correctionMetas.first(),
            value,
        )
        assertTrue(result.isRight())
        val correctionResult = result.getOrNull()!!
        assertTrue(correctionResult is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `applyCorrectionForValue results in error if correction applier cannot be initialized`() {
        every {
            valueCorrector.createCorrectionApplierInstance(any())
        } coAnswers {
            val correctionApplier = spyk(callOriginal().getOrNull()!!)
            every {
                correctionApplier.hint(CorrectionApplierResult::class).correct(any<Double>(), any<CorrectionApplierContext>())
            } throws NoSuchMethodException("No default constructor")
            either { correctionApplier }
        }
        val value = Money(100.0, "USD")
        val beanMetaData = beanInspection.inspect(value).getOrNull()!!
        val result = valueCorrector.applyCorrectionForValue(
            101.toDouble(),
            beanMetaData.properties.find { it.name == "amount" }!!.genericType,
            beanMetaData.properties.find { it.name == "amount" }!!.correctionMetas.first(),
            value,
        )
        assertTrue(result.isLeft())
    }
}
