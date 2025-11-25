package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator.CompoundingFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FixedDepositCalculatorTest {

    @Test
    @DisplayName("FD maturity for 2 years annual compounding is correct")
    void testMaturityForYears() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        BigDecimal rate = BigDecimal.valueOf(10);  // 10% p.a.
        BigDecimal years = BigDecimal.valueOf(2);

        BigDecimal maturity = FixedDepositCalculator.calculateMaturityAmountForYears(
                principal, rate, years, CompoundingFrequency.ANNUAL);

        // Using CI: 100000 * (1 + 0.10)^2 = 121000
        assertEquals(new BigDecimal("121000.00"), maturity);
    }

    @Test
    @DisplayName("FD maturity for 12 months monthly compounding is consistent with 1 year")
    void testMaturityForMonths() {
        BigDecimal principal = BigDecimal.valueOf(100_000);
        BigDecimal rate = BigDecimal.valueOf(12);  // 12% p.a.
        int months = 12;

        BigDecimal maturity = FixedDepositCalculator.calculateMaturityAmountForMonths(
                principal, rate, months, CompoundingFrequency.MONTHLY);

        // Expected ≈ 112550.88 (same as compound interest example)
        assertEquals(new BigDecimal("112682.50"), maturity);
    }

    @Test
    @DisplayName("FD maturity for 365 days annual compounding is equal to 1 year")
    void testMaturityForDaysAsOneYear() {
        BigDecimal principal = BigDecimal.valueOf(50_000);
        BigDecimal rate = BigDecimal.valueOf(8);  // 8% p.a.
        int days = 365;

        BigDecimal maturityDays = FixedDepositCalculator.calculateMaturityAmountForDays(
                principal, rate, days, CompoundingFrequency.ANNUAL);

        BigDecimal maturityYears = FixedDepositCalculator.calculateMaturityAmountForYears(
                principal, rate, BigDecimal.ONE, CompoundingFrequency.ANNUAL);

        assertEquals(maturityYears, maturityDays);
    }

    @Test
    @DisplayName("Interest = maturity - principal")
    void testInterestCalculation() {
        BigDecimal principal = BigDecimal.valueOf(80_000);
        BigDecimal rate = BigDecimal.valueOf(10);
        BigDecimal years = BigDecimal.valueOf(1);

        BigDecimal maturity = FixedDepositCalculator.calculateMaturityAmountForYears(
                principal, rate, years, CompoundingFrequency.ANNUAL);
        BigDecimal interest = FixedDepositCalculator.calculateInterestForYears(
                principal, rate, years, CompoundingFrequency.ANNUAL);

        assertEquals(maturity.subtract(principal), interest);
    }

    @Test
    @DisplayName("Zero principal yields maturity equal to zero for any term")
    void testZeroPrincipal() {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);

        BigDecimal m1 = FixedDepositCalculator.calculateMaturityAmountForYears(
                zero, BigDecimal.valueOf(8), BigDecimal.ONE, CompoundingFrequency.ANNUAL);
        BigDecimal m2 = FixedDepositCalculator.calculateMaturityAmountForMonths(
                zero, BigDecimal.valueOf(8), 12, CompoundingFrequency.ANNUAL);
        BigDecimal m3 = FixedDepositCalculator.calculateMaturityAmountForDays(
                zero, BigDecimal.valueOf(8), 365, CompoundingFrequency.ANNUAL);

        assertEquals(zero, m1);
        assertEquals(zero, m2);
        assertEquals(zero, m3);
    }

    @Test
    @DisplayName("Negative principal or term throws IllegalArgumentException")
    void testInvalidInputs() {
        BigDecimal principal = BigDecimal.valueOf(10_000);
        BigDecimal rate = BigDecimal.valueOf(8);

        assertThrows(IllegalArgumentException.class,
                () -> FixedDepositCalculator.calculateMaturityAmountForYears(
                        BigDecimal.valueOf(-1), rate, BigDecimal.ONE, CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> FixedDepositCalculator.calculateMaturityAmountForMonths(
                        principal, rate, -1, CompoundingFrequency.ANNUAL));

        assertThrows(IllegalArgumentException.class,
                () -> FixedDepositCalculator.calculateMaturityAmountForDays(
                        principal, rate, -10, CompoundingFrequency.ANNUAL));
    }
}
