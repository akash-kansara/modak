package io.github.akashkansara.modak.core.testbed

import io.github.akashkansara.modak.core.testbed.beans.Asset
import io.github.akashkansara.modak.core.testbed.beans.Branch
import io.github.akashkansara.modak.core.testbed.beans.Company
import io.github.akashkansara.modak.core.testbed.beans.Department
import io.github.akashkansara.modak.core.testbed.beans.JobTitle
import io.github.akashkansara.modak.core.testbed.beans.Meeting
import io.github.akashkansara.modak.core.testbed.beans.OfficeDay
import io.github.akashkansara.modak.core.testbed.beans.Person
import io.github.akashkansara.modak.core.testbed.beans.Prank
import io.github.akashkansara.modak.core.testbed.beans.branch
import io.github.akashkansara.modak.core.testbed.beans.chair
import io.github.akashkansara.modak.core.testbed.beans.company
import io.github.akashkansara.modak.core.testbed.beans.meeting
import io.github.akashkansara.modak.core.testbed.beans.money
import io.github.akashkansara.modak.core.testbed.beans.officeDay
import io.github.akashkansara.modak.core.testbed.beans.person
import io.github.akashkansara.modak.core.testbed.beans.phoneNumber
import io.github.akashkansara.modak.core.testbed.beans.prank
import io.github.akashkansara.modak.core.testbed.beans.printer
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass

fun buildSloughEmployees(): List<Person> {
    return listOf(
        person {
            name = "David Brent"
            catchPhrase = "Catch Phrase: Free Love Freeway"
            jobTitle = JobTitle.BRANCH_MANAGER
            emergencyContact = mutableMapOf(
                "Friend" to "   Finchy   ",
            )
            salary = money { amount = 100.0 }
        },
        person {
            name = "Gareth Keenan"
            catchPhrase = "Catch Phrase: I am the Assistant to the Regional Manager"
            jobTitle = JobTitle.ASSISTANT_TO_THE_BRANCH_MANAGER
            salary = money { amount = 110.0 }
        },
        person {
            name = "Tim Canterbury"
            jobTitle = JobTitle.SALES_REPRESENTATIVE
            emergencyContact = mutableMapOf(
                "Friend" to "\nDawn Tinsley",
            )
            salary = money { amount = 110.0 }
        },
        person {
            name = "Dawn Tinsley"
            jobTitle = JobTitle.RECEPTIONIST
        },
        person {
            name = "Keith Bishop"
            jobTitle = JobTitle.ACCOUNTANT
        },
    )
}

fun buildValidSloughEmployees(fixGroups: List<KClass<*>>): List<Person> {
    val catchPhrasePrefix = if (fixGroups.contains(SloughGroup::class)) {
        ""
    } else {
        "Catch Phrase: "
    }
    val defaultCatchPhrase = if (fixGroups.contains(SwindonGroup::class)) {
        "No catch phrase"
    } else {
        null
    }
    val fixEmergencyContact = fixGroups.contains(BranchGroup::class)
    val moneyAmount = if (fixGroups.contains(SwindonGroup::class)) {
        1.0
    } else {
        null
    }
    val moneyCurrencyCode = if (fixGroups.contains(SloughGroup::class)) {
        "GBP"
    } else {
        null
    }
    return listOf(
        person {
            name = "David Brent"
            catchPhrase = "${catchPhrasePrefix}Free Love Freeway"
            jobTitle = JobTitle.BRANCH_MANAGER
            emergencyContact = mutableMapOf(
                "Friend" to if (fixEmergencyContact) {
                    "Finchy"
                } else {
                    "   Finchy   "
                },
            )
            salary = money {
                amount = 100.0
                currencyCode = moneyCurrencyCode
            }
        },
        person {
            name = "Gareth Keenan"
            catchPhrase = "${catchPhrasePrefix}I am the Assistant to the Regional Manager"
            jobTitle = JobTitle.ASSISTANT_TO_THE_BRANCH_MANAGER
            salary = money {
                amount = 110.0
                currencyCode = moneyCurrencyCode
            }
        },
        person {
            name = "Tim Canterbury"
            catchPhrase = defaultCatchPhrase
            jobTitle = JobTitle.SALES_REPRESENTATIVE
            emergencyContact = mutableMapOf(
                "Friend" to if (fixEmergencyContact) {
                    "Dawn Tinsley"
                } else {
                    "\nDawn Tinsley"
                },
            )
            salary = money {
                amount = 110.0
                currencyCode = moneyCurrencyCode
            }
        },
        person {
            name = "Dawn Tinsley"
            catchPhrase = defaultCatchPhrase
            jobTitle = JobTitle.RECEPTIONIST
            salary = money {
                amount = moneyAmount
                currencyCode = moneyCurrencyCode
            }
        },
        person {
            name = "Keith Bishop"
            catchPhrase = defaultCatchPhrase
            jobTitle = JobTitle.ACCOUNTANT
            salary = money {
                amount = moneyAmount
                currencyCode = moneyCurrencyCode
            }
        },
    )
}

