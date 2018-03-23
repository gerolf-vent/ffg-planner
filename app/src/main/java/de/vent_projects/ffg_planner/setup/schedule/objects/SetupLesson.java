package de.vent_projects.ffg_planner.setup.schedule.objects;

import de.vent_projects.ffg_planner.download.schedule.objects.DownloadLesson;
import de.vent_projects.ffg_planner.main.schedule.objects.MainLesson;
import de.vent_projects.ffg_planner.schedule.objects.LessonInterface;
import de.vent_projects.ffg_planner.schedule.objects.PeriodTime;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

@RealmClass
public class SetupLesson extends RealmObject implements LessonInterface, RealmChangeListener<SetupLesson> {
    @PrimaryKey
    @Required
    public String ID;
    @Index
    private int day, period;
    private SetupSubject subject = new SetupSubject(); // Because of the empty-no-arguments constructor
    private String room;
    private SetupLesson blesson;

    private boolean isMarkedAsEmpty;

    // Sorry, but Realm needs an empty constructor with no arguments
    @Deprecated
    public SetupLesson() {

    }

    public SetupLesson(DownloadLesson lesson, boolean isBlesson) {
        this(lesson.getDay(), lesson.getPeriod(), isBlesson);
        this.setName(lesson.getName());
        this.setTeacher(lesson.getTeacher().replaceAll("\\*", ""));
        this.setRoom(lesson.getRoom());
    }

    /* Must be called */
    public SetupLesson(int day, int period, boolean isBlesson) {
        this.day = day;
        this.period = period;
        this.ID = getID(day, period, isBlesson);
    }

    // GETTERS

    @Override public int getDay() {
        return this.day;
    }

    @Override public int getPeriod() {
        return this.period;
    }

    @Override public PeriodTime getTime() {
        return getTime(this.getPeriod());
    }

    public static PeriodTime getTime(int period){
        return MainLesson.getTime(period);
    }

    @Override public String getName() {
        return this.subject.getName();
    }

    @Override public String getCourse(){
        return this.subject.getCourse();
    }

    @Override public String getTeacher() {
        return this.subject.getTeacher();
    }

    @Override public String getRoom() {
        return getTrimmedString(this.room);
    }

    @Override public boolean isFree(){
        return isStringBlank(this.subject.getName());
    }

    @Override public SetupLesson getBlesson() {
        return blesson;
    }

    @Override public boolean isABLesson(){
        return this.blesson != null;
    }

    public boolean isMarkedAsEmpty() {
        return this.isMarkedAsEmpty;
    }

    // SETTERS

    @Deprecated
    public void setDay(int day) {
        this.day = day;
    }

    @Deprecated
    public void setPeriod(int period) {
        this.period = period;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setName(String name) {
        this.subject.setName(name);
    }

    public void setTeacher(String teacher) {
        this.subject.setTeacher(teacher);
    }

    public void setBlesson(SetupLesson lesson) {
        if (lesson != null && (lesson.isMarkedAsEmpty() || (!lesson.isMarkedAsEmpty() && !lesson.isFree()))) {
            this.blesson = lesson;
        } else {
            this.blesson = null;
        }
    }

    /* notify change, when blesson changes */
    @Override
    public void onChange(SetupLesson lesson) {
        notify();
    }

    public void makeFree() {
        this.subject.setName(null);
        this.subject.setTeacher(null);
        this.setRoom(null);
    }

    public void markAsEmpty() {
        this.isMarkedAsEmpty = true;
    }

    public void unmarkAsEmpty() {
        this.isMarkedAsEmpty = false;
    }

    // ID MANAGEMENT

    public static String getID(int day, int period, boolean isBlesson) {
        return day + "-" + period + (isBlesson ? "-B" : "");
    }

    // SOME STANDARD IMPLEMENTATION

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SetupLesson)) return false;
        SetupLesson lesson = (SetupLesson) obj;
        return  lesson.getDay() == this.getDay() &&
                lesson.getPeriod() == this.getPeriod() &&
                lesson.getName().equals(this.getName()) &&
                lesson.getTeacher().equals(this.getTeacher()) &&
                lesson.getRoom().equals(this.getRoom()) &&
                lesson.getBlesson() == this.getBlesson();
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
        if (isABLesson()) {
            sb.append("; Blesson: ").append(getBlesson().toString());
        }
        return sb.toString();
    }
}

