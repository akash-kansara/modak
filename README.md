# Modak - A Data Correction Library

[![Build](https://github.com/akash-kansara/modak/actions/workflows/ci.yml/badge.svg)](https://github.com/akash-kansara/modak/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=coverage)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![Security](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![License](https://img.shields.io/github/license/akash-kansara/modak)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akash-kansara/modak-core)](https://search.maven.org/search?q=g:io.github.akash-kansara)

Modak is a companion library to [Jakarta Bean Validation](https://beanvalidation.org/).
**Bean Validation** defines **constraints** and checks whether your object model is valid.
**Modak** focuses only on **data correction**: applying annotation-based rules to automatically fix data.

If you are already using Bean Validation (Hibernate Validator, Apache BVal, or any implementation) and need automatic correction/sanitization, Modak integrates seamlessly as a drop-in companion.

It can also be used standalone, without Bean Validation, whenever you need annotation-driven data correction.

Main features:

1. Lets you express data correction rules on object models via annotations
2. Lets you write custom constraint in an extensible way
3. Provides APIs to correct objects
4. Works for Java & Kotlin projects

### 🍡 What's a Modak?

**Modak** is a traditional Indian sweet, typically a dumpling, made with rice or wheat flour and filled with a sweet mixture, often grated coconut and jaggery.

## 📦 Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    // Add only if you're not using bean validation already
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    // Modak dependencies
    implementation("io.github.akash-kansara:modak-api:$version")
    implementation("io.github.akash-kansara:modak-core:$version")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    // Add only if you're not using bean validation already
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    // Modak dependencies
    implementation 'io.github.akash-kansara:modak-api:$version'
    implementation 'io.github.akash-kansara:modak-core:$version'
}
```

### Maven
```xml
<!-- Add only if you're not using bean validation already -->
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>3.1.0</version>
</dependency>

<!-- Modak dependencies -->
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

### 1. Define your corrections

```java
// Annotation:
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Correction(correctedBy = {UserCorrectionApplier.class})
public @interface UserCorrection {
    String defaultRole() default "USER";
    String adminRole() default "ADMIN";
}

// Correction applier:
public class UserCorrectionApplier implements CorrectionApplier<UserCorrection, User> {
    private String defaultRole;
    private String adminRole;

    @Override
    public void initialize(UserCorrection annotation) {
        this.defaultRole = annotation.defaultRole();
        this.adminRole = annotation.adminRole();
    }

    @Override
    public CorrectionApplierResult<User> correct(User user, CorrectionApplierContext context) {
        if (user.role == null) {
            String newRole = this.defaultRole;
            if (user.email != null && user.email.endsWith("@company.com")) {
                newRole = this.adminRole;
            }
            user.role = newRole;
            return new CorrectionApplierResult.Edited<>(user, user); // Left value is original, right is corrected. Here we're updating in-place but you can return a new instance as well
        } else {
            return new CorrectionApplierResult.NoChange<>();
        }
    }
}
```

### 2. Define your model with corrections

```java
@UserCorrection(
        defaultRole = "DEFAULT",
        adminRole = "ADMIN"
)
public class User {
    @Trim                                       // Provided by library
    @DefaultValue(strValue = "Anonymous")       // If you're using getter/setter, you can annotate the getter instead of fields
    public String name;

    @NotNull
    @DefaultValue(                              // Provided by library
            intValue = 18,
            constraintFilter = {NotNull.class}
    )
    public Integer age;

    public String role;

    @RegexReplace(                              // Provided by library
            regexPattern = "[^a-zA-Z0-9@._-]",
            replaceStr = ""
    )
    public String email;

    public User(String name, Integer age, String role, String email) {
        this.name = name;
        this.age = age;
        this.role = role;
        this.email = email;
    }
}
```

### 3. Correct your data

```java
Corrector corrector = CorrectorFactory.buildCorrector();

User user = new User(null, null, null, "example@com!pany.com");

CorrectionResult<User, ErrorLike> result = corrector.correct(user);

System.out.println(result.isSuccess());         // true

if (result instanceof CorrectionResult.Success<User, ErrorLike> success) {
        System.out.println(                     // 4
            success.getAppliedCorrections().size()
        );
        System.out.println(user);               // User{name='Anonymous', age=18, role='ADMIN', email='example@company.com'}
}
```

## 📚 Documentation

**📖 [Full Documentation](docs/API.md)**

## ✨ Key Features

🔧 **Automatic Data Correction** - Fix data issues

📝 **Annotation-Based** - Simple annotations to define correction rules

🔗 **Jakarta Validation Integration** - Works with existing validation constraints

🎯 **Built-in Corrections** - Common corrections ready to use

🛠️ **Custom Corrections** - Easy to extend with your own logic

👥 **Group Support** - Sequence, filter corrections based on groups & group sequences

🎛️ **Constraint Filtering** - Target specific validation constraints

🌳 **Nested Objects** - Automatically traverse and correct nested structures

🛡️ **Type Safe** - Full Kotlin type safety with generics

## 🤝 Contributing

Contributions welcome! Please see our [contributing guidelines](CONTRIBUTING.md) and feel free to submit issues and pull requests.

## 📄 License

Licensed under the terms in the [LICENSE](LICENSE) file.

---

**Need help?** [Open an issue](https://github.com/akash-kansara/modak/issues) or check the [full documentation](docs/API.md).
