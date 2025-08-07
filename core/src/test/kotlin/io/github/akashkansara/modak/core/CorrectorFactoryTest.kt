package io.github.akashkansara.modak.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class CorrectorFactoryTest {
    @Test
    fun `corrector factory builds corrector object successfully`() {
        assertDoesNotThrow { CorrectorFactory().buildCorrector() }
    }
}
