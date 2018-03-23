package de.vent_projects.ffg_planner.replacement.objects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.vent_projects.ffg_planner.CommonUtils;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

import static de.vent_projects.ffg_planner.CommonUtils.getTrimmedString;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

@RealmClass
public class DateReplacement extends RealmObject {
    @PrimaryKey
    private long ID;
    private Date date;
    private String schoolClasses = "", lastChanges = "", title = "", info = "";
    private RealmList<Replacement> replacements;

    @Deprecated
    public DateReplacement(){
        this.replacements = new RealmList<>();
    }

    public DateReplacement(String date) {
        this.date = CommonUtils.toDate(date);
        this.ID = getID(this.date);
        this.replacements = new RealmList<>();
    }

    public DateReplacement(String date, ArrayList<Replacement> replacements, String schoolClasses, String lastChanges, String title, String info) {
        this(date);
        this.replacements = new RealmList<>();
        this.replacements.addAll(replacements);
        this.schoolClasses = schoolClasses;
        this.lastChanges = lastChanges;
        this.title = title;
        this.info = info;
    }

    // GETTERS

    public RealmList<Replacement> getReplacements(){
        return this.replacements;
    }

    public Date getDate() {
        return this.date;
    }

    public String getDateString(){
        if (this.date == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(this.date);
    }

    public String getSchoolClasses() {
        return this.schoolClasses;
    }

    public String getLastChanges() {
        return this.lastChanges;
    }

    public String getTitle() {
        return this.title;
    }

    public String getInfo(){
        return getTrimmedString(this.info);
    }

    public boolean isReplacement(){
        return this.replacements != null && this.replacements.size() > 0;
    }

    public boolean isInfo(){
        return this.info != null && !isStringBlank(this.info);
    }

    public boolean isEmpty() {
        return !(this.isReplacement() && this.isInfo());
    }

    // SETTERS

    public void addReplacement(Replacement replacement) {
        this.replacements.add(replacement);
    }

    public void setSchoolClasses(String schoolClasses) {
        this.schoolClasses = schoolClasses;
    }

    public void setLastChanges(String lastChanges) {
        this.lastChanges = lastChanges;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInfo(String info) {
        this.info = isStringBlank(info) ? "" : info;
    }

    // ID MANAGEMENT

    public static long getID(Date date) {
        return date.getTime();
    }

    // SOME STANDARD IMPLEMENTATION

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DateReplacement)) return false;
        DateReplacement dateReplacement = (DateReplacement) obj;
        return dateReplacement.ID == this.ID;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Date: ").append(this.date);
        stringBuilder.append("\nInfo: ").append(this.info);
        return stringBuilder.toString();
    }
}
