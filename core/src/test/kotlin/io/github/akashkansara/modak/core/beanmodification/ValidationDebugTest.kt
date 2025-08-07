package io.github.akashkansara.modak.core.beanmodification

import io.github.akashkansara.modak.core.testbed.buildSloughBranch
import io.github.akashkansara.modak.core.testbed.buildSloughOfficeDay
import jakarta.validation.Validation
import org.junit.jupiter.api.Test

class ValidationDebugTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `debug emergency contact validation`() {
        val sloughBranch = buildSloughBranch()
        val bean = buildSloughOfficeDay(sloughBranch)

        println("=== BEAN STRUCTURE ===")
        bean.branch.employees.forEach { employee ->
            println("Employee: ${employee.name}")
            employee.emergencyContact.forEach { (key, value) ->
                println("  Emergency Contact: $key -> '$value' (length: ${value.length})")
            }
        }

        println("\n=== VALIDATION RESULTS ===")
        val constraintViolations = validator.validate(bean)
        println("Total violations: ${constraintViolations.size}")

        constraintViolations.forEach { violation ->
            println("Violation:")
            println("  Property path: ${violation.propertyPath}")
            println("  Message: ${violation.message}")
            println("  Invalid value: '${violation.invalidValue}'")
            println("  Root bean: ${violation.rootBean::class.simpleName}")
        }
    }
} 
