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
import io.mockk.spyk

object TestCorrectorFactory {
    var groupSequenceGenerator = spyk(GroupSequenceGenerator())
    var typeUtil = spyk(TypeUtil())
    var annotationBeanInspector = spyk(AnnotationBeanInspector())
    var beanInspection = spyk(BeanInspection(annotationBeanInspector))
    var correctionApplierProvider = spyk(CorrectionApplierProvider(typeUtil))
    var beanUtil = spyk(BeanUtil())
    var valueCorrector = spyk(ValueCorrector(correctionApplierProvider))
    var beanTraverser = spyk(BeanTraverser(beanInspection, typeUtil, beanUtil))
    var beanModifier = spyk(
        BeanModifier(
            groupSequenceGenerator,
            beanInspection,
            typeUtil,
            beanTraverser,
            valueCorrector,
            beanUtil,
        ),
    )
    var corrector: Corrector = spyk(CorrectorImpl(beanModifier))

    fun reset() {
        groupSequenceGenerator = spyk(GroupSequenceGenerator())
        typeUtil = spyk(TypeUtil())
        annotationBeanInspector = spyk(AnnotationBeanInspector())
        beanInspection = spyk(BeanInspection(annotationBeanInspector))
        correctionApplierProvider = spyk(CorrectionApplierProvider(typeUtil))
        beanUtil = spyk(BeanUtil())
        valueCorrector = spyk(ValueCorrector(correctionApplierProvider))
        beanTraverser = spyk(BeanTraverser(beanInspection, typeUtil, beanUtil))
        beanModifier = spyk(
            BeanModifier(
                groupSequenceGenerator,
                beanInspection,
                typeUtil,
                beanTraverser,
                valueCorrector,
                beanUtil,
            ),
        )
        corrector = spyk(CorrectorImpl(beanModifier))
    }
}
