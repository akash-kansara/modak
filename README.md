# Modak - A Data Correction Library

[![Build](https://github.com/akash-kansara/modak/actions/workflows/ci.yml/badge.svg)](https://github.com/akash-kansara/modak/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=coverage)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![Security](https://sonarcloud.io/api/project_badges/measure?project=akash-kansara_modak&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=akash-kansara_modak)
[![License](https://img.shields.io/github/license/akash-kansara/modak)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akash-kansara/modak-core)](https://search.maven.org/search?q=g:io.github.akash-kansara)

Modak is a library that helps you define data correction rules and provides APIs to correct data in your objects based on those rules. Main features:

1. Lets you express data correction rules on object models via annotations
2. Lets you write custom constraint in an extensible way
3. Provides APIs to correct objects
4. Works for Java & Kotlin projects

### 🍡 What's a Modak?

**Modak** is a traditional Indian sweet, typically a dumpling, made with rice or wheat flour and filled with a sweet mixture, often grated coconut and jaggery.

## 📦 Installation

Available on [Maven Central](https://central.sonatype.com/artifact/io.github.akash-kansara/modak-core?smo=true)

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.akash-kansara:modak-core:$version")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'io.github.akash-kansara:modak-core:$version'
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.akash-kansara</groupId>
    <artifactId>modak-core</artifactId>
    <version>VERSION</version>
</dependency>
```

## 🚀 Quick Start

### 1. Define your corrections

```java
// Correction annotation:
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
@UserCorrection(                                // Your custom correction rule
        defaultRole = "DEFAULT",
        adminRole = "ADMIN"
)
public class User {
    @Trim                                       // Provided by library
    @DefaultValue(strValue = "Anonymous")       // If you're using getter/setter, you can annotate the getter instead of fields
    public String name;                         // public modifier is required

    @DefaultValue(intValue = 18)                // Provided by library
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
CorrectionResult.Success<User> successResult = (CorrectionResult.Success<User>) result;
System.out.println(                             // 4
        successResult.getAppliedCorrections().size()
);
System.out.println(user);                       // User{name='Anonymous', age=18, role='ADMIN', email='example@company.com'}

```

## 🔗 Synergy with Bean Validation

Modak is a companion library to [Jakarta Bean Validation](https://beanvalidation.org/). Its scope is limited to data correction and does not provide data validation features, but it seamlessly integrates with bean validation.
You can use any bean validation such as [Hibernate Validator](https://hibernate.org/validator/), [Apache BVal](https://bval.apache.org/) along with Modak.

### Bean Validation Example

```java
public class User {
    private String name;

    public User(String name) {
        this.name = name;
    }

    @NotNull                                    // Jakarta Bean Validation constraint
    @DefaultValue(
            strValue = "Anonymous",
            constraintFilter = {NotNull.class}  // Only apply if NotNull constraint fails
    )
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

User user = new User(null);
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
Set<ConstraintViolation<User>> violations = validator.validate(user);
Corrector corrector = CorrectorFactory.buildCorrector();
CorrectionResult<User, ErrorLike> result = corrector.correct(   // Since violations are supplied, correction will be applied only if NotNull constraint has failed
        user,
        violations
);
System.out.println(user.getName());                             // Anonymous
```

## 📚 Documentation

**📖 [Full Documentation](docs/REFERENCE_GUIDE.md)**

## ✨ Key Features

🔧 **Automatic Data Correction** - Automatically fix inconsistent or invalid data

📝 **Annotation-Based** - Use simple, declarative annotations to define correction rules

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

**Need help?** [Open an issue](https://github.com/akash-kansara/modak/issues) or check the [full documentation](docs/REFERENCE_GUIDE.md).
