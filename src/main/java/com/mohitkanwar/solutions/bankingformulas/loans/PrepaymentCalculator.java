package com.mohitkanwar.solutions.bankingformulas.loans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Utility class for simulating loan prepayment scenarios.
 *
 * <p>Current implementation supports a single one-time prepayment where:
 * <ul>
 *     <li>The prepayment is made at the end of a given EMI period (prepaymentMonth).</li>
 *     <li>The loan end date (original tenure) is kept the same.</li>
 *     <li>The EMI is recomputed for the remaining tenure (EMI reduction scenario).</li>
 * </ul>
 *
 * <p>This class provides a high-level view:
 * <ul>
 *     <li>Original EMI and tenure</li>
 *     <li>New EMI and tenure after prepayment</li>
 *     <li>Total interest without prepayment</li>
 *     <li>Total interest with prepayment</li>
 *     <li>Interest saved due to prepayment</li>
 * </ul>
 */
public final class PrepaymentCalculator {

    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private PrepaymentCalculator() {
        // utility class
    }

    /**
     * Simulates a single prepayment where EMI is reduced and maturity remains unchanged.
     *
     * <p>Algorithm:
     * <ol>
     *     <li>Generate the full amortization schedule without prepayment.</li>
     *     <li>Compute total interest without prepayment.</li>
     *     <li>Take the closing balance at the end of {@code prepaymentMonth} (without prepayment).</li>
     *     <li>Apply the prepayment lump sum to reduce the outstanding principal.</li>
     *     <li>Recalculate EMI for the remaining tenure (originalTenure - prepaymentMonth).</li>
     *     <li>Generate a new schedule for the remaining tenure with the reduced principal.</li>
     *     <li>Total interest with prepayment = interest (months 1..prepaymentMonth) + interest (remaining schedule).</li>
     * </ol>
     *
     * <p>Prepayment is assumed to be an extra payment in addition to the regular EMI
     * in the given prepayment month.
     *
     * @param principal        original principal (non-null, non-negative)
     * @param annualRate       annual interest rate in percent (non-null)
     * @param originalTenure   original tenure in months (&gt; 0)
     * @param prepaymentMonth  1-based month index at which prepayment is made
     *                         (must be between 1 and originalTenure - 1)
     * @param prepaymentAmount lump-sum prepayment amount (non-null, &gt; 0)
     * @return {@link PrepaymentResult} describing EMI, tenure and interest comparison
     */
    public static PrepaymentResult simulateEmiReduction(BigDecimal principal,
                                                        BigDecimal annualRate,
                                                        int originalTenure,
                                                        int prepaymentMonth,
                                                        BigDecimal prepaymentAmount) {

        validateInputs(principal, annualRate, originalTenure, prepaymentMonth, prepaymentAmount);

        if (principal.compareTo(BigDecimal.ZERO) == 0) {
            return PrepaymentResult.zeroLoan();
        }

        // 1. Original EMI & full schedule (no prepayment)
        BigDecimal originalEmi = LoanEmiCalculator.calculateEmi(principal, annualRate, originalTenure);
        List<AmortizationScheduleCalculator.Installment> baseSchedule =
                AmortizationScheduleCalculator.generateSchedule(principal, annualRate, originalTenure);

        BigDecimal totalInterestWithoutPrepayment = baseSchedule.stream()
                .map(AmortizationScheduleCalculator.Installment::getInterestComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 2. Interest paid up to and including prepayment month (same in both scenarios)
        BigDecimal interestUntilPrepayment = baseSchedule.stream()
                .filter(inst -> inst.getPeriodNumber() <= prepaymentMonth)
                .map(AmortizationScheduleCalculator.Installment::getInterestComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 3. Outstanding balance before prepayment (end of prepaymentMonth, without prepayment)
        AmortizationScheduleCalculator.Installment prepaymentInstallment =
                baseSchedule.get(prepaymentMonth - 1);
        BigDecimal balanceBeforePrepayment = prepaymentInstallment
                .getClosingBalance()
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 4. Apply prepayment lump sum
        BigDecimal newPrincipal = balanceBeforePrepayment
                .subtract(prepaymentAmount)
                .max(BigDecimal.ZERO)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        int remainingTenure = originalTenure - prepaymentMonth;

        // If prepayment fully closes the loan
        if (newPrincipal.compareTo(BigDecimal.ZERO) == 0 || remainingTenure <= 0) {
            BigDecimal totalInterestWithPrepayment = interestUntilPrepayment;
            BigDecimal interestSaved = totalInterestWithoutPrepayment
                    .subtract(totalInterestWithPrepayment)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);

            return new PrepaymentResult(
                    principal,
                    annualRate,
                    originalTenure,
                    prepaymentMonth,
                    prepaymentAmount,
                    originalEmi,
                    BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE),
                    originalTenure,                    // new tenure = original, but loan effectively ends early
                    totalInterestWithoutPrepayment,
                    totalInterestWithPrepayment,
                    interestSaved
            );
        }

        // 5. Recalculate EMI for remaining tenure with reduced principal
        BigDecimal newEmi = LoanEmiCalculator.calculateEmi(newPrincipal, annualRate, remainingTenure);

        // 6. New schedule for remaining tenure after prepayment
        List<AmortizationScheduleCalculator.Installment> postPrepaymentSchedule =
                AmortizationScheduleCalculator.generateSchedule(newPrincipal, annualRate, remainingTenure);

        BigDecimal interestAfterPrepayment = postPrepaymentSchedule.stream()
                .map(AmortizationScheduleCalculator.Installment::getInterestComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        BigDecimal totalInterestWithPrepayment = interestUntilPrepayment
                .add(interestAfterPrepayment)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        BigDecimal interestSaved = totalInterestWithoutPrepayment
                .subtract(totalInterestWithPrepayment)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);

        return new PrepaymentResult(
                principal,
                annualRate,
                originalTenure,
                prepaymentMonth,
                prepaymentAmount,
                originalEmi.setScale(CURRENCY_SCALE, ROUNDING_MODE),
                newEmi.setScale(CURRENCY_SCALE, ROUNDING_MODE),
                originalTenure,  // tenure in months remains the same in EMI reduction scenario
                totalInterestWithoutPrepayment,
                totalInterestWithPrepayment,
                interestSaved
        );
    }

