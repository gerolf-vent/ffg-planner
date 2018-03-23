package de.vent_projects.ffg_planner.main.schedule;

import java.util.Date;

public interface ScheduleUpToDateListener {
    void onChecked(boolean isUpToDate, Date uploadDate, Date validityDate);
    void onError();
}
