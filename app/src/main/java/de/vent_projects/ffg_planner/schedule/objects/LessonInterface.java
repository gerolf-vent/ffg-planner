package de.vent_projects.ffg_planner.schedule.objects;

public interface LessonInterface {
    int getDay();
    int getPeriod();
    PeriodTime getTime();
    String getName();
    String getCourse();
    String getTeacher();
    String getRoom();
    boolean isFree();
    LessonInterface getBlesson();
    boolean isABLesson();
}
