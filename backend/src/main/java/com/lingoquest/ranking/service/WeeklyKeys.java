package com.lingoquest.ranking.service;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

/** Shared definition of the "current week" bucket, so both backends agree. */
final class WeeklyKeys {

    private WeeklyKeys() {}

    static String current() {
        var today = LocalDate.now();
        int year = today.get(ChronoField.YEAR);
        int week = today.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        return "lingo:rank:weekly:" + year + "-W" + String.format("%02d", week);
    }
}
