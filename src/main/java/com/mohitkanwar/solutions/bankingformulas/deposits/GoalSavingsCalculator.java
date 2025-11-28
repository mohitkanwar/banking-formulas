package com.mohitkanwar.solutions.bankingformulas.deposits;


import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculator to determine the required periodic installment
 * to reach a target savings goal using recurring deposits (annuity model).
 *
 * <p>Formula for ordinary annuity (installment at end of period):
 *
 * <pre>
 * FV = I × [ (1 + r)^n - 1 ] / r
 *
 * Solve for I:
 *
 * I = FV × r / [ (1 + r)^n - 1 ]
 * </pre>
 *
 * <p>For annuity due (installment at beginning):
 *
 * <pre>
 * FV = I × [ (1 + r)^n - 1 ] / r × (1 + r)
 *
 * I = FV × r / [ (1 + r)^n - 1 ] × 1 / (1 + r)
 * </pre>
 */
public final class GoalSavingsCalculator {

    private static final int CURRENCY_SCALE = 2;
    private static final int INTERMEDIATE_SCALE = 20;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext MC = MathContext.DECIMAL64;

    private GoalSavingsCalculator() {}

    /**
     * Calculates the periodic installment required to reach the target amount.
     *
     * @param targetAmount target maturity amount (≥ 0, non-null)
     * @param annualRatePercent annual interest rate in percent (non-null)
     * @param numberOfInstallments total number of installments (≥ 1)
     * @param frequency compounding/instalment frequency (non-null)
     * @param installmentAtBeginning if true, installment is at beginning (annuity due)
     * @return required installment amount rounded to 2 decimal places
     */
    public static BigDecimal calculateInstallment(BigDecimal targetAmount,
                                                  BigDecimal annualRatePercent,
                                                  int numberOfInstallments,
                                                  CompoundingFrequency frequency,
                                                  boolean installmentAtBeginning) {

        validateInputs(targetAmount, annualRatePercent, numberOfInstallments, frequency);

        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING);
        }

        int m = frequency.getPeriodsPerYear();

        // periodic interest rate
        BigDecimal periodicRate = annualRatePercent
                .divide(BigDecimal.valueOf(100L), INTERMEDIATE_SCALE, ROUNDING)
                .divide(BigDecimal.valueOf(m), INTERMEDIATE_SCALE, ROUNDING);

        // zero-rate case
        if (periodicRate.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal installment = targetAmount
                    .divide(BigDecimal.valueOf(numberOfInstallments),
                            CURRENCY_SCALE, ROUNDING);
            return installment;
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(periodicRate);
        BigDecimal factor = onePlusR.pow(numberOfInstallments, MC);

        BigDecimal numerator = targetAmount.multiply(periodicRate, MC);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE);

        BigDecimal installment = numerator.divide(denominator, INTERMEDIATE_SCALE, ROUNDING);

        if (installmentAtBeginning) {
            // I_due = I_ordinary / (1 + r)
            installment = installment.divide(onePlusR, INTERMEDIATE_SCALE, ROUNDING);
        }

        return installment.setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Convenience: calculate required monthly installment.
     */
    public static BigDecimal calculateMonthlyGoalInstallment(BigDecimal targetAmount,
                                                             BigDecimal annualRatePercent,
                                                             int months,
                                                             boolean installmentAtBeginning) {
        return calculateInstallment(
                targetAmount,
                annualRatePercent,
                months,
                CompoundingFrequency.MONTHLY,
                installmentAtBeginning
        );
    }

    private static void validateInputs(BigDecimal targetAmount,
                                       BigDecimal annualRatePercent,
                                       int numberOfInstallments,
                                       CompoundingFrequency frequency) {
        if (targetAmount == null)
            throw new IllegalArgumentException("targetAmount must not be null");
        if (annualRatePercent == null)
            throw new IllegalArgumentException("annualRatePercent must not be null");
        if (frequency == null)
            throw new IllegalArgumentException("frequency must not be null");
        if (targetAmount.signum() < 0)
            throw new IllegalArgumentException("targetAmount must not be negative");
        if (numberOfInstallments <= 0)
            throw new IllegalArgumentException("numberOfInstallments must be > 0");
    }
}
