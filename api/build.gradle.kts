plugins {
    `maven-publish`
    jacoco
    signing
}

base {
    archivesName.set("modak-api")
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(libs.jakarta.validation.api)

    testImplementation(libs.junit.jupiter)
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "modak-api"
            pom {
                name.set("Modak API")
                description.set("Kotlin/Java API module for Modak")
                url.set("https://github.com/akash-kansara/modak")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("akash-kansara")
                        name.set("Akash Kansara")
                        email.set("akash-kansara@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/akash-kansara/modak.git")
                    developerConnection.set("scm:git:ssh://github.com/akash-kansara/modak.git")
                    url.set("https://github.com/akash-kansara/modak")
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

signing {
    val signingKey: String? = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword: String? = System.getenv("GPG_PASSPHRASE")

    isRequired = signingKey != null && signingPassword != null

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}
