package de.vent_projects.ffg_planner.schedule.objects;

import java.util.Locale;

public class PeriodTime {
    private int minute;
    private int hour;

    public PeriodTime(int hour, int minute){
        this.minute = minute;
        this.hour = hour;
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    @Override
    public String toString() {
        return String.format(Locale.GERMANY, "%02d:%02d", this.hour, this.minute);
    }

    private PeriodTime addTime(int hours, int minutes){
        return new PeriodTime((this.hour+hours)%24+((this.minute+minutes)/60), (this.minute+minutes)%60);
    }

    public PeriodTime getNextPeriodTime() {
        return addTime(0, 45);
    }
}
