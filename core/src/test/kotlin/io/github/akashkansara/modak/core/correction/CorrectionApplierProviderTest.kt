package io.github.akashkansara.modak.core.correction

import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.api.correction.RegexReplace
import io.github.akashkansara.modak.api.correction.Trim
import io.github.akashkansara.modak.api.correction.Truncate
import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.beanmetadata.BeanInspection
import io.github.akashkansara.modak.core.correction.correctionapplier.EnumDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.RegexReplaceCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.TrimCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.TruncateCorrectionApplier
import io.github.akashkansara.modak.core.testbed.beans.Department
import io.github.akashkansara.modak.core.testbed.beans.Money
import io.github.akashkansara.modak.core.testbed.beans.chair
import io.github.akashkansara.modak.core.testbed.beans.money
import io.github.akashkansara.modak.core.testbed.beans.person
import io.github.akashkansara.modak.core.testbed.beans.printer
import io.github.akashkansara.modak.core.testbed.corrections.MoneyCorrectionApplier
import io.github.akashkansara.modak.core.util.TypeUtil
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CorrectionApplierProviderTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var correctionApplierProvider: CorrectionApplierProvider
    private lateinit var typeUtil: TypeUtil
    private lateinit var beanInspection: BeanInspection

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        typeUtil = testFactory.typeUtil
        correctionApplierProvider = testFactory.correctionApplierProvider
        beanInspection = testFactory.beanInspection
    }

    @Test
    fun `should return TrimCorrectionApplier for field with Trim annotation`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val brandProperty = beanMetaData.properties.find { it.name == "brand" }!!
        val trimCorrectionMeta = brandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Trim::class.java
        }!!
        val result = correctionApplierProvider.provideCorrectionApplier(trimCorrectionMeta, String::class.java)
        assertTrue(result.isRight())
        assertEquals(TrimCorrectionApplier::class.java, result.getOrNull())
    }

    @Test
    fun `should return TruncateCorrectionApplier for field with Truncate annotation`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val brandProperty = beanMetaData.properties.find { it.name == "brand" }!!
        val truncateCorrectionMeta = brandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Truncate::class.java
        }!!
        val result = correctionApplierProvider.provideCorrectionApplier(truncateCorrectionMeta, String::class.java)
        assertTrue(result.isRight())
        assertEquals(TruncateCorrectionApplier::class.java, result.getOrNull())
    }

    @Test
    fun `should return EnumDefaultValueCorrectionApplier for field with DefaultValue annotation`() {
        val chair = chair {
            brand = "IKEA"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(chair).getOrNull()!!
        val assignedToProperty = beanMetaData.properties.find { it.name == "assignedTo" }!!
        val defaultValueCorrectionMeta = assignedToProperty.correctionMetas.find {
            it.annotation.annotationClass.java == DefaultValue::class.java
        }!!
        val result = correctionApplierProvider.provideCorrectionApplier(defaultValueCorrectionMeta, Department::class.java)
        assertTrue(result.isRight())
        assertEquals(EnumDefaultValueCorrectionApplier::class.java, result.getOrNull())
    }

    @Test
    fun `should return RegexReplaceCorrectionApplier field with RegexReplace annotation`() {
        val person = person {
            name = "Jim Halpert"
            catchPhrase = "Catch Phrase: Bears. Beets. Battlestar Galactica."
        }
        val beanMetaData = beanInspection.inspect(person).getOrNull()!!
        val catchPhraseProperty = beanMetaData.properties.find { it.name == "catchPhrase" }!!
        val regexReplaceCorrectionMeta = catchPhraseProperty.correctionMetas.find {
            it.annotation.annotationClass.java == RegexReplace::class.java
        }!!
        val result = correctionApplierProvider.provideCorrectionApplier(regexReplaceCorrectionMeta, String::class.java)
        assertTrue(result.isRight())
        assertEquals(RegexReplaceCorrectionApplier::class.java, result.getOrNull())
    }

    @Test
    fun `should return MoneyCorrectionApplier for class with MoneyCorrection annotation`() {
        val money = money {
            amount = 100.0
            currencyCode = "USD"
        }
        val beanMetaData = beanInspection.inspect(money).getOrNull()!!
        val moneyCorrectionMeta = beanMetaData.correctionMetas.first()
        val result = correctionApplierProvider.provideCorrectionApplier(moneyCorrectionMeta, Money::class.java)
        assertTrue(result.isRight())
        assertEquals(MoneyCorrectionApplier::class.java, result.getOrNull())
    }

    @Test
    fun `should use correct cache key to retrieve correction applier`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val printerBeanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val printerBrandProperty = printerBeanMetaData.properties.find { it.name == "brand" }!!
        val trimCorrectionMeta = printerBrandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Trim::class.java
        }!!
        val truncateCorrectionMeta = printerBrandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Truncate::class.java
        }!!
        val firstTrimResult = correctionApplierProvider.provideCorrectionApplier(trimCorrectionMeta, String::class.java)
        val firstTruncateResult = correctionApplierProvider.provideCorrectionApplier(truncateCorrectionMeta, String::class.java)
        val secondTrimResult = correctionApplierProvider.provideCorrectionApplier(trimCorrectionMeta, String::class.java)
        val secondTruncateResult = correctionApplierProvider.provideCorrectionApplier(truncateCorrectionMeta, String::class.java)
        assertTrue(firstTrimResult.isRight())
        assertTrue(firstTruncateResult.isRight())
        assertTrue(secondTrimResult.isRight())
        assertTrue(secondTruncateResult.isRight())
        assertEquals(TrimCorrectionApplier::class.java, firstTrimResult.getOrNull())
        assertEquals(TruncateCorrectionApplier::class.java, firstTruncateResult.getOrNull())
        assertEquals(firstTrimResult.getOrNull(), secondTrimResult.getOrNull())
        assertEquals(firstTruncateResult.getOrNull(), secondTruncateResult.getOrNull())
    }

    @Test
    fun `should return null when no matching correction applier found for unsupported type`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val brandProperty = beanMetaData.properties.find { it.name == "brand" }!!
        val trimCorrectionMeta = brandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Trim::class.java
        }!!
        val unsupportedType = List::class.java

        every { typeUtil.areTypesMatching(any(), any()) } returns false
        every { typeUtil.getBoxedType(any()) } returns null
        val result = correctionApplierProvider.provideCorrectionApplier(trimCorrectionMeta, unsupportedType)
        assertTrue(result.isRight())
        assertNull(result.getOrNull())
    }
}
