package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.ElementKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PathImplTest {
    @Test
    fun `should create empty PathImpl`() {
        val path = PathImpl()
        val iterator = path.iterator()
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `should add single node and iterate`() {
        val path = PathImpl()
        val node = PathImpl.NodeImpl("property", null, ElementKind.PROPERTY)
        path.addNode(node)
        val iterator = path.iterator()
        assertTrue(iterator.hasNext())
        val retrievedNode = iterator.next()
        assertEquals(node, retrievedNode)
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `should add multiple nodes and iterate in order`() {
        val path = PathImpl()
        val node1 = PathImpl.NodeImpl("property1", null, ElementKind.PROPERTY)
        val node2 = PathImpl.NodeImpl("property2", 0, ElementKind.CONTAINER_ELEMENT)
        val node3 = PathImpl.NodeImpl("property3", null, ElementKind.PROPERTY)
        path.addNode(node1)
        path.addNode(node2)
        path.addNode(node3)
        val nodes = path.toList()
        assertEquals(3, nodes.size)
        assertEquals(node1, nodes[0])
        assertEquals(node2, nodes[1])
        assertEquals(node3, nodes[2])
    }

    @Test
    fun `should copy path correctly`() {
        val original = PathImpl()
        val node1 = PathImpl.NodeImpl("property1", null, ElementKind.PROPERTY)
        val node2 = PathImpl.NodeImpl("property2", 0, ElementKind.CONTAINER_ELEMENT)
        original.addNode(node1)
        original.addNode(node2)
        val copy = original.copy()
        assertNotEquals(original, copy) // Different instances
        assertEquals(original.toList(), copy.toList()) // Same content
        assertEquals(2, copy.toList().size)
    }

    @Test
    fun `should handle toString for property only path`() {
        val path = PathImpl()
        path.addNode(PathImpl.NodeImpl("name", null, ElementKind.PROPERTY))
        val pathString = path.toString()
        assertEquals("name", pathString)
    }

    @Test
    fun `should handle toString for nested properties`() {
        val path = PathImpl()
        path.addNode(PathImpl.NodeImpl("user", null, ElementKind.PROPERTY))
        path.addNode(PathImpl.NodeImpl("address", null, ElementKind.PROPERTY))
        path.addNode(PathImpl.NodeImpl("street", null, ElementKind.PROPERTY))
        val pathString = path.toString()
        assertEquals("user.address.street", pathString)
    }

    @Test
    fun `should handle toString for container elements`() {
        val path = PathImpl()
        path.addNode(PathImpl.NodeImpl("items", null, ElementKind.PROPERTY))
        path.addNode(PathImpl.NodeImpl(null, 0, ElementKind.CONTAINER_ELEMENT))
        val pathString = path.toString()
        assertEquals("items[0]", pathString)
    }

    @Test
    fun `should handle toString for complex path`() {
        val path = PathImpl()
        path.addNode(PathImpl.NodeImpl("users", null, ElementKind.PROPERTY))
        path.addNode(PathImpl.NodeImpl(null, 1, ElementKind.CONTAINER_ELEMENT))
        path.addNode(PathImpl.NodeImpl("addresses", null, ElementKind.PROPERTY))
        path.addNode(PathImpl.NodeImpl(null, "home", ElementKind.CONTAINER_ELEMENT))
        path.addNode(PathImpl.NodeImpl("zipCode", null, ElementKind.PROPERTY))
        val pathString = path.toString()
        assertEquals("users[1].addresses[home].zipCode", pathString)
    }

    @Test
    fun `should handle toString for empty path`() {
        val path = PathImpl()
        val pathString = path.toString()
        assertEquals("", pathString)
    }

    @Test
    fun `should handle toString with bean kind`() {
        val path = PathImpl()
        path.addNode(PathImpl.NodeImpl("bean", null, ElementKind.BEAN))
        path.addNode(PathImpl.NodeImpl("property", null, ElementKind.PROPERTY))
        val pathString = path.toString()
        assertEquals("property", pathString) // Bean kind should not contribute to path string
    }

    @Test
    fun `should create NodeImpl with all parameters`() {
        val name = "testProperty"
        val index = 42
        val kind = ElementKind.PROPERTY
        val node = PathImpl.NodeImpl(name, index, kind)
        assertEquals(name, node.name)
        assertEquals(index, node.index)
        assertEquals(kind, node.kind)
    }

    @Test
    fun `should create NodeImpl with null name`() {
        val index = "key"
        val kind = ElementKind.CONTAINER_ELEMENT
        val node = PathImpl.NodeImpl(null, index, kind)
        assertNull(node.name)
        assertEquals(index, node.index)
        assertEquals(kind, node.kind)
    }

    @Test
    fun `should create NodeImpl with null index`() {
        val name = "property"
        val kind = ElementKind.PROPERTY
        val node = PathImpl.NodeImpl(name, null, kind)
        assertEquals(name, node.name)
        assertNull(node.index)
        assertEquals(kind, node.kind)
    }

    @Test
    fun `should handle different index types in container elements`() {
        val path = PathImpl()
        path.addNode(PathImpl.NodeImpl("map", null, ElementKind.PROPERTY))
        path.addNode(PathImpl.NodeImpl(null, "stringKey", ElementKind.CONTAINER_ELEMENT))
        path.addNode(PathImpl.NodeImpl(null, 42, ElementKind.CONTAINER_ELEMENT))
        val pathString = path.toString()
        assertEquals("map[stringKey][42]", pathString)
    }
} 
