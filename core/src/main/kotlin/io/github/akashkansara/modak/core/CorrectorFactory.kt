package io.github.akashkansara.modak.core

import io.github.akashkansara.modak.api.Corrector
import io.github.akashkansara.modak.core.beanmetadata.AnnotationBeanInspector
import io.github.akashkansara.modak.core.beanmetadata.BeanInspection
import io.github.akashkansara.modak.core.beanmodification.BeanModifier
import io.github.akashkansara.modak.core.beanmodification.ValueCorrector
import io.github.akashkansara.modak.core.beantraversal.BeanTraverser
import io.github.akashkansara.modak.core.correction.CorrectionApplierProvider
import io.github.akashkansara.modak.core.group.GroupSequenceGenerator
import io.github.akashkansara.modak.core.util.BeanUtil
import io.github.akashkansara.modak.core.util.TypeUtil

/**
 * Factory for creating Corrector instances with default configuration.
 */
class CorrectorFactory {
    companion object {
        /**
         * Creates a new Corrector instance with default configuration.
         *
         * @return A configured Corrector instance ready for use
         */
        @JvmStatic
        fun buildCorrector(): Corrector {
            val groupSequenceGenerator = GroupSequenceGenerator()
            val typeUtil = TypeUtil()
            val annotationBeanInspector = AnnotationBeanInspector()
            val beanInspection = BeanInspection(annotationBeanInspector)
            val correctionApplierProvider = CorrectionApplierProvider(typeUtil)
            val beanUtil = BeanUtil()
            val valueCorrector = ValueCorrector(correctionApplierProvider)
            val beanTraverser = BeanTraverser(beanInspection, typeUtil, beanUtil)
            val beanModifier = BeanModifier(
                groupSequenceGenerator,
                beanInspection,
                typeUtil,
                beanTraverser,
                valueCorrector,
                beanUtil,
            )
            return CorrectorImpl(beanModifier)
        }
    }
}
