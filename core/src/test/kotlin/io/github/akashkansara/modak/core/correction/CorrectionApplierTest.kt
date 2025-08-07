package io.github.akashkansara.modak.core.correction

import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.api.correction.RegexReplace
import io.github.akashkansara.modak.api.correction.Truncate
import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.beanmetadata.BeanInspection
import io.github.akashkansara.modak.core.correction.correctionapplier.BooleanDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.ByteDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.CharDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.DoubleDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.EnumDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.FloatDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.IntDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.LongDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.RegexReplaceCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.ShortDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.StringDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.TrimCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.TruncateCorrectionApplier
import io.github.akashkansara.modak.core.testbed.beans.Department
import io.github.akashkansara.modak.core.testbed.beans.person
import io.github.akashkansara.modak.core.testbed.beans.printer
import io.github.akashkansara.modak.core.testbed.buildSloughBranch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CorrectionApplierTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var context: CorrectionApplierContext
    private lateinit var beanInspection: BeanInspection

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        val branch = buildSloughBranch()
        context = CorrectionApplierContext(rootBean = branch, leafBean = branch.manager)
        beanInspection = testFactory.beanInspection
    }

    @Test
    fun `TrimCorrectionApplier should trim whitespace from string`() {
        val applier = TrimCorrectionApplier()
        val result = applier.correct("  hello world  ", context)
        assertTrue(result.edited)
        val editedResult = result as CorrectionApplierResult.Edited
        assertEquals("  hello world  ", editedResult.oldValue)
        assertEquals("hello world", editedResult.newValue)
    }

    @Test
    fun `TrimCorrectionApplier should return NoChange for already trimmed string`() {
        val applier = TrimCorrectionApplier()
        val result = applier.correct("hello world", context)
        assertFalse(result.edited)
        assertTrue(result is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `TrimCorrectionApplier should return NoChange for null value`() {
        val applier = TrimCorrectionApplier()
        val result = applier.correct(null, context)
        assertFalse(result.edited)
        assertTrue(result is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `TruncateCorrectionApplier should truncate string when length exceeded`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val brandProperty = beanMetaData.properties.find { it.name == "brand" }!!
        val truncateAnnotation = brandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Truncate::class.java
        }!!.annotation as Truncate
        val applier = TruncateCorrectionApplier()
        applier.initialize(truncateAnnotation)
        val longString = "This is a very long string that exceeds the truncate limit of 50 characters and should be truncated"
        val result = applier.correct(longString, context)

        assertTrue(result.edited)
        val editedResult = result as CorrectionApplierResult.Edited
        assertEquals(longString, editedResult.oldValue)
        assertTrue(editedResult.newValue!!.length <= truncateAnnotation.length)
    }

    @Test
    fun `TruncateCorrectionApplier should return NoChange when string is within length limit`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val brandProperty = beanMetaData.properties.find { it.name == "brand" }!!
        val truncateAnnotation = brandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Truncate::class.java
        }!!.annotation as Truncate
        val applier = TruncateCorrectionApplier()
        applier.initialize(truncateAnnotation)
        val result = applier.correct("short", context)
        assertFalse(result.edited)
        assertTrue(result is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `TruncateCorrectionApplier should return NoChange for null value`() {
        val printer = printer {
            brand = "HP"
            assignedTo = Department.SALES
        }
        val beanMetaData = beanInspection.inspect(printer).getOrNull()!!
        val brandProperty = beanMetaData.properties.find { it.name == "brand" }!!
        val truncateAnnotation = brandProperty.correctionMetas.find {
            it.annotation.annotationClass.java == Truncate::class.java
        }!!.annotation as Truncate
        val applier = TruncateCorrectionApplier()
        applier.initialize(truncateAnnotation)
        val result = applier.correct(null, context)
        assertFalse(result.edited)
        assertTrue(result is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `RegexReplaceCorrectionApplier should replace matching pattern`() {
        val person = person {
            name = "Jim Halpert"
            catchPhrase = "Catch Phrase: Bears. Beets. Battlestar Galactica."
        }
        val beanMetaData = beanInspection.inspect(person).getOrNull()!!
        val catchPhraseProperty = beanMetaData.properties.find { it.name == "catchPhrase" }!!
        val regexAnnotation = catchPhraseProperty.correctionMetas.find {
            it.annotation.annotationClass.java == RegexReplace::class.java
        }!!.annotation as RegexReplace
        val applier = RegexReplaceCorrectionApplier()
        applier.initialize(regexAnnotation)
        val result = applier.correct("Catch Phrase: Bears. Beets. Battlestar Galactica.", context)
        assertTrue(result.edited)
        val editedResult = result as CorrectionApplierResult.Edited
        assertEquals("Catch Phrase: Bears. Beets. Battlestar Galactica.", editedResult.oldValue)
        assertEquals("Bears. Beets. Battlestar Galactica.", editedResult.newValue)
    }

    @Test
    fun `RegexReplaceCorrectionApplier should return NoChange when pattern does not match`() {
        val person = person {
            name = "Jim Halpert"
            catchPhrase = "Bears. Beets. Battlestar Galactica."
        }
        val beanMetaData = beanInspection.inspect(person).getOrNull()!!
        val catchPhraseProperty = beanMetaData.properties.find { it.name == "catchPhrase" }!!
        val regexAnnotation = catchPhraseProperty.correctionMetas.find {
            it.annotation.annotationClass.java == RegexReplace::class.java
        }!!.annotation as RegexReplace

        val applier = RegexReplaceCorrectionApplier()
        applier.initialize(regexAnnotation)

        val result = applier.correct("hello world", context)

        assertFalse(result.edited)
        assertTrue(result is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `RegexReplaceCorrectionApplier should return NoChange for null value`() {
        val person = person {
            name = "Jim Halpert"
            catchPhrase = "Catch Phrase: Test"
        }
        val beanMetaData = beanInspection.inspect(person).getOrNull()!!
        val catchPhraseProperty = beanMetaData.properties.find { it.name == "catchPhrase" }!!
        val regexAnnotation = catchPhraseProperty.correctionMetas.find {
            it.annotation.annotationClass.java == RegexReplace::class.java
        }!!.annotation as RegexReplace

        val applier = RegexReplaceCorrectionApplier()
        applier.initialize(regexAnnotation)

        val result = applier.correct(null, context)

        assertFalse(result.edited)
        assertTrue(result is CorrectionApplierResult.NoChange)
    }

    @Test
    fun `should handle complex correction scenario with real TheOffice data`() {
        val person = person {
            name = "Jim Halpert"
            catchPhrase = "Catch Phrase: Bears. Beets. Battlestar Galactica."
        }
        val beanMetaData = beanInspection.inspect(person).getOrNull()!!
        val catchPhraseProperty = beanMetaData.properties.find { it.name == "catchPhrase" }!!
        val regexAnnotation = catchPhraseProperty.correctionMetas.find {
            it.annotation.annotationClass.java == RegexReplace::class.java
        }!!.annotation as RegexReplace
        val branch = buildSloughBranch()
        val context = CorrectionApplierContext(rootBean = branch, leafBean = person)
        val regexApplier = RegexReplaceCorrectionApplier()
        regexApplier.initialize(regexAnnotation)
        val result = regexApplier.correct(person.catchPhrase, context)
        assertTrue(result.edited)
        val editedResult = result as CorrectionApplierResult.Edited
        assertEquals("Bears. Beets. Battlestar Galactica.", editedResult.newValue)
    }

    @ParameterizedTest
    @ValueSource(
        classes = [
            Boolean::class,
            Char::class,
            Byte::class,
            Float::class,
            Short::class,
            Department::class,
            Long::class,
            Double::class,
            Int::class,
            String::class,
        ],
    )
    fun `should apply default value correctly`(dataType: Class<*>) {
        val testBeanWithNulls = DefaultValueTestBean()
        val testBeanWithData = DefaultValueTestBean(
            boolean = true,
            char = 'B',
            byte = 0.toByte(),
            float = 2.0f,
            short = 3.toShort(),
            enum = Department.HR,
            long = 99L,
            double = 12.0,
            int = 11,
            string = "",
        )
        val beanMetaData = beanInspection.inspect(testBeanWithNulls).getOrNull()!!
        val prop = beanMetaData.properties.find { testFactory.typeUtil.areTypesMatching(dataType, it.genericType) }!!
        val correctionMeta = prop.correctionMetas.first()
        when (dataType) {
            Boolean::class.java -> BooleanDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, false)
                val noChangeValue = it.correct(testBeanWithData.boolean, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Char::class.java -> CharDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 'A')
                val noChangeValue = it.correct(testBeanWithData.char, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Byte::class.java -> ByteDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 1.toByte())
                val noChangeValue = it.correct(testBeanWithData.byte, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Float::class.java -> FloatDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 1.0f)
                val noChangeValue = it.correct(testBeanWithData.float, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Short::class.java -> ShortDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 1.toShort())
                val noChangeValue = it.correct(testBeanWithData.short, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Department::class.java -> EnumDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, Department.SALES)
                val noChangeValue = it.correct(testBeanWithData.enum, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Long::class.java -> LongDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 100L)
                val noChangeValue = it.correct(testBeanWithData.long, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Double::class.java -> DoubleDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 11.0)
                val noChangeValue = it.correct(testBeanWithData.double, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            Int::class.java -> IntDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, 12)
                val noChangeValue = it.correct(testBeanWithData.int, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            String::class.java -> StringDefaultValueCorrectionApplier().also {
                it.initialize(correctionMeta.annotation as DefaultValue)
            }.also {
                val updatedValue = it.correct(null, context)
                assertTrue(updatedValue.edited)
                assertEquals((updatedValue as CorrectionApplierResult.Edited).oldValue, null)
                assertEquals(updatedValue.newValue, "default")
                val noChangeValue = it.correct(testBeanWithData.string, context)
                assertFalse(noChangeValue.edited)
                assertTrue(noChangeValue is CorrectionApplierResult.NoChange)
            }
            else -> throw IllegalArgumentException("Unsupported data type: $dataType")
        }
    }

    data class DefaultValueTestBean(
        @field:DefaultValue(booleanValue = false)
        val boolean: Boolean? = null,
        @field:DefaultValue(charValue = 'A')
        val char: Char? = null,
        @field:DefaultValue(byteValue = 1.toByte())
        val byte: Byte? = null,
        @field:DefaultValue(floatValue = 1.0f)
        val float: Float? = null,
        @field:DefaultValue(shortValue = 1.toShort())
        val short: Short? = null,
        @field:DefaultValue(enumValueName = "SALES", enumValueClass = Department::class)
        val enum: Department? = null,
        @field:DefaultValue(longValue = 100L)
        val long: Long? = null,
        @field:DefaultValue(doubleValue = 11.0)
        val double: Double? = null,
        @field:DefaultValue(intValue = 12)
        val int: Int? = null,
        @field:DefaultValue(strValue = "default")
        val string: String? = null,
    )
}
