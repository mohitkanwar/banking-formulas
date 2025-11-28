package com.mohitkanwar.solutions.bankingformulas.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class for calculating compound interest and maturity amount.
 *
 * <p>Standard compound interest formula:
 * <pre>
 * Amount = P × (1 + r/m)^(m × t)
 * Interest = Amount - P
 * </pre>
 *
 * Where:
 * <ul>
 *     <li><b>P</b> = Principal amount</li>
 *     <li><b>r</b> = Annual interest rate in percent (e.g. 8.5 for 8.5%)</li>
 *     <li><b>m</b> = Number of compounding periods per year</li>
 *     <li><b>t</b> = Time in years</li>
 * </ul>
 *
 * <p>The calculator works with {@link BigDecimal} for monetary values and
 * applies HALF_UP rounding to 2 decimal places for the final results.
 */
public final class CompoundInterestCalculator {

    private static final int CURRENCY_SCALE = 2;
    private static final int INTERMEDIATE_SCALE = 20;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext MC = MathContext.DECIMAL64;

    private CompoundInterestCalculator() {
        // utility class
    }

    /**
     * Supported compounding frequencies.
     */


    /**
     * Computes maturity amount for compound interest.
     *
     * <pre>
     * Amount = P × (1 + r/m)^(m × t)
     * </pre>
     *
     * @param principal  principal amount (non-null, ≥ 0)
     * @param annualRate annual interest rate in percent (non-null)
     * @param timeInYears time in years (non-null, ≥ 0). For precise integer periods,
     *                    {@code timeInYears × periodsPerYear} must be an integer.
     * @param frequency  compounding frequency (non-null)
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateAmount(BigDecimal principal,
                                             BigDecimal annualRate,
                                             BigDecimal timeInYears,
                                             CompoundingFrequency frequency) {
        validateInputs(principal, annualRate, timeInYears, frequency);

        if (principal.compareTo(BigDecimal.ZERO) == 0 ||
            timeInYears.compareTo(BigDecimal.ZERO) == 0) {
            return principal.setScale(CURRENCY_SCALE, ROUNDING);
        }

        int m = frequency.getPeriodsPerYear();

        // totalPeriods = m * t, must be an integer (e.g., 1.5 years * 4 = 6 quarters)
        BigDecimal totalPeriodsBD = timeInYears
                .multiply(BigDecimal.valueOf(m))
                .stripTrailingZeros();

        if (totalPeriodsBD.scale() > 0) {
            throw new IllegalArgumentException(
                    "timeInYears * periodsPerYear must be an integer for compound interest calculation");
        }

        int totalPeriods = totalPeriodsBD.intValueExact();

        // periodicRate = (annualRate / 100) / m
        BigDecimal periodicRate = annualRate
                .divide(BigDecimal.valueOf(100L), INTERMEDIATE_SCALE, ROUNDING)
                .divide(BigDecimal.valueOf(m), INTERMEDIATE_SCALE, ROUNDING);

        BigDecimal factor = BigDecimal.ONE
                .add(periodicRate)
                .pow(totalPeriods, MC);

        BigDecimal amount = principal
                .multiply(factor, MC)
                .setScale(CURRENCY_SCALE, ROUNDING);

        return amount;
    }

    /**
     * Computes interest earned for compound interest.
     *
     * <pre>
     * Interest = Amount - Principal
     * </pre>
     *
     * @param principal  principal amount
     * @param annualRate annual interest rate in percent
     * @param timeInYears time in years
     * @param frequency  compounding frequency
     * @return interest amount rounded to 2 decimal places
     */
    public static BigDecimal calculateInterest(BigDecimal principal,
                                               BigDecimal annualRate,
                                               BigDecimal timeInYears,
                                               CompoundingFrequency frequency) {
        BigDecimal amount = calculateAmount(principal, annualRate, timeInYears, frequency);
        return amount.subtract(principal)
                .setScale(CURRENCY_SCALE, ROUNDING);
    }

    private static void validateInputs(BigDecimal principal,
                                       BigDecimal annualRate,
                                       BigDecimal timeInYears,
                                       CompoundingFrequency frequency) {
        if (principal == null) {
            throw new IllegalArgumentException("principal must not be null");
        }
        if (annualRate == null) {
            throw new IllegalArgumentException("annualRate must not be null");
        }
        if (timeInYears == null) {
            throw new IllegalArgumentException("timeInYears must not be null");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("frequency must not be null");
        }
        if (principal.signum() < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
        if (timeInYears.signum() < 0) {
            throw new IllegalArgumentException("timeInYears must not be negative");
        }
    }
}
