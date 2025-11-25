package com.mohitkanwar.solutions.bankingformulas.interest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator.CompoundingFrequency;
import static org.junit.jupiter.api.Assertions.*;

class CompoundInterestCalculatorTest {

    @Test
    @DisplayName("Annual compounding matches standard formula")
    void testAnnualCompounding() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        BigDecimal annualRate = BigDecimal.valueOf(10);  // 10%
        BigDecimal timeInYears = BigDecimal.valueOf(2);  // 2 years

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, timeInYears, CompoundingFrequency.ANNUAL);

        // 100000 * (1 + 0.10)^2 = 121000
        assertEquals(new BigDecimal("121000.00"), amount);

        BigDecimal interest = CompoundInterestCalculator.calculateInterest(
                principal, annualRate, timeInYears, CompoundingFrequency.ANNUAL);

        assertEquals(new BigDecimal("21000.00"), interest);
    }

    @Test
    @DisplayName("Quarterly compounding example (12% for 1 year)")
    void testQuarterlyCompounding() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        BigDecimal annualRate = BigDecimal.valueOf(12);    // 12%
        BigDecimal timeInYears = BigDecimal.ONE;           // 1 year

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, timeInYears, CompoundingFrequency.QUARTERLY);

        // Expected: 100000 * (1 + 0.12/4)^4 ≈ 112550.88
        assertEquals(new BigDecimal("112550.88"), amount);
    }

    @Test
    @DisplayName("Zero principal or zero time yields no growth")
    void testZeroPrincipalOrTime() {
        BigDecimal annualRate = BigDecimal.valueOf(10);

        BigDecimal amountZeroPrincipal = CompoundInterestCalculator.calculateAmount(
                BigDecimal.ZERO, annualRate, BigDecimal.valueOf(5), CompoundingFrequency.ANNUAL);
        assertEquals(new BigDecimal("0.00"), amountZeroPrincipal);

        BigDecimal amountZeroTime = CompoundInterestCalculator.calculateAmount(
                BigDecimal.valueOf(50_000), annualRate, BigDecimal.ZERO, CompoundingFrequency.ANNUAL);
        assertEquals(new BigDecimal("50000.00"), amountZeroTime);
    }

    @Test
    @DisplayName("Non-integer total periods (time * frequency) throws IllegalArgumentException")
    void testNonIntegerPeriods() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        BigDecimal annualRate = BigDecimal.valueOf(10);
        BigDecimal timeInYears = new BigDecimal("1.3"); // 1.3 years with quarterly compounding -> 5.2 periods

        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        principal, annualRate, timeInYears, CompoundingFrequency.QUARTERLY));
    }

    @Test
    @DisplayName("Negative or null inputs throw IllegalArgumentException")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        BigDecimal.valueOf(-1),
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        BigDecimal.valueOf(-1),
                        CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        null,
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        BigDecimal.ONE,
                        null,
                        BigDecimal.ONE,
                        CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        null,
                        CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> CompoundInterestCalculator.calculateAmount(
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        null));
    }
}