fun buildSwindonEmployees(): List<Person> {
    return listOf(
        person {
            name = "Neil Godwin"
            catchPhrase = "Catch Phrase: I am the Regional Manager"
            jobTitle = JobTitle.BRANCH_MANAGER
            salary = money { amount = 200.0 }
        },
        person {
            name = "Trudy"
            jobTitle = JobTitle.RECEPTIONIST
        },
        person {
            name = "Rachel"
            jobTitle = JobTitle.SALES_REPRESENTATIVE
            emergencyContact = mutableMapOf(
                "Friend" to "Tim Canterbury\t",
            )
        },
    )
}

fun buildValidSwindonEmployees(fixGroups: List<KClass<*>>): List<Person> {
    val catchPhrasePrefix = if (fixGroups.contains(SloughGroup::class)) {
        ""
    } else {
        "Catch Phrase: "
    }
    val defaultCatchPhrase = if (fixGroups.contains(SwindonGroup::class)) {
        "No catch phrase"
    } else {
        null
    }
    val fixEmergencyContact = fixGroups.contains(BranchGroup::class)
    val moneyAmount = if (fixGroups.contains(SwindonGroup::class)) {
        1.0
    } else {
        null
    }
    val moneyCurrencyCode = if (fixGroups.contains(SloughGroup::class)) {
        "GBP"
    } else {
        null
    }
    return listOf(
        person {
            name = "Neil Godwin"
            catchPhrase = "${catchPhrasePrefix}I am the Regional Manager"
            jobTitle = JobTitle.BRANCH_MANAGER
            salary = money {
                amount = 200.0
                currencyCode = moneyCurrencyCode
            }
        },
        person {
            name = "Trudy"
            catchPhrase = defaultCatchPhrase
            jobTitle = JobTitle.RECEPTIONIST
            salary = money {
                amount = moneyAmount
                currencyCode = moneyCurrencyCode
            }
        },
        person {
            name = "Rachel"
            catchPhrase = defaultCatchPhrase
            jobTitle = JobTitle.SALES_REPRESENTATIVE
            emergencyContact = mutableMapOf(
                "Friend" to if (fixEmergencyContact) { "Tim Canterbury" } else {
                    "Tim Canterbury\t"
                },
            )
            salary = money {
                amount = moneyAmount
                currencyCode = moneyCurrencyCode
            }
        },
    )
}

fun buildSloughAssets(): List<Asset> {
    return listOf(
        printer {
            brand = " HP "
            assignedTo = Department.SALES
        },
        chair {
            brand = "IKEA"
        },
        chair {
            brand = "Herman Miller\t"
            assignedTo = Department.HR
        },
    )
}

fun buildValidSloughAssets(): List<Asset> {
    return listOf(
        printer {
            brand = "HP"
            assignedTo = Department.SALES
        },
        chair {
            brand = "IKEA"
            assignedTo = Department.SALES
        },
        chair {
            brand = "Herman Miller"
            assignedTo = Department.HR
        },
    )
}

fun buildSwindonAssets(): List<Asset> {
    return listOf(
        printer {
            brand = " Canon"
            assignedTo = Department.SALES
        },
        chair {
            brand = "Steelcase\n"
        },
    )
}

fun buildValidSwindonAssets(): List<Asset> {
    return listOf(
        printer {
            brand = "Canon"
            assignedTo = Department.SALES
        },
        chair {
            brand = "Steelcase"
            assignedTo = Department.SALES
        },
    )
}

fun buildSloughBranch(): Branch {
    return branch {
        name = "\nSlough Branch\t"
        employees = buildSloughEmployees()
        manager = employees.find { it.jobTitle == JobTitle.BRANCH_MANAGER }!!
        assets = buildSloughAssets()
        address = " 225, Street 99, Park 75, Slough, Berkshire, UK"
    }
}

fun buildValidSloughBranch(fixGroups: List<KClass<*>>): Branch {
    return branch {
        name = "Slough Branch"
        employees = buildValidSloughEmployees(fixGroups)
        manager = employees.find { it.jobTitle == JobTitle.BRANCH_MANAGER }!!
        assets = buildValidSloughAssets()
        address = "225, Street 99, Park 75, Sloug"
        establishedYear = 2001
    }
}

fun buildSwindonBranch(): Branch {
    return branch {
        name = " Swindon Branch"
        employees = buildSwindonEmployees()
        manager = employees.find { it.jobTitle == JobTitle.BRANCH_MANAGER }!!
        assets = buildSwindonAssets()
        address = " 125, Street 66, Park 56, Swindon, Wiltshire, United Kingdom"
    }
}

