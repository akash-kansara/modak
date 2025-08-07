package io.github.akashkansara.modak.core.beantraversal

import arrow.core.Either
import arrow.core.raise.either
import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.beanmetadata.PropertyMetaData
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.testbed.buildValidMinimalCompany
import io.github.akashkansara.modak.core.util.BeanUtil
import io.github.akashkansara.modak.core.util.TypeUtil
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BeanTraverserTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var beanTraverser: BeanTraverser
    private lateinit var beanUtil: BeanUtil
    private lateinit var typeUtil: TypeUtil

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        beanTraverser = testFactory.beanTraverser
        beanUtil = testFactory.beanUtil
        typeUtil = testFactory.typeUtil
    }

    @Test
    fun `test basic bean traversal with nested properties`() {
        val parentBean = buildValidMinimalCompany()
        val context = BeanTraverserTestContext()
        val result = beanTraverser.traverse(parentBean, BeanTraverserTestCallback(), context)
        val expectedTraversals = listOf(
            Pair("", TraversalNodeType.BEAN_ENTERED),
            Pair("branches", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch]", TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
            Pair("branches[Minimal Branch]", TraversalNodeType.BEAN_ENTERED),
            Pair("branches[Minimal Branch].address", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].address", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].assets", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].assets[0]", TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
            Pair("branches[Minimal Branch].assets[0]", TraversalNodeType.BEAN_ENTERED),
            Pair("branches[Minimal Branch].assets[0].assignedTo", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].assets[0].assignedTo", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].assets[0].brand", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].assets[0].brand", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].assets[0]", TraversalNodeType.BEAN_EXITED),
            Pair("branches[Minimal Branch].assets[0]", TraversalNodeType.CONTAINER_ELEMENT_EXITED),
            Pair("branches[Minimal Branch].assets", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0]", TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
            Pair("branches[Minimal Branch].employees[0]", TraversalNodeType.BEAN_ENTERED),
            Pair("branches[Minimal Branch].employees[0].catchPhrase", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].catchPhrase", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0].emergencyContact", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].emergencyContact", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0].jobTitle", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].jobTitle", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0].name", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].name", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0].salary", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].salary", TraversalNodeType.BEAN_ENTERED),
            Pair("branches[Minimal Branch].employees[0].salary.amount", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].salary.amount", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0].salary.currencyCode", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[0].salary.currencyCode", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0].salary", TraversalNodeType.BEAN_EXITED),
            Pair("branches[Minimal Branch].employees[0].salary", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[0]", TraversalNodeType.BEAN_EXITED),
            Pair("branches[Minimal Branch].employees[0]", TraversalNodeType.CONTAINER_ELEMENT_EXITED),
            Pair("branches[Minimal Branch].employees[1]", TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
            Pair("branches[Minimal Branch].employees[1]", TraversalNodeType.BEAN_ENTERED),
            Pair("branches[Minimal Branch].employees[1].catchPhrase", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].catchPhrase", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1].emergencyContact", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].emergencyContact", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1].jobTitle", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].jobTitle", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1].name", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].name", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1].salary", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].salary", TraversalNodeType.BEAN_ENTERED),
            Pair("branches[Minimal Branch].employees[1].salary.amount", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].salary.amount", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1].salary.currencyCode", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].employees[1].salary.currencyCode", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1].salary", TraversalNodeType.BEAN_EXITED),
            Pair("branches[Minimal Branch].employees[1].salary", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].employees[1]", TraversalNodeType.BEAN_EXITED),
            Pair("branches[Minimal Branch].employees[1]", TraversalNodeType.CONTAINER_ELEMENT_EXITED),
            Pair("branches[Minimal Branch].employees", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].establishedYear", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].establishedYear", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].manager", TraversalNodeType.PROPERTY_ENTERED),
            // No BEAN_ENTERED for manager as it's already visited in the employees list
            Pair("branches[Minimal Branch].manager", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch].name", TraversalNodeType.PROPERTY_ENTERED),
            Pair("branches[Minimal Branch].name", TraversalNodeType.PROPERTY_EXITED),
            Pair("branches[Minimal Branch]", TraversalNodeType.BEAN_EXITED),
            Pair("branches[Minimal Branch]", TraversalNodeType.CONTAINER_ELEMENT_EXITED),
            Pair("branches", TraversalNodeType.PROPERTY_EXITED),
            Pair("establishedYear", TraversalNodeType.PROPERTY_ENTERED),
            Pair("establishedYear", TraversalNodeType.PROPERTY_EXITED),
            Pair("headquarters", TraversalNodeType.PROPERTY_ENTERED),
            // No BEAN_ENTERED for headquarters as it's already visited in the branches map
            Pair("headquarters", TraversalNodeType.PROPERTY_EXITED),
            Pair("name", TraversalNodeType.PROPERTY_ENTERED),
            Pair("name", TraversalNodeType.PROPERTY_EXITED),
            Pair("phoneNumbers", TraversalNodeType.PROPERTY_ENTERED),
            Pair("phoneNumbers[0]", TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
            Pair("phoneNumbers[0]", TraversalNodeType.BEAN_ENTERED),
            Pair("phoneNumbers[0].number", TraversalNodeType.PROPERTY_ENTERED),
            Pair("phoneNumbers[0].number", TraversalNodeType.PROPERTY_EXITED),
            Pair("phoneNumbers[0]", TraversalNodeType.BEAN_EXITED),
            Pair("phoneNumbers[0]", TraversalNodeType.CONTAINER_ELEMENT_EXITED),
            Pair("phoneNumbers[1]", TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
            Pair("phoneNumbers[1]", TraversalNodeType.BEAN_ENTERED),
            Pair("phoneNumbers[1].number", TraversalNodeType.PROPERTY_ENTERED),
            Pair("phoneNumbers[1].number", TraversalNodeType.PROPERTY_EXITED),
            Pair("phoneNumbers[1]", TraversalNodeType.BEAN_EXITED),
            Pair("phoneNumbers[1]", TraversalNodeType.CONTAINER_ELEMENT_EXITED),
            Pair("phoneNumbers", TraversalNodeType.PROPERTY_EXITED),
            Pair("", TraversalNodeType.BEAN_EXITED),
        )
        assertTrue(result.isRight())
        assertIterableEquals(expectedTraversals, context.traversals)
    }

    @ParameterizedTest
    @ValueSource(strings = ["branches", "headquarters", "phoneNumbers"])
    fun `traverseBeanProperty fails with internal error if getValue fails`(propToFail: String) {
        every {
            beanTraverser.traverseBeanProperty<Any>(
                any(),
                any(),
                any(),
            )
        } coAnswers {
            val prop = thirdArg<PropertyMetaData>()
            if (prop.name == propToFail) {
                every { beanUtil.getPropertyValue(any(), any()) } throws IllegalAccessException("Access denied")
            }
            callOriginal()
        }
        val parentBean = buildValidMinimalCompany()
        val context = BeanTraverserTestContext()
        val result = beanTraverser.traverse(parentBean, BeanTraverserTestCallback(), context)
        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertTrue(error is InternalError.BeanTraversalError)
        assertTrue(error?.cause is IllegalAccessException)
    }

    @Test
    fun `traverse fails with internal error if getContainerType fails`() {
        every { typeUtil.getContainerType(any()) } throws Exception("Access denied")
        val parentBean = buildValidMinimalCompany()
        val context = BeanTraverserTestContext()
        val result = beanTraverser.traverse(parentBean, BeanTraverserTestCallback(), context)
        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertTrue(error is InternalError.BeanTraversalError)
        assertTrue(error?.cause is Exception)
    }

    @Test
    fun `traverse always clears the internal map`() {
        var contextMap: BeanTraversalContext<*, *>? = null
        every {
            beanTraverser.traverseBean<Any>(any(), any())
        } coAnswers {
            contextMap = firstArg<BeanTraversalContext<*, *>>()
            callOriginal()
        }
        // Happy path
        val parentBean = buildValidMinimalCompany()
        var context = BeanTraverserTestContext()
        var result = beanTraverser.traverse(parentBean, BeanTraverserTestCallback(), context)
        assertTrue(result.isRight())
        assertTrue(contextMap!!.visitedCount() == 0)
        // Managed Error path
        every { typeUtil.getContainerType(any()) } throws Exception("Access denied")
        context = BeanTraverserTestContext()
        result = beanTraverser.traverse(parentBean, BeanTraverserTestCallback(), context)
        assertTrue(result.isLeft())
        assertTrue(contextMap!!.visitedCount() == 0)
        // Random Exception path
        every { beanTraverser.traverseBean<Any>(any(), any()) } throws Exception("Random Exception")
        context = BeanTraverserTestContext()
        result = beanTraverser.traverse(parentBean, BeanTraverserTestCallback(), context)
        assertTrue(result.isLeft())
        assertTrue(contextMap!!.visitedCount() == 0)
    }

    class BeanTraverserTestCallback : TraversalCallback<BeanTraverserTestContext> {
        override fun onBeanEntered(node: TraversalNode.Bean<BeanTraverserTestContext>): Either<InternalError, Unit> =
            either {
                node.clientContext.traversals.add(
                    Pair(node.path.toString(), TraversalNodeType.BEAN_ENTERED),
                )
            }

        override fun onBeanExited(node: TraversalNode.Bean<BeanTraverserTestContext>): Either<InternalError, Unit> =
            either {
                node.clientContext.traversals.add(
                    Pair(node.path.toString(), TraversalNodeType.BEAN_EXITED),
                )
            }

        override fun onPropertyEntered(node: TraversalNode.Property<BeanTraverserTestContext>): Either<InternalError, Unit> =
            either {
                node.clientContext.traversals.add(
                    Pair(node.path.toString(), TraversalNodeType.PROPERTY_ENTERED),
                )
            }

        override fun onPropertyExited(node: TraversalNode.Property<BeanTraverserTestContext>): Either<InternalError, Unit> =
            either {
                node.clientContext.traversals.add(
                    Pair(node.path.toString(), TraversalNodeType.PROPERTY_EXITED),
                )
            }

        override fun onContainerElementEntered(node: TraversalNode.ContainerElement<BeanTraverserTestContext>): Either<InternalError, Unit> =
            either {
                node.clientContext.traversals.add(
                    Pair(node.path.toString(), TraversalNodeType.CONTAINER_ELEMENT_ENTERED),
                )
            }

        override fun onContainerElementExited(node: TraversalNode.ContainerElement<BeanTraverserTestContext>): Either<InternalError, Unit> =
            either {
                node.clientContext.traversals.add(
                    Pair(node.path.toString(), TraversalNodeType.CONTAINER_ELEMENT_EXITED),
                )
            }
    }

    enum class TraversalNodeType {
        BEAN_ENTERED,
        BEAN_EXITED,
        PROPERTY_ENTERED,
        PROPERTY_EXITED,
        CONTAINER_ELEMENT_ENTERED,
        CONTAINER_ELEMENT_EXITED,
    }

    data class BeanTraverserTestContext(
        val traversals: MutableList<Pair<String, TraversalNodeType>> = mutableListOf(),
    )
}
