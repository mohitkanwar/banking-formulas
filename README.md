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
  <version>0.2.0</version> <!-- Check the latest version -->
</dependency>
```

### Gradle

```kotlin
implementation("com.mohitkanwar.solutions.bankingformulas:banking-formulas:0.2.0")
```


### Features
This lib contains the different types of formulas used in the banking industry.

#### Deposits Calculators
These calculators ease down the different calculations related to deposits. The deposit product allows customers to get a bit of higher rate of interest as compared to the savings accounts.
The different types of deposit products are
* Fixed Deposits
* Recurring Deposits

##### Fixed Deposit Calculator

When dealing with Fixed Deposits, the customers like to know or visualize the interests acured or the maturity amount after specific period.
To ease these calculations we have following methods : 

Usage - Calculate Maturity Amount

```java
import java.math.BigDecimal;
import java.util.Date;

FixedDepositCalculator fdCalc = new FixedDepositCalculator();
BigDecimal fdAmount;
Date fdStartDate;
Date fdMaturityDate;
BigDecimal annualRate;
MaturityInstructions maturityInstructions;
BigDecimal maturityAmount = fdCalc.getMaturityAmount(fdAmount, fdStartDate, fdMaturityDate, annualRate, maturityInstructions);

```

##### Goal Savings Calculator

##### Recurring Deposit Calculator

##### SIP Calculator

#### Interest Calculators

##### Compound Interest Calculator
##### Rate Converter
##### Simple Interest Calculator

#### Loans Calculators
##### Amortization Schedule Calculator
##### Loan EMI Calculator
##### Prepayment Calculator


### Versioning
If the version starts with 0, e.g. 0.1.1, the lib is still in beta and should be used at your own discretion until fully tested.


Follows semantic versioning: MAJOR.MINOR.PATCH

- MAJOR: incompatible API changes.
- MINOR: added functionality in a backwards-compatible manner (upto one older version)
- PATCH: backwards-compatible bug fixes and small improvements.

Example: `1.2.3` → Major = 1, Minor = 2, Patch = 3

### License

Apache 2.0



