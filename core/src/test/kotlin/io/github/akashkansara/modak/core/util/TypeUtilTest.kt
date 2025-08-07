package io.github.akashkansara.modak.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import java.util.stream.Stream

class TypeUtilTest {
    private val typeUtil = TypeUtil()

    @Test
    fun `areTypesMatching should return true for identical classes`() {
        assertTrue(typeUtil.areTypesMatching(String::class.java, String::class.java))
        assertTrue(typeUtil.areTypesMatching(Int::class.java, Int::class.java))
        assertTrue(typeUtil.areTypesMatching(List::class.java, List::class.java))
    }

    @Test
    fun `areTypesMatching should return true for boxing compatible types`() {
        assertTrue(typeUtil.areTypesMatching(Int::class.java, java.lang.Integer::class.java))
        assertTrue(typeUtil.areTypesMatching(java.lang.Integer::class.java, Int::class.java))
        assertTrue(typeUtil.areTypesMatching(Boolean::class.java, java.lang.Boolean::class.java))
        assertTrue(typeUtil.areTypesMatching(Double::class.java, java.lang.Double::class.java))
        assertTrue(typeUtil.areTypesMatching(Float::class.java, java.lang.Float::class.java))
        assertTrue(typeUtil.areTypesMatching(Long::class.java, java.lang.Long::class.java))
        assertTrue(typeUtil.areTypesMatching(Short::class.java, java.lang.Short::class.java))
        assertTrue(typeUtil.areTypesMatching(Byte::class.java, java.lang.Byte::class.java))
        assertTrue(typeUtil.areTypesMatching(Char::class.java, java.lang.Character::class.java))
    }

    @Test
    fun `areTypesMatching should return true for compatible container types`() {
        assertTrue(typeUtil.areTypesMatching(HashMap::class.java, LinkedHashMap::class.java))
        assertTrue(typeUtil.areTypesMatching(ArrayList::class.java, LinkedList::class.java))
    }

    @Test
    fun `areTypesMatching should return false for incompatible types`() {
        assertFalse(typeUtil.areTypesMatching(String::class.java, Int::class.java))
        assertFalse(typeUtil.areTypesMatching(List::class.java, String::class.java))
        assertFalse(typeUtil.areTypesMatching(Map::class.java, Int::class.java))
    }

    @Test
    fun `areTypesMatching should handle parameterized types`() {
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val listStringType2 = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val listIntType = createParameterizedType(List::class.java, arrayOf(Int::class.java))

        assertTrue(typeUtil.areTypesMatching(listStringType, listStringType2))
        assertFalse(typeUtil.areTypesMatching(listStringType, listIntType))
    }

    @Test
    fun `areTypesMatching should handle complex parameterized types`() {
        // Map<String, Integer> vs Map<String, Integer>
        val mapStringIntType1 = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        val mapStringIntType2 = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        assertTrue(typeUtil.areTypesMatching(mapStringIntType1, mapStringIntType2))

        // Map<String, Integer> vs Map<String, String>
        val mapStringStringType = createParameterizedType(Map::class.java, arrayOf(String::class.java, String::class.java))
        assertFalse(typeUtil.areTypesMatching(mapStringIntType1, mapStringStringType))

        // Map<String, Integer> vs Map<Integer, String>
        val mapIntStringType = createParameterizedType(Map::class.java, arrayOf(java.lang.Integer::class.java, String::class.java))
        assertFalse(typeUtil.areTypesMatching(mapStringIntType1, mapIntStringType))
    }

    @Test
    fun `areTypesMatching should handle nested parameterized types`() {
        // List<Map<String, Integer>>
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        val listOfMapType1 = createParameterizedType(List::class.java, arrayOf(mapStringIntType))
        val listOfMapType2 = createParameterizedType(List::class.java, arrayOf(mapStringIntType))
        assertTrue(typeUtil.areTypesMatching(listOfMapType1, listOfMapType2))

        // List<Map<String, Integer>> vs List<Map<String, String>>
        val mapStringStringType = createParameterizedType(Map::class.java, arrayOf(String::class.java, String::class.java))
        val listOfMapStringStringType = createParameterizedType(List::class.java, arrayOf(mapStringStringType))
        assertFalse(typeUtil.areTypesMatching(listOfMapType1, listOfMapStringStringType))
    }

