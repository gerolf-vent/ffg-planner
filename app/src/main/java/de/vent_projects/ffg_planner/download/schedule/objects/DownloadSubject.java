package de.vent_projects.ffg_planner.download.schedule.objects;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

import static de.vent_projects.ffg_planner.CommonUtils.getNumberInString;
import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;

@RealmClass
public class DownloadSubject extends RealmObject {
    private String name;
    private String teacher;

    public DownloadSubject(){}

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
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    // SOME STANDARD IMPLEMENTATION

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DownloadSubject)) return false;
        DownloadSubject subject = (DownloadSubject) obj;
        return  subject.getName().equals(this.getName()) &&
                subject.getTeacher().equals(this.getTeacher());
    }
}

