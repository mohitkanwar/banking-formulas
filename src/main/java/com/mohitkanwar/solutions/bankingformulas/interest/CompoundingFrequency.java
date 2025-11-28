package com.mohitkanwar.solutions.bankingformulas.interest;

public enum CompoundingFrequency {
        ANNUAL(1),
        SEMI_ANNUAL(2),
        QUARTERLY(4),
        MONTHLY(12),
        DAILY(365);

        private final int periodsPerYear;

        CompoundingFrequency(int periodsPerYear) {
            this.periodsPerYear = periodsPerYear;
        }

        public int getPeriodsPerYear() {
            return periodsPerYear;
        }
    }