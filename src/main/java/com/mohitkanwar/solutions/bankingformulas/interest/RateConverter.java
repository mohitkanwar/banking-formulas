package com.mohitkanwar.solutions.bankingformulas.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class for converting between different interest rate representations.
 *
 * <p>All methods expect and return rates in PERCENT form (e.g. 12.0 for 12%).
 *
 * <p>Supported conversions:
 * <ul>
 *     <li>Annual &lt;-&gt; Monthly (simple linear: divide/multiply by 12)</li>
 *     <li>Nominal annual &lt;-&gt; Effective annual (with compounding periods)</li>
 *     <li>Percent &lt;-&gt; decimal (e.g. 12% &lt;-&gt; 0.12)</li>
 * </ul>
 */
public final class RateConverter {

    private static final int RATE_SCALE = 10;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext MC = MathContext.DECIMAL64;

    private RateConverter() {
        // utility class
    }

    /**
     * Converts annual rate (in percent) to a simple monthly rate (in percent)
     * using linear division:
     *
     * <pre>
     * monthlyRate = annualRate / 12
     * </pre>
     */
    public static BigDecimal annualToMonthlySimple(BigDecimal annualRatePercent) {
        requireNonNull(annualRatePercent, "annualRatePercent");
        return annualRatePercent
                .divide(BigDecimal.valueOf(12L), RATE_SCALE, ROUNDING);
    }

    /**
     * Converts monthly rate (in percent) to a simple annual rate (in percent)
     * using linear multiplication:
     *
     * <pre>
     * annualRate = monthlyRate × 12
     * </pre>
     */
    public static BigDecimal monthlyToAnnualSimple(BigDecimal monthlyRatePercent) {
        requireNonNull(monthlyRatePercent, "monthlyRatePercent");
        return monthlyRatePercent
                .multiply(BigDecimal.valueOf(12L))
                .setScale(RATE_SCALE, ROUNDING);
    }

    /**
     * Converts a nominal annual rate (in percent) with compounding periods per year
     * into an effective annual rate (in percent).
     *
     * <pre>
     * r_eff = (1 + r_nom/m)^m - 1
     * </pre>
     *
     * where:
     * <ul>
     *     <li>r_nom = nominal annual rate in decimal (e.g. 0.12 for 12%)</li>
     *     <li>m = periods per year (e.g. 12 for monthly)</li>
     * </ul>
     */
    public static BigDecimal nominalToEffectiveAnnual(BigDecimal nominalAnnualRatePercent,
                                                      int periodsPerYear) {
        requireNonNull(nominalAnnualRatePercent, "nominalAnnualRatePercent");
        requirePositive(periodsPerYear, "periodsPerYear");

        BigDecimal rNomDecimal = percentToDecimal(nominalAnnualRatePercent);
        BigDecimal periodicRate = rNomDecimal
                .divide(BigDecimal.valueOf(periodsPerYear), RATE_SCALE, ROUNDING);

        BigDecimal factor = BigDecimal.ONE
                .add(periodicRate)
                .pow(periodsPerYear, MC);

        BigDecimal rEffDecimal = factor.subtract(BigDecimal.ONE);
        return decimalToPercent(rEffDecimal);
    }

    /**
     * Converts an effective annual rate (in percent) into a nominal annual rate (in percent)
     * given the number of compounding periods per year.
     *
     * <p>This requires taking the m-th root and is implemented using double-based
     * approximation internally. It is generally sufficient for rate calculations
     * but should not be used for monetary amounts directly.
     *
     * <pre>
     * 1 + r_eff = (1 + r_nom/m)^m
     * 1 + r_nom/m = (1 + r_eff)^(1/m)
     * r_nom = m × ((1 + r_eff)^(1/m) - 1)
     * </pre>
     */
    public static BigDecimal effectiveAnnualToNominal(BigDecimal effectiveAnnualRatePercent,
                                                      int periodsPerYear) {
        requireNonNull(effectiveAnnualRatePercent, "effectiveAnnualRatePercent");
        requirePositive(periodsPerYear, "periodsPerYear");

        BigDecimal rEffDecimal = percentToDecimal(effectiveAnnualRatePercent);

        double base = BigDecimal.ONE.add(rEffDecimal).doubleValue();
        double root = Math.pow(base, 1.0 / periodsPerYear); // (1 + r_eff)^(1/m)
        double periodicRate = root - 1.0;
        double nominalDecimal = periodicRate * periodsPerYear;

        BigDecimal nominalDecimalBD = BigDecimal.valueOf(nominalDecimal);
        return decimalToPercent(nominalDecimalBD);
    }

    /**
     * Converts a rate from percent form to decimal form:
     * <pre>
     * 12% -> 0.12
     * </pre>
     */
    public static BigDecimal percentToDecimal(BigDecimal ratePercent) {
        requireNonNull(ratePercent, "ratePercent");
        return ratePercent
                .divide(BigDecimal.valueOf(100L), RATE_SCALE, ROUNDING);
    }

    /**
     * Converts a rate from decimal form to percent form:
     * <pre>
     * 0.12 -> 12%
     * </pre>
     */
    public static BigDecimal decimalToPercent(BigDecimal rateDecimal) {
        requireNonNull(rateDecimal, "rateDecimal");
        return rateDecimal
                .multiply(BigDecimal.valueOf(100L))
                .setScale(RATE_SCALE, ROUNDING);
    }

    private static void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
    }
}
