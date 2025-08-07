package io.github.akashkansara.modak.core.models

import io.github.akashkansara.modak.api.ElementKind
import io.github.akashkansara.modak.api.Path

class PathImpl : Path {
    private val nodes = mutableListOf<Path.Node>()

    override fun iterator(): Iterator<Path.Node> {
        return nodes.toList().iterator()
    }

    data class NodeImpl(
        override val name: String?,
        override val index: Any?,
        override val kind: ElementKind,
    ) : Path.Node {
        override fun <T : Path.Node> asType(nodeType: Class<T>): T {
            return nodeType.cast(this)
        }

        override fun toString(): String {
            return "Node(name='$name', index='$index', kind='$kind')"
        }
    }

    override fun addNode(node: Path.Node) {
        nodes.add(node)
    }

    override fun copy(): PathImpl {
        val newPath = PathImpl()
        newPath.nodes.addAll(this.nodes)
        return newPath
    }

    override fun toString(): String {
        val iterator = iterator()
        var pathStr = ""
        while (iterator.hasNext()) {
            val node = iterator.next()
            pathStr += when (node.kind) {
                ElementKind.CONTAINER_ELEMENT -> node.index ?.let { "[$it]" } ?: ""
                ElementKind.PROPERTY -> node.name?.let {
                    if (pathStr.isEmpty()) it else ".$it"
                } ?: ""
                else -> ""
            }
        }
        return pathStr
    }
}
