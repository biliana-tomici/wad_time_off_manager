package com.project.timeOffManager.enums;

import java.time.LocalDate;

public enum TimePeriod {
    WEEKLY,
    MONTHLY,
    YEARLY;

    public LocalDate getStartDate() {
        return switch (this) {
            case WEEKLY -> LocalDate.now().minusWeeks(1);
            case MONTHLY -> LocalDate.now().minusMonths(1);
            case YEARLY -> LocalDate.now().minusYears(1);
            default -> throw new IllegalArgumentException("Unknown time period: " + this);
        };
    }
}
