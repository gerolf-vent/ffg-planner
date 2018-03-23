package de.vent_projects.ffg_planner.download.schedule.objects;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

/**
 * Die DownloadLesson ist nur für den Download des Stundenplans relevant.
 * Dabei sind alle Eigenschaften einer normalen Schulstunde enthalten.
 *
 * Wichtig für Realm: Es gibt keinen primary key, damit auch für 11/12 Klasse mehrere Stunden zur gleichen
 * Zeit abgespeichert werden können.
 *
 * Auch gibt es keine B-Stunden, da diese erst im Setup (mit dem de.vent_projects.ffg_planner.schedule.transferer.ScheduleTransferer)
 * miteinander verknüpft werden.
 */

@RealmClass
public class DownloadLesson extends RealmObject {
    @Index
    private int day, period;
    private DownloadSubject subject;
    private String room;

    public DownloadLesson() {
        this.subject = new DownloadSubject();
    }

    // GETTERS

    public int getDay() {
        return this.day;
    }

    public int getPeriod() {
        return this.period;
    }

    public String getName() {
        return this.subject.getName();
    }

    public String getTeacher() {
        return this.subject.getTeacher();
    }

    public String getRoom() {
        return getTrimmedString(this.room);
    }

    public boolean isFree(){
        return isStringBlank(this.subject.getName());
    }

    // SETTERS

    public void setDay(int day) {
        this.day = day;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setName(String name) {
        this.subject.setName(name);
    }

    public void setTeacher(String teacher) {
        this.subject.setTeacher(teacher);
    }

    public void setRoom(String room) {
        this.room = room;
    }

    // EINIGE STANDARD IMPLEMENTATIONEN

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DownloadLesson)) return false;
        DownloadLesson lesson = (DownloadLesson) obj;
        return  lesson.getPeriod() == this.getPeriod() &&
                lesson.getName().equals(this.getName()) &&
                lesson.getTeacher().equals(this.getTeacher()) &&
                lesson.getRoom().equals(this.getRoom()) &&
                lesson.getDay() == this.getDay();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDay()).append("-").append(getPeriod()).append(": ");
        if (isFree()) {
            sb.append("is free");
        } else {
            sb.append("name: ").append(getName()).append(", ")
                    .append("teacher: ").append(getTeacher()).append(", ")
                    .append("room: ").append(getRoom());
        }
        return sb.toString();
    }
}

