package io.github.akashkansara.modak.core.beanmodification

import arrow.core.Either
import arrow.core.raise.either
import com.google.common.annotations.VisibleForTesting
import io.github.akashkansara.modak.api.CorrectionApplier
import io.github.akashkansara.modak.api.CorrectionApplierContext
import io.github.akashkansara.modak.api.CorrectionApplierResult
import io.github.akashkansara.modak.core.beanmetadata.CorrectionMeta
import io.github.akashkansara.modak.core.correction.CorrectionApplierProvider
import io.github.akashkansara.modak.core.models.InternalError
import java.lang.reflect.Type

class ValueCorrector(
    private val correctionApplierProvider: CorrectionApplierProvider,
) {
    fun applyCorrectionForValue(
        value: Any?,
        valueType: Type,
        correctionMeta: CorrectionMeta,
        rootBean: Any,
    ): Either<InternalError.CorrectionError, CorrectionApplierResult<*>> {
        return either {
            val correctionApplierContext = CorrectionApplierContext(
                rootBean,
                value,
            )
            val correctionApplierClazz = correctionApplierProvider.provideCorrectionApplier(correctionMeta, valueType)
                .bind()
                ?: return@either CorrectionApplierResult.NoChange()
            val correctionApplier = createCorrectionApplierInstance(correctionApplierClazz).bind()
                ?: return@either CorrectionApplierResult.NoChange()
            Either.catch {
                correctionApplier.initialize(correctionMeta.annotation)
                correctionApplier.correct(value, correctionApplierContext)
            }.mapLeft { InternalError.CorrectionError(it, "Failed to apply correction '${correctionMeta.annotation.annotationClass.simpleName}'") }.bind()
        }
    }

    @VisibleForTesting
    fun createCorrectionApplierInstance(
        correctionApplierClazz: Class<*>,
    ): Either<InternalError.CorrectionError, CorrectionApplier<Annotation, Any>?> {
        return Either.catch {
            @Suppress("UNCHECKED_CAST")
            correctionApplierClazz.getDeclaredConstructor().newInstance() as? CorrectionApplier<Annotation, Any>
        }.mapLeft { InternalError.CorrectionError(it, "Failed to instantiate correction applier '${correctionApplierClazz.simpleName}'") }
    }
}
