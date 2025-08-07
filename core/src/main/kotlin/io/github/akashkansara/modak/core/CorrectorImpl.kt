package io.github.akashkansara.modak.core

import io.github.akashkansara.modak.api.CorrectionResult
import io.github.akashkansara.modak.api.Corrector
import io.github.akashkansara.modak.api.ErrorLike
import io.github.akashkansara.modak.core.beanmodification.BeanModifier
import io.github.akashkansara.modak.core.models.ErrorLikeImpl
import io.github.akashkansara.modak.core.models.InternalError
import jakarta.validation.ConstraintViolation

class CorrectorImpl(
    private val beanModifier: BeanModifier,
) : Corrector {
    override fun <T> correct(
        obj: T,
        correctViolationsOnly: Boolean,
        constraintViolations: Set<ConstraintViolation<T>>?,
        vararg groups: Class<*>,
    ): CorrectionResult<T, ErrorLike> {
        val groupList = if (groups.isEmpty()) {
            null
        } else {
            groups.toList()
        }
        val modifyBeanResult = beanModifier.modifyBean(
            obj,
            correctViolationsOnly,
            constraintViolations,
            groupList,
        )
        return modifyBeanResult.fold(
            ifLeft = {
                val appliedCorrections = when (it) {
                    is InternalError.BeanModificationError -> it.appliedCorrections
                    else -> emptyList()
                }
                CorrectionResult.Failure(
                    ErrorLikeImpl(
                        message = it.message,
                        cause = it.cause,
                        appliedCorrections = appliedCorrections,
                    ),
                )
            },
            ifRight = {
                CorrectionResult.Success(it)
            },
        )
    }
}