fun buildValidSwindonBranch(fixGroups: List<KClass<*>>): Branch {
    return branch {
        name = "Swindon Branch"
        employees = buildValidSwindonEmployees(fixGroups)
        manager = employees.find { it.jobTitle == JobTitle.BRANCH_MANAGER }!!
        assets = buildValidSwindonAssets()
        address = "125, Street 66, Park 56, Swind"
        establishedYear = 2001
    }
}

fun buildCompany(): Company {
    return company {
        name = "\nWernham-Hogg paper company\t "
        branches = mutableMapOf(
            "Slough" to buildSloughBranch(),
            "Swindon" to buildSwindonBranch(),
        )
        headquarters = branches["Slough"]!!
        establishedYear = 2001
        phoneNumbers = arrayOf(
            phoneNumber { number = " +44123456789 " },
            phoneNumber { number = null },
        )
    }
}

fun buildValidCompany(fixGroups: List<KClass<*>>): Company {
    return company {
        name = "Wernham-Hogg paper company"
        branches = mutableMapOf(
            "Slough" to buildValidSloughBranch(fixGroups),
            "Swindon" to buildValidSwindonBranch(fixGroups),
        )
        headquarters = branches["Slough"]!!
        establishedYear = 2001
        phoneNumbers = arrayOf(
            phoneNumber { number = "+44123456789" },
            phoneNumber { number = "+44123456789" },
        )
    }
}

fun buildSwindonPranks(tim: Person, gareth: Person, trudy: Person, keith: Person): List<Prank> {
    return listOf(
        prank {
            name = "Phone Stuck in Draw"
            prankster = tim
            target = gareth
        },
        prank {
            name = "Fake Fire Alarm"
            prankster = trudy
            target = keith
        },
    )
}

fun buildSloughPranks(tim: Person, gareth: Person): List<Prank> {
    return listOf(
        prank {
            name = "Gel in Telephone"
            prankster = tim
            target = gareth
        },
        prank {
            name = "Stapler in Jelly"
            prankster = tim
            target = gareth
        },
    )
}

fun buildSloughMeetings(sloughEmployees: List<Person>): List<Meeting> {
    return listOf(
        meeting {
            title = "Redundancy Consultation Meeting"
            agenda = "  Explain that nobody is being made redundant... yet  "
            participants = sloughEmployees.toTypedArray()
            date = Instant.parse("2001-03-01T09:00:00Z")
            noOfAwkwardMoments = null
        },
        meeting {
            title = "David's Motivational Speech Session"
            agenda = "\tGuitar performance + 'Free Love Freeway'\n"
            participants = arrayOf(
                sloughEmployees.find { it.name == "David Brent" }!!,
                sloughEmployees.find { it.name == "Dawn Tinsley" }!!,
                sloughEmployees.find { it.name == "Tim Canterbury" }!!,
            )
            date = Instant.parse("2001-03-01T15:00:00Z")
            noOfAwkwardMoments = null
        },
    )
}

fun buildValidSloughMeetings(sloughEmployees: List<Person>, fixGroups: List<KClass<*>>): List<Meeting> {
    return listOf(
        meeting {
            title = "Redun"
            agenda = "Explain that nobody is being made redundant... yet"
            participants = sloughEmployees.toTypedArray()
            date = Instant.parse("2001-03-01T09:00:00Z")
            noOfAwkwardMoments = 5
        },
        meeting {
            title = "David"
            agenda = "Guitar performance + 'Free Love Freeway'"
            participants = arrayOf(
                sloughEmployees.find { it.name == "David Brent" }!!,
                sloughEmployees.find { it.name == "Dawn Tinsley" }!!,
                sloughEmployees.find { it.name == "Tim Canterbury" }!!,
            )
            date = Instant.parse("2001-03-01T15:00:00Z")
            noOfAwkwardMoments = 5
        },
    )
}

fun buildSwindonMeetings(swindonEmployees: List<Person>, sloughEmployees: List<Person>): List<Meeting> {
    return listOf(
        meeting {
            title = "Swindon Merger Planning Session"
            agenda = " \nEnsure Swindon and Slough integration is painless (it's not)\t "
            participants = (swindonEmployees + sloughEmployees).toTypedArray()
            date = Instant.parse("2002-09-01T10:00:00Z")
            noOfAwkwardMoments = null
        },
        meeting {
            title = "Fire Safety Briefing Meeting"
            agenda = "  Gareth attempts to lead, Neil interrupts  "
            participants = arrayOf(
                sloughEmployees.find { it.name == "Gareth Keenan" }!!,
                swindonEmployees.find { it.name == "Neil Godwin" }!!,
            )
            date = Instant.parse("2002-09-01T14:00:00Z")
            noOfAwkwardMoments = null
        },
    )
}

