package com.mohitkanwar.solutions.bankingformulas.deposits;


import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class for calculating SIP (Systematic Investment Plan) future value
 * and wealth gain.
 *
 * <p>Modeled as a series of equal periodic investments (an annuity) earning
 * returns at a periodic rate derived from the annual expected return.
 *
 * <p>For installments invested at END of each period (ordinary annuity):
 *
 * <pre>
 * FV = I × [ (1 + r)^n - 1 ] / r
 * </pre>
 *
 * <p>For installments invested at BEGINNING of each period (annuity due):
 *
 * <pre>
 * FV = I × [ (1 + r)^n - 1 ] / r × (1 + r)
 * </pre>
 *
 * Where:
 * <ul>
 *     <li>I = periodic SIP installment</li>
 *     <li>r = periodic rate (annualRate / (m × 100))</li>
 *     <li>n = number of installments</li>
 *     <li>m = compounding periods per year</li>
 * </ul>
 */
public final class SipCalculator {

    private static final int CURRENCY_SCALE = 2;
    private static final int INTERMEDIATE_SCALE = 20;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext MC = MathContext.DECIMAL64;

    private SipCalculator() {
        // utility class
    }

    /**
     * Calculates the future value of a SIP.
     *
     * @param installmentAmount     periodic SIP amount (non-null, ≥ 0)
     * @param annualRatePercent    expected annual return in percent (non-null)
     * @param numberOfInstallments total number of installments (≥ 0)
     * @param frequency            compounding / contribution frequency (non-null),
     *                             assumed same as SIP frequency.
     * @param installmentAtBeginning true if investment happens at the beginning of
     *                               the period (annuity due), false for end of period.
     * @return future value (maturity amount) rounded to 2 decimal places
     */
    public static BigDecimal calculateFutureValue(BigDecimal installmentAmount,
                                                  BigDecimal annualRatePercent,
                                                  int numberOfInstallments,
                                                  CompoundingFrequency frequency,
                                                  boolean installmentAtBeginning) {

        validateInputs(installmentAmount, annualRatePercent, numberOfInstallments, frequency);

        if (installmentAmount.compareTo(BigDecimal.ZERO) == 0 ||
            numberOfInstallments == 0) {
            return BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING);
        }

        int m = frequency.getPeriodsPerYear();

        // periodicRate = (annualRate / 100) / m
        BigDecimal periodicRate = annualRatePercent
                .divide(BigDecimal.valueOf(100L), INTERMEDIATE_SCALE, ROUNDING)
                .divide(BigDecimal.valueOf(m), INTERMEDIATE_SCALE, ROUNDING);

        // Zero-rate case: no investment growth -> just the sum of contributions
        if (periodicRate.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = installmentAmount
                    .multiply(BigDecimal.valueOf(numberOfInstallments));
            return total.setScale(CURRENCY_SCALE, ROUNDING);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(periodicRate);
        BigDecimal factor = onePlusR.pow(numberOfInstallments, MC);

        // FV of ordinary annuity
        BigDecimal fvOrdinary = installmentAmount
                .multiply(factor.subtract(BigDecimal.ONE))
                .divide(periodicRate, INTERMEDIATE_SCALE, ROUNDING);

        BigDecimal fv = installmentAtBeginning
                ? fvOrdinary.multiply(onePlusR, MC)  // annuity due
                : fvOrdinary;

        return fv.setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Convenience method for a typical monthly SIP with monthly compounding,
     * installments at the end of month.
     */
    public static BigDecimal calculateMonthlySipFutureValue(BigDecimal monthlyInstallment,
                                                            BigDecimal annualRatePercent,
                                                            int numberOfMonths) {
        return calculateFutureValue(
                monthlyInstallment,
                annualRatePercent,
                numberOfMonths,
                CompoundingFrequency.MONTHLY,
                false
        );
    }

    /**
     * Calculates wealth gain:
     *
     * <pre>
     * gain = futureValue - totalContributions
     * </pre>
     */
    public static BigDecimal calculateWealthGain(BigDecimal installmentAmount,
                                                 BigDecimal annualRatePercent,
                                                 int numberOfInstallments,
                                                 CompoundingFrequency frequency,
                                                 boolean installmentAtBeginning) {

        BigDecimal fv = calculateFutureValue(
                installmentAmount,
                annualRatePercent,
                numberOfInstallments,
                frequency,
                installmentAtBeginning
        );

        BigDecimal totalContribution = installmentAmount
                .multiply(BigDecimal.valueOf(numberOfInstallments));

        return fv
                .subtract(totalContribution)
                .setScale(CURRENCY_SCALE, ROUNDING);
    }

    private static void validateInputs(BigDecimal installmentAmount,
                                       BigDecimal annualRatePercent,
                                       int numberOfInstallments,
                                       CompoundingFrequency frequency) {
        if (installmentAmount == null) {
            throw new IllegalArgumentException("installmentAmount must not be null");
        }
        if (annualRatePercent == null) {
            throw new IllegalArgumentException("annualRatePercent must not be null");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("frequency must not be null");
        }
        if (installmentAmount.signum() < 0) {
            throw new IllegalArgumentException("installmentAmount must not be negative");
        }
        if (numberOfInstallments < 0) {
            throw new IllegalArgumentException("numberOfInstallments must not be negative");
        }
    }
}
