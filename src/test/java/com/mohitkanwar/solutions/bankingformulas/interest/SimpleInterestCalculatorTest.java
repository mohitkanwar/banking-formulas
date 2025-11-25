package com.mohitkanwar.solutions.bankingformulas.interest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimpleInterestCalculatorTest {

    @Test
    @DisplayName("Simple interest is computed correctly")
    void testSimpleInterest() {
        BigDecimal interest = SimpleInterestCalculator.calculateInterest(
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(2)
        );

        assertEquals(new BigDecimal("20000.00"), interest);
    }

    @Test
    @DisplayName("Maturity amount is computed correctly")
    void testAmount() {
        BigDecimal amount = SimpleInterestCalculator.calculateAmount(
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(1.5)
        );

        assertEquals(new BigDecimal("59000.00"), amount);
    }

    @Test
    @DisplayName("Zero principal or zero duration results in zero interest")
    void testZeroCases() {
        assertEquals(new BigDecimal("0.00"),
                SimpleInterestCalculator.calculateInterest(
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(10)
                ));

        assertEquals(new BigDecimal("0.00"),
                SimpleInterestCalculator.calculateInterest(
                        BigDecimal.valueOf(10000),
                        BigDecimal.valueOf(8),
                        BigDecimal.ZERO
                ));
    }

    @Test
    @DisplayName("Negative inputs throw IllegalArgumentException")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> SimpleInterestCalculator.calculateInterest(
                        BigDecimal.valueOf(-1),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(1)
                ));

        assertThrows(IllegalArgumentException.class,
                () -> SimpleInterestCalculator.calculateInterest(
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(-1)
                ));
    }

    @Test
    @DisplayName("Null inputs throw IllegalArgumentException")
    void testNullInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> SimpleInterestCalculator.calculateInterest(null, BigDecimal.ONE, BigDecimal.ONE));

        assertThrows(IllegalArgumentException.class,
                () -> SimpleInterestCalculator.calculateInterest(BigDecimal.ONE, null, BigDecimal.ONE));

        assertThrows(IllegalArgumentException.class,
                () -> SimpleInterestCalculator.calculateInterest(BigDecimal.ONE, BigDecimal.ONE, null));
    }
}
