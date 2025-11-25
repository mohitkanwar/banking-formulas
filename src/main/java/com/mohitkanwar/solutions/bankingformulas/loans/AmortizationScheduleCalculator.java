package com.mohitkanwar.solutions.bankingformulas.loans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for generating an amortization schedule for a standard EMI-based loan.
 *
 * <p>The schedule breaks each installment into:
 * <ul>
 *     <li>Opening balance</li>
 *     <li>Interest component</li>
 *     <li>Principal component</li>
 *     <li>Closing balance</li>
 * </ul>
 *
 * <p>Assumptions:
 * <ul>
 *     <li>Fixed interest rate (annual, in percent).</li>
 *     <li>Equal monthly installments (EMI) throughout the tenure.</li>
 *     <li>Interest is calculated on the outstanding principal using a monthly rate
 *     derived from the annual rate (annualRate / (12 * 100)).</li>
 * </ul>
 */
public final class AmortizationScheduleCalculator {

    // Scale for monetary values (e.g. rupees and paise).
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private AmortizationScheduleCalculator() {
        // utility class
    }

    /**
     * Generates a monthly amortization schedule for the given loan parameters.
     *
     * <p>The EMI is computed using {@link LoanEmiCalculator#calculateEmi(BigDecimal, BigDecimal, int)}.
     * The schedule is generated such that:
     * <ul>
     *     <li>The EMI remains constant for all periods.</li>
     *     <li>Rounding is applied to interest and principal components to currency scale.</li>
     *     <li>The final installment adjusts the principal component so that the closing balance becomes exactly zero.</li>
     * </ul>
     *
     * @param principal      principal amount (non-null, non-negative)
     * @param annualRate     annual interest rate in percent (non-null). Negative rates are allowed but
     *                       should be validated at business layer if undesired.
     * @param tenureInMonths tenure in months (&gt; 0)
     * @return an immutable list of {@link Installment} entries representing the schedule
     * @throws IllegalArgumentException if inputs are invalid
     */
    public static List<Installment> generateSchedule(BigDecimal principal,
                                                     BigDecimal annualRate,
                                                     int tenureInMonths) {

        // Basic validation (delegates to LoanEmiCalculator for detailed checks)
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

        if (principal.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyList();
        }

        BigDecimal emi = LoanEmiCalculator.calculateEmi(principal, annualRate, tenureInMonths);

        // Compute monthly rate the same way as in LoanEmiCalculator
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12 * 100L), 20, ROUNDING_MODE); // high precision

        List<Installment> installments = new ArrayList<>(tenureInMonths);

        BigDecimal balance = principal.setScale(CURRENCY_SCALE, ROUNDING_MODE);

        for (int period = 1; period <= tenureInMonths; period++) {
            BigDecimal openingBalance = balance;

            BigDecimal interest = openingBalance
                    .multiply(monthlyRate)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);

            BigDecimal principalComponent = emi.subtract(interest)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);

            BigDecimal closingBalance = openingBalance.subtract(principalComponent)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);

            // Adjust last installment to avoid tiny rounding residuals
            if (period == tenureInMonths) {
                // Force closing balance to exactly zero
                principalComponent = openingBalance;
                interest = emi.subtract(principalComponent).setScale(CURRENCY_SCALE, ROUNDING_MODE);
                closingBalance = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
            }

            Installment installment = new Installment(
                    period,
                    openingBalance,
                    emi,
                    interest,
                    principalComponent,
                    closingBalance
            );

            installments.add(installment);
            balance = closingBalance;
        }

        return Collections.unmodifiableList(installments);
    }

    /**
     * A single installment (period) in an amortization schedule.
     */
    public static final class Installment {

        private final int periodNumber;
        private final BigDecimal openingBalance;
        private final BigDecimal emi;
        private final BigDecimal interestComponent;
        private final BigDecimal principalComponent;
        private final BigDecimal closingBalance;

        public Installment(int periodNumber,
                           BigDecimal openingBalance,
                           BigDecimal emi,
                           BigDecimal interestComponent,
                           BigDecimal principalComponent,
                           BigDecimal closingBalance) {
            this.periodNumber = periodNumber;
            this.openingBalance = openingBalance;
            this.emi = emi;
            this.interestComponent = interestComponent;
            this.principalComponent = principalComponent;
            this.closingBalance = closingBalance;
        }

        public int getPeriodNumber() {
            return periodNumber;
        }

        public BigDecimal getOpeningBalance() {
            return openingBalance;
        }

        public BigDecimal getEmi() {
            return emi;
        }

        public BigDecimal getInterestComponent() {
            return interestComponent;
        }

        public BigDecimal getPrincipalComponent() {
            return principalComponent;
        }

        public BigDecimal getClosingBalance() {
            return closingBalance;
        }

        @Override
        public String toString() {
            return "Installment{" +
                    "periodNumber=" + periodNumber +
                    ", openingBalance=" + openingBalance +
                    ", emi=" + emi +
                    ", interestComponent=" + interestComponent +
                    ", principalComponent=" + principalComponent +
                    ", closingBalance=" + closingBalance +
                    '}';
        }
    }
}
