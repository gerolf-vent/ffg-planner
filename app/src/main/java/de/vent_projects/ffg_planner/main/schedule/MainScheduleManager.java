package de.vent_projects.ffg_planner.main.schedule;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.main.schedule.objects.MainLesson;
import de.vent_projects.ffg_planner.main.schedule.objects.MainSubject;
import de.vent_projects.ffg_planner.main.settings.MainSettingsManager;
import de.vent_projects.ffg_planner.schedule.ScheduleManager;
import de.vent_projects.ffg_planner.schedule.objects.Break;
import de.vent_projects.ffg_planner.settings.objects.SchoolClass;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;
import io.realm.Realm;
import io.realm.RealmResults;

public  class MainScheduleManager extends ScheduleManager {
    private static final String TAG = MainScheduleManager.class.getSimpleName();

    private MainSettingsManager settingsManager;

    public MainScheduleManager(Context context) {
        super(context);
        this.settingsManager = new MainSettingsManager(context);
    }

    // GET SCHEDULE, LESSONS & SUBJECTS

    public RealmResults<MainLesson> getAllLessons() {
        return getRealm().where(MainLesson.class).findAll();
    }
    public RealmResults<MainLesson> getAllLessonsAsync() {
        return getRealm().where(MainLesson.class).findAllAsync();
    }

    public RealmResults<MainLesson> getLessonsForDay(int day) {
        return getRealm().where(MainLesson.class).equalTo("day", day).not().endsWith("ID", "-B").findAllSorted("period");
    }
    public RealmResults<MainLesson> getLessonsForDayAsync(int day) {
        return getRealm().where(MainLesson.class).equalTo("day", day).not().endsWith("ID", "-B").findAllSortedAsync("period");
    }

    public MainLesson getLessonForDayAndPeriod(int day, int period) {
        return getRealm().where(MainLesson.class).equalTo("ID", MainLesson.getID(day, period, false)).findFirst();
    }
    public MainLesson getLessonForDayAndPeriodAsync(int day, int period) {
        return getRealm().where(MainLesson.class).equalTo("ID", MainLesson.getID(day, period, false)).findFirstAsync();
    }

    public RealmResults<MainSubject> getAllSubjects() {
        return getRealm().where(MainSubject.class).isNotEmpty("name").findAll();
    }

    public Date getUploadDate() {
        return this.settingsManager.getScheduleUploadDate();
    }

    public Date getValidityDate() {
        return this.settingsManager.getScheduleValidityDate();
    }

    // SETUP & UPDATE SCHEDULE

    public void clearSchedule() {
        Realm realm = getRealm();
        realm.beginTransaction();
        realm.delete(MainLesson.class);
        realm.delete(MainSubject.class);
        realm.commitTransaction();
        setUploadDate(null);
        setValidityDate(null);
    }

    public void replaceSchedule(List<SetupLesson> lessons) {
        clearSchedule();
        Realm realm = getRealm();
        realm.beginTransaction();
        for (SetupLesson lesson: lessons) {
            realm.copyToRealmOrUpdate(new MainLesson(lesson, false));
        }
        realm.commitTransaction();
    }

    public void setUploadDate(Date date) {
        this.settingsManager.setScheduleUploadDate(date);
    }

    public void setValidityDate(Date date) {
        this.settingsManager.setScheduleValidityDate(date);
    }

    // GET BREAKS

    public ArrayList<Break> getDefaultBreaks() {
        ArrayList<Break> breaks = new ArrayList<>();
        breaks.add(new Break(getContext().getResources().getString(R.string.word_break_morning), "09:20", "09:45"));
        breaks.add(new Break(getContext().getResources().getString(R.string.word_break), "11:15", "11:35"));
        breaks.add(new Break(getContext().getResources().getString(R.string.word_break_lunch), "13:05", "13:45"));
        return breaks;
    }

    // CHECK FOR AN UPDATED SCHEDULE OR AN NEEDED UPDATE

    /* Must be called by an async threat */
    public void isScheduleUpToDate(SchoolClass schoolClass, ScheduleUpToDateListener listener) {
        String urlStr;
        if (schoolClass.isUpperLevel()){
            urlStr = "http://www.ffg-dbr.de/plaene/stundenplan/iiplank"+Integer.toString(schoolClass.getGrade())+".xml";
        } else {
            urlStr = "http://www.ffg-dbr.de/plaene/stundenplan/iiplank"+Integer.toString(schoolClass.getGrade())+Character.toUpperCase(schoolClass.getName())+".xml";
        }
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream responseData = urlConnection.getInputStream();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();
                    if (responseData != null) {
                        xpp.setInput(responseData, "ISO-8859-1");
                        int eventType = xpp.getEventType();
                        String text = "";
                        Date uploadDate = null;
                        Date validityDate = null;
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            String tag_name = xpp.getName();
                            if (eventType == XmlPullParser.TEXT) {
                                text = xpp.getText();
                            } else if (tag_name.equals("datum")) {
                                uploadDate = DateFormat.getDateInstance().parse(text);
                            } else if (tag_name.equals("gueltigab")) {
                                validityDate = DateFormat.getDateInstance().parse(text);
                            }
                            eventType = xpp.next();
                        }
                        listener.onChecked(getUploadDate() != null && getUploadDate().equals(uploadDate), uploadDate, validityDate);
                    } else {
                        listener.onError();
                    }
                } else {
                    listener.onError();
                }
            } catch (XmlPullParserException e){
                listener.onError();
                e.printStackTrace();
            } catch (IOException e){
                listener.onError();
                e.printStackTrace();
            } catch (ParseException e) {
                listener.onError();
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e){
            listener.onError();
            e.printStackTrace();
        }
    }

    public boolean needConfiguration() {
        return getAllLessons().isEmpty();
    }
}