    @Test
    fun `areTypesMatching should handle parameterized types with boxing compatibility`() {
        // List<Integer> vs List<Int> - should match due to boxing compatibility
        val listIntegerType = createParameterizedType(List::class.java, arrayOf(java.lang.Integer::class.java))
        val listIntType = createParameterizedType(List::class.java, arrayOf(Int::class.java))
        assertTrue(typeUtil.areTypesMatching(listIntegerType, listIntType))

        // Map<Boolean, Double> vs Map<java.lang.Boolean, java.lang.Double>
        val mapBoolDoubleType = createParameterizedType(Map::class.java, arrayOf(Boolean::class.java, Double::class.java))
        val mapBooleanDoubleType = createParameterizedType(Map::class.java, arrayOf(java.lang.Boolean::class.java, java.lang.Double::class.java))
        assertTrue(typeUtil.areTypesMatching(mapBoolDoubleType, mapBooleanDoubleType))
    }

    @Test
    fun `areTypesMatching should handle parameterized types with different raw types`() {
        // List<String> vs ArrayList<String> - should match due to container compatibility
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val arrayListStringType = createParameterizedType(ArrayList::class.java, arrayOf(String::class.java))
        assertTrue(typeUtil.areTypesMatching(listStringType, arrayListStringType))

        // Map<String, Integer> vs HashMap<String, Integer>
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        val hashMapStringIntType = createParameterizedType(HashMap::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        assertTrue(typeUtil.areTypesMatching(mapStringIntType, hashMapStringIntType))

        // List<String> vs Map<String, String> - should not match
        val mapStringStringType = createParameterizedType(Map::class.java, arrayOf(String::class.java, String::class.java))
        assertFalse(typeUtil.areTypesMatching(listStringType, mapStringStringType))
    }

    @Test
    fun `areTypesMatching should handle parameterized types with different argument counts`() {
        // List<String> (1 arg) vs Map<String, Integer> (2 args)
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        assertFalse(typeUtil.areTypesMatching(listStringType, mapStringIntType))

        // Map<String> (1 arg - invalid but for testing) vs Map<String, Integer> (2 args)
        val mapStringOnlyType = createParameterizedType(Map::class.java, arrayOf(String::class.java))
        assertFalse(typeUtil.areTypesMatching(mapStringOnlyType, mapStringIntType))
    }

    @Test
    fun `areTypesMatching should handle parameterized type vs raw type`() {
        // List<String> vs raw List
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        assertFalse(typeUtil.areTypesMatching(listStringType, List::class.java))
        assertFalse(typeUtil.areTypesMatching(List::class.java, listStringType))
    }

    @Test
    fun `areTypesMatching should handle parameterized types with null arguments`() {
        // Test with parameterized type that has null type arguments
        val typeWithNullArgs = createParameterizedTypeWithNullArgs(List::class.java)
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))

