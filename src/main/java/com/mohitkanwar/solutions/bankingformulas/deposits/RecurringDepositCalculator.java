package com.mohitkanwar.solutions.bankingformulas.deposits;


import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class for calculating Recurring Deposit (RD) maturity and interest.
 *
 * <p>Modelled as a series of equal periodic installments (an annuity) invested at
 * a periodic interest rate derived from the annual rate.
 *
 * <p>General formula (future value of annuity):
 *
 * <pre>
 * If installments are paid at END of each period (ordinary annuity):
 *   FV = I × [ (1 + r)^n - 1 ] / r
 *
 * If installments are paid at BEGINNING of each period (annuity due):
 *   FV = I × [ (1 + r)^n - 1 ] / r × (1 + r)
 *
 * Where:
 *   I = installment amount
 *   r = periodic interest rate (annualRate / (periodsPerYear × 100))
 *   n = total number of installments
 * </pre>
 *
 * <p>Note: This is a generic RD calculator. Real bank implementations may have
 * additional rules, taxation, or specific compounding conventions.
 */
public final class RecurringDepositCalculator {

    private static final int CURRENCY_SCALE = 2;
    private static final int INTERMEDIATE_SCALE = 20;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext MC = MathContext.DECIMAL64;

    private RecurringDepositCalculator() {
        // utility class
    }

    /**
     * Calculates the RD maturity amount using a generic periodic compounding model.
     *
     * @param installmentAmount     fixed installment amount per period (non-null, &gt;= 0)
     * @param annualRatePercent    annual interest rate in percent (non-null)
     * @param numberOfInstallments total number of installments (must be &gt;= 0)
     * @param frequency            compounding / installment frequency (non-null).
     *                             It is assumed that installments are made at the same
     *                             frequency as compounding.
     * @param installmentAtBeginning if true, treats installments as paid at the start of
     *                               each period (annuity due). If false, end of period
     *                               (ordinary annuity).
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateMaturityAmount(BigDecimal installmentAmount,
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

        // Zero-rate case: no interest, maturity is just total contributions
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

        BigDecimal fv;
        if (installmentAtBeginning) {
            // annuity due = ordinary × (1 + r)
            fv = fvOrdinary.multiply(onePlusR, MC);
        } else {
            fv = fvOrdinary;
        }

        return fv.setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Convenience method for a typical monthly RD with monthly compounding, where
     * installments are paid at the end of each month (ordinary annuity).
     *
     * @param monthlyInstallment   installment amount per month
     * @param annualRatePercent   annual interest rate in percent
     * @param numberOfMonths      number of monthly installments
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateMonthlyRdMaturity(BigDecimal monthlyInstallment,
                                                        BigDecimal annualRatePercent,
                                                        int numberOfMonths) {
        return calculateMaturityAmount(
                monthlyInstallment,
                annualRatePercent,
                numberOfMonths,
                CompoundingFrequency.MONTHLY,
                false
        );
    }

    /**
     * Calculates interest earned for the RD:
     *
     * <pre>
     * Interest = Maturity - TotalContributions
     *          = Maturity - (installmentAmount × numberOfInstallments)
     * </pre>
     */
    public static BigDecimal calculateInterest(BigDecimal installmentAmount,
                                               BigDecimal annualRatePercent,
                                               int numberOfInstallments,
                                               CompoundingFrequency frequency,
                                               boolean installmentAtBeginning) {

        BigDecimal maturity = calculateMaturityAmount(
                installmentAmount,
                annualRatePercent,
                numberOfInstallments,
                frequency,
                installmentAtBeginning
        );

        BigDecimal totalContribution = installmentAmount
                .multiply(BigDecimal.valueOf(numberOfInstallments));

        return maturity
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
