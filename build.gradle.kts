import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.diffplug.spotless") version "6.25.0" apply false
    alias(libs.plugins.kotlin.jvm)
    id("org.sonarqube") version "6.2.0.5505"
    jacoco
    `maven-publish`
    signing
}

object ProjectInfo {
    val version = System.getenv("GH_RELEASE_TAG") ?: "1.0.0"
    const val group = "io.github.akashkansara"
    const val artifact = "modak"
}

allprojects {
    group = ProjectInfo.group
    version = ProjectInfo.version
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    extensions.configure<SpotlessExtension>("spotless") {
        kotlin {
            target("src/**/*.kt")
            ktlint("0.50.0")
        }
        kotlinGradle {
            target("*.gradle.kts", "buildSrc/**/*.kt")
            ktlint("0.50.0")
        }
        java {
            target("src/**/*.java")
            googleJavaFormat()
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Code coverage and SonarQube configuration

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoRootReport") {
    group = "verification"
    dependsOn(subprojects.map { it.tasks.named("test") })
    val allExecutionData = files(subprojects.map {
        it.layout.buildDirectory.file("jacoco/test.exec").get().asFile
    })
    executionData.setFrom(allExecutionData)
    val sourceDirs = files(subprojects.map {
        it.projectDir.resolve("src/main/kotlin")
    })
    val classDirs = files(subprojects.map {
        it.layout.buildDirectory.dir("classes/kotlin/main")
    })
    sourceDirectories.setFrom(sourceDirs)
    classDirectories.setFrom(classDirs)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.named("check") {
    dependsOn("jacocoRootReport")
}

sonarqube {
    properties {
        property("sonar.projectKey", "akash-kansara_modak")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", System.getenv("SONAR_TOKEN"))
        property("sonar.organization", "akash-kansara")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml")
    }
}

tasks.named("sonar") {
    dependsOn("jacocoRootReport")
}

// Publishing configuration

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = ProjectInfo.group
            artifactId = ProjectInfo.artifact
            version = ProjectInfo.version
            pom {
                name.set("Modak")
                description.set("A Kotlin/Java library for auto-correcting data using annotations")
                url.set("https://github.com/akash-kansara/modak")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/akash-kansara/modak.git")
                    developerConnection.set("scm:git:ssh://github.com/akash-kansara/modak.git")
                    url.set("https://github.com/akash-kansara/modak")
                }
                developers {
                    developer {
                        id.set("akashkansara")
                        name.set("Akash Kansara")
                        email.set("akash-kansara@users.noreply.github.com")
                    }
                }
            }
        }
    }
    repositories {
        if (System.getenv("OSSRH_USERNAME") != null && System.getenv("OSSRH_PASSWORD") != null) {
            maven {
                name = "OSSRH"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                }
            }
        }
        mavenLocal()
    }
}

signing {
    val signingKey: String? = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword: String? = System.getenv("GPG_PASSPHRASE")
    isRequired = signingKey != null && signingPassword != null
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(
            signingKey,
            signingPassword
        )
        afterEvaluate {
            sign(publishing.publications["mavenJava"])
        }
    }
}
