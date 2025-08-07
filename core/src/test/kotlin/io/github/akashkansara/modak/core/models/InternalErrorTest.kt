package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.DefaultGroup
import io.github.akashkansara.modak.api.ElementKind
import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.core.beanmetadata.ConfigurationSource
import io.github.akashkansara.modak.core.testbed.beans.Chair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InternalErrorTest {
    @Test
    fun `should create CorrectionError with message only`() {
        val message = "Correction failed"
        val error = InternalError.CorrectionError(message = message)
        assertEquals(message, error.message)
        assertNull(error.cause)
    }

    @Test
    fun `should create CorrectionError with message and cause`() {
        val message = "Correction failed"
        val cause = RuntimeException("Root cause")
        val error = InternalError.CorrectionError(cause = cause, message = message)
        assertEquals(message, error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `should create BeanModificationError with message only`() {
        val message = "Bean modification failed"
        val error = InternalError.BeanModificationError(message = message)
        assertEquals(message, error.message)
        assertNull(error.cause)
        assertTrue(error.appliedCorrections.isEmpty())
    }

    @Test
    fun `should create BeanModificationError with applied corrections`() {
        val message = "Bean modification failed"
        val appliedCorrections = createSampleAppliedCorrections()
        val error = InternalError.BeanModificationError(
            message = message,
            appliedCorrections = appliedCorrections,
        )
        assertEquals(message, error.message)
        assertEquals(appliedCorrections, error.appliedCorrections)
        assertEquals(2, error.appliedCorrections.size)
    }

    @Test
    fun `should create BeanModificationError with all parameters`() {
        val message = "Bean modification failed"
        val cause = IllegalStateException("Invalid state")
        val appliedCorrections = createSampleAppliedCorrections()
        val error = InternalError.BeanModificationError(
            cause = cause,
            message = message,
            appliedCorrections = appliedCorrections,
        )
        assertEquals(message, error.message)
        assertEquals(cause, error.cause)
        assertEquals(appliedCorrections, error.appliedCorrections)
    }

    @Test
    fun `should create BeanInspectionError with configuration source`() {
        val message = "Bean inspection failed"
        val configurationSource = ConfigurationSource.ANNOTATION
        val error = InternalError.BeanInspectionError(
            configurationSource = configurationSource,
            message = message,
        )
        assertEquals(message, error.message)
        assertEquals(configurationSource, error.configurationSource)
        assertNull(error.cause)
    }

    @Test
    fun `should create BeanInspectionError with all parameters`() {
        val message = "Bean inspection failed"
        val cause = NoSuchMethodException("Method not found")
        val configurationSource = ConfigurationSource.ANNOTATION
        val error = InternalError.BeanInspectionError(
            configurationSource = configurationSource,
            cause = cause,
            message = message,
        )
        assertEquals(message, error.message)
        assertEquals(cause, error.cause)
        assertEquals(configurationSource, error.configurationSource)
    }

    @Test
    fun `should create GroupSequenceError with message only`() {
        val message = "Group sequence error"
        val error = InternalError.GroupSequenceError(message = message)
        assertEquals(message, error.message)
        assertNull(error.cause)
    }

    @Test
    fun `should create GroupSequenceError with message and cause`() {
        val message = "Group sequence error"
        val cause = IllegalArgumentException("Invalid group")
        val error = InternalError.GroupSequenceError(cause = cause, message = message)
        assertEquals(message, error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `should create BeanTraversalError with message only`() {
        val message = "Bean traversal error"
        val error = InternalError.BeanTraversalError(message = message)
        assertEquals(message, error.message)
        assertNull(error.cause)
    }

    @Test
    fun `should create BeanTraversalError with message and cause`() {
        val message = "Bean traversal error"
        val cause = IllegalAccessException("Access denied")
        val error = InternalError.BeanTraversalError(cause = cause, message = message)
        assertEquals(message, error.message)
        assertEquals(cause, error.cause)
    }

    private fun createSampleAppliedCorrections(): List<AppliedCorrection<*>> {
        val path1 = PathImpl().apply {
            addNode(PathImpl.NodeImpl("property1", null, ElementKind.PROPERTY))
        }
        val path2 = PathImpl().apply {
            addNode(PathImpl.NodeImpl("property2", 0, ElementKind.CONTAINER_ELEMENT))
        }

        val descriptor1 = CorrectionDescriptorImpl(
            annotation = getDefaultValueAnnotation(),
            groups = setOf(DefaultGroup::class.java),
        )
        val descriptor2 = CorrectionDescriptorImpl(
            annotation = getDefaultValueAnnotation(),
        )

        return listOf(
            AppliedCorrectionImpl(
                root = "TestRoot1",
                propertyPath = path1,
                oldValue = "oldValue1",
                newValue = "newValue1",
                correctionDescriptor = descriptor1,
            ),
            AppliedCorrectionImpl(
                root = "TestRoot2",
                propertyPath = path2,
                oldValue = null,
                newValue = "newValue2",
                correctionDescriptor = descriptor2,
            ),
        )
    }

    private fun getDefaultValueAnnotation(): DefaultValue {
        return Chair::class.java.getDeclaredField("assignedTo").getAnnotation(DefaultValue::class.java)
    }
} 
