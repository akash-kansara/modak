package io.github.akashkansara.modak.core.group

class GroupSequenceIterator(groups: List<Class<*>>) : Iterator<Class<*>> {
    private val sequence: List<Class<*>> = groups
    private var currentIndex: Int = 0

    override fun hasNext() = currentIndex < sequence.size

    override fun next(): Class<*> {
        if (!hasNext()) {
            throw NoSuchElementException("Failed to get next validation group from sequence - attempted to access element beyond index ${currentIndex - 1} in sequence of ${sequence.size} groups")
        }
        return sequence[currentIndex++]
    }
}
