# Modak - A Data Correction Library

[![Build](https://github.com/akash-kansara/modak/actions/workflows/ci.yml/badge.svg)](https://github.com/akash-kansara/modak/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=coverage)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![Security](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![License](https://img.shields.io/github/license/akash-kansara/modak)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akash-kansara/modak)](https://search.maven.org/search?q=g:io.github.akash-kansara)

A robust Kotlin/Java library that automatically fixes common data validation issues (e.g., nulls, formatting, value mismatches) using annotation-driven correction logic. Seamlessly integrates with Jakarta Bean Validation and supports nested objects, group sequences, and container-level fixes.

## 📦 Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.akash-kansara:modak-api:$version")
    implementation("io.github.akash-kansara:modak-core:$version")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'io.github.akash-kansara:modak-api:$version'
    implementation 'io.github.akash-kansara:modak-core:$version'
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.akash-kansara</groupId>
    <artifactId>modak-api</artifactId>
    <version>VERSION</version>
</dependency>
<dependency>
    <groupId>io.github.akash-kansara</groupId>
    <artifactId>modak-core</artifactId>
    <version>VERSION</version>
</dependency>
```

## 🚀 Quick Start

### 1. Define Your Data Class with Corrections

```kotlin
data class User(
    @field:Trim                                    // Remove leading/trailing whitespace
    @field:DefaultValue(strValue = "Anonymous")    // Set default if null/empty
    val name: String?,
    
    @field:NotNull
    @field:DefaultValue(intValue = 18, constraintFilter = [NotNull::class])
    val age: Int?,
    
    @field:Truncate(length = 100)                  // Limit length to 100 characters
    @field:Trim
    val bio: String?,
    
    @field:RegexReplace(
        regexPattern = "[^a-zA-Z0-9@._-]", 
        replaceStr = ""
    )
    val email: String?
)
```

### 2. Create and Use the Corrector

```kotlin
// Create corrector instance
val corrector = CorrectorFactory().buildCorrector()

// Create user with data issues
val user = User(
    name = "  ", // Empty after trimming - will be corrected to "Anonymous"
    age = null,  // Null - will be corrected to 18
    bio = "This is a very long biography that exceeds the maximum allowed length and will be truncated to fit within the specified limit of 100 characters. This part will be removed.",
    email = "user@domain!#$.com" // Invalid characters - will be cleaned
)

// Apply corrections
// Parameters: object, correctViolationsOnly, constraintViolations, groups
when (val result = corrector.correct(user, false, null)) {
    is CorrectionResult.Success -> {
        val correctedUser = result.correctedObject
        val appliedCorrections = result.appliedCorrections
        
        println("Corrected user: $correctedUser")
        println("Applied ${appliedCorrections.size} corrections")
    }
    is CorrectionResult.Failure -> {
        println("Correction failed: ${result.error.message}")
    }
}
```

## 📚 Documentation

**📖 [Full Documentation](docs/API.md)**

## ✨ Key Features

🔧 **Automatic Data Correction** - Fix validation issues instead of just reporting them  
📝 **Annotation-Based** - Simple annotations to define correction rules  
🔗 **Jakarta Validation Integration** - Works with existing validation constraints  
🎯 **Built-in Corrections** - Common corrections ready to use  
🛠️ **Custom Corrections** - Easy to extend with your own logic  
👥 **Group Support** - Apply corrections based on validation groups
🎛️ **Constraint Filtering** - Target specific validation constraints
🌳 **Nested Objects** - Automatically traverse and correct nested structures
🛡️ **Type Safe** - Full Kotlin type safety with generics

## 🤝 Contributing

Contributions welcome! Please see our [contributing guidelines](CONTRIBUTING.md) and feel free to submit issues and pull requests.

## 📄 License

Licensed under the terms in the [LICENSE](LICENSE) file.

---

**Need help?** [Open an issue](https://github.com/akash-kansara/modak/issues) or check the [full documentation](docs/API.md).
