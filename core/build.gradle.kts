plugins {
    `maven-publish`
    jacoco
    signing
}

base {
    archivesName.set("modak-core")
}

java {
    withSourcesJar()
    withJavadocJar()
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "modak-core"
            pom {
                name.set("Modak Core")
                description.set("Kotlin/Java Core module for Modak")
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
    val signingKeyId: String? = System.getenv("JRELEASER_GPG_KEY_ID")
    val signingKey: String? = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword: String? = System.getenv("GPG_PASSPHRASE")

    isRequired = signingKeyId != null && signingKey != null && signingPassword != null

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}
