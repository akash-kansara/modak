package io.github.akashkansara.modak.core.beanmetadata

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type

data class BeanProperty(
    val name: String,
    val genericType: Type,
    val readMethod: Method? = null,
    val writeMethod: Method? = null,
    val field: Field? = null,
)
