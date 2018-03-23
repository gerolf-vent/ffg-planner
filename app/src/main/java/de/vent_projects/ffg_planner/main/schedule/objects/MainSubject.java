package de.vent_projects.ffg_planner.main.schedule.objects;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

import static de.vent_projects.ffg_planner.CommonUtils.getNumberInString;
import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

@RealmClass
public class MainSubject extends RealmObject {
    @PrimaryKey
    private String ID;
    private String name;
    private String teacher;

    public MainSubject() {
        this.updateID();
    }

    public MainSubject(String name, String teacher){
        this.name = name;
        this.teacher = teacher;
        this.updateID();
    }

    // GETTERS

    public String getName() {
        return getTrimmedString(this.name);
    }

    public String getCourse(){
        return getNumberInString(this.name);
    }

    public String getTeacher() {
        return getTrimmedString(this.teacher);
    }

    // SETTERS

    public void setName(String name) {
        this.name = name;
        this.updateID();
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
        this.updateID();
    }

    // ID MANAGEMENT

    private void updateID() {
        if (isStringBlank(this.name)) {
            this.ID = UUID.randomUUID().toString();
        } else {
            this.ID = this.name + "-" + this.teacher;
        }
    }

    // SOME STANDARD IMPLEMENTATION

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MainSubject)) return false;
        MainSubject subject = (MainSubject) obj;
        return  subject.getName().equals(this.getName()) &&
                subject.getTeacher().equals(this.getTeacher());
    }
}

