package io.github.akashkansara.modak.core.group

import arrow.core.Either
import arrow.core.raise.either
import io.github.akashkansara.modak.api.DefaultGroup
import io.github.akashkansara.modak.api.GroupSequence
import io.github.akashkansara.modak.core.models.InternalError

class GroupSequenceGenerator {
    fun generateGroupSequence(groups: List<Class<*>>?): Either<InternalError, List<Class<*>>> {
        return either {
            if (groups == null) {
                return Either.Right(listOf(DefaultGroup::class.java))
            }
            val resolved = mutableListOf<Class<*>>()
            val visited = mutableSetOf<Class<*>>()
            val processing = mutableSetOf<Class<*>>()
            for (group in groups) {
                resolveGroup(group, resolved, visited, processing).bind()
            }
            resolved.toList()
        }
    }

    private fun resolveGroup(
        group: Class<*>,
        resolved: MutableList<Class<*>>,
        visited: MutableSet<Class<*>>,
        processing: MutableSet<Class<*>>,
    ): Either<InternalError, Unit> {
        return either {
            if (group in visited) return@either
            if (!group.isInterface) {
                raise(
                    InternalError.GroupSequenceError(
                        null,
                        "Only interfaces can be used as groups. Invalid: ${group.name}",
                    ),
                )
            }
            if (group in processing) {
                raise(
                    InternalError.GroupSequenceError(
                        null,
                        "Cyclic group dependency detected involving: ${group.name}",
                    ),
                )
            }
            processing.add(group)
            val sequence = getGroupSequence(group)
            if (sequence != null) {
                for (member in sequence.value) {
                    if (isGroupSequence(member.java)) {
                        raise(
                            InternalError.GroupSequenceError(
                                null,
                                "Nested GroupSequence detected. $group contains $member, which is also a sequence",
                            ),
                        )
                    }
                }
                for (member in sequence.value) {
                    resolveGroup(member.java, resolved, visited, processing)
                }
            } else {
                for (superInterface in group.interfaces) {
                    resolveGroup(superInterface, resolved, visited, processing)
                }
                resolved.add(group)
            }
            processing.remove(group)
            visited.add(group)
        }
    }

    private fun isGroupSequence(group: Class<*>) = group.isAnnotationPresent(GroupSequence::class.java)

    private fun getGroupSequence(group: Class<*>) = group.getAnnotation(GroupSequence::class.java)
}
