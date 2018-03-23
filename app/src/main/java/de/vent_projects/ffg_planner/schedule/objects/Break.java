package de.vent_projects.ffg_planner.schedule.objects;

public class Break {
    private String name, time;

    public Break(String name, String timeStart, String timeEnd) {
        this.name = name;
        this.time = timeStart + " - " + timeEnd;
    }
    public Break(String name) {
        this.name = name;
    }

    public String getName() {
        return (name == null) ? "" : name.trim();
    }
    public String getTime() {
        return (time == null) ? "" : time.trim();
    }
}
