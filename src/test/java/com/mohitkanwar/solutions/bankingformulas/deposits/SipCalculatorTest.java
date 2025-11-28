package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class SipCalculatorTest {

    @Test
    @DisplayName("Monthly SIP future value matches double-based annuity formula (ordinary)")
    void testMonthlySipFutureValue() {
        BigDecimal installment = BigDecimal.valueOf(1000);
        BigDecimal annualRate = BigDecimal.valueOf(12);  // 12% expected return
        int months = 12;

        BigDecimal fv = SipCalculator.calculateMonthlySipFutureValue(
                installment, annualRate, months);

        // Manual double-based check:
        double I = 1000.0;
        double r = 0.12 / 12.0;
        int n = 12;
        double factor = Math.pow(1 + r, n);
        double fvExpected = I * (factor - 1) / r;

        BigDecimal expected = BigDecimal.valueOf(fvExpected)
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(expected, fv);
    }

    @Test
    @DisplayName("Installments at beginning (annuity due) yield higher future value")
    void testAnnuityDueHigherFutureValue() {
        BigDecimal installment = BigDecimal.valueOf(5000);
        BigDecimal annualRate = BigDecimal.valueOf(10);
        int periods = 60; // 5 years

        BigDecimal ordinary = SipCalculator.calculateFutureValue(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal due = SipCalculator.calculateFutureValue(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                true
        );

        assertTrue(due.compareTo(ordinary) > 0,
                "SIP at beginning of period should grow to a higher value");
    }

    @Test
    @DisplayName("Zero rate -> future value equals total contributions")
    void testZeroRate() {
        BigDecimal installment = BigDecimal.valueOf(2500);
        BigDecimal rate = BigDecimal.ZERO;
        int periods = 24;

        BigDecimal fv = SipCalculator.calculateFutureValue(
                installment,
                rate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal totalContribution = installment
                .multiply(BigDecimal.valueOf(periods))
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(totalContribution, fv);
    }

    @Test
    @DisplayName("Wealth gain = future value - total contributions")
    void testWealthGain() {
        BigDecimal installment = BigDecimal.valueOf(3000);
        BigDecimal annualRate = BigDecimal.valueOf(12);
        int periods = 36;

        BigDecimal gain = SipCalculator.calculateWealthGain(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal fv = SipCalculator.calculateFutureValue(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal totalContribution = installment
                .multiply(BigDecimal.valueOf(periods));

        BigDecimal expectedGain = fv
                .subtract(totalContribution)
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(expectedGain, gain);
        assertTrue(gain.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Zero installment or zero periods -> zero future value")
    void testZeroInstallmentOrPeriods() {
        BigDecimal rate = BigDecimal.valueOf(10);

        BigDecimal fvZeroInstallment = SipCalculator.calculateFutureValue(
                BigDecimal.ZERO,
                rate,
                12,
                CompoundingFrequency.MONTHLY,
                false
        );
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), fvZeroInstallment);

        BigDecimal fvZeroPeriods = SipCalculator.calculateFutureValue(
                BigDecimal.valueOf(1000),
                rate,
                0,
                CompoundingFrequency.MONTHLY,
                false
        );
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), fvZeroPeriods);
    }

    @Test
    @DisplayName("Invalid inputs throw IllegalArgumentException")
    void testInvalidInputs() {
        BigDecimal rate = BigDecimal.valueOf(10);

        assertThrows(IllegalArgumentException.class,
                () -> SipCalculator.calculateFutureValue(
                        BigDecimal.valueOf(-1),
                        rate,
                        12,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> SipCalculator.calculateFutureValue(
                        BigDecimal.valueOf(1000),
                        null,
                        12,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> SipCalculator.calculateFutureValue(
                        BigDecimal.valueOf(1000),
                        rate,
                        12,
                        null,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> SipCalculator.calculateFutureValue(
                        BigDecimal.valueOf(1000),
                        rate,
                        -1,
                        CompoundingFrequency.MONTHLY,
                        false));
    }
}
