package de.vent_projects.ffg_planner.setup.schedule;

import android.content.Context;

import java.util.List;

import de.vent_projects.ffg_planner.schedule.ScheduleManager;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupSubject;
import io.realm.Realm;
import io.realm.RealmResults;

public class SetupScheduleManager extends ScheduleManager {

    public SetupScheduleManager(Context context) {
        super(context);
    }

    /* Used in adapting a new schedule with current subjects */
    /*public ArrayList<MainLesson> getLessonsFilteredByCurrentSubjects(ArrayList<MainLesson> lessonsToSearch) {
        ArrayList<MainLesson> lessons = new ArrayList<>();
        RealmResults<MainSubject> subjects = getAllSubjects();
        for (MainLesson lesson: lessonsToSearch) {
            for (MainSubject subject : subjects) {
                if (!isStringBlank(lesson.getName()) && lesson.getName().equals(subject.getName())) {
                    lessons.add(lesson);
                    break;
                }
            }
        }
        return lessons;
    }*/

    // TRANSACTIONS

    public void beginTransaction() {
        getRealm().beginTransaction();
    }

    public void commitTransaction() {
        getRealm().commitTransaction();
    }

    // GET LESSONS

    public RealmResults<SetupLesson> getAllLessons() {
        return getRealm().where(SetupLesson.class).findAll();
    }

    public SetupLesson getLessonForDayAndPeriod(int day, int period) {
        return getLessonForDayAndPeriod(day, period, false);
    }

    public SetupLesson getLessonForDayAndPeriod(int day, int period, boolean isBlesson) {
        Realm realm = getRealm();
        SetupLesson lesson = realm.where(SetupLesson.class).equalTo("ID", SetupLesson.getID(day, period, isBlesson)).findFirst();
        if (lesson == null) {
            lesson = createEmptyLessonForDayAndPeriod(day, period, isBlesson);
        }
        return lesson;
    }

    private SetupLesson createEmptyLessonForDayAndPeriod(int day, int period, boolean isBlesson) {
        Realm realm = getRealm();
        realm.beginTransaction();
        SetupLesson lesson = realm.createObject(SetupLesson.class, SetupLesson.getID(day, period, isBlesson));
        //noinspection deprecation
        lesson.setDay(day);
        //noinspection deprecation
        lesson.setPeriod(period);
        lesson.markAsEmpty();
        realm.commitTransaction();
        return lesson;
    }

    public boolean areAllLessonsNonEmpty() {
        return getRealm().where(SetupLesson.class).equalTo("isMarkedAsEmpty", true).count() == 0;
    }

    /* public SetupLesson createLessonFromDownloadLesson(DownloadLesson downloadLesson, boolean isBlesson) {
        Realm realm = getRealm();
        realm.beginTransaction();
        SetupLesson lesson = realm.createObject(SetupLesson.class, SetupLesson.getID(downloadLesson.getDay(), downloadLesson.getPeriod(), isBlesson));
        lesson.setDay(downloadLesson.getDay());
        lesson.setPeriod(downloadLesson.getPeriod());
        lesson.setName(downloadLesson.getName());
        lesson.setTeacher(downloadLesson.getTeacher());
        lesson.setRoom(downloadLesson.getRoom());
        lesson.unmarkAsEmpty();
        realm.commitTransaction();
        return lesson;
    }*/

    // SETUP SCHEDULE MANAGER INTERFACE

    public void setLesson(SetupLesson lesson) {
        Realm realm = getRealm();
        realm.beginTransaction();
        SetupLesson setupLesson = this.getLessonForDayAndPeriod(lesson.getDay(), lesson.getPeriod());
        setupLesson.setName(lesson.getName());
        setupLesson.setTeacher(lesson.getTeacher());
        setupLesson.setRoom(lesson.getRoom());
        if (lesson.isMarkedAsEmpty()) {
            setupLesson.markAsEmpty();
        } else {
            setupLesson.unmarkAsEmpty();
        }
        realm.commitTransaction();
    }

    public void setLessons(List<SetupLesson> lessons) {
        Realm realm = getRealm();
        realm.beginTransaction();
        for (SetupLesson lesson : lessons) {
            realm.copyToRealmOrUpdate(lesson);
        }
        realm.commitTransaction();
    }

    // CLEAR SCHEDULE

    public void clearSchedule() {
        Realm realm = getRealm();
        RealmResults<SetupLesson> lessons = realm.where(SetupLesson.class).findAll();
        realm.beginTransaction();
        for (SetupLesson lesson : lessons) {
            lesson.markAsEmpty();
        }
        realm.commitTransaction();
    }

    public void purgeSchedule() {
        Realm realm = getRealm();
        realm.beginTransaction();
        realm.delete(SetupLesson.class);
        realm.delete(SetupSubject.class);
        realm.commitTransaction();
    }
}
