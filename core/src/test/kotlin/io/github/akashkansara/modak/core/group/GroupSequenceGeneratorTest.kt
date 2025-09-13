package io.github.akashkansara.modak.core.group

import io.github.akashkansara.modak.api.DefaultGroup
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.testbed.BranchGroup
import io.github.akashkansara.modak.core.testbed.CorporateGroup
import io.github.akashkansara.modak.core.testbed.InvalidGroupSequence
import io.github.akashkansara.modak.core.testbed.InvalidNestedGroup
import io.github.akashkansara.modak.core.testbed.RegionalBranchGroup
import io.github.akashkansara.modak.core.testbed.SloughGroup
import io.github.akashkansara.modak.core.testbed.SwindonGroup
import io.github.akashkansara.modak.core.testbed.beans.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GroupSequenceGeneratorTest {
    private val generator = GroupSequenceGenerator()

    @Test
    fun `generateGroupSequence should return DefaultGroup when input is null`() {
        val result = generator.generateGroupSequence(null)
        assertTrue(result.isRight())
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(DefaultGroup::class.java, result.getOrNull()!!.first())
    }

    @Test
    fun `generateGroupSequence should return empty list when input is empty`() {
        val result = generator.generateGroupSequence(emptyList())
        assertTrue(result.isRight())
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun `generateGroupSequence should resolve simple group sequence`() {
        val inputGroups = listOf(CorporateGroup::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isRight())
        val groups = result.getOrNull()!!
        assertEquals(2, groups.size)
        assertEquals(SloughGroup::class.java, groups[0])
        assertEquals(SwindonGroup::class.java, groups[1])
    }

    @Test
    fun `generateGroupSequence should resolve multiple independent groups`() {
        val inputGroups = listOf(SwindonGroup::class.java, SloughGroup::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isRight())
        val groups = result.getOrNull()!!
        assertEquals(2, groups.size)
        assertEquals(SwindonGroup::class.java, groups[0])
        assertEquals(SloughGroup::class.java, groups[1])
    }

    @Test
    fun `generateGroupSequence should handle interface inheritance correctly`() {
        val inputGroups = listOf(RegionalBranchGroup::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isRight())
        val groups = result.getOrNull()!!
        assertEquals(2, groups.size)
        assertEquals(BranchGroup::class.java, groups[0])
        assertEquals(RegionalBranchGroup::class.java, groups[1])
    }

    @Test
    fun `generateGroupSequence should return error for non-interface class`() {
        val inputGroups = listOf(Money::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isLeft())
        val error = result.leftOrNull() as InternalError.GroupSequenceError
        assertTrue(error.message.contains("Only interfaces can be used as groups"))
        assertTrue(error.message.contains("Money"))
    }

    @Test
    fun `generateGroupSequence should handle duplicate groups correctly`() {
        val inputGroups = listOf(SloughGroup::class.java, SloughGroup::class.java, SwindonGroup::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isRight())
        val groups = result.getOrNull()!!
        assertEquals(2, groups.size)
        assertTrue(groups.contains(SloughGroup::class.java))
        assertTrue(groups.contains(SwindonGroup::class.java))
    }

    @Test
    fun `generateGroupSequence should detect nested GroupSequence`() {
        val inputGroups = listOf(InvalidGroupSequence::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isLeft())
        val error = result.leftOrNull() as InternalError.GroupSequenceError
        assertTrue(error.message.contains("Nested GroupSequence detected"))
    }

    @Test
    fun `generateGroupSequence should detect circular GroupSequence`() {
        val inputGroups = listOf(InvalidNestedGroup::class.java)
        val result = generator.generateGroupSequence(inputGroups)
        assertTrue(result.isLeft())
        val error = result.leftOrNull() as InternalError.GroupSequenceError
        assertTrue(error.message.contains("Nested GroupSequence detected"))
    }
} 
