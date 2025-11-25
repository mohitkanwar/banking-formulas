package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator.CompoundingFrequency;

/**
 * Utility class for calculating Fixed Deposit (FD) maturity and interest.
 *
 * <p>This is a thin convenience wrapper over {@link CompoundInterestCalculator} with
 * FD-friendly method signatures (years, months, days) and sensible defaults.
 *
 * <p>General formula:
 * <pre>
 * Amount = P × (1 + r/m)^(m × t)
 * Interest = Amount - P
 * </pre>
 *
 * Where:
 * <ul>
 *     <li>P = principal</li>
 *     <li>r = annual interest rate in percent</li>
 *     <li>m = compounding frequency per year</li>
 *     <li>t = time in years</li>
 * </ul>
 */
public final class FixedDepositCalculator {

    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private FixedDepositCalculator() {
        // utility class
    }

    /**
     * Calculates FD maturity amount using a term specified in full years.
     *
     * @param principal  principal amount (non-null, ≥ 0)
     * @param annualRate annual interest rate in percent (non-null)
     * @param years      term in years (non-null, ≥ 0)
     * @param frequency  compounding frequency (non-null)
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateMaturityAmountForYears(BigDecimal principal,
                                                             BigDecimal annualRate,
                                                             BigDecimal years,
                                                             CompoundingFrequency frequency) {
        validatePrincipalAndRate(principal, annualRate);

        if (years == null) {
            throw new IllegalArgumentException("years must not be null");
        }
        if (years.signum() < 0) {
            throw new IllegalArgumentException("years must not be negative");
        }

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, years, frequency);
        return amount.setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Calculates FD maturity amount using a term specified in months.
     *
     * <p>Converts months to years as: {@code years = months / 12}.
     *
     * @param principal  principal amount (non-null, ≥ 0)
     * @param annualRate annual interest rate in percent (non-null)
     * @param months     term in months (≥ 0)
     * @param frequency  compounding frequency (non-null)
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateMaturityAmountForMonths(BigDecimal principal,
                                                              BigDecimal annualRate,
                                                              int months,
                                                              CompoundingFrequency frequency) {
        validatePrincipalAndRate(principal, annualRate);

        if (months < 0) {
            throw new IllegalArgumentException("months must not be negative");
        }

        BigDecimal years = BigDecimal.valueOf(months)
                .divide(BigDecimal.valueOf(12L), 10, ROUNDING);

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, years, frequency);
        return amount.setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Calculates FD maturity amount using a term in days.
     *
     * <p>Converts days to years using a 365-day convention:
     * {@code years = days / 365}.
     *
     * @param principal  principal amount (non-null, ≥ 0)
     * @param annualRate annual interest rate in percent (non-null)
     * @param days       term in days (≥ 0)
     * @param frequency  compounding frequency (non-null)
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateMaturityAmountForDays(BigDecimal principal,
                                                            BigDecimal annualRate,
                                                            int days,
                                                            CompoundingFrequency frequency) {
        validatePrincipalAndRate(principal, annualRate);

        if (days < 0) {
            throw new IllegalArgumentException("days must not be negative");
        }

        BigDecimal years = BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(365L), 10, ROUNDING);

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, years, frequency);
        return amount.setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Calculates interest earned for a term specified in years.
     *
     * <p>Interest = Maturity - Principal.
     */
    public static BigDecimal calculateInterestForYears(BigDecimal principal,
                                                       BigDecimal annualRate,
                                                       BigDecimal years,
                                                       CompoundingFrequency frequency) {
        BigDecimal maturity = calculateMaturityAmountForYears(principal, annualRate, years, frequency);
        return maturity.subtract(principal).setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Calculates interest earned for a term specified in months.
     */
    public static BigDecimal calculateInterestForMonths(BigDecimal principal,
                                                        BigDecimal annualRate,
                                                        int months,
                                                        CompoundingFrequency frequency) {
        BigDecimal maturity = calculateMaturityAmountForMonths(principal, annualRate, months, frequency);
        return maturity.subtract(principal).setScale(CURRENCY_SCALE, ROUNDING);
    }

    /**
     * Calculates interest earned for a term specified in days.
     */
    public static BigDecimal calculateInterestForDays(BigDecimal principal,
                                                      BigDecimal annualRate,
                                                      int days,
                                                      CompoundingFrequency frequency) {
        BigDecimal maturity = calculateMaturityAmountForDays(principal, annualRate, days, frequency);
        return maturity.subtract(principal).setScale(CURRENCY_SCALE, ROUNDING);
    }

    private static void validatePrincipalAndRate(BigDecimal principal, BigDecimal annualRate) {
        if (principal == null) {
            throw new IllegalArgumentException("principal must not be null");
        }
        if (annualRate == null) {
            throw new IllegalArgumentException("annualRate must not be null");
        }
        if (principal.signum() < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
    }
}
