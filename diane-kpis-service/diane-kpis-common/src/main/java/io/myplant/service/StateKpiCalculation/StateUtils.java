package io.myplant.service.StateKpiCalculation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

public class StateUtils {
    private static final int ONE_DAY_IN_SECONDS = 86400;
    private static final String dailyPattern = "yyyyMMdd";
    private static final String monthlyPattern ="yyyyMM";


    public static long getTimestampEndOfDay(long timestamp){
        return getTimestampStartOfDay(timestamp) + ((ONE_DAY_IN_SECONDS-1)*1000L + 999);
    }

    public static long getTimestampStartOfDay(long timestamp){
        LocalDate localDate = Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).toLocalDate();
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static long getTimestampEndOfMonth(long timestamp) {
        return 0;
    }


    public static String getDailyString(long timestamp) {
        return getKeyWith(dailyPattern, new Date(timestamp));
    }

    public static String getDailyString(Date date) {
        return getKeyWith(dailyPattern, date);
    }

    public static String getMonthlyString(long timestamp) {
        return getKeyWith(monthlyPattern, new Date(timestamp));
    }

    public static String getMonthlyString(Date date) {
        return getKeyWith(monthlyPattern, date);
    }

    private static String getKeyWith(String pattern, Date date) {
        DateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static long getComissionDateTimestamp(String commissionDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsedDate = dateFormat.parse(commissionDate);
        return parsedDate.getTime();
    }


}
