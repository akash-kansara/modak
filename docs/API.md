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
When `isSuccess` is `true`, you can safely cast the result to  which you can iterate over in order to see which validation errors occurred.

### 2.2. Correction composition

Corrections can be composed by applying multiple correction annotations to the same element:

```kotlin
data class User(
    @field:Trim
    @field:DefaultValue(strValue = "Anonymous")
    val name: String?
)
```

### 2.3. Correction applier implementation

Correction logic is implemented by classes that implement the `CorrectionApplier` interface:

#### 2.3.1. Type-based correction applier selection

When a correction annotation is applied to a property, the correction engine selects the appropriate `CorrectionApplier` implementation based on **type matching**. The selection process works as follows:

**Example of type-based selection:**

```kotlin
@Correction(correctedBy = [
    StringDefaultValueCorrectionApplier::class, // CorrectionApplier<DefaultValue, String>
    IntDefaultValueCorrectionApplier::class, // CorrectionApplier<DefaultValue, Int>
    EnumDefaultValueCorrectionApplier::class // CorrectionApplier<DefaultValue, Enum<*>>
])
annotation class DefaultValue(...)

data class User(
    @field:DefaultValue(strValue = "Anonymous")
    val name: String?,              // → StringDefaultValueCorrectionApplier selected

    @field:DefaultValue(intValue = 18)
    val age: Int?,                  // → IntDefaultValueCorrectionApplier selected
    
    @field:DefaultValue(enumValueClass = Status::class, enumValueName = "ACTIVE")
    val status: Status?             // → EnumDefaultValueCorrectionApplier selected
)
```

This type-based selection ensures **type safety** and allows a single correction annotation to support multiple data types through specialized applier implementations.

```kotlin
interface CorrectionApplier<A : Annotation, T> {
    fun initialize(correctionAnnotation: A) {
        // Default implementation does nothing
    }

    fun correct(value: T?, context: CorrectionApplierContext?): CorrectionApplierResult<T>
}
```

#### 2.3.2. Examples

```kotlin
class StringDefaultValueCorrectionApplier : CorrectionApplier<DefaultValue, String> {
    private lateinit var annotation: DefaultValue

    override fun initialize(correctionAnnotation: DefaultValue) {
        annotation = correctionAnnotation
    }

    override fun correct(value: String?, context: CorrectionApplierContext?): CorrectionApplierResult<String> {
        return if (value.isNullOrBlank() && annotation.strValue.isNotEmpty()) {
            CorrectionApplierResult.Edited(
                oldValue = value,
                newValue = annotation.strValue
            )
        } else {
            CorrectionApplierResult.NoChange()
        }
    }
}
```

---

## 3. Correction declaration and application process

### 3.1. Requirements on classes to be corrected

Classes to be corrected must follow standard JavaBean conventions:
- Public default constructor (for object creation during correction)
- Accessible properties (public fields or getter/setter methods)

### 3.2. Correction declaration

Corrections are declared using correction annotations on:
- **Fields**: `@field:DefaultValue(strValue = "default")`
- **Properties**: Applied to getter methods
- **Types**: Applied to class declarations
- **Container elements**: Applied to collection/map elements

### 3.3. Inheritance (interface and superclass)

Correction declarations are inherited from:
- Superclasses
- Implemented interfaces

Subclass declarations override superclass declarations for the same property.

### 3.4. Group and group sequence

#### 3.4.1. Group inheritance

Groups are inherited following the same rules as Jakarta Bean Validation:
- Interface groups are inherited by implementing classes
- Superclass groups are inherited by subclasses

#### 3.4.2. Group sequence

Groups can be ordered using `@GroupSequence`:

```kotlin
@GroupSequence(BasicGroup::class, AdvancedGroup::class)
interface ValidationSequence
```

### 3.5. Nested object corrections

The `@CorrectNested` annotation enables automatic traversal and correction of nested objects and their properties.

#### 3.5.1. @CorrectNested annotation

The `@CorrectNested` annotation instructs the correction engine to:
1. **Traverse** into the nested object or each element in collection
2. **Apply corrections** to nested properties based on their annotations
3. **Maintain object relationships** during the correction process

#### 3.5.2. Nested object correction

For single nested objects, `@CorrectNested` enables correction of the nested object's properties:

