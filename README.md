# banking-formulas

Reusable Java library for common banking formulas and calculators:
- Loan EMI
- Amortization schedules
- FD/RD maturity
- Eligibility helpers, and more.

## Installation

### Maven

```xml
<dependency>
  <groupId>com.mohitkanwar.solutions.bankingformulas</groupId>
  <artifactId>banking-formulas</artifactId>
  <version>0.0.1</version>
</dependency>
```

### Gradle

```kotlin
implementation("com.mohitkanwar.solutions.bankingformulas:banking-formulas:0.0.1")
```

### Usage

```json

BigDecimal emi = LoanEmiCalculator.calculateEmi(
    BigDecimal.valueOf(500_000),
    BigDecimal.valueOf(8.5),
    240
);
System.out.println("EMI = " + emi); // 4339.12
```

### Features

✅ Precise EMI calculation with BigDecimal

🚧 More calculators coming soon: FD, RD, amortization schedules

### Versioning

Follows semantic versioning: MAJOR.MINOR.PATCH

### License

Apache 2.0



