package io.github.akashkansara.modak.core.correction

import arrow.core.Either
import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.api.correction.RegexReplace
import io.github.akashkansara.modak.api.correction.Trim
import io.github.akashkansara.modak.api.correction.Truncate
import io.github.akashkansara.modak.core.beanmetadata.CorrectionMeta
import io.github.akashkansara.modak.core.correction.correctionapplier.BooleanDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.ByteDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.CharDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.DoubleDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.EnumDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.FloatDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.IntDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.LongDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.RegexReplaceCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.ShortDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.StringDefaultValueCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.TrimCorrectionApplier
import io.github.akashkansara.modak.core.correction.correctionapplier.TruncateCorrectionApplier
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.util.TypeUtil
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.locks.ReentrantReadWriteLock

class CorrectionApplierProvider(
    private val typeUtil: TypeUtil,
) {
    private val correctionAppliers: MutableMap<Pair<Class<*>, Type>, Class<*>> = mutableMapOf()
    private val lock = ReentrantReadWriteLock()

    fun provideCorrectionApplier(
        correctionMeta: CorrectionMeta,
        dataType: Type,
    ): Either<InternalError.CorrectionError, Class<*>?> {
        return Either.catch {
            val correctionApplier = checkCorrectionApplierInCache(correctionMeta, dataType)
            if (correctionApplier != null) {
                return Either.Right(correctionApplier)
            }
            val correctionApplierClazz = correctionMeta.correction.correctedBy.firstOrNull {
                val typeParam = it.java.genericInterfaces
                    .mapNotNull { t -> t as? ParameterizedType }
                    .flatMap { t -> t.actualTypeArguments.toList() }[1]
                typeUtil.areTypesMatching(typeParam, dataType)
            }
            if (correctionApplierClazz != null) {
                lock.writeLock().lock()
                correctionAppliers[
                    Pair(correctionMeta.annotation.annotationClass.java, dataType),
                ] = correctionApplierClazz.java
                lock.writeLock().unlock()
                return Either.Right(correctionApplierClazz.java)
            }
            null
        }.mapLeft {
            InternalError.CorrectionError(
                it,
                "Failed to find correction applier for " +
                    "${correctionMeta.annotation.annotationClass.java.simpleName} on type $dataType",
            )
        }
    }

    private fun checkCorrectionApplierInCache(correctionMeta: CorrectionMeta, dataType: Type): Class<*>? {
        (dataType as? Class<*>)?.let {
            if (it.isEnum) {
                return EnumDefaultValueCorrectionApplier::class.java
            }
        }
        val searchKey = Pair(correctionMeta.annotation.annotationClass.java, dataType)
        val boxedSearchKey = (dataType as? Class<*>)
            ?.let { typeUtil.getBoxedType(it) }
            ?.let { Pair(correctionMeta.annotation.annotationClass.java, it) }
        return correctionAppliers[searchKey] ?: boxedSearchKey?.let { correctionAppliers[boxedSearchKey] }
    }

    private fun seedCorrectionApplier() {
        val seededCorrectionAppliers = mapOf(
            Pair(DefaultValue::class.java, Boolean::class.java) to BooleanDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Byte::class.java) to ByteDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Char::class.java) to CharDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Double::class.java) to DoubleDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Float::class.java) to FloatDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Int::class.java) to IntDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Long::class.java) to LongDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, Short::class.java) to ShortDefaultValueCorrectionApplier::class.java,
            Pair(DefaultValue::class.java, String::class.java) to StringDefaultValueCorrectionApplier::class.java,
            Pair(RegexReplace::class.java, String::class.java) to RegexReplaceCorrectionApplier::class.java,
            Pair(Trim::class.java, String::class.java) to TrimCorrectionApplier::class.java,
            Pair(Truncate::class.java, String::class.java) to TruncateCorrectionApplier::class.java,
        )
        correctionAppliers.putAll(seededCorrectionAppliers)
    }

    init {
        seedCorrectionApplier()
    }
}