```kotlin
data class Company(
    @field:Trim
    val name: String?,

    @field:CorrectNested
    val headquarters: Office?  // Office properties will be corrected
)

data class Office(
    @field:Trim
    @field:DefaultValue(strValue = "Unknown Location")
    val address: String?,
    
    @field:DefaultValue(intValue = 2000)
    val establishedYear: Int?
)
```

#### 3.5.3. Collection corrections

`@CorrectNested` works with all supported container types, applying corrections to each element:

```kotlin
data class Department(
    @field:CorrectNested
    val employees: List<Employee>,  // Each Employee will be corrected

    @field:CorrectNested
    val assets: Map<String, Asset>  // Each Asset will be corrected
)
```

#### 3.5.4. Nested correction with groups

Group conversion can be applied during nested correction:

```kotlin
data class Company(
    @field:CorrectNested
    @field:ConvertGroup(from = Default::class, to = CompanyValidation::class)
    val branches: List<Branch>
)
```

### 3.6. Container element corrections

Container element corrections target elements within lists, maps, and arrays rather than the container itself.

#### 3.6.1. Supported container types

The library supports three container types:

| Container Type | Java/Kotlin Types | Description |
|---|---|---|
| **LIST** | `List<T>`, `ArrayList<T>`, `LinkedList<T>`, etc. | Ordered collections with indexed access |
| **MAP** | `Map<K,V>`, `HashMap<K,V>`, `LinkedHashMap<K,V>`, etc. | Key-value pairs with key-based access |
| **ARRAY** | `Array<T>`, `IntArray`, `StringArray`, etc. | Fixed-size arrays including primitive arrays |

#### 3.6.2. Container type detection

Container types are detected using the following rules:

**Examples of supported types:**
- **Lists**: `List<String>`, `ArrayList<User>`, `MutableList<Int>`
- **Maps**: `Map<String, User>`, `HashMap<Long, Order>`, `MutableMap<String, String>`
- **Arrays**: `Array<String>`, `IntArray`, `Array<User>`

#### 3.6.3. Container element correction syntax

Use `CorrectionTarget.CONTAINER_ELEMENT` to correct elements within containers:

```kotlin
data class ContactInfo(
    // Correct each phone number string in the list
    @field:Trim(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT)
    @field:RegexReplace(
        regexPattern = "[^0-9+()-]",
        replaceStr = "",
        correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    val phoneNumbers: List<String>,

    // Correct each email address in the map values
    @field:Trim(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT)
    val emailAddresses: Map<String, String>,

    // Correct each name in the array
    @field:DefaultValue(
        strValue = "Unknown",
        correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    val names: Array<String>
)
```

#### 3.6.4. Container element access patterns

The correction engine uses different access patterns for each container type:

#### 3.6.5. Combining container corrections with nested corrections

Container element corrections and nested corrections can be combined:

```kotlin
data class Organization(
    // Apply @CorrectNested to traverse into each Employee object
    // AND apply container element corrections to the list structure
    @field:CorrectNested
    @field:CustomEmployeeCorrection(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT, groups = [SpecialGroup::class])
    val employees: List<Employee>
)

data class Employee(
    @field:Trim
    @field:DefaultValue(strValue = "Unknown")
    val name: String?
)
```

**Correction order:**
1. **Nested object corrections** are applied first via `@CorrectNested`
2. **Container element corrections** are applied second

#### 3.6.6. Complete example: Nested and container corrections

Here's a comprehensive example demonstrating all correction types:

