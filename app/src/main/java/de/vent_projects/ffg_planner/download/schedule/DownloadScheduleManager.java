package de.vent_projects.ffg_planner.download.schedule;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.vent_projects.ffg_planner.CommonUtils;
import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.download.schedule.objects.DownloadLesson;
import de.vent_projects.ffg_planner.download.schedule.objects.DownloadSubject;
import de.vent_projects.ffg_planner.download.settings.DownloadSettingsManager;
import de.vent_projects.ffg_planner.schedule.ScheduleManager;
import de.vent_projects.ffg_planner.settings.SettingsConfigurationManager;
import de.vent_projects.ffg_planner.settings.objects.SchoolClass;
import io.realm.Realm;
import io.realm.RealmResults;

import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class DownloadScheduleManager extends ScheduleManager {
    private static final String TAG = DownloadScheduleManager.class.getSimpleName();

    private DownloadSettingsManager settingsManager;
    private SettingsConfigurationManager settingsConfigurationManager;

    public DownloadScheduleManager(@NonNull Context context, @NonNull SettingsConfigurationManager settingsConfigurationManager) {
        super(context);
        this.settingsManager = new DownloadSettingsManager(context);
        this.settingsConfigurationManager = settingsConfigurationManager;
    }

    // GET LESSONS

    public RealmResults<DownloadLesson> getAllLessonsForDayAndPeriod(int day, int period) {
        return getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).findAll();
    }

    public RealmResults<DownloadLesson> getAllALessonsForDayAndPeriod(int day, int period) {
        return  getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).not().contains("subject.teacher", "**").findAll();
    }

    public RealmResults<DownloadLesson> getAllBLessonsForDayAndPeriod(int day, int period) {
        return  getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).contains("subject.teacher", "**").findAll();
    }

    public boolean isABLessonForDayAndPeriod(int day, int period) {
        return getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).contains("subject.teacher", "*").count() > 0;
    }

    public RealmResults<DownloadLesson> getLessonsWithSimilarSubjectName(String name) {
        if (isStringBlank(name)) return null;
        return getRealm().where(DownloadLesson.class).equalTo("subject.name", name).findAll();
    }

    // SOME SUBJECTS ARE POSSIBLE

    public boolean isLessonPossible(DownloadLesson lesson) {
        return  lesson.getName().toLowerCase().contains("fö") ||
                lesson.getName().toLowerCase().contains("wp") ||
                lesson.getName().equalsIgnoreCase("lrs") ||
                lesson.getName().equals("daz");
    }

    // SET LESSONS

    private void clearSchedule() {
        Realm realm = getRealm();
        realm.beginTransaction();
        realm.delete(DownloadLesson.class);
        realm.delete(DownloadSubject.class);
        realm.commitTransaction();
        realm.close();
    }

    // REMOVE UNSELECTED SUBJECTS

    public void removeUnselectedSubjects() {
        SchoolClass schoolClass = this.settingsConfigurationManager.getSchoolClass();
        if (schoolClass.isUpperLevel()) return;

        ArrayList<String> unselectedSubjectNames = new ArrayList<>();
        unselectedSubjectNames.addAll(Arrays.asList(getContext().getResources().getStringArray(R.array.options_languages_values)));
        unselectedSubjectNames.remove(settingsConfigurationManager.getLanguage());
        unselectedSubjectNames.addAll(Arrays.asList(getContext().getResources().getStringArray(R.array.options_believes_values)));
        unselectedSubjectNames.remove(settingsConfigurationManager.getBelieve());
        unselectedSubjectNames.addAll(Arrays.asList(getContext().getResources().getStringArray(R.array.options_third_languages_values)));
        unselectedSubjectNames.remove("none");
        unselectedSubjectNames.remove(settingsConfigurationManager.getThirdLanguage());
        /*if (schoolClass.getGrade() == 10 && !settingsConfigurationManager.isSpanish()) {
            unselectedSubjectNames.add(getContext().getString(R.string.lesson_spanish_abbreviation));
        }*/

        Realm realm = getRealm();
        realm.beginTransaction();
        realm.where(DownloadLesson.class).in("subject.name", unselectedSubjectNames.toArray(new String[]{})).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    // DOWNLOAD SCHEDULE FROM SCHOOL SERVER

    private void downloadScheduleFromServer(DownloadScheduleListener listener){
        clearSchedule();
        Realm realm = getRealm();
        String urlStr;
        SchoolClass schoolClass = this.settingsConfigurationManager.getSchoolClass();
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
                        DownloadLesson lesson = new DownloadLesson();
                        Integer period = 1;
                        realm.beginTransaction();
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            String tag_name = xpp.getName();
                            if (eventType == XmlPullParser.START_TAG) {
                                if (tag_name.contains("tag")) {
                                    Matcher dayNumber = Pattern.compile("[0-9]+").matcher(tag_name);
                                    lesson = new DownloadLesson();
                                    if (dayNumber.find()) {
                                        lesson.setDay(Integer.parseInt(dayNumber.group()) + 1);
                                        lesson.setPeriod(period);
                                    } else {
                                        Log.e(TAG, "No day number is available in XML tag '" + tag_name + "'");
                                    }
                                }
                            } else if (eventType == XmlPullParser.TEXT) {
                                text = xpp.getText();
                            } else if (eventType == XmlPullParser.END_TAG) {
                                if (tag_name.equals("stunde")) {
                                    period = Integer.parseInt(text);
                                } else if (tag_name.equals("fach")) {
                                    lesson.setName(text);
                                } else if (tag_name.equals("lehrer")) {
                                    lesson.setTeacher(text);
                                } else if (tag_name.equals("raum")) {
                                    lesson.setRoom(text);
                                } else if (tag_name.equals("gruppe")) {
                                    if (!isStringBlank(text)) {
                                        lesson.setName(text);
                                    }
                                } else if (tag_name.contains("tag")) {
                                    Matcher dayNumber = Pattern.compile("[0-9]+").matcher(tag_name);
                                    if (dayNumber.find()) {
                                        if (Integer.parseInt(dayNumber.group()) <= 5) {
                                            realm.copyToRealm(lesson);
                                        }
                                    }
                                } else if (tag_name.equals("datum")) {
                                    this.settingsManager.setScheduleUploadDate(CommonUtils.toDate(text));
                                } else if (tag_name.equals("gueltigab")) {
                                    this.settingsManager.setScheduleValidityDate(CommonUtils.toDate(text));
                                }
                            }
                            eventType = xpp.next();
                        }
                        listener.onFinished();
                    } else {
                        listener.onUnknownError();
                    }
                } else {
                    listener.onNetworkError(urlConnection.getResponseCode(), urlConnection.getResponseMessage());
                }
            } catch (XmlPullParserException e){
                listener.onParseError();
                e.printStackTrace();
            } catch (SocketException e) {
                listener.onBadNetwork();
                e.printStackTrace();
            } catch (IOException e){
                listener.onUnknownError();
                e.printStackTrace();
            } finally {
                if (realm.isInTransaction()) {
                    realm.commitTransaction();
                }
                urlConnection.disconnect();
            }
        } catch (UnknownHostException e) {
            listener.onBadNetwork();
            e.printStackTrace();
        } catch (IOException e){
            listener.onUnknownError();
            e.printStackTrace();
        }
    }

    private static class DownloadScheduleAsyncTask extends AsyncTask<Void, Void, Void> {
        private DownloadScheduleListener listener;
        private DownloadScheduleManager scheduleManager;
        private ConnectivityManager connectivityManager;

        public DownloadScheduleAsyncTask(DownloadScheduleManager scheduleManager, DownloadScheduleListener listener) {
            this.listener = listener;
            this.scheduleManager = scheduleManager;
            this.connectivityManager = (ConnectivityManager) scheduleManager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        public Void doInBackground(Void ...voids) {
            if (connectivityManager == null ||
                    connectivityManager.getActiveNetworkInfo() == null ||
                    !connectivityManager.getActiveNetworkInfo().isConnected()) {
                if (this.listener != null) this.listener.onNoNetworkAvailable();
                return null;
            }

            this.scheduleManager.downloadScheduleFromServer(this.listener);
            return null;
        }
    }

    public void downloadScheduleFromServerAsync(DownloadScheduleListener listener) {
        new DownloadScheduleAsyncTask(this, listener).execute();
    }
}
