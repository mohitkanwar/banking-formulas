package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator;
import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FixedDepositCalculator – Maturity Amount Calculations")
class FixedDepositCalculatorTest {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final FixedDepositCalculator calculator = new FixedDepositCalculator();

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    @DisplayName("Monthly compounding: 12 calendar months should be treated as 1 year")
    void monthlyCompounding_OneYearTenor_MapsToTwelveMonths() {
        // Given
        BigDecimal principal = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("12.0"); // 12% p.a.

        LocalDate startLocal = LocalDate.of(2025, 1, 1);
        LocalDate endLocal = LocalDate.of(2026, 1, 1); // exactly 12 calendar months later

        Date startDate = toDate(startLocal);
        Date maturityDate = toDate(endLocal);

        // Expected: monthsBetween = 12 -> years = 12 / 12 = 1 year
        BigDecimal years = BigDecimal.valueOf(12L)
                .divide(BigDecimal.valueOf(12L), 10, ROUNDING);

        BigDecimal expected = CompoundInterestCalculator
                .calculateAmount(principal, annualRate, years, CompoundingFrequency.MONTHLY)
                .setScale(2, ROUNDING);

        // When
        BigDecimal actual = calculator.getMaturityAmount(
                principal,
                startDate,
                maturityDate,
                annualRate,
                MaturityInstructions.ON_MATURITY_MONTHLY_COMPOUNDING
        );

        // Then
        assertEquals(
                expected,
                actual,
                "Monthly compounding for exactly 12 calendar months should match 1-year compound interest"
        );
    }

    @Test
    @DisplayName("Daily compounding: same start and maturity date should accrue zero interest")
    void dailyCompounding_SameStartAndEndDate_NoInterest() {
        // Given
        BigDecimal principal = new BigDecimal("50000");
        BigDecimal annualRate = new BigDecimal("7.5");

        LocalDate date = LocalDate.of(2025, 1, 1);
        Date startDate = toDate(date);
        Date maturityDate = toDate(date); // same day

        // Expected: 0 days -> 0 years
        BigDecimal years = BigDecimal.ZERO;

        BigDecimal expected = CompoundInterestCalculator
                .calculateAmount(principal, annualRate, years, CompoundingFrequency.DAILY)
                .setScale(2, ROUNDING);

        // When
        BigDecimal actual = calculator.getMaturityAmount(
                principal,
                startDate,
                maturityDate,
                annualRate,
                MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
        );

        // Then
        assertEquals(
                expected,
                actual,
                "For same start and maturity date, daily compounding should not add any interest"
        );
        assertEquals(
                principal.setScale(2, ROUNDING),
                actual,
                "Principal must remain unchanged for 0-day FD"
        );
    }



    @Test
    @DisplayName("Validation: FD amount must be positive (> 0)")
    void invalidAmount_ZeroOrNegative_ThrowsException() {
        Date start = toDate(LocalDate.of(2025, 1, 1));
        Date end = toDate(LocalDate.of(2025, 1, 10));
        BigDecimal rate = new BigDecimal("7.0");

        // Zero amount
        IllegalArgumentException zeroEx = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.getMaturityAmount(
                        BigDecimal.ZERO,
                        start,
                        end,
                        rate,
                        MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
                ),
                "Expected getMaturityAmount to throw for zero FD amount"
        );
        assertTrue(
                zeroEx.getMessage().contains("FD Amount"),
                "Exception message for zero amount should mention 'FD Amount'"
        );

        // Negative amount
        IllegalArgumentException negativeEx = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.getMaturityAmount(
                        new BigDecimal("-1000"),
                        start,
                        end,
                        rate,
                        MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
                ),
                "Expected getMaturityAmount to throw for negative FD amount"
        );
        assertTrue(
                negativeEx.getMessage().contains("FD Amount"),
                "Exception message for negative amount should mention 'FD Amount'"
        );
    }

    @Test
    @DisplayName("Validation: FD start date must not be null")
    void nullStartDate_ThrowsException() {
        Date end = toDate(LocalDate.of(2025, 1, 10));
        BigDecimal principal = new BigDecimal("10000");
        BigDecimal rate = new BigDecimal("7.0");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.getMaturityAmount(
                        principal,
                        null,
                        end,
                        rate,
                        MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
                ),
                "Expected getMaturityAmount to throw when FD start date is null"
        );
        assertTrue(
                ex.getMessage().contains("FD start date"),
                "Exception message should clearly mention 'FD start date'"
        );
    }

    @Test
    @DisplayName("Validation: FD maturity date must not be null")
    void nullMaturityDate_ThrowsException() {
        Date start = toDate(LocalDate.of(2025, 1, 10));
        BigDecimal principal = new BigDecimal("10000");
        BigDecimal rate = new BigDecimal("7.0");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.getMaturityAmount(
                        principal,
                        start,
                        null,
                        rate,
                        MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
                ),
                "Expected getMaturityAmount to throw when FD maturity date is null"
        );
        assertTrue(
                ex.getMessage().contains("FD maturity date"),
                "Exception message should clearly mention 'FD maturity date'"
        );
    }

    @Test
    @DisplayName("Validation: FD maturity date must not be before start date")
    void maturityBeforeStart_ThrowsException() {
        Date start = toDate(LocalDate.of(2025, 1, 10));
        Date end = toDate(LocalDate.of(2025, 1, 5)); // before start
        BigDecimal principal = new BigDecimal("10000");
        BigDecimal rate = new BigDecimal("7.0");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.getMaturityAmount(
                        principal,
                        start,
                        end,
                        rate,
                        MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
                ),
                "Expected getMaturityAmount to throw when maturity date is before start date"
        );
        assertTrue(
                ex.getMessage().contains("must not come before"),
                "Exception message should indicate that maturity date must not come before start date"
        );
    }

    @Test
    @DisplayName("Validation: annual interest rate must not be negative")
    void negativeAnnualRate_ThrowsException() {
        Date start = toDate(LocalDate.of(2025, 1, 1));
        Date end = toDate(LocalDate.of(2025, 1, 10));
        BigDecimal principal = new BigDecimal("10000");
        BigDecimal rate = new BigDecimal("-1.0");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.getMaturityAmount(
                        principal,
                        start,
                        end,
                        rate,
                        MaturityInstructions.ON_MATURITY_DAILY_COMPOUNDING
                ),
                "Expected getMaturityAmount to throw for negative annual rate"
        );
        assertTrue(
            ex.getMessage().contains("annualRate"),
            "Exception message should clearly mention 'annualRate'"
        );
    }
}
