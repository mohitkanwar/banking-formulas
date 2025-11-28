package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GoalSavingsCalculatorTest {

    @Test
    @DisplayName("Monthly goal calculation matches reverse annuity formula")
    void testMonthlyGoal() {
        // goal: 1 lakh in 12 months @ 12%
        BigDecimal target = BigDecimal.valueOf(100_000);
        BigDecimal annualRate = BigDecimal.valueOf(12);
        int months = 12;

        BigDecimal installment = GoalSavingsCalculator.calculateInstallment(
                target, annualRate, months, CompoundingFrequency.MONTHLY, false);

        // manually computed using double math
        double FV = 100_000;
        double r = 0.12 / 12.0;
        int n = 12;
        double expected = FV * r / (Math.pow(1 + r, n) - 1);

        BigDecimal expectedBD = BigDecimal.valueOf(expected).setScale(2, BigDecimal.ROUND_HALF_UP);

        assertEquals(expectedBD, installment);
    }

    @Test
    @DisplayName("Installment at beginning (annuity due) requires LOWER installment")
    void testAnnuityDueLowerInstallment() {
        BigDecimal target = BigDecimal.valueOf(200_000);
        BigDecimal rate = BigDecimal.valueOf(10);
        int months = 24;

        BigDecimal ordinary = GoalSavingsCalculator.calculateInstallment(
                target, rate, months, CompoundingFrequency.MONTHLY, false);

        BigDecimal due = GoalSavingsCalculator.calculateInstallment(
                target, rate, months, CompoundingFrequency.MONTHLY, true);

        assertTrue(due.compareTo(ordinary) < 0,
                "Paying at beginning should require smaller installment");
    }

    @Test
    @DisplayName("Zero rate -> installment = target / periods")
    void testZeroRate() {
        BigDecimal installment = GoalSavingsCalculator.calculateMonthlyGoalInstallment(
                BigDecimal.valueOf(60_000),
                BigDecimal.ZERO,
                12,
                false);

        assertEquals(new BigDecimal("5000.00"), installment);
    }

    @Test
    @DisplayName("Invalid inputs throw IllegalArgumentException")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> GoalSavingsCalculator.calculateInstallment(
                        BigDecimal.valueOf(-1),
                        BigDecimal.TEN,
                        10,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> GoalSavingsCalculator.calculateInstallment(
                        BigDecimal.valueOf(10_000),
                        null,
                        10,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> GoalSavingsCalculator.calculateInstallment(
                        BigDecimal.valueOf(10_000),
                        BigDecimal.TEN,
                        0,
                        CompoundingFrequency.MONTHLY,
                        false));
    }
}