        assertFalse(typeUtil.areTypesMatching(typeWithNullArgs, listStringType))
        assertFalse(typeUtil.areTypesMatching(listStringType, typeWithNullArgs))
    }

    @Test
    fun `areTypesMatching should handle deeply nested parameterized types`() {
        // Map<String, List<Map<Integer, String>>>
        val innerMapType = createParameterizedType(Map::class.java, arrayOf(java.lang.Integer::class.java, String::class.java))
        val listOfMapsType = createParameterizedType(List::class.java, arrayOf(innerMapType))
        val outerMapType1 = createParameterizedType(Map::class.java, arrayOf(String::class.java, listOfMapsType))
        val outerMapType2 = createParameterizedType(Map::class.java, arrayOf(String::class.java, listOfMapsType))

        assertTrue(typeUtil.areTypesMatching(outerMapType1, outerMapType2))

        // Change the deepest type argument
        val differentInnerMapType = createParameterizedType(Map::class.java, arrayOf(java.lang.Integer::class.java, java.lang.Integer::class.java))
        val differentListOfMapsType = createParameterizedType(List::class.java, arrayOf(differentInnerMapType))
        val differentOuterMapType = createParameterizedType(Map::class.java, arrayOf(String::class.java, differentListOfMapsType))

        assertFalse(typeUtil.areTypesMatching(outerMapType1, differentOuterMapType))
    }

    @Test
    fun `areTypesMatching should handle wildcard-like scenarios with Object`() {
        // List<Object> vs List<String>
        val listObjectType = createParameterizedType(List::class.java, arrayOf(Object::class.java))
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        assertFalse(typeUtil.areTypesMatching(listObjectType, listStringType))

        // Map<Object, Object> vs Map<String, Integer>
        val mapObjectObjectType = createParameterizedType(Map::class.java, arrayOf(Object::class.java, Object::class.java))
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        assertFalse(typeUtil.areTypesMatching(mapObjectObjectType, mapStringIntType))
    }

    @Test
    fun `areTypesMatching should handle parameterized types with array type arguments`() {
        // List<Array<String>> vs List<Array<String>> - should match
        val listArrayStringType1 = createParameterizedType(List::class.java, arrayOf(Array<String>::class.java))
        val listArrayStringType2 = createParameterizedType(List::class.java, arrayOf(Array<String>::class.java))
        assertTrue(typeUtil.areTypesMatching(listArrayStringType1, listArrayStringType2))

        // List<Array<String>> vs List<Array<Integer>> - should not match
        val listArrayIntType = createParameterizedType(List::class.java, arrayOf(Array<Int>::class.java))
        assertFalse(typeUtil.areTypesMatching(listArrayStringType1, listArrayIntType))

        // Map<String, Array<Integer>> vs Map<String, Array<Integer>> - should match
        val mapStringArrayIntType1 = createParameterizedType(Map::class.java, arrayOf(String::class.java, Array<Int>::class.java))
        val mapStringArrayIntType2 = createParameterizedType(Map::class.java, arrayOf(String::class.java, Array<Int>::class.java))
        assertTrue(typeUtil.areTypesMatching(mapStringArrayIntType1, mapStringArrayIntType2))

        // Map<String, Array<Integer>> vs Map<String, Array<String>> - should not match
        val mapStringArrayStringType = createParameterizedType(Map::class.java, arrayOf(String::class.java, Array<String>::class.java))
        assertFalse(typeUtil.areTypesMatching(mapStringArrayIntType1, mapStringArrayStringType))
    }

    @Test
    fun `areTypesMatching should handle array types with parameterized type arguments`() {
        // Array<Map<String, Integer>> vs Array<Map<String, Integer>> - should match
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        val arrayMapType1 = createParameterizedType(Array::class.java, arrayOf(mapStringIntType))
        val arrayMapType2 = createParameterizedType(Array::class.java, arrayOf(mapStringIntType))
        assertTrue(typeUtil.areTypesMatching(arrayMapType1, arrayMapType2))

        // Array<Map<String, Integer>> vs Array<List<String>> - should not match
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val arrayListType = createParameterizedType(Array::class.java, arrayOf(listStringType))
        assertFalse(typeUtil.areTypesMatching(arrayMapType1, arrayListType))

        // Array<List<String>> vs Array<ArrayList<String>> - should match due to container compatibility
        val arrayListStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val arrayArrayListStringType = createParameterizedType(Array::class.java, arrayOf(createParameterizedType(ArrayList::class.java, arrayOf(String::class.java))))
        val arrayListStringType2 = createParameterizedType(Array::class.java, arrayOf(arrayListStringType))
        assertTrue(typeUtil.areTypesMatching(arrayListStringType2, arrayArrayListStringType))
    }

    @Test
    fun `areTypesMatching should handle mixed parameterized and array combinations`() {
        // List<Array<Map<String, Integer>>> - complex nesting
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        val arrayMapType = createParameterizedType(Array::class.java, arrayOf(mapStringIntType))
        val listArrayMapType1 = createParameterizedType(List::class.java, arrayOf(arrayMapType))
        val listArrayMapType2 = createParameterizedType(List::class.java, arrayOf(arrayMapType))
        assertTrue(typeUtil.areTypesMatching(listArrayMapType1, listArrayMapType2))

        // List<Array<Map<String, Integer>>> vs List<Array<Map<String, String>>> - should not match
        val mapStringStringType = createParameterizedType(Map::class.java, arrayOf(String::class.java, String::class.java))
        val arrayMapStringType = createParameterizedType(Array::class.java, arrayOf(mapStringStringType))
        val listArrayMapStringType = createParameterizedType(List::class.java, arrayOf(arrayMapStringType))
        assertFalse(typeUtil.areTypesMatching(listArrayMapType1, listArrayMapStringType))

        // Array<List<Map<String, Integer>>> vs Array<List<Map<String, Integer>>> - should match
        val listMapType = createParameterizedType(List::class.java, arrayOf(mapStringIntType))
        val arrayListMapType1 = createParameterizedType(Array::class.java, arrayOf(listMapType))
        val arrayListMapType2 = createParameterizedType(Array::class.java, arrayOf(listMapType))
        assertTrue(typeUtil.areTypesMatching(arrayListMapType1, arrayListMapType2))
    }

    @Test
    fun `areTypesMatching should handle parameterized types with raw type arguments vs parameterized type arguments`() {
        // List<Map> (raw Map) vs List<Map<String, Integer>> (parameterized Map) - should not match
        val listRawMapType = createParameterizedType(List::class.java, arrayOf(Map::class.java))
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, java.lang.Integer::class.java))
        val listParameterizedMapType = createParameterizedType(List::class.java, arrayOf(mapStringIntType))
        assertFalse(typeUtil.areTypesMatching(listRawMapType, listParameterizedMapType))

        // Array<List> vs Array<List<String>> - should not match
        val arrayRawListType = createParameterizedType(Array::class.java, arrayOf(List::class.java))
        val listStringType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        val arrayParameterizedListType = createParameterizedType(Array::class.java, arrayOf(listStringType))
        assertFalse(typeUtil.areTypesMatching(arrayRawListType, arrayParameterizedListType))
    }

    @Test
    fun `areTypesMatching should return false for unsupported type combinations`() {
        val mockType = object : Type {}
        assertFalse(typeUtil.areTypesMatching(mockType, String::class.java))
        assertFalse(typeUtil.areTypesMatching(String::class.java, mockType))
    }

    @ParameterizedTest
    @MethodSource("primitiveToBoxedTypePairs")
    fun `getBoxedType should return correct boxed type for primitives`(primitive: Class<*>, boxed: Class<*>) {
        assertEquals(boxed, typeUtil.getBoxedType(primitive))
    }

    @ParameterizedTest
    @MethodSource("boxedToPrimitiveTypePairs")
    fun `getBoxedType should return correct primitive type for boxed types`(boxed: Class<*>, primitive: Class<*>) {
        assertEquals(primitive, typeUtil.getBoxedType(boxed))
    }

    @Test
    fun `getBoxedType should return null for non-primitive types`() {
        assertNull(typeUtil.getBoxedType(String::class.java))
        assertNull(typeUtil.getBoxedType(List::class.java))
        assertNull(typeUtil.getBoxedType(Map::class.java))
    }

    @Test
    fun `getParameterizedTypeArgs should return type arguments for parameterized type`() {
        val mapStringIntType = createParameterizedType(Map::class.java, arrayOf(String::class.java, Int::class.java))
        val args = typeUtil.getParameterizedTypeArgs(mapStringIntType)

        assertEquals(2, args.size)
        assertEquals(String::class.java, args[0])
        assertEquals(Int::class.java, args[1])
    }

    @Test
    fun `getParameterizedTypeArgs should return empty list for type without arguments`() {
        val rawListType = createParameterizedType(List::class.java, arrayOf())
        val args = typeUtil.getParameterizedTypeArgs(rawListType)

        assertTrue(args.isEmpty())
    }

    @Test
    fun `getContainerType should return MAP for Map types`() {
        assertEquals(ContainerType.MAP, typeUtil.getContainerType(HashMap::class.java))
        assertEquals(ContainerType.MAP, typeUtil.getContainerType(LinkedHashMap::class.java))
        assertEquals(ContainerType.MAP, typeUtil.getContainerType(Map::class.java))
        val parameterizedMapType = createParameterizedType(Map::class.java, arrayOf(String::class.java, Int::class.java))
        assertEquals(ContainerType.MAP, typeUtil.getContainerType(parameterizedMapType))
    }

    @Test
    fun `getContainerType should return LIST for List types`() {
        assertEquals(ContainerType.LIST, typeUtil.getContainerType(ArrayList::class.java))
        assertEquals(ContainerType.LIST, typeUtil.getContainerType(LinkedList::class.java))
        assertEquals(ContainerType.LIST, typeUtil.getContainerType(List::class.java))
        val parameterizedListType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        assertEquals(ContainerType.LIST, typeUtil.getContainerType(parameterizedListType))
    }

    @Test
    fun `getContainerType should return ARRAY for Array types`() {
        assertEquals(ContainerType.ARRAY, typeUtil.getContainerType(Array<String>::class.java))
        assertEquals(ContainerType.ARRAY, typeUtil.getContainerType(Array<Int>::class.java))
        assertEquals(ContainerType.ARRAY, typeUtil.getContainerType(IntArray::class.java))
        assertEquals(ContainerType.ARRAY, typeUtil.getContainerType(StringArray::class.java))
    }

    @Test
    fun `getContainerType should return null for non-container types`() {
        assertNull(typeUtil.getContainerType(String::class.java))
        assertNull(typeUtil.getContainerType(Int::class.java))
        assertNull(typeUtil.getContainerType(Object::class.java))
    }

    // Test isMap methods
    @Test
    fun `isMap should return true for Map class types`() {
        assertTrue(typeUtil.isMap(Map::class.java))
        assertTrue(typeUtil.isMap(HashMap::class.java))
        assertTrue(typeUtil.isMap(LinkedHashMap::class.java))
        assertTrue(typeUtil.isMap(TreeMap::class.java))
    }

    @Test
    fun `isMap should return true for parameterized Map types`() {
        val parameterizedMapType = createParameterizedType(Map::class.java, arrayOf(String::class.java, Int::class.java))
        assertTrue(typeUtil.isMap(parameterizedMapType))
        val parameterizedHashMapType = createParameterizedType(HashMap::class.java, arrayOf(String::class.java, Int::class.java))
        assertTrue(typeUtil.isMap(parameterizedHashMapType))
    }

    @Test
    fun `isMap should return false for non-Map types`() {
        assertFalse(typeUtil.isMap(String::class.java))
        assertFalse(typeUtil.isMap(List::class.java))
        assertFalse(typeUtil.isMap(Array<String>::class.java))
        val mockType = object : Type {}
        assertFalse(typeUtil.isMap(mockType))
    }

    @Test
    fun `isList should return true for List class types`() {
        assertTrue(typeUtil.isList(List::class.java))
        assertTrue(typeUtil.isList(ArrayList::class.java))
        assertTrue(typeUtil.isList(LinkedList::class.java))
        assertTrue(typeUtil.isList(Vector::class.java))
    }

    @Test
    fun `isList should return true for parameterized List types`() {
        val parameterizedListType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        assertTrue(typeUtil.isList(parameterizedListType))
        val parameterizedArrayListType = createParameterizedType(ArrayList::class.java, arrayOf(String::class.java))
        assertTrue(typeUtil.isList(parameterizedArrayListType))
    }

    @Test
    fun `isList should return false for non-List types`() {
        assertFalse(typeUtil.isList(String::class.java))
        assertFalse(typeUtil.isList(Map::class.java))
        assertFalse(typeUtil.isList(Array<String>::class.java))
        val mockType = object : Type {}
        assertFalse(typeUtil.isList(mockType))
    }

    @Test
    fun `isArray should return true for Array types`() {
        assertTrue(typeUtil.isArray(Array<String>::class.java))
        assertTrue(typeUtil.isArray(Array<Int>::class.java))
        assertTrue(typeUtil.isArray(IntArray::class.java))
        assertTrue(typeUtil.isArray(StringArray::class.java))
        assertTrue(typeUtil.isArray(ByteArray::class.java))
    }

    @Test
    fun `isArray should return false for non-Array types`() {
        assertFalse(typeUtil.isArray(String::class.java))
        assertFalse(typeUtil.isArray(List::class.java))
        assertFalse(typeUtil.isArray(Map::class.java))
        val parameterizedListType = createParameterizedType(List::class.java, arrayOf(String::class.java))
        assertFalse(typeUtil.isArray(parameterizedListType))
        val mockType = object : Type {}
        assertFalse(typeUtil.isArray(mockType))
    }

    private fun createParameterizedType(rawType: Class<*>, typeArgs: Array<Type>): ParameterizedType {
        return object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = typeArgs
            override fun getRawType(): Type = rawType
            override fun getOwnerType(): Type? = null
        }
    }

    private fun createParameterizedTypeWithNullArgs(rawType: Class<*>): ParameterizedType {
        return object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type>? = null
            override fun getRawType(): Type = rawType
            override fun getOwnerType(): Type? = null
        }
    }

    companion object {
        @JvmStatic
        fun primitiveToBoxedTypePairs(): Stream<Arguments> = Stream.of(
            Arguments.of(Int::class.java, java.lang.Integer::class.java),
            Arguments.of(Long::class.java, java.lang.Long::class.java),
            Arguments.of(Boolean::class.java, java.lang.Boolean::class.java),
            Arguments.of(Double::class.java, java.lang.Double::class.java),
            Arguments.of(Float::class.java, java.lang.Float::class.java),
            Arguments.of(Short::class.java, java.lang.Short::class.java),
            Arguments.of(Byte::class.java, java.lang.Byte::class.java),
            Arguments.of(Char::class.java, java.lang.Character::class.java),
        )

        @JvmStatic
        fun boxedToPrimitiveTypePairs(): Stream<Arguments> = Stream.of(
            Arguments.of(java.lang.Integer::class.java, Int::class.java),
            Arguments.of(java.lang.Long::class.java, Long::class.java),
            Arguments.of(java.lang.Boolean::class.java, Boolean::class.java),
            Arguments.of(java.lang.Double::class.java, Double::class.java),
            Arguments.of(java.lang.Float::class.java, Float::class.java),
            Arguments.of(java.lang.Short::class.java, Short::class.java),
            Arguments.of(java.lang.Byte::class.java, Byte::class.java),
            Arguments.of(java.lang.Character::class.java, Char::class.java),
        )
    }
}

typealias StringArray = Array<String>
