plugins {
    `maven-publish`
    jacoco
}

dependencies {
    implementation(libs.jakarta.validation.api)

    testImplementation(libs.junit.jupiter)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "modak-api"
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
