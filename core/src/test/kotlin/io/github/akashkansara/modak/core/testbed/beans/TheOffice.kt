package io.github.akashkansara.modak.core.testbed.beans

import io.github.akashkansara.modak.api.CorrectNested
import io.github.akashkansara.modak.api.CorrectionTarget
import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.api.correction.RegexReplace
import io.github.akashkansara.modak.api.correction.Trim
import io.github.akashkansara.modak.api.correction.Truncate
import io.github.akashkansara.modak.core.testbed.BranchGroup
import io.github.akashkansara.modak.core.testbed.SloughGroup
import io.github.akashkansara.modak.core.testbed.SwindonGroup
import io.github.akashkansara.modak.core.testbed.corrections.CustomGenericValue1Correction
import io.github.akashkansara.modak.core.testbed.corrections.CustomGenericValue2Correction
import io.github.akashkansara.modak.core.testbed.corrections.MoneyCorrection
import io.github.akashkansara.modak.core.testbed.validation.ValidMoney
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

enum class JobTitle(val isRealTitle: Boolean = true) {
    BRANCH_MANAGER(),
    ASSISTANT_TO_THE_BRANCH_MANAGER(false),
    SALES_REPRESENTATIVE(),
    ACCOUNTANT(),
    TEMP(),
    RECEPTIONIST(),
}

enum class Department {
    SALES,
    ACCOUNTING,
    HR,
}

data class PhoneNumber(
    @field:Trim
    @field:DefaultValue(strValue = "+44123456789")
    val number: String? = null,
) {
    class Builder {
        var number: String? = null
        fun build(): PhoneNumber {
            return PhoneNumber(number = number)
        }
    }
}

interface Asset {
    @DefaultValue(enumValueName = "SALES", enumValueClass = Department::class)
    val assignedTo: Department?
}

data class Printer(
    @field:Trim
    @field:Truncate(length = 50)
    val brand: String,
    override val assignedTo: Department,
) : Asset {
    class Builder {
        var brand: String = ""
        var assignedTo: Department = Department.SALES
        fun build(): Printer {
            return Printer(
                brand = brand,
                assignedTo = assignedTo,
            )
        }
    }
}

data class Chair(
    @field:Trim
    val brand: String,
    @field:DefaultValue(enumValueName = "SALES", enumValueClass = Department::class)
    override val assignedTo: Department? = null,
) : Asset {
    class Builder {
        var brand: String = ""
        var assignedTo: Department? = null
        fun build(): Chair {
            return Chair(
                brand = brand,
                assignedTo = assignedTo,
            )
        }
    }
}

@ValidMoney(groups = [SloughGroup::class])
@MoneyCorrection(constraintFilter = [ValidMoney::class], groups = [SloughGroup::class])
data class Money(
    @field:NotNull(groups = [SwindonGroup::class])
    @field:DefaultValue(constraintFilter = [NotNull::class], doubleValue = 1.0, groups = [SwindonGroup::class])
    val amount: Double? = null,
    val currencyCode: String?,
) {
    class Builder {
        var amount: Double? = null
        var currencyCode: String? = null
        fun build(): Money {
            return Money(
                amount = amount,
                currencyCode = currencyCode,
            )
        }
    }
}

data class Person(
    val name: String,
    @field:DefaultValue(strValue = "No catch phrase", groups = [SwindonGroup::class])
    @field:RegexReplace(
        regexPattern = "Catch Phrase: ",
        replaceStr = "",
        groups = [SloughGroup::class],
    )
    val catchPhrase: String?,
    val jobTitle: JobTitle,
    @field:Valid
    @field:CorrectNested
    val salary: Money? = null,
    @field:Valid
    @field:Trim(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT, groups = [BranchGroup::class])
    val emergencyContact: Map<String, String> = mutableMapOf(),
) {
    class Builder {
        var name: String = ""
        var catchPhrase: String? = null
        var jobTitle: JobTitle = JobTitle.TEMP
        var salary: Money? = Money(null, null)
        var emergencyContact: Map<String, String> = mutableMapOf()
        fun build(): Person {
            return Person(
                name = name,
                catchPhrase = catchPhrase,
                jobTitle = jobTitle,
                salary = salary,
                emergencyContact = emergencyContact,
            )
        }
    }
}

data class Branch(
    @field:Trim
    val name: String,
    @field:Valid
    @field:CorrectNested
    val manager: Person,
    @field:Valid
    @field:CorrectNested
    val employees: List<@Valid Person>,
    @field:Valid
    @field:CorrectNested
    val assets: List<Asset> = emptyList(),
    @field:Trim
    @field:Truncate(length = 30)
    val address: String? = null,
    @field:NotNull
    @field:DefaultValue(intValue = 2001, constraintFilter = [NotNull::class])
    val establishedYear: Int? = null,
) {
    class Builder {
        var name: String = ""
        var manager: Person? = null
        var employees: List<Person> = emptyList()
        var assets: List<Asset> = emptyList()
        var address: String? = null
        var establishedYear: Int? = null
        fun build(): Branch {
            return Branch(
                name = name,
                manager = manager!!,
                employees = employees,
                assets = assets,
                address = address,
                establishedYear = establishedYear,
            )
        }
    }
}

