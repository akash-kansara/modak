package io.github.akashkansara.modak.core.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class TypeUtil {
    private val boxedToPrimitivePairs = listOf(
        Pair(Int::class.java, java.lang.Integer::class.java),
        Pair(Long::class.java, java.lang.Long::class.java),
        Pair(Boolean::class.java, java.lang.Boolean::class.java),
        Pair(Double::class.java, java.lang.Double::class.java),
        Pair(Float::class.java, java.lang.Float::class.java),
        Pair(Short::class.java, java.lang.Short::class.java),
        Pair(Byte::class.java, java.lang.Byte::class.java),
        Pair(Char::class.java, java.lang.Character::class.java),
    )

    private val boxingCompatibilityMap = boxedToPrimitivePairs
        .flatMap { listOf(it, Pair(it.second, it.first)) }
        .toMap()

    fun areTypesMatching(a: Type, b: Type): Boolean {
        return when (a) {
            is ParameterizedType -> {
                (b as? ParameterizedType)?.let { areParameterizedTypesMatching(a, it) } ?: false
            }
            is Class<*> -> {
                (b as? Class<*>)?.let { areClassesMatching(a, it) } ?: false
            }
            else -> false
        }
    }

    private fun areParameterizedTypesMatching(a: ParameterizedType, b: ParameterizedType): Boolean {
        val rawTypeA = a.rawType
        val rawTypeB = b.rawType
        val isRawTypeMatching = when {
            rawTypeA is ParameterizedType && rawTypeB is ParameterizedType ->
                areParameterizedTypesMatching(rawTypeA, rawTypeB)
            rawTypeA is Class<*> && rawTypeB is Class<*> -> areClassesMatching(rawTypeA, rawTypeB)
            else -> rawTypeA == rawTypeB
        }
        if (!isRawTypeMatching) return false
        val argsA = a.actualTypeArguments
        val argsB = b.actualTypeArguments
        if (argsA == null || argsB == null) return false
        if (argsA.size != argsB.size) return false
        return argsA.zip(argsB).all { (argA, argB) -> areTypesMatching(argA, argB) }
    }

    private fun areClassesMatching(a: Class<*>, b: Class<*>) = a == b ||
        areBoxingCompatible(a, b) ||
        (isMap(a) && isMap(b)) ||
        (isList(a) && isList(b))

    private fun areBoxingCompatible(a: Class<*>, b: Class<*>) =
        boxingCompatibilityMap[a] == b || boxingCompatibilityMap[b] == a

    fun getBoxedType(clazz: Class<*>) = boxingCompatibilityMap[clazz]

    fun getParameterizedTypeArgs(type: ParameterizedType) = type.actualTypeArguments?.toList() ?: emptyList()

    fun getContainerType(type: Type) = when {
        isMap(type) -> ContainerType.MAP
        isArray(type) -> ContainerType.ARRAY
        isList(type) -> ContainerType.LIST
        else -> null
    }

    fun isMap(type: Type) = when (type) {
        is ParameterizedType -> (type.rawType as? Class<*>)?.let { isMap(it) } ?: false
        is Class<*> -> isMap(type)
        else -> false
    }

    fun isArray(type: Type) = (type as? Class<*>)?.isArray ?: false

    fun isList(type: Type) = when (type) {
        is ParameterizedType -> (type.rawType as? Class<*>)?.let { isList(it) } ?: false
        is Class<*> -> isList(type)
        else -> false
    }

    fun isMap(clazz: Class<*>) = Map::class.java.isAssignableFrom(clazz)

    fun isArray(clazz: Class<*>) = clazz.isArray

    fun isList(clazz: Class<*>) = List::class.java.isAssignableFrom(clazz)
}
