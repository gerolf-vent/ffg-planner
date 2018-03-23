package de.vent_projects.ffg_planner;

import android.content.Context;

import java.util.Calendar;
import java.util.Locale;

import de.vent_projects.ffg_planner.settings.SettingsManager;

import static de.vent_projects.ffg_planner.CommonUtils.isValidDateString;
import static de.vent_projects.ffg_planner.CommonUtils.makeOnlyDate;

public class TimeManager {
    private Context context;
    private SettingsManager settingsManager;

    public TimeManager(Context context) {
        this.context = context;
        this.settingsManager = new SettingsManager(context);
    }

    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        /*if (BuildConfig.DEBUG && settingsManager.getSharedPreferences().getBoolean(context.getString(R.string.pref_dev_manipulate_time), context.getResources().getBoolean(R.bool.pref_dev_manipulate_time_default))){
            String date = getPreferences(context).getString(context.getString(R.string.pref_dev_date), context.getString(R.string.pref_dev_date_default));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("\\.")[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(date.split("\\.")[1])-1);
            calendar.set(Calendar.YEAR, Integer.parseInt(date.split("\\.")[2]));
            String time = getPreferences(context).getString(context.getString(R.string.pref_dev_time), context.getString(R.string.pref_dev_time_default));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
        }*/
        return calendar;
    }

    public String getRelativeDate(String date) {
        if (!isValidDateString(date)) return date;
        Calendar now = getCalendar();
        makeOnlyDate(now);
        Calendar then = CommonUtils.toCalendar(date);
        makeOnlyDate(then);
        if (then == null) return date;
        int dayDifference = getDayDifference(then, now);
        int weekDifference = getWeekDifference(dayDifference, now.get(Calendar.DAY_OF_WEEK));
        int dayOfWeek = then.get(Calendar.DAY_OF_WEEK);
        if (weekDifference < 0) weekDifference *= -1;
        if (dayDifference == 0) {
            return context.getString(R.string.week_today);
        } else if (dayDifference == 1) {
            return context.getString(R.string.week_tomorrow);
        } else if (weekDifference == 0) {
            return context.getString(getDayOfWeekName(dayOfWeek));
        } else if (weekDifference == 1) {
            if (context.getResources().getConfiguration().locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
                return context.getString(R.string.rel_week_next) + " " + context.getString(getDayOfWeekName(dayOfWeek));
            } else {
                return context.getString(getDayOfWeekName(dayOfWeek)) + ", " + context.getString(R.string.rel_week_next);
            }
        } else {
            return String.format(Locale.getDefault(), context.getString(R.string.rel_week_common), context.getString(getDayOfWeekName(dayOfWeek)), weekDifference);
        }
    }

    private int getDayOfWeekName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return R.string.week_monday;
            case Calendar.TUESDAY: return R.string.week_tuesday;
            case Calendar.WEDNESDAY: return R.string.week_wednesday;
            case Calendar.THURSDAY: return R.string.week_thursday;
            case Calendar.FRIDAY: return R.string.week_friday;
            case Calendar.SATURDAY: return R.string.week_saturday;
            case Calendar.SUNDAY: return R.string.week_sunday;
            default: return R.string.word_error;
        }
    }

    private int getDayDifference(Calendar greatest, Calendar smallest) {
        Long diff = (greatest.getTimeInMillis() - smallest.getTimeInMillis());
        return diff.intValue() / (1000 * 60 * 60 * 24);
    }

    private int getWeekDifference(int dayDifference, int dayOfWeek) {
        return (dayDifference + convertDayOfWeek(dayOfWeek)) / 7;
    }

    private int convertDayOfWeek(int dayOfWeek) {
        return (dayOfWeek < 2) ? dayOfWeek + 5 : dayOfWeek - 2;
    }
}
