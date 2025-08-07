package io.github.akashkansara.modak.core.group

import io.github.akashkansara.modak.core.testbed.SloughGroup
import io.github.akashkansara.modak.core.testbed.SwindonGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GroupSequenceIteratorTest {
    @Test
    fun `iterator should handle empty list correctly`() {
        val emptyGroups = emptyList<Class<*>>()
        val iterator = GroupSequenceIterator(emptyGroups)
        assertFalse(iterator.hasNext())
        val exception = assertThrows(NoSuchElementException::class.java) {
            iterator.next()
        }
        assertTrue(exception.message!!.contains("Failed to get next validation group from sequence"))
        assertTrue(exception.message!!.contains("sequence of 0 groups"))
    }

    @Test
    fun `iterator should iterate through single group correctly`() {
        val singleGroup = listOf(SwindonGroup::class.java)
        val iterator = GroupSequenceIterator(singleGroup)
        assertTrue(iterator.hasNext())
        assertEquals(SwindonGroup::class.java, iterator.next())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `iterator should iterate through multiple groups in correct order`() {
        val multipleGroups = listOf(
            SwindonGroup::class.java,
            SloughGroup::class.java,
        )
        val iterator = GroupSequenceIterator(multipleGroups)
        assertTrue(iterator.hasNext())
        assertEquals(SwindonGroup::class.java, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(SloughGroup::class.java, iterator.next())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `iterator should throw exception when accessing beyond bounds`() {
        val groups = listOf(SwindonGroup::class.java, SloughGroup::class.java)
        val iterator = GroupSequenceIterator(groups)
        iterator.next()
        iterator.next()
        assertFalse(iterator.hasNext())
        assertThrows(NoSuchElementException::class.java) {
            iterator.next()
        }
    }
}
