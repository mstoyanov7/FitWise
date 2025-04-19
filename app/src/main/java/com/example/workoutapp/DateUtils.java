package com.example.workoutapp;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.List;

public final class DateUtils {

    private DateUtils() {}

    /** Returns a Monday‑through‑Sunday list that contains the supplied day. */
    public static List<LocalDate> currentWeek(LocalDate today) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<LocalDate> list = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) { list.add(monday.plusDays(i)); }
        return list;
    }
}