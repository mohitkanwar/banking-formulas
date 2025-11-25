package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator.CompoundingFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class RecurringDepositCalculatorTest {

    @Test
    @DisplayName("Monthly RD maturity matches expected future value (ordinary annuity)")
    void testMonthlyRdMaturityOrdinaryAnnuity() {
        BigDecimal installment = BigDecimal.valueOf(1000);
        BigDecimal annualRate = BigDecimal.valueOf(12);  // 12% p.a.
        int months = 12;

        BigDecimal maturity = RecurringDepositCalculator.calculateMonthlyRdMaturity(
                installment, annualRate, months);

        // Manually compute expected using double math for comparison:
        // r_monthly = 0.12 / 12 = 0.01
        // FV = I * ((1+r)^n - 1) / r
        double I = 1000.0;
        double r = 0.12 / 12.0;
        int n = 12;
        double factor = Math.pow(1 + r, n);
        double fvExpected = I * (factor - 1) / r;

        BigDecimal expected = BigDecimal.valueOf(fvExpected).setScale(2, RoundingMode.HALF_UP);

        assertEquals(expected, maturity,
                "Maturity should match future value of ordinary annuity for monthly RD");
    }

    @Test
    @DisplayName("RD with installments at the beginning (annuity due) yields higher maturity")
    void testAnnuityDueHigherMaturity() {
        BigDecimal installment = BigDecimal.valueOf(2000);
        BigDecimal annualRate = BigDecimal.valueOf(10);
        int periods = 24;

        BigDecimal ordinaryMaturity = RecurringDepositCalculator.calculateMaturityAmount(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false // end of period
        );

        BigDecimal dueMaturity = RecurringDepositCalculator.calculateMaturityAmount(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                true // beginning of period
        );

        assertTrue(dueMaturity.compareTo(ordinaryMaturity) > 0,
                "Annuity due maturity must be greater than ordinary annuity maturity");
    }

    @Test
    @DisplayName("Zero rate yields maturity equal to total contributions")
    void testZeroRateCase() {
        BigDecimal installment = BigDecimal.valueOf(1500);
        BigDecimal annualRate = BigDecimal.ZERO;
        int periods = 10;

        BigDecimal maturity = RecurringDepositCalculator.calculateMaturityAmount(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal expected = installment
                .multiply(BigDecimal.valueOf(periods))
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(expected, maturity);
    }

    @Test
    @DisplayName("Interest = maturity - total contributions")
    void testInterestCalculation() {
        BigDecimal installment = BigDecimal.valueOf(2500);
        BigDecimal annualRate = BigDecimal.valueOf(9);
        int periods = 24;

        BigDecimal maturity = RecurringDepositCalculator.calculateMaturityAmount(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal interest = RecurringDepositCalculator.calculateInterest(
                installment,
                annualRate,
                periods,
                CompoundingFrequency.MONTHLY,
                false
        );

        BigDecimal totalContribution = installment.multiply(BigDecimal.valueOf(periods));

        assertEquals(
                maturity.subtract(totalContribution).setScale(2, RoundingMode.HALF_UP),
                interest
        );
    }

    @Test
    @DisplayName("Zero installment or zero periods -> zero maturity")
    void testZeroInstallmentOrPeriods() {
        BigDecimal annualRate = BigDecimal.valueOf(8);

        BigDecimal maturityZeroInstallment = RecurringDepositCalculator.calculateMaturityAmount(
                BigDecimal.ZERO,
                annualRate,
                12,
                CompoundingFrequency.MONTHLY,
                false
        );
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), maturityZeroInstallment);

        BigDecimal maturityZeroPeriods = RecurringDepositCalculator.calculateMaturityAmount(
                BigDecimal.valueOf(1000),
                annualRate,
                0,
                CompoundingFrequency.MONTHLY,
                false
        );
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), maturityZeroPeriods);
    }

    @Test
    @DisplayName("Invalid inputs throw IllegalArgumentException")
    void testInvalidInputs() {
        BigDecimal annualRate = BigDecimal.valueOf(8);

        assertThrows(IllegalArgumentException.class,
                () -> RecurringDepositCalculator.calculateMaturityAmount(
                        BigDecimal.valueOf(-1),
                        annualRate,
                        12,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> RecurringDepositCalculator.calculateMaturityAmount(
                        BigDecimal.valueOf(1000),
                        null,
                        12,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> RecurringDepositCalculator.calculateMaturityAmount(
                        BigDecimal.valueOf(1000),
                        annualRate,
                        -1,
                        CompoundingFrequency.MONTHLY,
                        false));

        assertThrows(IllegalArgumentException.class,
                () -> RecurringDepositCalculator.calculateMaturityAmount(
                        BigDecimal.valueOf(1000),
                        annualRate,
                        12,
                        null,
                        false));
    }
}
