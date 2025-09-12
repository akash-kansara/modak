package io.github.akashkansara.modak.api

/**
 * Represents a path to a property within an object graph.
 */
interface Path : Iterable<Path.Node> {
    /**
     * Represents a single node in the path.
     */
    interface Node {
        /** Name of the property or field */
        val name: String?

        /** Index for array/list elements or key for map entries */
        val index: Any?

        /** Kind of element this node represents */
        val kind: ElementKind

        /** Casts this node to the specified type */
        fun <T : Node> asType(nodeType: Class<T>): T
        override fun toString(): String
    }

    /** Creates a copy of this path */
    fun copy(): Path

    /** Adds a node to the end of this path */
    fun addNode(node: Node)
}
