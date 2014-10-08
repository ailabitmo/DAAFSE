package ru.ifmo.ailab.daafse.streampublisher.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ParserUtils {

    public static final String DAY = "Timestamp__Date";
    public static final String HOUR = "Timestamp__Hour";
    public static final String MINUTE = "Timestamp__Min";
    public static final String SECONDS = "Timestamp__Sec";

    public static long toTimestamp(String date, int hour, int min, int sec) {
        String[] d = date.split("-");
        Calendar c = new GregorianCalendar(
                Integer.parseInt("20" + d[2]),
                Integer.parseInt(d[1]),
                Integer.parseInt(d[0]),
                hour, min, sec);
        return c.toInstant().toEpochMilli();
    }

}
