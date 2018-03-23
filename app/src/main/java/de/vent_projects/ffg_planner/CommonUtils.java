package de.vent_projects.ffg_planner;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    // STRINGS

    public static boolean isStringBlank(String string) {
        return string == null || !string.matches("(?s).*([a-zA-Z0-9]|ö|ü|ä).*");
    }

    public static boolean isValidDateString(String string){
        return !isStringBlank(string) && string.matches("..\\...\\.....");
    }

    public static String getTrimmedString(String string) {
        return isStringBlank(string) ? "" : string.trim();
    }

    public static String getNumberInString(String string) {
        if (!isStringBlank(string)) {
            Matcher matcher = Pattern.compile("[0-9]+").matcher(string);
            if (matcher.find()){
                return matcher.group();
            }
        }
        return "";
    }

    // DATE AND CALENDAR

    public static String toDateString(Calendar calendar) {
        return String.format(Locale.GERMANY, "%1$td.%1$tm.%1$tY", calendar);
    }

    public static Calendar toCalendar(String date) {
        if (!isValidDateString(date)) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("\\.")[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.split("\\.")[1]) - 1);
        calendar.set(Calendar.YEAR, Integer.parseInt(date.split("\\.")[2]));
        makeOnlyDate(calendar);
        return calendar;
    }

    public static Calendar makeOnlyDate(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Date toDate(Calendar calendar) {
        return (calendar == null) ? null : calendar.getTime();
    }

    public static Date toDate(String date) {
        return toDate(toCalendar(date));
    }

    // INFO

    public static String getAttentionInInfo(String info) {
        Matcher matcher = Pattern.compile("ACHTUNG!(.*\\n)+", Pattern.CASE_INSENSITIVE).matcher(info + "\n");
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static String getAttentionWithoutTitle(String attention) {
        return attention.replaceAll("ACHTUNG!.*\\n", "");
    }
}
