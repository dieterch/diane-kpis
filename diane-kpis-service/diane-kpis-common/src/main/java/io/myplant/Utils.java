package io.myplant;

import io.myplant.service.StateKpiCalculation.StateUtils;
import io.myplant.domain.AssetInformation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static long getTimeFromString(String dateString, boolean useDateString){
        if(useDateString){
            return Date.from(ZonedDateTime.parse(dateString).toInstant()).getTime();
        }
        else
            return Long.parseLong(dateString);
    }


    public static boolean getBoolenFromString(String boolString){
        return StringUtils.isNotEmpty(boolString) && (boolString.equals("1") || boolString.toLowerCase().equals("true"));
    }

    private static String dateFormatWithZone = "yyyy/MM/dd HH:mm:ss.SSS";
    //private static String dateFormatWithZone = "EEE dd/MM/yyyy HH:mm:ss.SSS";
    private static String dateFormatOffset = "Z";
    private static String dateFormat = "yyyy/MM/dd HH:mm:ss.SSS";
    //private static String dateFormat = "EEE dd/MM/yyyy HH:mm:ss.SSS";
    public static String getTimeAtZone(long timestamp, String timezone){
        if(StringUtils.isNoneEmpty(timezone)){
        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.LONG, new Locale("de"));
            DateFormat df = new SimpleDateFormat(dateFormatWithZone);
            df.setTimeZone(TimeZone.getTimeZone(timezone));
            return df.format(new Date(timestamp));
        }
        return null;
    }

    public static String getTimezoneOffset(long timestamp, String timezone){
        if(StringUtils.isNoneEmpty(timezone)){
            //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.LONG, new Locale("de"));
            DateFormat df = new SimpleDateFormat(dateFormatOffset);
            df.setTimeZone(TimeZone.getTimeZone(timezone));
            return df.format(new Date(timestamp));
        }
        return null;
    }

    public static String getTimeGmt(long timestamp) {
        DateFormat df = new SimpleDateFormat(dateFormat);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date(timestamp));
    }


    public static long getMonitoringStart(long assetId, AssetInformation assetInformation) {
        if(assetInformation == null)
            return 0;

        try {
            if (!StringUtils.isBlank(assetInformation.getRamStartDate())) {
                return StateUtils.getComissionDateTimestamp(assetInformation.getRamStartDate());
            }
            if (!StringUtils.isBlank(assetInformation.getCommissionDate())) {
                return StateUtils.getComissionDateTimestamp(assetInformation.getCommissionDate());
            }
        } catch (Exception ex) {
            logger.error("assetId " + assetId + ": commissioning date has wrong format: " + assetInformation.getCommissionDate(), ex);
        }
        return 0;
    }

    private static double MILLIS2HOUR = 1000*60*60;
    public static double millisToHour(long millis){
        return (double)millis / MILLIS2HOUR;
    }
    public static double millisToHour(double millis){
        return millis / MILLIS2HOUR;
    }

    public static String doubleToString(double value){
        return String.format(Locale.US, "%.4f", value);
    }

}
