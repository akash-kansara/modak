package io.github.akashkansara.modak.core.beanmetadata

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type

data class PropertyMetaData(
    val configurationSource: ConfigurationSource = ConfigurationSource.ANNOTATION,
    val name: String,
    val genericType: Type,
    val correctionMetas: List<CorrectionMeta>,
    val correctionModifiers: Set<Annotation>,
    val readMethod: Method?,
    val writeMethod: Method?,
    val field: Field?,
)
