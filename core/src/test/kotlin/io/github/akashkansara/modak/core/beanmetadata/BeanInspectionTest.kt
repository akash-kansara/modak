package io.github.akashkansara.modak.core.beanmetadata

import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.testbed.beans.Chair
import io.github.akashkansara.modak.core.testbed.beans.Department
import io.github.akashkansara.modak.core.testbed.beans.Money
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BeanInspectionTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var beanInspection: BeanInspection

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        beanInspection = testFactory.beanInspection
    }

    @Test
    fun `should use cache to retrieve bean meta data`() {
        val value = Money(100.0, "USD")
        val firstInspection = beanInspection.inspect(value).getOrNull()!!
        val secondInspection = beanInspection.inspect(value).getOrNull()!!
        verify(exactly = 1) { testFactory.annotationBeanInspector.inspect(any()) }
        assertEquals(firstInspection, secondInspection)
    }

    @Test
    fun `should use correct cache key to retrieve bean meta data`() {
        val value1 = Chair("Office Chair", Department.SALES)
        val value2 = Money(100.0, "USD")
        val firstInspection1 = beanInspection.inspect(value1).getOrNull()!!
        val firstInspection2 = beanInspection.inspect(value2).getOrNull()!!
        val secondInspection1 = beanInspection.inspect(value1).getOrNull()!!
        val secondInspection2 = beanInspection.inspect(value2).getOrNull()!!
        verify(exactly = 2) { testFactory.annotationBeanInspector.inspect(any()) }
        assertEquals(firstInspection1, secondInspection1)
        assertEquals(firstInspection2, secondInspection2)
    }

    @Test
    fun `should return null for null value`() {
        val result = beanInspection.inspect(null)
        assertEquals(null, result.getOrNull())
        verify(exactly = 0) { testFactory.annotationBeanInspector.inspect(any()) }
    }
}
