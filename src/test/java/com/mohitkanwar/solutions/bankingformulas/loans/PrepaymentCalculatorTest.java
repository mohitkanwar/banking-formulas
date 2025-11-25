package com.mohitkanwar.solutions.bankingformulas.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class PrepaymentCalculatorTest {

    @Test
    @DisplayName("Prepayment with EMI reduction lowers EMI and saves interest")
    void testEmiReductionPrepaymentReducesEmiAndInterest() {
        BigDecimal principal = BigDecimal.valueOf(500_000);
        BigDecimal annualRate = BigDecimal.valueOf(8.5);
        int tenureInMonths = 240;
        int prepaymentMonth = 36;
        BigDecimal prepaymentAmount = BigDecimal.valueOf(200_000);

        PrepaymentCalculator.PrepaymentResult result =
                PrepaymentCalculator.simulateEmiReduction(
                        principal,
                        annualRate,
                        tenureInMonths,
                        prepaymentMonth,
                        prepaymentAmount
                );

        // Basic sanity checks
        assertEquals(principal, result.getOriginalPrincipal());
        assertEquals(annualRate, result.getAnnualRate());
        assertEquals(tenureInMonths, result.getOriginalTenureMonths());
        assertEquals(prepaymentMonth, result.getPrepaymentMonth());
        assertEquals(prepaymentAmount, result.getPrepaymentAmount());

        // New EMI should be less than original EMI
        assertTrue(result.getNewEmi().compareTo(result.getOriginalEmi()) < 0,
                () -> "New EMI (" + result.getNewEmi() + ") should be less than original EMI (" +
                        result.getOriginalEmi() + ")");

        // Interest with prepayment should be less than without prepayment
        assertTrue(result.getTotalInterestWithPrepayment()
                        .compareTo(result.getTotalInterestWithoutPrepayment()) < 0,
                () -> "Interest with prepayment (" + result.getTotalInterestWithPrepayment()
                        + ") should be less than without prepayment (" +
                        result.getTotalInterestWithoutPrepayment() + ")");

        // interestSaved should be positive and consistent with the two totals
        BigDecimal expectedSaved = result.getTotalInterestWithoutPrepayment()
                .subtract(result.getTotalInterestWithPrepayment())
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(0, result.getInterestSaved().compareTo(expectedSaved),
                "interestSaved should equal totalInterestWithout - totalInterestWith");
        assertTrue(result.getInterestSaved().compareTo(BigDecimal.ZERO) > 0,
                "interestSaved must be positive");
    }

    @Test
    @DisplayName("Very large prepayment effectively closes loan and sets new EMI to zero")
    void testFullPrepaymentClosesLoan() {
        BigDecimal principal = BigDecimal.valueOf(300_000);
        BigDecimal annualRate = BigDecimal.valueOf(9.0);
        int tenureInMonths = 120;
        int prepaymentMonth = 12;

        // Prepayment amount large enough to wipe out remaining principal
        BigDecimal prepaymentAmount = BigDecimal.valueOf(1_000_000);

        PrepaymentCalculator.PrepaymentResult result =
                PrepaymentCalculator.simulateEmiReduction(
                        principal,
                        annualRate,
                        tenureInMonths,
                        prepaymentMonth,
                        prepaymentAmount
                );

        // New EMI should be zero in this scenario
        assertEquals(
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                result.getNewEmi(),
                "New EMI should be zero when prepayment fully closes the loan"
        );

        // Interest with prepayment must be less than without
        assertTrue(result.getTotalInterestWithPrepayment()
                        .compareTo(result.getTotalInterestWithoutPrepayment()) < 0,
                "Total interest with full prepayment must be less than without prepayment");

        assertTrue(result.getInterestSaved().compareTo(BigDecimal.ZERO) > 0,
                "There must be some interest saved in full prepayment scenario");
    }

    @Test
    @DisplayName("Zero principal returns a zeroed PrepaymentResult")
    void testZeroPrincipal() {
        PrepaymentCalculator.PrepaymentResult result =
                PrepaymentCalculator.simulateEmiReduction(
                        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(8.5),
                        240,
                        12,
                        BigDecimal.valueOf(50_000)
                );

        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        assertEquals(zero, result.getOriginalPrincipal());
        assertEquals(zero, result.getOriginalEmi());
        assertEquals(zero, result.getNewEmi());
        assertEquals(zero, result.getTotalInterestWithoutPrepayment());
        assertEquals(zero, result.getTotalInterestWithPrepayment());
        assertEquals(zero, result.getInterestSaved());
        assertEquals(0, result.getOriginalTenureMonths());
    }

    @Nested
    @DisplayName("Input validation")
    class InputValidationTests {

        @Test
        @DisplayName("Negative principal throws IllegalArgumentException")
        void testNegativePrincipal() {
            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            BigDecimal.valueOf(-1),
                            BigDecimal.valueOf(8.5),
                            240,
                            12,
                            BigDecimal.valueOf(50_000)
                    ));
        }

        @Test
        @DisplayName("Zero or negative prepayment amount throws IllegalArgumentException")
        void testInvalidPrepaymentAmount() {
            BigDecimal principal = BigDecimal.valueOf(500_000);
            BigDecimal annualRate = BigDecimal.valueOf(8.5);
            int tenure = 240;
            int prepaymentMonth = 12;

            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            principal, annualRate, tenure, prepaymentMonth, BigDecimal.ZERO));

            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            principal, annualRate, tenure, prepaymentMonth, BigDecimal.valueOf(-10_000)));
        }

        @Test
        @DisplayName("Invalid prepayment month throws IllegalArgumentException")
        void testInvalidPrepaymentMonth() {
            BigDecimal principal = BigDecimal.valueOf(500_000);
            BigDecimal annualRate = BigDecimal.valueOf(8.5);
            int tenure = 240;

            // 0 month
            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            principal, annualRate, tenure, 0, BigDecimal.valueOf(50_000)));

            // greater than tenure
            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            principal, annualRate, tenure, 241, BigDecimal.valueOf(50_000)));
        }

        @Test
        @DisplayName("Null arguments throw IllegalArgumentException")
        void testNullArguments() {
            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            null, BigDecimal.valueOf(8.5), 240, 12, BigDecimal.valueOf(50_000)));

            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            BigDecimal.valueOf(500_000), null, 240, 12, BigDecimal.valueOf(50_000)));

            assertThrows(IllegalArgumentException.class,
                    () -> PrepaymentCalculator.simulateEmiReduction(
                            BigDecimal.valueOf(500_000), BigDecimal.valueOf(8.5),
                            240, 12, null));
        }
    }
}