data class Meeting(
    @field:Truncate(length = 5)
    val title: String,
    @field:Trim
    val agenda: String,
    @field:Valid
    @field:CorrectNested
    val participants: Array<@Valid Person>,
    val date: Instant?,
    @field:DefaultValue(intValue = 5)
    val noOfAwkwardMoments: Int? = null,
) {
    class Builder {
        var title: String = ""
        var agenda: String = ""
        var participants: Array<Person> = emptyArray()
        var date: Instant? = null
        var noOfAwkwardMoments: Int? = null
        fun build(): Meeting {
            return Meeting(
                title = title,
                agenda = agenda,
                participants = participants,
                date = date,
                noOfAwkwardMoments = noOfAwkwardMoments,
            )
        }
    }
}

data class OfficeDay(
    @field:Valid
    @field:CorrectNested
    val branch: Branch,
    val date: LocalDate,
    @field:CorrectNested
    val meetings: List<@Valid Meeting>,
    @field:CorrectNested
    val pranks: List<@Valid Prank>? = null,
    @field:CustomGenericValue1Correction
    val tag: CustomGenericValue1<*>? = null,
    @field:CustomGenericValue2Correction
    val amount: CustomGenericValue2<*>? = null,
) {
    class Builder {
        var branch: Branch? = null
        var date: LocalDate? = null
        var meetings: List<Meeting> = emptyList()
        var pranks: List<Prank>? = null
        var tag: CustomGenericValue1<*>? = null
        var amount: CustomGenericValue2<*>? = null
        fun build(): OfficeDay {
            return OfficeDay(
                branch = branch!!,
                date = date!!,
                meetings = meetings,
                pranks = pranks,
                tag = tag,
                amount = amount,
            )
        }
    }
}

/**
 * No annotations
 */
data class Prank(
    val name: String,
    val prankster: Person,
    val target: Person,
) {
    class Builder {
        var name: String = ""
        var prankster: Person? = null
        var target: Person? = null
        fun build(): Prank {
            return Prank(
                name = name,
                prankster = prankster!!,
                target = target!!,
            )
        }
    }
}

data class Company(
    @field:Trim
    val name: String,
    @field:Valid
    @field:CorrectNested
    val branches: Map<String, Branch>,
    @field:Valid
    @field:CorrectNested
    val headquarters: Branch,
    val establishedYear: Int,
    @field:Valid
    @field:CorrectNested
    val phoneNumbers: Array<PhoneNumber> = emptyArray(),
) {
    class Builder {
        var name: String = ""
        var branches = mapOf<String, Branch>()
        var headquarters: Branch? = null
        var establishedYear: Int = 0
        var phoneNumbers: Array<PhoneNumber> = emptyArray()
        fun build(): Company {
            return Company(
                name = name,
                branches = branches,
                headquarters = headquarters!!,
                establishedYear = establishedYear,
                phoneNumbers = phoneNumbers,
            )
        }
    }
}

fun company(block: Company.Builder.() -> Unit): Company {
    val builder = Company.Builder()
    builder.block()
    return builder.build()
}

fun money(block: Money.Builder.() -> Unit): Money {
    val builder = Money.Builder()
    builder.block()
    return builder.build()
}

fun person(block: Person.Builder.() -> Unit): Person {
    val builder = Person.Builder()
    builder.block()
    return builder.build()
}

fun branch(block: Branch.Builder.() -> Unit): Branch {
    val builder = Branch.Builder()
    builder.block()
    return builder.build()
}

fun meeting(block: Meeting.Builder.() -> Unit): Meeting {
    val builder = Meeting.Builder()
    builder.block()
    return builder.build()
}

fun officeDay(block: OfficeDay.Builder.() -> Unit): OfficeDay {
    val builder = OfficeDay.Builder()
    builder.block()
    return builder.build()
}

fun prank(block: Prank.Builder.() -> Unit): Prank {
    val builder = Prank.Builder()
    builder.block()
    return builder.build()
}

fun printer(block: Printer.Builder.() -> Unit): Printer {
    val builder = Printer.Builder()
    builder.block()
    return builder.build()
}

fun chair(block: Chair.Builder.() -> Unit): Chair {
    val builder = Chair.Builder()
    builder.block()
    return builder.build()
}

fun phoneNumber(block: PhoneNumber.Builder.() -> Unit): PhoneNumber {
    val builder = PhoneNumber.Builder()
    builder.block()
    return builder.build()
}