fun buildValidSwindonMeetings(
    swindonEmployees: List<Person>,
    sloughEmployees: List<Person>,
    fixGroups: List<KClass<*>>,
): List<Meeting> {
    return listOf(
        meeting {
            title = "Swind"
            agenda = "Ensure Swindon and Slough integration is painless (it's not)"
            participants = (swindonEmployees + sloughEmployees).toTypedArray()
            date = Instant.parse("2002-09-01T10:00:00Z")
            noOfAwkwardMoments = 5
        },
        meeting {
            title = "Fire "
            agenda = "Gareth attempts to lead, Neil interrupts"
            participants = arrayOf(
                sloughEmployees.find { it.name == "Gareth Keenan" }!!,
                swindonEmployees.find { it.name == "Neil Godwin" }!!,
            )
            date = Instant.parse("2002-09-01T14:00:00Z")
            noOfAwkwardMoments = 5
        },
    )
}

fun buildSloughOfficeDay(sloughBranch: Branch): OfficeDay {
    return officeDay {
        branch = sloughBranch
        date = LocalDate.parse("2001-09-01")
        meetings = buildSloughMeetings(
            sloughEmployees = sloughBranch.employees,
        )
        pranks = buildSloughPranks(
            tim = sloughBranch.employees.find { it.name == "Tim Canterbury" }!!,
            gareth = sloughBranch.employees.find { it.name == "Gareth Keenan" }!!,
        )
    }
}

fun buildValidSloughOfficeDay(sloughBranch: Branch, fixGroups: List<KClass<*>>): OfficeDay {
    return officeDay {
        branch = sloughBranch
        date = LocalDate.parse("2001-09-01")
        meetings = buildValidSloughMeetings(
            sloughEmployees = sloughBranch.employees,
            fixGroups = fixGroups,
        )
        pranks = buildSloughPranks(
            tim = sloughBranch.employees.find { it.name == "Tim Canterbury" }!!,
            gareth = sloughBranch.employees.find { it.name == "Gareth Keenan" }!!,
        )
    }
}

fun buildSwindonOfficeDay(sloughBranch: Branch, swindonBranch: Branch): OfficeDay {
    return officeDay {
        branch = swindonBranch
        date = LocalDate.parse("2002-09-01")
        meetings = buildSwindonMeetings(
            sloughEmployees = sloughBranch.employees,
            swindonEmployees = swindonBranch.employees,
        )
        pranks = buildSwindonPranks(
            tim = sloughBranch.employees.find { it.name == "Tim Canterbury" }!!,
            gareth = sloughBranch.employees.find { it.name == "Gareth Keenan" }!!,
            trudy = swindonBranch.employees.find { it.name == "Trudy" }!!,
            keith = sloughBranch.employees.find { it.name == "Keith Bishop" }!!,
        )
    }
}

fun buildValidSwindonOfficeDay(sloughBranch: Branch, swindonBranch: Branch, fixGroups: List<KClass<*>>): OfficeDay {
    return officeDay {
        branch = swindonBranch
        date = LocalDate.parse("2002-09-01")
        meetings = buildValidSwindonMeetings(
            sloughEmployees = sloughBranch.employees,
            swindonEmployees = swindonBranch.employees,
            fixGroups = fixGroups,
        )
        pranks = buildSwindonPranks(
            tim = sloughBranch.employees.find { it.name == "Tim Canterbury" }!!,
            gareth = sloughBranch.employees.find { it.name == "Gareth Keenan" }!!,
            trudy = swindonBranch.employees.find { it.name == "Trudy" }!!,
            keith = sloughBranch.employees.find { it.name == "Keith Bishop" }!!,
        )
    }
}

/**
 * There should be no correction requirement here
 */
fun buildValidMinimalCompany(): Company {
    return company {
        name = "Minimal Company"
        branches = mutableMapOf(
            "Minimal Branch" to branch {
                name = "Minimal Branch"
                employees = listOf(
                    person {
                        name = "Minimal Manager"
                        jobTitle = JobTitle.BRANCH_MANAGER
                    },
                    person {
                        name = "Minimal Employee"
                        jobTitle = JobTitle.SALES_REPRESENTATIVE
                    },
                )
                manager = employees.first()
                assets = listOf(
                    printer {
                        brand = "Minimal Printer"
                        assignedTo = Department.SALES
                    },
                )
                establishedYear = 2002
            },
        )
        headquarters = branches["Minimal Branch"]!!
        establishedYear = 2023
        phoneNumbers = arrayOf(
            phoneNumber { number = "+44123456789" },
            phoneNumber { number = "+44123456799" },
        )
    }
}
