# API Specification

## Table of Contents

* [1. Introduction](#1-introduction)
* [2. Correction definition](#2-correction-definition)
  * [2.1. Correction annotation](#21-correction-annotation)
  * [2.2. Correction composition](#22-correction-composition)
  * [2.3. Correction applier implementation](#23-correction-applier-implementation)
* [3. Correction declaration and application process](#3-correction-declaration-and-application-process)
  * [3.1. Requirements on classes to be corrected](#31-requirements-on-classes-to-be-corrected)
  * [3.2. Correction declaration](#32-correction-declaration)
  * [3.3. Inheritance (interface and superclass)](#33-inheritance-interface-and-superclass)
  * [3.4. Group and group sequence](#34-group-and-group-sequence)
  * [3.5. Nested object corrections](#35-nested-object-corrections)
  * [3.6. Container element corrections](#36-container-element-corrections)
  * [3.7. Correction routine](#37-correction-routine)
* [4. Correction APIs](#4-correction-apis)
  * [4.1. Corrector API](#41-corrector-api)
  * [4.2. Bootstrapping](#42-bootstrapping)
* [5. Built-in Correction definitions](#5-built-in-correction-definitions)
  * [5.1. @DefaultValue correction](#51-defaultvalue-correction)
  * [5.2. @Trim correction](#52-trim-correction)
  * [5.3. @Truncate correction](#53-truncate-correction)
  * [5.4. @RegexReplace correction](#54-regexreplace-correction)
* [6. Integration](#6-integration)
  * [6.1. Jakarta Bean Validation integration](#61-jakarta-bean-validation-integration)
  * [6.2. Constraint filtering](#62-constraint-filtering)
* [Appendix A: CorrectionTarget enumeration](#appendix-a-correctiontarget-enumeration)

---

## 1. Introduction

The library provides a framework for automatically correcting data validation issues using annotation-based corrections.

The primary goals of this specification are:

- **Language compatibility**: Fully compatible with both Java and Kotlin projects
- **Annotation-based**: Provide a simple, declarative way to define correction rules using annotations
- **Jakarta Bean Validation integration**: Work seamlessly with Jakarta Bean Validation constraints
- **Extensibility**: Allow developers to create custom correction logic
- **Type safety**: Ensure full type safety with generic correction appliers
- **Group support**: Enable conditional corrections based on validation groups
- **Constraint filtering**: Allow corrections to target specific validation constraints

Relationship to Jakarta Bean Validation:

This library is inspired by and designed to complement [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/3.1/jakarta-validation-spec-3.1). It retains similar APIs and concepts such as:

- **Groups**: Apply corrections conditionally based on validation groups
- **Group sequences**: Define ordered execution of correction groups
- **Payload**: Attach metadata to correction annotations
- **Constraint integration**: React to specific constraint violations

---

## 2. Getting Started

### 2.1. Setup project

Add the library dependency to your project:
**Maven:**
```xml
<dependency>
    <groupId>io.github.akash-kansara</groupId>
    <artifactId>modak-core</artifactId>
    <version>VERSION</version>
```

***Gradle (Kotlin DSL):***
```kotlin
dependencies {
    implementation("io.github.akash-kansara:modak-core:$VERSION")
}
```

#### 2.2 Defining corrections

Example: Class User with corrections

```java
import io.github.akashkansara.modak.api.correction.DefaultValue;
import io.github.akashkansara.modak.api.correction.RegexReplace;
import io.github.akashkansara.modak.api.correction.Trim;

public class User {
    @Trim
    @DefaultValue(strValue = "Anonymous")
    public String name;

    @DefaultValue(intValue = 18)
    public Integer age;

    public String role;

    @RegexReplace(
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

_NOTE: If you're using getter/setter methods, you can annotate the getter instead of fields_

The `@Trim`, `@DefaultValue` and `@RegexReplace` annotations are used to declare the corrections which should be applied to the fields of a User instance:

- If `name` is null, assign a default value of "Anonymous"
- Trim leading and trailing whitespace from `name`
- If `age` is null, assign a default value of 18
- Replace any characters from `email` that match the specified regex pattern

#### 2.3 Applying corrections

```java
import io.github.akashkansara.modak.api.CorrectionResult;
import io.github.akashkansara.modak.api.Corrector;
import io.github.akashkansara.modak.core.CorrectorFactory;

public class Main {
    public static void main(String[] args) {
    Corrector corrector = CorrectorFactory.buildCorrector();
    User user = new User("  John Doe  ", null, null, "example@com!pany.com");
    var result = corrector.correct(user);
        if (result.isSuccess()) {
            CorrectionResult.Success<User> successResult = (CorrectionResult.Success<User>) result;
            System.out.println(successResult.getAppliedCorrections().size());
            System.out.println(user); // User{name='John Doe', age=18, role=null, email='example@company.com'}
        } else {
            CorrectionResult.Failure failure = ((CorrectionResult.Failure) result);
            System.out.println(failure.getError().getMessage());
            System.out.println(failure.getError().getCause());
            System.out.println(failure.getError().getAppliedCorrections());
        }
    }
}
```

The `correct()` method returns `CorrectionResult` which has a property `isSuccess` that indicates whether correction was successful.

When `isSuccess` is `true`, you can safely cast the result to `CorrectionResult.Success<User>`. This object has `getAppliedCorrections` API returns a list of `AppliedCorrection` which you can use to iterate over applied corrections.

When `isSuccess` is `false`, you can safely cast the result to `CorrectionResult.Failure`. This object has `getError` API which you can use to get details about the failure. This object also has `getAppliedCorrections` API which you can use to iterate over the list of corrections that were applied before the failure occurred.

### 3. Declaring and applying corrections

#### 3.1 Declaring corrections

Corrections are declared using annotations. There are 3 types of corrections:
- field / property corrections (Can be applied to fields or getter methods)
- container element corrections (List, Map, Array element corrections)
- class corrections

```java
@MyCustomUserCorrection(
        defaultRole = "DEFAULT",
        adminRole = "ADMIN"
)
public class User {
    @Trim
    @DefaultValue(strValue = "Anonymous")
    public String name;

    public String role;

    @DefaultValue(intValue = 18)
    public Integer age;

    @Trim(
            correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    public List<String> contactNumbers;
}

class Main {
    public static void main(String[] args) {
        Corrector corrector = CorrectorFactory.buildCorrector();
        User user = new User(null, null, null, Arrays.asList("  +1-555-123-4567  ", "555-987-6543\n"));
        var result = corrector.correct(user);
        CorrectionResult.Success<User> successResult = (CorrectionResult.Success<User>) result;
        System.out.println(user);
        // User{
        //   name='Anonymous',
        //   age=18,
        //   role='DEFAULT',
        //   contactNumbers=['+1-555-123-4567', '555-987-6543']
        // }
    }
}
```

- `@MyCustomUserCorrection` is a class-level correction that applies to the entire User object
- `@Trim` and `@DefaultValue` are field-level corrections that apply to the respective fields
- `@Trim(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT)` is a container element correction that applies to each element in the `contactNumbers` list

Corrections are inherited:

```java
public class BaseUser {
    @Trim
    @DefaultValue(strValue = "Anonymous")
    public String name;
}

public class User extends BaseUser {
    @DefaultValue(intValue = 18)
    public Integer age;
}

// User.name will have @Trim and @DefaultValue corrections inherited from BaseUser
```

Object graph traversal and nested corrections:

```java
public class Headquarters {
    @Truncate(length = 100)
    public String address;

    @DefaultValue(intValue = 2000)
    public Integer establishedYear;
}

public class Company {
    @Trim
    public String name;

    @CorrectNested
    public Office headquarters;
}

// Correction:
Headquarters hq = new Headquarters("  123 Main St  ", null);
Company company = new Company("  Acme Corp  ", hq);
CorrectionResult<Company> result = corrector.correct(company);
CorrectionResult.Success<Company> successResult = (CorrectionResult.Success<Company>) result;
System.out.println(company);
// Company{
//   name='Acme Corp',
//   headquarters=Office{
//     address='123 Main St',
//     establishedYear=2000
//   }
// }
```

Here, `@CorrectNested` on the `headquarters` field of `Company` enables automatic traversal into the `Headquarters` object and applies its corrections.

Object graph traversal and nested corrections for container elements:

```java
public class Employee {
    public Boolean isManager;
    
    @Trim
    @DefaultValue(strValue = "Unknown Employee")
    public String name;

    @DefaultValue(intValue = 18)
    public Integer age;
}

public class Branch {
    @Trim
    public String name;

    @ManagerCorrection(                         // Targets each Employee in the list
            correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    @RemoveDuplicateEmployeesCorrection         // Targets the entire list of employees
    @CorrectNested                              // Enables nested correction for each Employee in the list
    public List<Employee> employees;
}

// Correction:
Employee emp1 = new Employee(null, null, null);
Employee emp2 = new Employee(true, "  Alice  ", 30);
Employee emp3 = new Employee(true, "  Alice  ", 30); // Duplicate
Branch branch = new Branch("  Branch 1  ", Arrays.asList(emp1, emp2, emp3));
CorrectionResult<Branch> result = corrector.correct(branch);
System.out.println(branch);
// Branch{
//   name='Branch 1',
//   employees=[
//     Employee{isManager=false, name='Unknown Employee', age=18},
//     Employee{isManager=true, name='Alice', age=30}
//   ]
// }
```

Assuming that:
- `@ManagerCorrection` is a custom correction that sets `isManager` to `false` if it's null
- `@RemoveDuplicateEmployeesCorrection` is a custom correction that removes duplicate employees based on `name` and `age`

Here, `@CorrectNested` on the `employees` field of `Branch` enables automatic traversal into each `Employee` object in the list and applies their corrections. The `@ManagerCorrection` applies to each `Employee` in the list and `@RemoveDuplicateEmployeesCorrection` applies to the entire list.

#### 3.2 `AppliedCorrection`

The `AppliedCorrection` class provides details about each correction that was applied during the correction process:

- **propertyPath**: The path to the property that was corrected (e.g., "name", "headquarters.address", "employees[0].name")
- **correctionAnnotation**: The annotation instance that triggered the correction (e.g., `@Trim`, `@DefaultValue`)
- **oldValue**: The original value before correction
- **newValue**: The new value after correction
- **correctionApplierClass**: The class of the `CorrectionApplier` that performed the correction (e.g., `TrimCorrectionApplier`, `RemoveDuplicateEmployeesCorrectionApplier`)

#### 3.3 Built-in corrections

The library provides several built-in correction annotations such as:

**DefaultValue**

The `@DefaultValue` annotation sets a default value for null fields. It supports multiple data types including String, Integer, Long, Double, Float, Boolean, Character, Byte, Short, and Enum types.

**Trim**

The `@Trim` annotation removes leading and trailing whitespace from string fields.

**Truncate**

The `@Truncate` annotation limits the length of string fields by truncating excess characters from either the start or end, defaulting to truncating from the end.

**RegexReplace**

The `@RegexReplace` annotation replaces text in string fields that match a specified regex pattern with a given replacement string.

### 4. Grouping corrections

#### 4.1 Groups

The `correct` method also takes a var-arg argument of groups. Groups allow you to restrict set of corrections that should be applied.

Example:

```java
public interface BasicCorrection {
}

public interface RoleCorrection {
}

@MyCustomUserCorrection(
        defaultRole = "DEFAULT",
        adminRole = "ADMIN",
        groups = {RoleCorrection.class}
)
public class User {
  @Trim(
          groups = {BasicCorrection.class}
  )
  @DefaultValue(
          strValue = "Anonymous",
          groups = {BasicCorrection.class}
  )
  public String name;

  public String role;

  @DefaultValue(
          intValue = 18,
          groups = {BasicCorrection.class}
  )
  public Integer age;
}

User user = new User(null, null, null, null);
CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(user);
System.out.println(result.getAppliedCorrections().size());  // 0

CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(
        user,
        BasicCorrection.class
);
System.out.println(result.getAppliedCorrections().size());  // 3

CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(
        user,
        BasicCorrection.class, RoleCorrection.class         // Corrects BasicCorrection first and then RoleCorrection
);
System.out.println(result.getAppliedCorrections().size());  // 4
```

The reason why no corrections were applied when groups weren't specified in the 1st `correct` call is because all corrections have been assigned to specific groups.
When corrections are not assigned any group _OR_ they are assigned the `DefaultGroup` group, they are applied when no groups are passed in the `correct` call.

#### 4.2 Group inheritance

Groups can also inherit from other groups. This allows you to create a hierarchy of groups and apply corrections based on that hierarchy.

```java
public interface BasicCorrection {
}

public interface RoleCorrection extends BasicCorrection {
}

CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(
        user,
        RoleCorrection.class                                // Corrects BasicCorrection first and then RoleCorrection
);
System.out.println(result.getAppliedCorrections().size());  // 4
```

#### 4.3 Group sequences

You might have a requirement to apply corrections in a specific order. For example, you might want to apply basic corrections first and then role-related corrections.
To enforce this, you can use `GroupSequence` annotation.

```java
public interface BasicCorrection {
}

public interface RoleCorrection {
}

@GroupSequence({BasicCorrection.class, RoleCorrection.class})
public interface OrderedCorrections {
}

CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(
        user,
        OrderedCorrections.class                            // Corrects BasicCorrection first and then RoleCorrection
);
System.out.println(result.getAppliedCorrections().size());  // 4
```

### 5. Creating custom corrections

To create a custom correction, 2 things are needed:
- A correction annotation
- Implement a correction applier

Let's consider the following model:

```java
public class Phone {
    public String countryCode;
    public String number;

    public Phone(String countryCode, String number) {
        this.countryCode = countryCode;
        this.number = number;
    }
}
```

Now, if we wanted to create a custom correction that adds a default value to country code if it's null, we would do the following:

```java
import io.github.akashkansara.modak.api.Correction;
import io.github.akashkansara.modak.api.CorrectionTarget;

@Target({ElementType.TYPE, ElementType.FIELD})
@Correction(correctedBy = {PhoneCorrectionApplier.class})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneCorrection {
    String defaultCountryCode() default "+1";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
    CorrectionTarget correctionTarget() default CorrectionTarget.PROPERTY;
}
```

Implementation of the correction applier:

```java
import io.github.akashkansara.modak.api.CorrectionApplier;
import io.github.akashkansara.modak.api.CorrectionApplierContext;
import io.github.akashkansara.modak.api.CorrectionApplierResult;

public class PhoneCorrectionApplier implements CorrectionApplier<PhoneCorrection, Phone> {
    private String defaultCountryCode;

    @Override
    public void initialize(PhoneCorrection annotation) {
        this.defaultCountryCode = annotation.defaultCountryCode();
    }

    @Override
    public PhoneCorrectionApplier<Phone> correct(Phone phone, CorrectionApplierContext context) {
        if (phone.countryCode == null) {
            Phone newPhone = new Phone(this.defaultCountryCode, phone.number);
            return new CorrectionApplierResult.Edited<>(phone, newPhone);
        } else {
            return new CorrectionApplierResult.NoChange<>();
        }
    }
}
```

Simple usage:

```java
public class User {
    public String name;
    @PhoneCorrection()
    public Phone phone;

    public User(String name, Phone phone) {
        this.name = name;
        this.phone = phone;
    }
}

User user = new User("John Doe", new Phone(null, "555-123-4567"));
CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(user);
System.out.println(user);                   // User{name='John Doe', phone=Phone{countryCode='+1', number='555-123-4567'}}
System.out.println(                         // Phone{countryCode='null', number='555-123-4567'}
        successResult.getAppliedCorrections().get(0).getOldValue()
);
System.out.println(                         // Phone{countryCode='+1', number='555-123-4567'}
        successResult.getAppliedCorrections().get(0).getNewValue()
);
```

Container element correction:

```java
public class User {
    public String name;
    @PhoneCorrection(
            defaultCountryCode = "+45",
            correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    public List<Phone> phones;

    public User(String name, List<Phone> phones) {
        this.name = name;
        this.phones = phones;
    }
}

User user = new User("John Doe", Arrays.asList(
        new Phone(null, "555-123-4567"),
        new Phone("+44", "020 7946 0958")
));
CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(user);
System.out.println(user);                   // User{name='John Doe', phones=[Phone{countryCode='+45', number='555-123-4567'}, Phone{countryCode='+44', number='020 7946 0958'}]}
```

Corrections can be applied to classes as well:

```java
@UserCorrection(
        defaultRole = "DEFAULT",
        adminRole = "ADMIN"
)
public class User {
  ...
}
```

### 6. Integrating with Jakarta Bean Validation

The library can be integrated with Jakarta Bean Validation seamlessly. The integration feature allows you to pass in constraint violations to the `correct` method, and it will only apply corrections that are relevant to those violations.
You can additionally supply groups as well to further control sequence of corrections as discussed above.

```java
public class User {
    @NotNull
    @DefaultValue(
            strValue = "Anonymous",
            constraintFilter = {NotNull.class}          // Only apply if NotNull constraint fails
    )
    public String name;

    @Min(18)
    @MinAgeCorrection(
            value = 18,
            constraintFilter = {Min.class}              // Only apply if Min constraint fails
    )
    public Integer age;
}

User user = new User(null, 15);
Set<ConstraintViolation<User>> violations = validator.validate(user);
CorrectionResult.Success<User> result = (CorrectionResult.Success<User>) corrector.correct(
        user,
        violations                                      // Only apply corrections relevant to these violations
);
System.out.println(user);                       // User{name='Anonymous', age=18}
```
