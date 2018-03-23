package de.vent_projects.ffg_planner.main.schedule.objects;

import de.vent_projects.ffg_planner.schedule.objects.LessonInterface;
import de.vent_projects.ffg_planner.schedule.objects.PeriodTime;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

@RealmClass
public class MainLesson extends RealmObject implements LessonInterface {
	@PrimaryKey
    @Required
	private String ID;
    @Index
    private int day, period;
    private MainSubject subject;
    private String room;
    private MainLesson blesson;

    @Deprecated
    public MainLesson() {}

    public MainLesson(SetupLesson lesson, boolean isBlesson) {
        this(lesson.getDay(), lesson.getPeriod(), isBlesson);
        this.setName(lesson.getName());
        this.setTeacher(lesson.getTeacher());
        this.setRoom(lesson.getRoom());
        if (lesson.isABLesson() &&
                !(this.getName().equals(lesson.getBlesson().getName()) &&
                  this.getTeacher().equals(lesson.getBlesson().getTeacher()) &&
                  this.getRoom().equals(lesson.getBlesson().getRoom()))) {
            this.setBlesson(new MainLesson(lesson.getBlesson(), true));
        }
    }

    public MainLesson(int day, int period, String name, String teacher, String room, boolean isBlesson) {
        this(day, period, isBlesson);
        this.setName(name);
        this.setTeacher(teacher);
        this.setRoom(room);
    }

    /* Must be called */
    public MainLesson(int day, int period, boolean isBlesson) {
        this.day = day;
        this.period = period;
        this.ID = getID(day, period, isBlesson);
        this.subject = new MainSubject();
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
        switch (period){
            case 1:  return new PeriodTime(7, 50);
            case 2:  return new PeriodTime(8, 35);
            case 3:  return new PeriodTime(9, 45);
            case 4:  return new PeriodTime(10, 30);
            case 5:  return new PeriodTime(11, 35);
            case 6:  return new PeriodTime(12, 20);
            case 7:  return new PeriodTime(14, 0);
            case 8:  return new PeriodTime(14, 45);
            default: return new PeriodTime(0, 0);
        }
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

    @Override public boolean isABLesson(){
        return this.blesson != null;
    }

    @Override public MainLesson getBlesson() {
        return blesson;
    }

    // SETTERS

    public void setRoom(String room) {
        this.room = room;
    }

    public void setName(String name) {
        this.subject.setName(name);
    }

    public void setTeacher(String teacher) {
        this.subject.setTeacher(teacher);
    }

    public void setBlesson(MainLesson lesson) {
        if (lesson != null && !lesson.isFree()) {
            this.blesson = lesson;
        } else {
            this.blesson = null;
        }
    }

    public void makeFree() {
        this.subject.setName(null);
        this.subject.setTeacher(null);
        this.setRoom(null);
    }

    // ID MANAGEMENT

    public static String getID(int day, int period, boolean isBlesson) {
        return day + "-" + period + (isBlesson ? "-B" : "");
    }

    // SOME STANDARD IMPLEMENTATION

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MainLesson)) return false;
        MainLesson lesson = (MainLesson) obj;
        return  lesson.getPeriod() == this.getPeriod() &&
                lesson.getName().equals(this.getName()) &&
                lesson.getTeacher().equals(this.getTeacher()) &&
                lesson.getRoom().equals(this.getRoom()) &&
                lesson.getDay() == this.getDay() &&
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

