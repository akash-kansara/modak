plugins {
    `maven-publish`
    jacoco
}

dependencies {
    api(project(":api"))

    implementation(libs.arrow.kt.core)
    implementation(libs.guava)
    implementation(libs.jakarta.validation.api)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.hibernate.validator)
    testImplementation(libs.glassfish.expressly)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "modak-core"
        }
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/kotlin"))
        }
    }
    test {
        java {
            setSrcDirs(listOf("src/test/kotlin"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