```kotlin
data class Company(
    @field:Trim
    @field:DefaultValue(strValue = "Unnamed Company")
    val name: String?,

    // Nested object correction
    @field:CorrectNested
    val headquarters: Office?,
    
    // Nested collection with object corrections
    @field:CorrectNested
    val branches: List<Branch>,

    // Container element corrections on phone numbers
    @field:Trim(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT)
    @field:RegexReplace(
        regexPattern = "[^0-9+()-]",
        replaceStr = "",
        correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    val phoneNumbers: MutableList<String>,

    // Mixed: nested objects in map with container element corrections
    @field:CorrectNested
    @field:DefaultValue(
        strValue = "info@company.com",
        correctionTarget = CorrectionTarget.CONTAINER_ELEMENT
    )
    val departmentContacts: MutableMap<String, Contact>
)

data class Office(
    @field:Trim
    @field:DefaultValue(strValue = "Unknown Location")
    val address: String?,

    @field:DefaultValue(intValue = 2000)
    val establishedYear: Int?
)

data class Branch(
    @field:Trim
    val name: String?,

    @field:CorrectNested
    val employees: List<Employee>
)

data class Employee(
    @field:Trim
    @field:DefaultValue(strValue = "Unknown Employee")
    val name: String?,

    @field:DefaultValue(intValue = 18)
    val age: Int?
)

data class Contact(
    @field:Trim
    @field:DefaultValue(strValue = "Unknown")
    val name: String?,

    @field:Trim(correctionTarget = CorrectionTarget.CONTAINER_ELEMENT)
    val emails: MutableList<String>
)

// Usage example
val company = Company(
    name = "  ",  // Will be corrected to "Unnamed Company"
    headquarters = Office(
        address = "  123 Main St  ",  // Will be trimmed
        establishedYear = null        // Will be set to 2000
    ),
    branches = listOf(
        Branch(
            name = "  Branch 1  ",    // Will be trimmed
            employees = listOf(
                Employee(name = null, age = null)  // Will get defaults
            )
        )
    ),
    phoneNumbers = mutableListOf(
        "  +1-555-123-4567!!  ",     // Will be trimmed and cleaned
        "555.987.6543"               // Will be cleaned
    ),
    departmentContacts = mutableMapOf(
        "sales" to Contact(
            name = "  John Doe  ",    // Will be trimmed
            emails = mutableListOf("  john@company.com  ")  // Will be trimmed
        )
    )
)

val corrector = CorrectorFactory.buildCorrector()
when (val result = corrector.correct(company)) {
    is CorrectionResult.Success -> {
        val correctedUser = result.correctedObject
        val appliedCorrections = result.appliedCorrections
        // Result will have all corrections applied:
        // - company.name = "Unnamed Company"
        // - company.headquarters.address = "123 Main St"
        // - company.headquarters.establishedYear = 2000
        // - company.branches[0].name = "Branch 1"
        // - company.branches[0].employees[0].name = "Unknown Employee"
        // - company.branches[0].employees[0].age = 18
        // - company.phoneNumbers[0] = "+1-555-123-4567"
        // - company.phoneNumbers[1] = "5559876543"
        // - company.departmentContacts["sales"].name = "John Doe"
        // - company.departmentContacts["sales"].emails[0] = "john@company.com"
    }
    is CorrectionResult.Failure -> {
        println("Correction failed: ${result.error.message}")
        result.error.appliedCorrections // Corrections applied before the failure happened
    }
}
```

### 3.7. Correction routine

The correction process follows these steps:

1. **Object traversal**: Navigate the object graph using `@CorrectNested` annotations
2. **Correction discovery**: Find applicable corrections based on annotations and groups
3. **Constraint filtering**: Apply corrections only if specified constraints are violated
4. **Correction application**: Execute correction appliers in order
5. **Result collection**: Gather information about applied corrections

---

## 4. Correction APIs

### 4.1. Corrector API

Function for applying corrections:

```kotlin
fun <T> correct(
  obj: T,
  vararg groups: Class<*>
): CorrectionResult<T, ErrorLike>

fun <T> correct(
  obj: T,
  constraintViolations: Set<ConstraintViolation<T>>,
  vararg groups: Class<*>,
): CorrectionResult<T, ErrorLike>
```

#### 4.1.1. Parameters

- **obj**: The object to be corrected
- **correctViolationsOnly**: If true, only apply corrections for violated constraints
- **constraintViolations**: Existing constraint violations (optional)
- **groups**: Validation groups to consider for correction

### 4.2. Bootstrapping

Create a `Corrector` instance using the factory:

```kotlin
val corrector = CorrectorFactory.buildCorrector()
corrector.correct(...)
```

---

## 5. Built-in Correction definitions

### 5.1. @DefaultValue correction

Sets default values for null fields.

```kotlin
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE)
@Correction(correctedBy = [])
annotation class DefaultValue(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    val strValue: String = "",
    val intValue: Int = 0,
    val longValue: Long = 0L,
    val doubleValue: Double = 0.0,
    val floatValue: Float = 0.0f,
    val booleanValue: Boolean = false,
    val charValue: Char = '\u0000',
    val byteValue: Byte = 0,
    val shortValue: Short = 0,
    val enumValueClass: KClass<out Enum<*>> = Nothing::class,
    val enumValueName: String = ""
)
```

