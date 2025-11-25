package com.mohitkanwar.solutions.bankingformulas.loans;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class for calculating Equated Monthly Installment (EMI) for loans.
 *
 * <p>This class provides a single public method {@link #calculateEmi(BigDecimal, BigDecimal, int)}
 * which implements the standard EMI formula:
 *
 * <p>`EMI = [P * r * (1+r)^n] / [(1+r)^n - 1]` where:
 * - `P` is the principal,
 * - `r` is the monthly interest rate (annual rate divided by 12 and 100),
 * - `n` is the number of monthly installments (tenureInMonths).
 *
 * <p>All monetary calculations use {@link BigDecimal} to avoid floating-point rounding issues.
 * The method documents and validates inputs, explains rounding choices, and handles the zero-rate
 * (interest-free) case explicitly.
 */
public class LoanEmiCalculator {

    // Scale used for intermediate rate calculations to preserve precision.
    private static final int INTERMEDIATE_SCALE = 20;

    // Scale used for the final EMI amount (currency scale, e.g., two decimal places).
    private static final int CURRENCY_SCALE = 2;

    // Rounding mode used for monetary rounding.
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final MathContext MC = MathContext.DECIMAL64;

    /**
     * Calculates the Equated Monthly Installment (EMI) for a loan.
     *
     * @param principal      Principal amount as a {@link BigDecimal}. Must be non-null and
     *                       non-negative.
     * @param annualRate     Annual interest rate in percent (for example, 10.5 for 10.5\%).
     *                       Must be non-null. Negative rates are allowed (will reduce EMI),
     *                       but handle them with care in business logic.
     * @param tenureInMonths Tenure in months. Must be greater than zero.
     * @return The monthly EMI as a {@link BigDecimal} rounded to two decimal places using
     *         {@link RoundingMode#HALF_UP}.
     * @throws IllegalArgumentException if principal or annualRate is null, principal is negative,
     *                                  or tenureInMonths is not positive.
     */
    public static BigDecimal calculateEmi(BigDecimal principal,
                                          BigDecimal annualRate,
                                          int tenureInMonths) {

        // Validate inputs
        if (principal == null) {
            throw new IllegalArgumentException("principal must not be null");
        }
        if (annualRate == null) {
            throw new IllegalArgumentException("annualRate must not be null");
        }
        if (principal.signum() < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
        if (tenureInMonths <= 0) {
            throw new IllegalArgumentException("tenureInMonths must be greater than zero");
        }

        // Convert annual percentage rate to monthly decimal rate:
        // e.g., annualRate = 12 -> monthlyRate = 0.01 (1% per month)
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12 * 100L), INTERMEDIATE_SCALE, ROUNDING_MODE);

        // If monthly rate is zero (interest-free loan), EMI is simply principal / n.
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal
                    .divide(BigDecimal.valueOf(tenureInMonths), CURRENCY_SCALE, ROUNDING_MODE);
        }

        // Compute (1 + r)^n where r is the monthlyRate and n is tenureInMonths.
        BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate).pow(tenureInMonths, MC);

        // Numerator: P * r * (1 + r)^n
        BigDecimal numerator = principal
                .multiply(monthlyRate, MC)
                .multiply(onePlusRPowerN, MC);

        // Denominator: (1 + r)^n - 1
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE, MC);

        // Final EMI = numerator / denominator, rounded to currency scale.
        return numerator.divide(denominator, CURRENCY_SCALE, ROUNDING_MODE);
    }
}