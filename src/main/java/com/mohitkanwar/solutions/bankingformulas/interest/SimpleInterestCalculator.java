package com.mohitkanwar.solutions.bankingformulas.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for calculating simple interest and maturity amount.
 *
 * <p>Simple Interest Formula:
 * <pre>
 * Interest = P × r × t
 * Amount   = P + Interest
 * </pre>
 *
 * Where:
 * <ul>
 *     <li><b>P</b> = Principal amount</li>
 *     <li><b>r</b> = Annual interest rate (in percent, e.g., 8.5 for 8.5%)</li>
 *     <li><b>t</b> = Time in years (e.g., 2.5 years)</li>
 * </ul>
 *
 * <p>This calculator is widely applicable in:
 * <ul>
 *     <li>Penalties &amp; overdue interest</li>
 *     <li>Short-term loans</li>
 *     <li>Legacy fixed deposit rules</li>
 *     <li>Interest on savings for partial-period calculations</li>
 * </ul>
 */
public final class SimpleInterestCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private SimpleInterestCalculator() {
        // utility class
    }

    /**
     * Computes simple interest using:
     * <pre>
     * Interest = P × (r / 100) × t
     * </pre>
     *
     * @param principal principal amount (must be non-null and ≥ 0)
     * @param annualRate annual interest rate in percent (must be non-null)
     * @param timeInYears time period in years (must be ≥ 0)
     * @return interest amount rounded to 2 decimal places
     */
    public static BigDecimal calculateInterest(BigDecimal principal,
                                               BigDecimal annualRate,
                                               BigDecimal timeInYears) {
        validateInputs(principal, annualRate, timeInYears);

        BigDecimal rateDecimal = annualRate
                .divide(BigDecimal.valueOf(100), 10, ROUNDING);

        BigDecimal interest = principal
                .multiply(rateDecimal)
                .multiply(timeInYears)
                .setScale(SCALE, ROUNDING);

        return interest;
    }

    /**
     * Computes maturity amount:
     * <pre>
     * Amount = Principal + Interest
     * </pre>
     *
     * @param principal principal amount (≥ 0)
     * @param annualRate annual interest rate in percent
     * @param timeInYears time period in years
     * @return maturity amount rounded to 2 decimal places
     */
    public static BigDecimal calculateAmount(BigDecimal principal,
                                             BigDecimal annualRate,
                                             BigDecimal timeInYears) {
        BigDecimal interest = calculateInterest(principal, annualRate, timeInYears);
        return principal.add(interest).setScale(SCALE, ROUNDING);
    }

    private static void validateInputs(BigDecimal principal,
                                       BigDecimal annualRate,
                                       BigDecimal timeInYears) {
        if (principal == null) {
            throw new IllegalArgumentException("principal must not be null");
        }
        if (annualRate == null) {
            throw new IllegalArgumentException("annualRate must not be null");
        }
        if (timeInYears == null) {
            throw new IllegalArgumentException("timeInYears must not be null");
        }
        if (principal.signum() < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
        if (timeInYears.signum() < 0) {
            throw new IllegalArgumentException("timeInYears must not be negative");
        }
    }
}