**Supported types**: String, Integer, Long, Double, Float, Boolean, Character, Byte, Short, Enum types

**Example**:
```kotlin
data class User(
    @field:DefaultValue(strValue = "Anonymous")
    val name: String?,

    @field:DefaultValue(intValue = 18)
    val age: Int?,

    @field:DefaultValue(enumValueClass = Status::class, enumValueName = "ACTIVE")
    val status: Status?
)
```

### 5.2. @Trim correction

Removes leading and trailing whitespace from strings.

```kotlin
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE)
@Correction(correctedBy = [])
annotation class Trim(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY
)
```

**Supported types**: String

**Example**:
```kotlin
data class Contact(
    @field:Trim
    val name: String?,

    @field:Trim
    val address: String?
)
```

### 5.3. @Truncate correction

Limits string length by truncating excess characters.

```kotlin
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE)
@Correction(correctedBy = [])
annotation class Truncate(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    val length: Int,
    val fromEnd: Boolean = true
)
```

**Parameters**:
- **length**: Maximum allowed length (must be positive)
- **fromEnd**: If `true`, truncates from end; if `false`, truncates from start. Defaults to `true`

**Supported types**: String

**Example**:
```kotlin
data class Post(
    @field:Truncate(length = 50)
    val title: String?,

    @field:Truncate(length = 200, fromEnd = false)
    val summary: String?
)
```

### 5.4. @RegexReplace correction

Replaces text matching a regex pattern.

```kotlin
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE)
@Correction(correctedBy = [])
annotation class RegexReplace(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val constraintFilter: Array<KClass<*>> = [],
    val correctionTarget: CorrectionTarget = CorrectionTarget.PROPERTY,
    val regexPattern: String,
    val replaceStr: String
)
```

**Parameters**:
- **regexPattern**: The regex pattern to match
- **replaceStr**: The replacement string

**Supported types**: String

**Example**:
```kotlin
data class PhoneNumber(
    @field:RegexReplace(
        regexPattern = "[^0-9+()-]",
        replaceStr = ""
    )
    val number: String?
)
```

---

## 6. Integration

### 6.1. Jakarta Bean Validation integration

The library integrates seamlessly with Jakarta Bean Validation:

```kotlin
data class User(
    @field:Size(min = 3, max = 50)
    @field:Truncate(
        length = 50,
        constraintFilter = [Size::class] // Truncate will apply only if there was a Size constraint violation
    )
    @field:DefaultValue(strValue = "Anonymous")
    val name: String?
)

// Corrections can be applied before or after validation
val corrector = CorrectorFactory.buildCorrector()
val validator = Validation.buildDefaultValidatorFactory().validator

val violations = validator.validate(correctionResult.correctedObject)
val correctionResult = corrector.correct(
    user,
    violations
)
```

### 6.2. Constraint filtering

Corrections can be configured to only apply when specific constraints are violated:

```kotlin
data class User(
    @field:NotNull
    @field:Size(min = 3)
    @field:DefaultValue(
        strValue = "DefaultUser",
        constraintFilter = [NotNull::class]  // Only apply when @NotNull fails
    )
    val username: String?
)
```

**Constraint filtering behavior**:
- If `constraintFilter` is empty, the correction always applies
- If `constraintFilter` is specified, the correction only applies if one of the specified constraints is violated
- Multiple constraint types can be specified in the filter

**Example with multiple constraints**:
```kotlin
@field:DefaultValue(
    intValue = 18,
    constraintFilter = [NotNull::class, Min::class]
)
val age: Int?  // Applies if @NotNull or @Min constraint fails
```

---

## Appendix A: CorrectionTarget enumeration

Note on Correction Target:

```kotlin
data class ContactNumber(
    @field:DefaultValue(strValue = "123456789")
    val value: String?
)

data class User(
    @field:CorrectNested // Ensures correction defined on `ContactNumber` are applied such as ContactNumber.value
    @field:MyCustomContactNumberCorrection(correctionTarget = CorrectionTarget.PROPERTY) // Applies to each element in `contactNumbers` list
    @field:MyCustomListCorrection // Applies to `contactNumbers`
    val contactNumbers: List<ContactNumber?>
)
```

---
