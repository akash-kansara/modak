package io.github.akashkansara.modak.core.beanmetadata

import io.github.akashkansara.modak.api.CorrectNested
import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.api.correction.Trim
import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.testbed.beans.Chair
import io.github.akashkansara.modak.core.testbed.beans.Company
import io.github.akashkansara.modak.core.testbed.beans.Money
import io.github.akashkansara.modak.core.testbed.beans.Prank
import io.github.akashkansara.modak.core.testbed.beans.Printer
import io.github.akashkansara.modak.core.testbed.corrections.MoneyCorrection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AnnotationBeanInspectorTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var annotationBeanInspector: AnnotationBeanInspector

    @BeforeEach
    fun setUp() {
        annotationBeanInspector = testFactory.annotationBeanInspector
    }

    @Test
    fun `should inspect bean without annotations successfully`() {
        val clazz = Prank::class.java
        val result = annotationBeanInspector.inspect(clazz)
        assertTrue(result.isRight())
        val beanMetaData = result.getOrNull()!!
        assertEquals(clazz, beanMetaData.type)
        assertEquals(ConfigurationSource.ANNOTATION, beanMetaData.configurationSource)
        assertEquals(3, beanMetaData.properties.size)
        val propNames = Prank::class.java.declaredFields.toSet()
        propNames.forEach {
            val prop = beanMetaData.properties.find { p -> p.name == it.name }
            assertNotNull(prop)
            assertTrue(prop!!.correctionMetas.isEmpty())
            assertTrue(prop.correctionModifiers.isEmpty())
            assertNotNull(prop.field)
            assertNotNull(prop.readMethod)
            assertEquals(null, prop.writeMethod)
        }
    }

    @Test
    fun `should inspect bean with annotations successfully`() {
        val clazz = Money::class.java
        val result = annotationBeanInspector.inspect(clazz)
        assertTrue(result.isRight())
        val beanMetaData = result.getOrNull()!!
        assertEquals(clazz, beanMetaData.type)
        assertEquals(ConfigurationSource.ANNOTATION, beanMetaData.configurationSource)
        assertEquals(2, beanMetaData.properties.size)
        val propNames = Money::class.java.declaredFields.toSet()
        propNames.forEach {
            val prop = beanMetaData.properties.find { p -> p.name == it.name }
            assertNotNull(prop)
            when (it.name) {
                "amount" -> {
                    assertEquals(1, prop!!.correctionMetas.size)
                    assertNotNull(prop.correctionMetas.find { cm -> cm.annotation is DefaultValue })
                    assertTrue(prop.correctionModifiers.isEmpty())
                }
            }
        }
        val moneyCorrection = beanMetaData.correctionMetas.firstOrNull { cm -> cm.annotation is MoneyCorrection }
        assertNotNull(moneyCorrection)
    }

    @Test
    fun `should inspect modifiers successfully`() {
        val clazz = Company::class.java
        val result = annotationBeanInspector.inspect(clazz)
        assertTrue(result.isRight())
        val beanMetaData = result.getOrNull()!!
        assertEquals(clazz, beanMetaData.type)
        assertEquals(ConfigurationSource.ANNOTATION, beanMetaData.configurationSource)
        assertEquals(5, beanMetaData.properties.size)
        val prop1 = beanMetaData.properties.find { p -> p.name == "phoneNumbers" }!!
        assertIterableEquals(setOf(CorrectNested::class.java), prop1.correctionModifiers.map { it.annotationClass.java })
        val prop2 = beanMetaData.properties.find { p -> p.name == "branches" }!!
        assertIterableEquals(setOf(CorrectNested::class.java), prop2.correctionModifiers.map { it.annotationClass.java })
        val prop3 = beanMetaData.properties.find { p -> p.name == "headquarters" }!!
        assertIterableEquals(setOf(CorrectNested::class.java), prop3.correctionModifiers.map { it.annotationClass.java })
    }

    @ParameterizedTest
    @ValueSource(classes = [Printer::class, Chair::class])
    fun `should inspect bean with inheritance successfully`(clazz: Class<*>) {
        val result = annotationBeanInspector.inspect(clazz)
        val propNames = clazz.declaredFields.toSet()
        assertTrue(result.isRight())
        val beanMetaData = result.getOrNull()!!
        assertEquals(clazz, beanMetaData.type)
        when (clazz) {
            Printer::class.java -> {
                assertEquals(2, beanMetaData.properties.size)
                propNames.forEach {
                    val prop = beanMetaData.properties.find { p -> p.name == it.name }
                    assertNotNull(prop)
                    when (it.name) {
                        "brand" -> {
                            assertEquals(2, prop!!.correctionMetas.size)
                            assertEquals(0, prop.correctionModifiers.size)
                        }
                        "assignedTo" -> {
                            assertTrue(prop!!.correctionMetas.isEmpty())
                        }
                    }
                }
            }
            Chair::class.java -> {
                assertEquals(2, beanMetaData.properties.size)
                propNames.forEach {
                    val prop = beanMetaData.properties.find { p -> p.name == it.name }
                    assertNotNull(prop)
                    when (it.name) {
                        "brand" -> {
                            assertEquals(1, prop!!.correctionMetas.size)
                            assertEquals(0, prop.correctionModifiers.size)
                            assertEquals(Trim::class.java, prop.correctionMetas.first().annotation.annotationClass.java)
                        }
                        "assignedTo" -> {
                            assertEquals(1, prop!!.correctionMetas.size)
                            assertEquals(0, prop.correctionModifiers.size)
                            assertEquals(
                                DefaultValue::class.java,
                                prop.correctionMetas.first().annotation.annotationClass.java,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `should inspect bean with inheritance successfully`() {
        open class Parent {
            @DefaultValue(strValue = "default")
            var name: String? = null
        }
        open class Child : Parent() {
            @Trim
            var description: String? = null
        }
        class GrandChild : Child() {
            @DefaultValue(doubleValue = 1.0)
            var number: Double? = null
        }
        val result = annotationBeanInspector.inspect(GrandChild::class.java)
        assertTrue(result.isRight())
        val beanMetaData = result.getOrNull()!!
        assertEquals(3, beanMetaData.properties.size)
    }
}
