package io.github.akashkansara.modak.core

import arrow.core.Either
import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.CorrectionResult
import io.github.akashkansara.modak.api.Corrector
import io.github.akashkansara.modak.core.beanmodification.BeanModifier
import io.github.akashkansara.modak.core.models.ErrorImpl
import io.github.akashkansara.modak.core.models.InternalError
import jakarta.validation.ConstraintViolation

class CorrectorImpl(private val beanModifier: BeanModifier) : Corrector {
    override fun <T> correct(
        obj: T,
        vararg groups: Class<*>,
    ): CorrectionResult<T> {
        val groupList = getGroupList(*groups)
        val modifyBeanResult = beanModifier.modifyBean(obj, null, groupList)
        return processModificationResult(modifyBeanResult)
    }

    override fun <T> correct(
        obj: T,
        constraintViolations: Set<ConstraintViolation<T>>,
        vararg groups: Class<*>,
    ): CorrectionResult<T> {
        val groupList = getGroupList(*groups)
        val modifyBeanResult = beanModifier.modifyBean(obj, constraintViolations, groupList)
        return processModificationResult(modifyBeanResult)
    }

    private fun getGroupList(vararg groups: Class<*>): List<Class<*>>? {
        return if (groups.isEmpty()) {
            null
        } else {
            groups.toList()
        }
    }

    private fun <T> processModificationResult(
        result: Either<InternalError, List<AppliedCorrection<T>>>,
    ): CorrectionResult<T> {
        return result.fold(
            ifLeft = {
                val appliedCorrections = when (it) {
                    is InternalError.BeanModificationError -> it.appliedCorrections
                    else -> emptyList()
                }
                CorrectionResult.Failure(
                    ErrorImpl(
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
