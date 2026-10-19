package com.project.timeOffManager.service;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class LeaveDayCalculator {

    public static long calculateWorkingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        long days = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }
}
