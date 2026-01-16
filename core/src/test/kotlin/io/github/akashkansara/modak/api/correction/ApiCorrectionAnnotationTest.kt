package io.github.akashkansara.modak.api.correction

import io.github.akashkansara.modak.api.CorrectionResult
import io.github.akashkansara.modak.core.CorrectorFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApiCorrectionAnnotationTest {
    private val corrector = CorrectorFactory.buildCorrector()

    data class User(
        @field:DefaultValue(strValue = "Anonymous")
        val name: String? = null,

        @field:RegexReplace(
            regexPattern = "\\(",
            replaceStr = "",
        )
        @field:RegexReplace(
            regexPattern = "\\)",
            replaceStr = "",
        )
        @field:RegexReplace(
            regexPattern = "-",
            replaceStr = "",
        )
        @field:Trim
        val phoneNumber: String,

        @field:Trim
        val email: String,

        @field:Truncate(length = 10)
        @field:Truncate(length = 5)
        val addressLine1: String,
    )

    @Test
    fun `all corrections work`() {
        val user = User(
            name = null,
            phoneNumber = " (123) - 456-7890 ",
            email = " anonymous@test.com \t",
            addressLine1 = "12345 Long Street Name",
        )
        val result = corrector.correct(user)
        assert(result.isSuccess)
        val success = result as CorrectionResult.Success
        assertEquals(1, success.appliedCorrections.filter { it.propertyPath.toString() == User::name.name }.size)
        assertEquals(4, success.appliedCorrections.filter { it.propertyPath.toString() == User::phoneNumber.name }.size)
        assertEquals(1, success.appliedCorrections.filter { it.propertyPath.toString() == User::email.name }.size)
        assertEquals(2, success.appliedCorrections.filter { it.propertyPath.toString() == User::addressLine1.name }.size)
        val expectedUser = User(
            name = "Anonymous",
            phoneNumber = "123  4567890",
            email = "anonymous@test.com",
            addressLine1 = "12345",
        )
        assertEquals(expectedUser, user)
    }
}
