package com.mohitkanwar.solutions.bankingformulas.deposits;

import com.mohitkanwar.solutions.bankingformulas.interest.CompoundInterestCalculator;
import com.mohitkanwar.solutions.bankingformulas.interest.CompoundingFrequency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;


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
    public static final long DAYS_IN_YEAR = 365L;


    public BigDecimal getMaturityAmount(BigDecimal fdAmount, Date fdStartDate, Date fdMaturityDate, BigDecimal annualRate, MaturityInstructions maturityInstructions) {
        validateFdAmount(fdAmount);
        validateFdStartDate(fdStartDate);
        validateFdMaturityDate(fdStartDate, fdMaturityDate);
        validateAnnualRate(annualRate);
        LocalDate start = fdStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = fdMaturityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


        switch (maturityInstructions) {
            case ON_MATURITY_MONTHLY_COMPOUNDING -> {
                long months = ChronoUnit.MONTHS.between(start, end);
                return calculateMaturityAmountForMonths(fdAmount, annualRate, months);
            }
            case ON_MATURITY_DAILY_COMPOUNDING -> {
                long days = ChronoUnit.DAYS.between(start, end);
                if (!start.equals(end)) {
                    days++;
                }

                return calculateMaturityAmountForDays(fdAmount, annualRate, days, CompoundingFrequency.DAILY);
            }
            default -> throw new IllegalArgumentException("Given Maturity Instructions are not supported yet: " + maturityInstructions);
        }
    }

    private void validateAnnualRate(BigDecimal annualRate) {
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("annualRate must not be negative or null");
        }
    }

    private void validateFdMaturityDate(Date fdStartDate, Date fdMaturityDate) {
        if (fdMaturityDate == null ) throw new IllegalArgumentException("FD maturity date must be a valid date");
        if (fdMaturityDate.before(fdStartDate) ) throw new IllegalArgumentException("FD maturity date must not come before FD start date");
    }

    private void validateFdStartDate(Date fdStartDate) {
        if (fdStartDate == null ) throw new IllegalArgumentException("FD start date must be a valid date");
    }

    private void validateFdAmount(BigDecimal fdAmount) {
        if (fdAmount == null || fdAmount.compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new IllegalArgumentException("FD Amount must be a positive number");
        }
    }

    private BigDecimal calculateMaturityAmountForMonths(BigDecimal principal,
                                                              BigDecimal annualRate,
                                                              long months) {
        if (months < 0) {
            throw new IllegalArgumentException("months must not be negative");
        }

        BigDecimal years = BigDecimal.valueOf(months)
                .divide(BigDecimal.valueOf(12L), 10, ROUNDING);

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, years, CompoundingFrequency.MONTHLY);
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
    private BigDecimal calculateMaturityAmountForDays(BigDecimal principal,
                                                            BigDecimal annualRate,
                                                            long days,
                                                            CompoundingFrequency frequency) {

        if (days < 0) {
            throw new IllegalArgumentException("days must not be negative");
        }

        BigDecimal years = BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, ROUNDING);

        BigDecimal amount = CompoundInterestCalculator.calculateAmount(
                principal, annualRate, years, frequency);
        return amount.setScale(CURRENCY_SCALE, ROUNDING);
    }
}
