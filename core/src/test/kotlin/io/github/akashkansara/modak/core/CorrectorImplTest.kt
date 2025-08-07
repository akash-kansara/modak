package io.github.akashkansara.modak.core

import arrow.core.raise.either
import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.CorrectionResult
import io.github.akashkansara.modak.api.Corrector
import io.github.akashkansara.modak.api.DefaultGroup
import io.github.akashkansara.modak.core.beanmodification.BeanModifier
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.testbed.beans.Company
import io.github.akashkansara.modak.core.testbed.buildCompany
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CorrectorImplTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var beanModifier: BeanModifier
    private lateinit var corrector: Corrector

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        beanModifier = testFactory.beanModifier
        corrector = testFactory.corrector
    }

    @Test
    fun `should apply all corrections to bean`() {
        val bean = buildCompany()
        var actualCorrections: List<AppliedCorrection<*>> = emptyList()
        every { beanModifier.modifyBean(any<Company>(), any(), any(), any()) } coAnswers {
            val modificationResult = callOriginal()
            actualCorrections = modificationResult.getOrNull()!!
            modificationResult
        }
        val result = corrector.correct(bean, false, null)
        assertTrue(result.isSuccess)
        assertIterableEquals((result as CorrectionResult.Success).appliedCorrections, actualCorrections)
    }

    @Test
    fun `should generate error when modification fails`() {
        val bean = buildCompany()
        var countPropExited = 0
        every { beanModifier.onPropertyExited(any()) } coAnswers {
            if (countPropExited++ == 3) {
                either { raise(InternalError.BeanModificationError(null, "Error")) }
            } else {
                callOriginal()
            }
        }
        val result = corrector.correct(bean, false, null, DefaultGroup::class.java)
        assertFalse(result.isSuccess)
        assertEquals(3, (result as CorrectionResult.Failure).error.appliedCorrections.size)
    }
}
