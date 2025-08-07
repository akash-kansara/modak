import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.diffplug.spotless") version "6.25.0" apply false
    alias(libs.plugins.kotlin.jvm)
    id("org.sonarqube") version "6.2.0.5505"
    jacoco
    id("org.jreleaser") version "1.12.0"
}

object ProjectInfo {
    val version = System.getenv("JRELEASER_PROJECT_VERSION") ?: "1.0.0"
    const val group = "io.github.akashkansara"
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
