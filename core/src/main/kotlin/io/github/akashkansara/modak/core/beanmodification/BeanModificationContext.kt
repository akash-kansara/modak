package io.github.akashkansara.modak.core.beanmodification

import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.Path
import io.github.akashkansara.modak.core.beanmetadata.CorrectionMeta
import io.github.akashkansara.modak.core.group.GroupSequenceIterator
import io.github.akashkansara.modak.core.models.AppliedCorrectionImpl
import io.github.akashkansara.modak.core.models.CorrectionDescriptorImpl
import jakarta.validation.ConstraintViolation

class BeanModificationContext<T>(
    val root: T,
    private val groupSequenceIterator: GroupSequenceIterator,
    constraintViolations: Set<ConstraintViolation<T>>?,
) {
    private var currentGroup: Class<*>? = null
    private val appliedCorrections = mutableListOf<AppliedCorrection<T>>()
    val constraintViolationsMap = constraintViolations?.groupBy { it.propertyPath.toString() }?.toMutableMap()
    val correctViolationsOnly = constraintViolations != null

    fun nextGroup() = if (groupSequenceIterator.hasNext()) {
        currentGroup = groupSequenceIterator.next()
        currentGroup
    } else {
        null
    }

    fun currentGroup(): Class<*>? = currentGroup

    fun hasNextGroup() = groupSequenceIterator.hasNext()

    fun addCorrection(
        propertyPath: Path,
        oldValue: Any?,
        newValue: Any?,
        correctionMeta: CorrectionMeta,
    ) {
        appliedCorrections.add(
            AppliedCorrectionImpl(
                root = root,
                propertyPath = propertyPath,
                oldValue = oldValue,
                newValue = newValue,
                correctionDescriptor = CorrectionDescriptorImpl(
                    annotation = correctionMeta.annotation,
                    groups = correctionMeta.groups,
                    payload = correctionMeta.payload,
                    constraintFilter = correctionMeta.constraintFilter,
                ),
            ),
        )
    }

    fun getCorrections(): List<AppliedCorrection<T>> = appliedCorrections.toList()

    fun clear() {
        constraintViolationsMap?.clear()
    }
}
