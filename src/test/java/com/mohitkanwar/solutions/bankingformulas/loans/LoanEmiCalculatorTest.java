package com.mohitkanwar.solutions.bankingformulas.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link LoanEmiCalculator}.
 *
 * Covers:
 *  - Standard EMI calculation
 *  - Zero-interest loans
 *  - Boundary / edge inputs
 *  - Behavioural properties (rate / tenure changes)
 *  - Input validation
 */
class LoanEmiCalculatorTest {

    @Test
    @DisplayName("EMI for ₹5,00,000 @ 8.5% for 240 months is 4339.12")
    void testStandardEmiCalculation() {
        BigDecimal emi = LoanEmiCalculator.calculateEmi(
                BigDecimal.valueOf(500_000),
                BigDecimal.valueOf(8.5),
                240
        );

        assertEquals(new BigDecimal("4339.12"), emi, "Incorrect EMI for standard case");
    }

    @Test
    @DisplayName("Zero interest: EMI equals principal divided by tenure")
    void testZeroInterestLoan() {
        BigDecimal principal = BigDecimal.valueOf(120_000);
        int tenureInMonths = 12;

        BigDecimal emi = LoanEmiCalculator.calculateEmi(
                principal,
                BigDecimal.ZERO,
                tenureInMonths
        );

        BigDecimal expected = principal
                .divide(BigDecimal.valueOf(tenureInMonths), 2, RoundingMode.HALF_UP);

        assertEquals(expected, emi, "EMI should be simple principal/tenure for zero-rate loan");
    }

    @Test
    @DisplayName("One-month loan: EMI equals principal * (1 + monthlyRate)")
    void testSingleMonthLoan() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        BigDecimal annualRate = BigDecimal.valueOf(12); // 12% p.a. -> 1% per month

        BigDecimal emi = LoanEmiCalculator.calculateEmi(principal, annualRate, 1);

        // For n=1, EMI formula collapses to P * (1 + r_monthly)
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12 * 100L), 20, RoundingMode.HALF_UP); // 0.01
        BigDecimal expected = principal
                .multiply(BigDecimal.ONE.add(monthlyRate))
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(expected, emi, "One-month loan EMI should be P * (1 + r_monthly)");
    }

    @Test
    @DisplayName("Zero principal: EMI is zero regardless of rate/tenure")
    void testZeroPrincipal() {
        BigDecimal emi = LoanEmiCalculator.calculateEmi(
                BigDecimal.ZERO,
                BigDecimal.valueOf(10),
                36
        );

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), emi);
    }

    @Test
    @DisplayName("Higher rate produces higher EMI (same principal & tenure)")
    void testHigherRateProducesHigherEmi() {
        BigDecimal principal = BigDecimal.valueOf(500_000);
        int tenure = 240;

        BigDecimal emiAt8_5 = LoanEmiCalculator.calculateEmi(
                principal, BigDecimal.valueOf(8.5), tenure);
        BigDecimal emiAt10 = LoanEmiCalculator.calculateEmi(
                principal, BigDecimal.valueOf(10), tenure);

        assertTrue(emiAt10.compareTo(emiAt8_5) > 0,
                () -> "EMI at 10% (" + emiAt10 + ") should be greater than EMI at 8.5% (" + emiAt8_5 + ")");
    }

    @Test
    @DisplayName("Longer tenure produces lower EMI (same principal & rate)")
    void testLongerTenureProducesLowerEmi() {
        BigDecimal principal = BigDecimal.valueOf(500_000);
        BigDecimal rate = BigDecimal.valueOf(8.5);

        BigDecimal emiAt10Years = LoanEmiCalculator.calculateEmi(principal, rate, 120);
        BigDecimal emiAt20Years = LoanEmiCalculator.calculateEmi(principal, rate, 240);

        assertTrue(emiAt10Years.compareTo(emiAt20Years) > 0,
                () -> "EMI at 10 years (" + emiAt10Years + ") should be greater than EMI at 20 years (" + emiAt20Years + ")");
    }

    @Test
    @DisplayName("Negative interest rate leads to EMI less than zero-interest EMI (mathematically)")
    void testNegativeInterestRateBehaviour() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        int tenure = 12;

        BigDecimal emiZeroRate = LoanEmiCalculator.calculateEmi(principal, BigDecimal.ZERO, tenure);
        BigDecimal emiNegativeRate = LoanEmiCalculator.calculateEmi(principal, BigDecimal.valueOf(-1), tenure);

        assertTrue(emiNegativeRate.compareTo(emiZeroRate) < 0,
                () -> "EMI at -1% (" + emiNegativeRate + ") should be less than EMI at 0% (" + emiZeroRate + ")");
    }

    @Nested
    @DisplayName("Input validation")
    class InputValidationTests {

        @ParameterizedTest(name = "principal={0}, annualRate={1}, tenure={2} should throw IllegalArgumentException")
        @CsvSource(value = {
                "null, 8.5, 240",
                "500000, null, 240",
                "-1, 8.5, 240",
                "500000, 8.5, 0",
                "500000, 8.5, -12"
        }, nullValues = "null")
        @DisplayName("Invalid inputs throw IllegalArgumentException")
        void testInvalidInputs(BigDecimal principal, BigDecimal annualRate, int tenure) {
            assertThrows(IllegalArgumentException.class,
                    () -> LoanEmiCalculator.calculateEmi(principal, annualRate, tenure));
        }
    }
}
