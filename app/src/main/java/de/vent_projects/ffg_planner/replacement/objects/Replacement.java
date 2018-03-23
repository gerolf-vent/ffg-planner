package de.vent_projects.ffg_planner.replacement.objects;

import de.vent_projects.ffg_planner.main.schedule.objects.MainLesson;
import de.vent_projects.ffg_planner.schedule.objects.LessonInterface;
import de.vent_projects.ffg_planner.schedule.objects.PeriodTime;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

import static de.vent_projects.ffg_planner.CommonUtils.getNumberInString;
import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

@RealmClass
public class Replacement extends RealmObject implements LessonInterface {
    @PrimaryKey
    private String ID;
    private int period;
    private String name, teacher, room;
    private boolean nameChanged, teacherChanged, roomChanged;
    private String info, schoolClass, date;

    @Deprecated
    public Replacement(){}

    public Replacement(String date, int period, String name, boolean nameChanged, String teacher, boolean teacherChanged, String room, boolean roomChanged, String info, String schoolClass){
        this.ID = getID(date, period);
        this.date = date;
        this.period = period;
        this.name = name;
        this.nameChanged = nameChanged;
        this.teacher = teacher;
        this.teacherChanged = teacherChanged;
        this.room = room;
        this.roomChanged = roomChanged;
        this.info = info;
        this.schoolClass = schoolClass;
    }

    // GETTERS

    @Override public int getDay() {
        return 0;
    }

    @Override public int getPeriod() {
        return this.period;
    }

    @Override public String getName() {
        return getTrimmedString(this.name);
    }

    @Override public String getCourse(){
        return getNumberInString(this.name);
    }

    @Override public String getTeacher() {
        return getTrimmedString(this.teacher);
    }

    @Override public String getRoom() {
        return getTrimmedString(this.room);
    }

    @Override public PeriodTime getTime() {
        return MainLesson.getTime(this.getPeriod());
    }

    @Override public boolean isFree(){
        return this.isLessonCanceled() || isStringBlank(this.name);
    }

    @Override public boolean isABLesson(){
        return false;
    }

    @Override public LessonInterface getBlesson() {
        return null;
    }

    public String getDate() {
        return this.date;
    }

    public String getInfo() {
        return getTrimmedString(this.info);
    }

    public String getSchoolClass(){
        return getTrimmedString(this.schoolClass).toLowerCase();
    }

    private boolean isLessonCanceled(){
        return getName().contains("--");
    }

    private boolean isLessonTeacherless(){
        return getInfo().matches("Aufg(aben|\\.)");
    }
    // TODO: Add "Selbst" and contains

    // SETTERS

    @Deprecated
    public void setDate(String date) {

    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    // ID MANAGEMENT

    public static String getID(String date, int period) {
        return date + "-" + period;
    }

    // SOME STANDARD IMPLEMENTATION

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Replacement)) return false;
        Replacement replacement = (Replacement) obj;
        return  replacement.getPeriod() == this.getPeriod() &&
                        replacement.getName().equals(this.getName()) &&
                        replacement.getTeacher().equals(this.getTeacher()) &&
                        replacement.getRoom().equals(this.getRoom()) &&
                        replacement.getDate().equals(this.getDate()) &&
                        replacement.getInfo().equals(this.getInfo());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDay()).append("-").append(getPeriod()).append(": ");
        if (isLessonTeacherless()) {
            sb.append("is teacherless");
        } else {
            sb.append("name: ").append(getName()).append(", ")
                    .append("teacher: ").append(getTeacher()).append(", ")
                    .append("room: ").append(getRoom())
                    .append("info: ").append(getInfo());
        }

        return sb.toString();
    }
}

