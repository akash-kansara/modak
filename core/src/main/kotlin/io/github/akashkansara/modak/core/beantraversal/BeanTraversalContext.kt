package io.github.akashkansara.modak.core.beantraversal

import io.github.akashkansara.modak.api.Path
import io.github.akashkansara.modak.core.models.PathImpl
import java.util.IdentityHashMap

class BeanTraversalContext<T, C>(
    val root: T,
    val clientContext: C,
    val callback: TraversalCallback<C>,
) {
    private val visited = IdentityHashMap<Any, Boolean>()
    private var currentPath: Path = PathImpl()
    private var parentNode: TraversalNode<C>? = null

    fun getCurrentPath(): Path = currentPath

    fun setCurrentPath(path: Path) {
        currentPath = path
    }

    fun setParentNode(node: TraversalNode<C>?) {
        parentNode = node
    }

    fun getParentNode(): TraversalNode<C>? = parentNode

    fun markVisited(obj: Any) {
        visited[obj] = true
    }

    fun hasVisited(obj: Any): Boolean = visited.contains(obj)

    fun visitedCount(): Int = visited.size

    fun clear() {
        visited.clear()
    }
}
