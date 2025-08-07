package io.github.akashkansara.modak.api

interface Path : Iterable<Path.Node> {
    interface Node {
        val name: String?
        val index: Any?
        val kind: ElementKind
        fun <T : Node> asType(nodeType: Class<T>): T
        override fun toString(): String
    }

    fun copy(): Path

    fun addNode(node: Node)
}