    private static void validateInputs(BigDecimal principal,
                                       BigDecimal annualRate,
                                       int originalTenure,
                                       int prepaymentMonth,
                                       BigDecimal prepaymentAmount) {

        if (principal == null) {
            throw new IllegalArgumentException("principal must not be null");
        }
        if (annualRate == null) {
            throw new IllegalArgumentException("annualRate must not be null");
        }
        if (prepaymentAmount == null) {
            throw new IllegalArgumentException("prepaymentAmount must not be null");
        }
        if (principal.signum() < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
        if (originalTenure <= 0) {
            throw new IllegalArgumentException("originalTenure must be greater than zero");
        }
        if (prepaymentMonth <= 0 || prepaymentMonth > originalTenure) {
            throw new IllegalArgumentException("prepaymentMonth must be between 1 and originalTenure");
        }
        if (prepaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("prepaymentAmount must be greater than zero");
        }
    }

    /**
     * Result object representing a single prepayment simulation.
     */
    public static final class PrepaymentResult {

        private final BigDecimal originalPrincipal;
        private final BigDecimal annualRate;
        private final int originalTenureMonths;
        private final int prepaymentMonth;
        private final BigDecimal prepaymentAmount;

        private final BigDecimal originalEmi;
        private final BigDecimal newEmi;
        private final int newTenureMonths;

        private final BigDecimal totalInterestWithoutPrepayment;
        private final BigDecimal totalInterestWithPrepayment;
        private final BigDecimal interestSaved;

        public PrepaymentResult(BigDecimal originalPrincipal,
                                BigDecimal annualRate,
                                int originalTenureMonths,
                                int prepaymentMonth,
                                BigDecimal prepaymentAmount,
                                BigDecimal originalEmi,
                                BigDecimal newEmi,
                                int newTenureMonths,
                                BigDecimal totalInterestWithoutPrepayment,
                                BigDecimal totalInterestWithPrepayment,
                                BigDecimal interestSaved) {

            this.originalPrincipal = originalPrincipal;
            this.annualRate = annualRate;
            this.originalTenureMonths = originalTenureMonths;
            this.prepaymentMonth = prepaymentMonth;
            this.prepaymentAmount = prepaymentAmount;
            this.originalEmi = originalEmi;
            this.newEmi = newEmi;
            this.newTenureMonths = newTenureMonths;
            this.totalInterestWithoutPrepayment = totalInterestWithoutPrepayment;
            this.totalInterestWithPrepayment = totalInterestWithPrepayment;
            this.interestSaved = interestSaved;
        }

        static PrepaymentResult zeroLoan() {
            BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return new PrepaymentResult(
                    zero, zero,
                    0, 0, zero,
                    zero, zero, 0,
                    zero, zero, zero
            );
        }

        public BigDecimal getOriginalPrincipal() {
            return originalPrincipal;
        }

        public BigDecimal getAnnualRate() {
            return annualRate;
        }

        public int getOriginalTenureMonths() {
            return originalTenureMonths;
        }

        public int getPrepaymentMonth() {
            return prepaymentMonth;
        }

        public BigDecimal getPrepaymentAmount() {
            return prepaymentAmount;
        }

        public BigDecimal getOriginalEmi() {
            return originalEmi;
        }

        public BigDecimal getNewEmi() {
            return newEmi;
        }

        public int getNewTenureMonths() {
            return newTenureMonths;
        }

        public BigDecimal getTotalInterestWithoutPrepayment() {
            return totalInterestWithoutPrepayment;
        }

        public BigDecimal getTotalInterestWithPrepayment() {
            return totalInterestWithPrepayment;
        }

        public BigDecimal getInterestSaved() {
            return interestSaved;
        }

        @Override
        public String toString() {
            return "PrepaymentResult{" +
                    "originalPrincipal=" + originalPrincipal +
                    ", annualRate=" + annualRate +
                    ", originalTenureMonths=" + originalTenureMonths +
                    ", prepaymentMonth=" + prepaymentMonth +
                    ", prepaymentAmount=" + prepaymentAmount +
                    ", originalEmi=" + originalEmi +
                    ", newEmi=" + newEmi +
                    ", newTenureMonths=" + newTenureMonths +
                    ", totalInterestWithoutPrepayment=" + totalInterestWithoutPrepayment +
                    ", totalInterestWithPrepayment=" + totalInterestWithPrepayment +
                    ", interestSaved=" + interestSaved +
                    '}';
        }
    }
}
