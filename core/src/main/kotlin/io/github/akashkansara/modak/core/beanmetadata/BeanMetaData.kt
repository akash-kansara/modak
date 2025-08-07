package io.github.akashkansara.modak.core.beanmetadata

data class BeanMetaData<T>(
    val configurationSource: ConfigurationSource = ConfigurationSource.ANNOTATION,
    val type: Class<T>,
    val correctionMetas: List<CorrectionMeta>,
    val properties: List<PropertyMetaData>,
) {
    class Builder(clazz: Class<*>) {
        private val type = clazz
        private val correctionMetas = mutableListOf<CorrectionMeta>()
        private val properties = mutableListOf<PropertyMetaData>()
        private val seenProperties = mutableSetOf<String>()

        fun addBeanCorrectionMeta(annotations: List<CorrectionMeta>) {
            correctionMetas.addAll(annotations)
        }

        fun addPropertyMetaData(props: List<PropertyMetaData>) {
            val propertiesToAdd = props.filter { !seenProperties.contains(it.name) }
            seenProperties.addAll(propertiesToAdd.map { it.name })
            properties.addAll(propertiesToAdd)
        }

        fun toReflectedBean() = BeanMetaData(
            type = type,
            correctionMetas = correctionMetas,
            properties = properties,
        )
    }
}
