package com.mohitkanwar.solutions.bankingformulas.interest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.mohitkanwar.solutions.bankingformulas.interest.RateConverter.*;
import static org.junit.jupiter.api.Assertions.*;

class RateConverterTest {

    @Test
    @DisplayName("Annual to monthly simple conversion (12% -> 1% per month)")
    void testAnnualToMonthlySimple() {
        BigDecimal annual = BigDecimal.valueOf(12.0);
        BigDecimal monthly = RateConverter.annualToMonthlySimple(annual);

        assertEquals(new BigDecimal("1.0000000000"), monthly);
    }

    @Test
    @DisplayName("Monthly to annual simple conversion (1% -> 12% per year)")
    void testMonthlyToAnnualSimple() {
        BigDecimal monthly = BigDecimal.valueOf(1.0);
        BigDecimal annual = RateConverter.monthlyToAnnualSimple(monthly);

        assertEquals(new BigDecimal("12.0000000000"), annual);
    }

    @Test
    @DisplayName("Percent <-> decimal conversion is symmetric")
    void testPercentDecimalRoundTrip() {
        BigDecimal ratePercent = BigDecimal.valueOf(8.5);
        BigDecimal decimal = RateConverter.percentToDecimal(ratePercent);
        BigDecimal backToPercent = RateConverter.decimalToPercent(decimal);

        assertEquals(0, ratePercent.compareTo(backToPercent),
                "percent -> decimal -> percent should recover original value (up to scale)");
    }

    @Test
    @DisplayName("Nominal to effective annual for 12% nominal, monthly compounding")
    void testNominalToEffectiveAnnual() {
        BigDecimal nominalAnnual = BigDecimal.valueOf(12.0); // 12% nominal
        int m = 12; // monthly

        BigDecimal effectiveAnnual = RateConverter.nominalToEffectiveAnnual(nominalAnnual, m);

        // Expected ≈ 12.6825%
        BigDecimal expected = new BigDecimal("12.6825030132");
        // compare at 4 decimal places
        assertEquals(0,
                effectiveAnnual.setScale(4, BigDecimal.ROUND_HALF_UP)
                        .compareTo(expected.setScale(4, BigDecimal.ROUND_HALF_UP)),
                "Effective annual rate should be close to expected value");
    }

    @Test
    @DisplayName("Effective to nominal annual for monthly compounding is roughly inverse")
    void testEffectiveToNominalAnnual() {
        BigDecimal effectiveAnnual = new BigDecimal("12.6825030132"); // ≈ from previous test
        int m = 12;

        BigDecimal nominalAnnual = RateConverter.effectiveAnnualToNominal(effectiveAnnual, m);

        // Should be approx 12%
        BigDecimal expected = new BigDecimal("12.0000000000");

        assertEquals(0,
                nominalAnnual.setScale(4, BigDecimal.ROUND_HALF_UP)
                        .compareTo(expected.setScale(4, BigDecimal.ROUND_HALF_UP)),
                "Nominal annual rate should be close to 12%");
    }

    @Test
    @DisplayName("Null inputs and invalid periods throw IllegalArgumentException")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> RateConverter.annualToMonthlySimple(null));

        assertThrows(IllegalArgumentException.class,
                () -> RateConverter.nominalToEffectiveAnnual(BigDecimal.TEN, 0));

        assertThrows(IllegalArgumentException.class,
                () -> RateConverter.effectiveAnnualToNominal(null, 12));

        assertThrows(IllegalArgumentException.class,
                () -> RateConverter.effectiveAnnualToNominal(BigDecimal.TEN, -1));
    }
}
