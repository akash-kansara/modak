package io.github.akashkansara.modak.core.beanmodification

import arrow.core.raise.either
import io.github.akashkansara.modak.api.AppliedCorrection
import io.github.akashkansara.modak.api.DefaultGroup
import io.github.akashkansara.modak.api.correction.DefaultValue
import io.github.akashkansara.modak.api.correction.RegexReplace
import io.github.akashkansara.modak.api.correction.Trim
import io.github.akashkansara.modak.api.correction.Truncate
import io.github.akashkansara.modak.core.TestCorrectorFactory
import io.github.akashkansara.modak.core.models.InternalError
import io.github.akashkansara.modak.core.testbed.BranchGroup
import io.github.akashkansara.modak.core.testbed.SloughGroup
import io.github.akashkansara.modak.core.testbed.SwindonGroup
import io.github.akashkansara.modak.core.testbed.beans.Department
import io.github.akashkansara.modak.core.testbed.beans.Money
import io.github.akashkansara.modak.core.testbed.buildCompany
import io.github.akashkansara.modak.core.testbed.buildSloughBranch
import io.github.akashkansara.modak.core.testbed.buildSloughMeetings
import io.github.akashkansara.modak.core.testbed.buildSloughOfficeDay
import io.github.akashkansara.modak.core.testbed.buildSwindonBranch
import io.github.akashkansara.modak.core.testbed.buildSwindonMeetings
import io.github.akashkansara.modak.core.testbed.buildSwindonOfficeDay
import io.github.akashkansara.modak.core.testbed.buildValidCompany
import io.github.akashkansara.modak.core.testbed.buildValidMinimalCompany
import io.github.akashkansara.modak.core.testbed.buildValidSloughBranch
import io.github.akashkansara.modak.core.testbed.buildValidSwindonBranch
import io.github.akashkansara.modak.core.testbed.buildValidSwindonOfficeDay
import io.github.akashkansara.modak.core.testbed.corrections.MoneyCorrection
import io.github.akashkansara.modak.core.util.BeanUtil
import io.github.akashkansara.modak.core.util.ContainerType
import io.mockk.every
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BeanModifierTest {
    private val testFactory = TestCorrectorFactory
    private lateinit var beanModifier: BeanModifier
    private lateinit var beanUtil: BeanUtil
    private val validator = Validation.byDefaultProvider().configure().buildValidatorFactory().validator

    @BeforeEach
    fun setUp() {
        testFactory.reset()
        beanModifier = testFactory.beanModifier
        beanUtil = testFactory.beanUtil
    }

    @Test
    fun `should apply all corrections to bean when no filters are supplied`() {
        val bean = buildCompany()
        val correctionResult = beanModifier.modifyBean(bean, null, null)
        assertTrue(correctionResult.isRight())
        val result = correctionResult.getOrNull()!!
        val expectedResult = buildValidCompany(emptyList())
        assertEquals(expectedResult.name, bean.name)
        assertEquals(expectedResult.branches, bean.branches)
        assertEquals(expectedResult.headquarters, bean.headquarters)
        assertEquals(true, expectedResult.phoneNumbers.contentEquals(bean.phoneNumbers))
        assertEquals(expectedResult.establishedYear, bean.establishedYear)
        val expectedCorrections = listOf(
            ExpectedCorrection(
                pathStr = "branches[Slough].address",
                newValue = "225, Street 99, Park 75, Slough, Berkshire, UK",
                oldValue = " 225, Street 99, Park 75, Slough, Berkshire, UK",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Slough].address",
                newValue = "225, Street 99, Park 75, Sloug",
                oldValue = "225, Street 99, Park 75, Slough, Berkshire, UK",
                correctionDescriptorAnnotationClass = Truncate::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Slough].assets[0].brand",
                newValue = "HP",
                oldValue = " HP ",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Slough].assets[1].assignedTo",
                newValue = Department.SALES,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Slough].assets[2].brand",
                newValue = "Herman Miller",
                oldValue = "Herman Miller\t",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Slough].establishedYear",
                newValue = 2001,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Slough].name",
                newValue = "Slough Branch",
                oldValue = "\nSlough Branch\t",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].address",
                newValue = "125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom",
                oldValue = " 125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].address",
                newValue = "125, Street 66, Park 56, Swind",
                oldValue = "125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom",
                correctionDescriptorAnnotationClass = Truncate::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].assets[0].brand",
                newValue = "Canon",
                oldValue = " Canon",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].assets[1].assignedTo",
                newValue = Department.SALES,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].assets[1].brand",
                newValue = "Steelcase",
                oldValue = "Steelcase\n",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].establishedYear",
                newValue = 2001,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branches[Swindon].name",
                newValue = "Swindon Branch",
                oldValue = " Swindon Branch",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "name",
                newValue = "Wernham-Hogg paper company",
                oldValue = "\nWernham-Hogg paper company\t ",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "phoneNumbers[0].number",
                newValue = "+44123456789",
                oldValue = " +44123456789 ",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "phoneNumbers[1].number",
                newValue = "+44123456789",
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
        )
        assertIterableEquals(expectedCorrections, toExpectedCorrections(result))
    }

    @Test
    fun `should apply group specific corrections when groups are specified`() {
        val sloughBranch = buildSloughBranch()
        val swindonBranch = buildSwindonBranch()
        val bean = buildSwindonOfficeDay(sloughBranch, swindonBranch)
        val sloughGroupCorrectedSloughBranch = sloughBranch.copy(
            employees = sloughBranch.employees.map {
                it.copy(
                    catchPhrase = it.catchPhrase?.replace("Catch Phrase: ", ""),
                    salary = it.salary?.let { s ->
                        val newCurrencyCode = if (s.currencyCode == null) {
                            "GBP"
                        } else {
                            null
                        }
                        s.copy(currencyCode = newCurrencyCode)
                    },
                    emergencyContact = it.emergencyContact.mapValues { v -> v.value.trim() }.toMutableMap(),
                )
            },
        )
        val sloughGroupCorrectedSwindonBranch = swindonBranch.copy(
            employees = swindonBranch.employees.map {
                it.copy(
                    catchPhrase = it.catchPhrase?.replace("Catch Phrase: ", ""),
                    salary = it.salary?.let { s ->
                        val newCurrencyCode = if (s.currencyCode == null) {
                            "GBP"
                        } else {
                            null
                        }
                        s.copy(currencyCode = newCurrencyCode)
                    },
                    emergencyContact = it.emergencyContact.mapValues { v -> v.value.trim() }.toMutableMap(),
                )
            },
        )
        val expectedResult = bean.copy(
            branch = sloughGroupCorrectedSwindonBranch,
            meetings = buildSwindonMeetings(
                sloughGroupCorrectedSwindonBranch.employees,
                sloughGroupCorrectedSloughBranch.employees,
            ),
        )
        val correctionResult = beanModifier.modifyBean(
            bean,
            null,
            listOf(SloughGroup::class.java, BranchGroup::class.java),
        )
        assertTrue(correctionResult.isRight())
        val result = correctionResult.getOrNull()!!
        assertEquals(expectedResult.branch, bean.branch)
        assertEquals(expectedResult.date, bean.date)
        expectedResult.meetings.zip(bean.meetings).forEach { (expectedMeeting, actualMeeting) ->
            assertEquals(expectedMeeting.date, actualMeeting.date)
            assertEquals(expectedMeeting.noOfAwkwardMoments, actualMeeting.noOfAwkwardMoments)
            assertEquals(expectedMeeting.agenda, actualMeeting.agenda)
            assertEquals(expectedMeeting.title, actualMeeting.title)
            assertTrue(expectedMeeting.participants.contentEquals(actualMeeting.participants))
        }
        assertEquals(expectedResult.pranks, bean.pranks)
        assertEquals(expectedResult.tag, bean.tag)
        assertEquals(expectedResult.amount, bean.amount)
        val expectedCorrections = listOf(
            ExpectedCorrection(
                pathStr = "branch.employees[0].catchPhrase",
                newValue = "I am the Regional Manager",
                oldValue = "Catch Phrase: I am the Regional Manager",
                correctionDescriptorAnnotationClass = RegexReplace::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[0].salary",
                newValue = Money(amount = 200.0, currencyCode = "GBP"),
                oldValue = Money(amount = 200.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[1].salary",
                newValue = Money(amount = null, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[2].salary",
                newValue = Money(amount = null, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[3].catchPhrase",
                newValue = "Free Love Freeway",
                oldValue = "Catch Phrase: Free Love Freeway",
                correctionDescriptorAnnotationClass = RegexReplace::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[3].salary",
                newValue = Money(amount = 100.0, currencyCode = "GBP"),
                oldValue = Money(amount = 100.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[4].catchPhrase",
                newValue = "I am the Assistant to the Regional Manager",
                oldValue = "Catch Phrase: I am the Assistant to the Regional Manager",
                correctionDescriptorAnnotationClass = RegexReplace::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[4].salary",
                newValue = Money(amount = 110.0, currencyCode = "GBP"),
                oldValue = Money(amount = 110.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[5].salary",
                newValue = Money(amount = 110.0, currencyCode = "GBP"),
                oldValue = Money(amount = 110.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[6].salary",
                newValue = Money(amount = null, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[7].salary",
                newValue = Money(amount = null, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[2].emergencyContact[Friend]",
                newValue = "Tim Canterbury",
                oldValue = "Tim Canterbury\t",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[3].emergencyContact[Friend]",
                newValue = "Finchy",
                oldValue = "   Finchy   ",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[5].emergencyContact[Friend]",
                newValue = "Dawn Tinsley",
                oldValue = "\nDawn Tinsley",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
        )
        assertIterableEquals(expectedCorrections, toExpectedCorrections(result))
    }

    @Test
    fun `should apply validation specific corrections when validations are specified`() {
        val sloughBranch = buildSloughBranch()
        val bean = buildSloughOfficeDay(sloughBranch)
        val correctedSloughBranch = sloughBranch.copy(establishedYear = 2001)
        val expectedResult = bean.copy(
            branch = sloughBranch,
            meetings = buildSloughMeetings(correctedSloughBranch.employees),
        )
        val constraintViolations = validator.validate(bean)
        val correctionResult = beanModifier.modifyBean(
            bean,
            constraintViolations,
            null,
        )
        assertTrue(correctionResult.isRight())
        val result = correctionResult.getOrNull()!!
        assertEquals(expectedResult.branch, bean.branch)
        assertEquals(expectedResult.date, bean.date)
        expectedResult.meetings.zip(bean.meetings).forEach { (expectedMeeting, actualMeeting) ->
            assertEquals(expectedMeeting.date, actualMeeting.date)
            assertEquals(expectedMeeting.noOfAwkwardMoments, actualMeeting.noOfAwkwardMoments)
            assertEquals(expectedMeeting.agenda, actualMeeting.agenda)
            assertEquals(expectedMeeting.title, actualMeeting.title)
            assertTrue(expectedMeeting.participants.contentEquals(actualMeeting.participants))
        }
        assertEquals(expectedResult.pranks, bean.pranks)
        assertEquals(expectedResult.tag, bean.tag)
        assertEquals(expectedResult.amount, bean.amount)
        val expectedCorrections = listOf(
            ExpectedCorrection(
                pathStr = "branch.establishedYear",
                newValue = 2001,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
        )
        assertIterableEquals(expectedCorrections, toExpectedCorrections(result))
    }

    @Test
    fun `should apply group & validation specific corrections when they're are specified`() {
        val fixGroups = listOf(SloughGroup::class, SwindonGroup::class, DefaultGroup::class)
        val sloughBranch = buildSloughBranch()
        val swindonBranch = buildSwindonBranch()
        val bean = buildSwindonOfficeDay(sloughBranch, swindonBranch)
        val expectedResult = buildValidSwindonOfficeDay(
            sloughBranch = buildValidSloughBranch(fixGroups),
            swindonBranch = buildValidSwindonBranch(fixGroups),
            fixGroups = fixGroups,
        )
        val correctionResult = beanModifier.modifyBean(
            bean,
            null,
            fixGroups.map { it.java },
        )
        assertTrue(correctionResult.isRight())
        val result = correctionResult.getOrNull()!!
        assertEquals(expectedResult.branch, bean.branch)
        assertEquals(expectedResult.date, bean.date)
        expectedResult.meetings.zip(bean.meetings).forEach { (expectedMeeting, actualMeeting) ->
            assertEquals(expectedMeeting.date, actualMeeting.date)
            assertEquals(expectedMeeting.noOfAwkwardMoments, actualMeeting.noOfAwkwardMoments)
            assertEquals(expectedMeeting.agenda, actualMeeting.agenda)
            assertEquals(expectedMeeting.title, actualMeeting.title)
            assertTrue(expectedMeeting.participants.contentEquals(actualMeeting.participants))
        }
        assertEquals(expectedResult.pranks, bean.pranks)
        assertEquals(expectedResult.tag, bean.tag)
        assertEquals(expectedResult.amount, bean.amount)
        val expectedCorrections = listOf(
            ExpectedCorrection(
                pathStr = "branch.employees[0].catchPhrase",
                newValue = "I am the Regional Manager",
                oldValue = "Catch Phrase: I am the Regional Manager",
                correctionDescriptorAnnotationClass = RegexReplace::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[0].salary",
                newValue = Money(amount = 200.0, currencyCode = "GBP"),
                oldValue = Money(amount = 200.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[1].salary",
                newValue = Money(amount = 1.0, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[2].salary",
                newValue = Money(amount = 1.0, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[3].catchPhrase",
                newValue = "Free Love Freeway",
                oldValue = "Catch Phrase: Free Love Freeway",
                correctionDescriptorAnnotationClass = RegexReplace::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[3].salary",
                newValue = Money(amount = 100.0, currencyCode = "GBP"),
                oldValue = Money(amount = 100.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[4].catchPhrase",
                newValue = "I am the Assistant to the Regional Manager",
                oldValue = "Catch Phrase: I am the Assistant to the Regional Manager",
                correctionDescriptorAnnotationClass = RegexReplace::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[4].salary",
                newValue = Money(amount = 110.0, currencyCode = "GBP"),
                oldValue = Money(amount = 110.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[5].salary",
                newValue = Money(amount = 110.0, currencyCode = "GBP"),
                oldValue = Money(amount = 110.0, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[6].salary",
                newValue = Money(amount = 1.0, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[7].salary",
                newValue = Money(amount = 1.0, currencyCode = "GBP"),
                oldValue = Money(amount = null, currencyCode = null),
                correctionDescriptorAnnotationClass = MoneyCorrection::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[1].catchPhrase",
                newValue = "No catch phrase",
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[1].salary.amount",
                newValue = 1.0,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[2].catchPhrase",
                newValue = "No catch phrase",
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.employees[2].salary.amount",
                newValue = 1.0,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[5].catchPhrase",
                newValue = "No catch phrase",
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[6].catchPhrase",
                newValue = "No catch phrase",
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[6].salary.amount",
                newValue = 1.0,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[7].catchPhrase",
                newValue = "No catch phrase",
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].participants[7].salary.amount",
                newValue = 1.0,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.address",
                newValue = "125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom",
                oldValue = " 125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.address",
                newValue = "125, Street 66, Park 56, Swind",
                oldValue = "125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom",
                correctionDescriptorAnnotationClass = Truncate::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.assets[0].brand",
                newValue = "Canon",
                oldValue = " Canon",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.assets[1].assignedTo",
                newValue = Department.SALES,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.assets[1].brand",
                newValue = "Steelcase",
                oldValue = "Steelcase\n",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.establishedYear",
                newValue = 2001,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "branch.name",
                newValue = "Swindon Branch",
                oldValue = " Swindon Branch",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].agenda",
                newValue = "Ensure Swindon and Slough integration is painless (it's not)",
                oldValue = " \nEnsure Swindon and Slough integration is painless (it's not)\t ",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].noOfAwkwardMoments",
                newValue = 5,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[0].title",
                newValue = "Swind",
                oldValue = "Swindon Merger Planning Session",
                correctionDescriptorAnnotationClass = Truncate::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[1].agenda",
                newValue = "Gareth attempts to lead, Neil interrupts",
                oldValue = "  Gareth attempts to lead, Neil interrupts  ",
                correctionDescriptorAnnotationClass = Trim::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[1].noOfAwkwardMoments",
                newValue = 5,
                oldValue = null,
                correctionDescriptorAnnotationClass = DefaultValue::class.java,
            ),
            ExpectedCorrection(
                pathStr = "meetings[1].title",
                newValue = "Fire ",
                oldValue = "Fire Safety Briefing Meeting",
                correctionDescriptorAnnotationClass = Truncate::class.java,
            ),
        )
        assertIterableEquals(expectedCorrections, toExpectedCorrections(result))
    }

    @Test
    fun `should apply no corrections when bean requires no corrections`() {
        val bean = buildValidMinimalCompany()
        val correctionResult = beanModifier.modifyBean(bean, null, null)
        assertTrue(correctionResult.isRight())
        val result = correctionResult.getOrNull()!!
        val expectedResult = buildValidMinimalCompany()
        assertEquals(expectedResult.name, bean.name)
        assertEquals(expectedResult.branches, bean.branches)
        assertEquals(expectedResult.headquarters, bean.headquarters)
        assertEquals(true, expectedResult.phoneNumbers.contentEquals(bean.phoneNumbers))
        assertEquals(expectedResult.establishedYear, bean.establishedYear)
        assertIterableEquals(emptyList<ExpectedCorrection>(), toExpectedCorrections(result))
    }

    @Test
    fun `should apply no corrections when bean is null`() {
        val correctionResult = beanModifier.modifyBean(null, null, null)
        assertTrue(correctionResult.isRight())
        val result = correctionResult.getOrNull()!!
        assertIterableEquals(emptyList<ExpectedCorrection>(), toExpectedCorrections(result))
    }

    @Test
    fun `modifyBean returns list of applied corrections when an intermittent failure happens`() {
        val bean = buildCompany()
        var countPropExited = 0
        every { beanModifier.onPropertyExited(any()) } coAnswers {
            if (countPropExited++ == 3) {
                either { raise(InternalError.BeanModificationError(null, "Error")) }
            } else {
                callOriginal()
            }
        }
        val correctionResult = beanModifier.modifyBean(bean, null, null)
        assertTrue(correctionResult.isLeft())
        val result = correctionResult.leftOrNull()!! as InternalError.BeanModificationError
        assertEquals(3, result.appliedCorrections.size)
    }

    @Test
    fun `getContainerValue returns error when an intermittent failure happens`() {
        val list = mutableListOf(1, 2, 3)
        val arr = arrayOf(1, 2, 3)
        val map = mapOf("a" to 1, "b" to 2, "c" to 3)
        every { beanUtil.getMapValue(any(), any()) } throws IllegalAccessException()
        every { beanUtil.getListValue(any(), any()) } throws IllegalAccessException()
        every { beanUtil.getArrayValue(any(), any()) } throws IllegalAccessException()
        var result = beanModifier.getContainerValue(ContainerType.LIST, list, 0)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is InternalError.BeanModificationError)
        result = beanModifier.getContainerValue(ContainerType.ARRAY, arr, 0)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is InternalError.BeanModificationError)
        result = beanModifier.getContainerValue(ContainerType.MAP, map, "a")
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is InternalError.BeanModificationError)
    }

    @Test
    fun `setContainerValue returns error when an intermittent failure happens`() {
        val list = mutableListOf(1, 2, 3)
        val arr = arrayOf(1, 2, 3)
        val map = mutableMapOf("a" to 1, "b" to 2, "c" to 3)
        every { beanUtil.setMapValue(any(), any(), any()) } throws IllegalAccessException()
        every { beanUtil.setListValue(any(), any(), any()) } throws IllegalAccessException()
        every { beanUtil.setArrayValue(any(), any(), any()) } throws IllegalAccessException()
        var result = beanModifier.setContainerValue(ContainerType.LIST, list, 0, 0)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is InternalError.BeanModificationError)
        result = beanModifier.setContainerValue(ContainerType.ARRAY, arr, 0, 0)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is InternalError.BeanModificationError)
        result = beanModifier.setContainerValue(ContainerType.MAP, map, "a", 0)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is InternalError.BeanModificationError)
    }

    @Test
    fun `getContainerValue gets value successfully`() {
        val list = mutableListOf(1, 2, 3)
        val arr = arrayOf(1, 2, 3)
        val map = mapOf("a" to 1, "b" to 2, "c" to 3)
        var result = beanModifier.getContainerValue(ContainerType.LIST, list, 0)
        assertTrue(result.isRight())
        result = beanModifier.getContainerValue(ContainerType.ARRAY, arr, 0)
        assertTrue(result.isRight())
        result = beanModifier.getContainerValue(ContainerType.MAP, map, "a")
        assertTrue(result.isRight())
    }

    @Test
    fun `setContainerValue updates value successfully`() {
        val list = mutableListOf(1, 2, 3)
        val arr = arrayOf(1, 2, 3)
        val map = mutableMapOf("a" to 1, "b" to 2, "c" to 3)
        var result = beanModifier.setContainerValue(ContainerType.LIST, list, 0, 0)
        assertTrue(result.isRight())
        result = beanModifier.setContainerValue(ContainerType.ARRAY, arr, 0, 0)
        assertTrue(result.isRight())
        result = beanModifier.setContainerValue(ContainerType.MAP, map, "a", 0)
        assertTrue(result.isRight())
    }

    data class ExpectedCorrection(
        val pathStr: String,
        val newValue: Any?,
        val oldValue: Any?,
        val correctionDescriptorAnnotationClass: Class<out Annotation>?,
    )

    private fun toExpectedCorrections(
        corrections: List<AppliedCorrection<*>>,
    ) = corrections.map { toExpectedCorrection(it) }

    private fun toExpectedCorrection(
        correction: AppliedCorrection<*>,
    ) = ExpectedCorrection(
        pathStr = correction.propertyPath.toString(),
        newValue = correction.newValue,
        oldValue = correction.oldValue,
        correctionDescriptorAnnotationClass = correction.correctionDescriptor.annotation.annotationClass.java,
    )
}
