package com.mohitkanwar.solutions.bankingformulas.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmortizationScheduleCalculatorTest {

    @Test
    @DisplayName("Schedule size equals tenure and last balance is zero")
    void testScheduleSizeAndFinalBalance() {
        BigDecimal principal = BigDecimal.valueOf(500_000);
        BigDecimal annualRate = BigDecimal.valueOf(8.5);
        int tenureInMonths = 240;

        List<AmortizationScheduleCalculator.Installment> schedule =
                AmortizationScheduleCalculator.generateSchedule(principal, annualRate, tenureInMonths);

        assertEquals(tenureInMonths, schedule.size(), "Schedule length must equal tenure");

        AmortizationScheduleCalculator.Installment last = schedule.get(schedule.size() - 1);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                last.getClosingBalance(), "Final closing balance must be zero");
    }

    @Test
    @DisplayName("All installments have same EMI as LoanEmiCalculator")
    void testEmiConsistency() {
        BigDecimal principal = BigDecimal.valueOf(500_000);
        BigDecimal annualRate = BigDecimal.valueOf(8.5);
        int tenureInMonths = 240;

        BigDecimal expectedEmi = LoanEmiCalculator.calculateEmi(principal, annualRate, tenureInMonths);
        List<AmortizationScheduleCalculator.Installment> schedule =
                AmortizationScheduleCalculator.generateSchedule(principal, annualRate, tenureInMonths);

        for (AmortizationScheduleCalculator.Installment inst : schedule) {
            assertEquals(expectedEmi, inst.getEmi(), "EMI must remain constant for all periods");
        }
    }

    @Test
    @DisplayName("Sum of principal components approximately equals original principal")
    void testPrincipalSum() {
        BigDecimal principal = BigDecimal.valueOf(500_000);
        BigDecimal annualRate = BigDecimal.valueOf(8.5);
        int tenureInMonths = 240;

        List<AmortizationScheduleCalculator.Installment> schedule =
                AmortizationScheduleCalculator.generateSchedule(principal, annualRate, tenureInMonths);

        BigDecimal sumPrincipal = schedule.stream()
                .map(AmortizationScheduleCalculator.Installment::getPrincipalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal expected = principal.setScale(2, RoundingMode.HALF_UP);

        assertEquals(expected, sumPrincipal,
                "Sum of principal components should match original principal (up to rounding)");
    }

    @Test
    @DisplayName("Zero principal returns empty schedule")
    void testZeroPrincipal() {
        List<AmortizationScheduleCalculator.Installment> schedule =
                AmortizationScheduleCalculator.generateSchedule(
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(10),
                        12
                );

        assertTrue(schedule.isEmpty(), "Zero principal should produce an empty schedule");
    }
}
