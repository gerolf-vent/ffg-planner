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

/**
 * Der DownloadScheduleManager hat verschiedene Aufgaben:
 *  - Herunterladen des Stundenplanes
 *  - Abfragen in Realm
 *  - Vorbereiten der Daten für das Setup
 */

public class DownloadScheduleManager extends ScheduleManager {
    private static final String TAG = DownloadScheduleManager.class.getSimpleName();

    private DownloadSettingsManager settingsManager;
    private SettingsConfigurationManager settingsConfigurationManager;

    public DownloadScheduleManager(@NonNull Context context, @NonNull SettingsConfigurationManager settingsConfigurationManager) {
        super(context);
        this.settingsManager = new DownloadSettingsManager(context);
        this.settingsConfigurationManager = settingsConfigurationManager;
    }

    // ABFRAGEN DER STUNDEN

    // Gibt alle Stunden für einen Tag und eine Stunde aus
    public RealmResults<DownloadLesson> getAllLessonsForDayAndPeriod(int day, int period) {
        return getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).findAll();
    }

    /*
     * Gibt alle A-Stunden aus.
     * Dies ist nicht nur für die 10. Klassen relevant!
     */
    public RealmResults<DownloadLesson> getAllALessonsForDayAndPeriod(int day, int period) {
        return  getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).not().contains("subject.teacher", "**").findAll();
    }

    // Gibt alle B-Stunden aus.
    public RealmResults<DownloadLesson> getAllBLessonsForDayAndPeriod(int day, int period) {
        return  getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).contains("subject.teacher", "**").findAll();
    }

    // Hier wird abgefragt, ob die jeweilige Stunde eine A/B-Stunde ist
    public boolean isABLessonForDayAndPeriod(int day, int period) {
        return getRealm().where(DownloadLesson.class).equalTo("day", day).equalTo("period", period).contains("subject.teacher", "*").count() > 0;
    }

    /*
     * Hier wird nach gleichen Stundennamen gesucht.
     * Dies ist für die automatische Auswahl der Kurse im Setup notwendig, jedoch nur für die Oberstufe relevant.
     */
    public RealmResults<DownloadLesson> getLessonsWithSimilarSubjectName(String name) {
        if (isStringBlank(name)) return null;
        return getRealm().where(DownloadLesson.class).equalTo("subject.name", name).findAll();
    }

    /*
     * Dies ist eine extrem wichtige Funktion!
     * Sie steuert, ob eine Stunde als optional angesehen wird. Dies ist z.B. bei Wahlpflicht der Fall oder bei Förderunterricht.
     * Wenn die Funktion true zurückgibt, wird beim Transferer (de.vent_project.ffg-planner.schedule.transferer.ScheduleTransferer) diese Stunde
     * ignoriert und alle anderen Stunden bevorzugt behandelt. Für weitere Informationen bitte die Kommentare des ScheduleTransferers ansehen.
     */
    public boolean isLessonPossible(DownloadLesson lesson) {
        return  lesson.getName().toLowerCase().contains("fö") ||
                lesson.getName().toLowerCase().contains("wp") ||
                lesson.getName().equalsIgnoreCase("lrs") ||
                lesson.getName().equals("daz");
    }

    // BEARBEITUNG DER STUNDEN

    /* Löscht die alle heruntergeladen Stundenplan-Daten
     * TODO: Hochlade- und Gültigkeitsdatum ebenfalls löschen (DownloadSettingsManager)
     */
    private void clearSchedule() {
        Realm realm = getRealm();
        realm.beginTransaction();
        realm.delete(DownloadLesson.class);
        realm.delete(DownloadSubject.class);
        realm.commitTransaction();
        realm.close();
    }

    // ENTFERNUNG UNAUSGEWÄHLTER FÄCHER

    /*
     * Dies ist nur für das Setup relevant.
     * Diese Funktion entfernt alle im CommonFragment (de.vent_projects.ffg_planner.setup.ui.common.SetupCommonFragment)
     * nicht ausgewählten optionen und erleichtert somit die Arbeit des ScheduleTransferers (de.vent_project.ffg-planner.schedule.transferer.ScheduleTransferer).
     */
    public void removeUnselectedSubjects() {
        SchoolClass schoolClass = this.settingsConfigurationManager.getSchoolClass();
        if (schoolClass.isUpperLevel()) return;

        ArrayList<String> unselectedSubjectNames = new ArrayList<>();
        unselectedSubjectNames.addAll(Arrays.asList(getContext().getResources().getStringArray(R.array.options_languages_values)));
        unselectedSubjectNames.remove(settingsConfigurationManager.getLanguage());
        unselectedSubjectNames.addAll(Arrays.asList(getContext().getResources().getStringArray(R.array.options_believes_values)));
        unselectedSubjectNames.remove(settingsConfigurationManager.getBelieve());

        /*
         * Es gibt nun an der Schule neben Spanisch auch Griechisch als Drittsprache.
         * Hier wird also die nicht ausgewählte Sprache entfernt oder beide falls keine zweite Fremdsprache ausgewählt wurde.
         * Dies ist auch nur für die 10. Klassen relevant. Hier jedoch aus kompatibilitätsgründen schon gleich auf alle Klassen angewandt.
         * Falls es bei den Drittsprachen Änderungen gibt muss diese Stelle und das SetupCommonFragment (de.vent_projects.ffg_planner.setup.ui.common.SetupCommonFragment)
         * bearbeitet werden.
         */
        unselectedSubjectNames.addAll(Arrays.asList(getContext().getResources().getStringArray(R.array.options_third_languages_values)));
        unselectedSubjectNames.remove("none");
        unselectedSubjectNames.remove(settingsConfigurationManager.getThirdLanguage());
        /*
         * Hier ist aus Einfachheit die alte Vorgehensweise noch einmal auskommentiert.
         *
         * if (schoolClass.getGrade() == 10 && !settingsConfigurationManager.isSpanish()) {
         *    unselectedSubjectNames.add(getContext().getString(R.string.lesson_spanish_abbreviation));
        }*/

        Realm realm = getRealm();
        realm.beginTransaction();
        realm.where(DownloadLesson.class).in("subject.name", unselectedSubjectNames.toArray(new String[]{})).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    // HERUNTERLADEN DES STUNDENPLANES

    /*
     * Hier wird der Stundenplan heruntergeladen und in direkt in Realm abgespeichert.
     *
     * Bitte diese Funktion niemals direkt benutzen sondern deren asynchrone variante (siehe unten), ansonsten wird der Main-Thread oder
     * der Thread, der gerade genutzt wird blockiert, bis der Download komplett ist oder abgebrochen wurde.
     *
     * Dabei gibt es für den Stundenplan der Sekundarstufe 1 & 2 unterschiedliche Namensformate, welche unten
     * separat gebildet werden.
     *
     * Als Parser der XML-Dateien wird der XMLPullParser genutzt, welcher bei Events (z.B. bei einem Öffnen-/Schließen-Tag,
     * Text oder Attributen) verschieden reagieren kann. Durch dieses Prinzip werden erst die Daten in die DownloadLesson
     * eingefügt und beim Schließen-Tag die DownloadLesson abgespeichert.
     */
    private void downloadScheduleFromServer(DownloadScheduleListener listener){
        clearSchedule();
        Realm realm = getRealm();
        String urlStr;
        SchoolClass schoolClass = this.settingsConfigurationManager.getSchoolClass();
        //Generierung der URL
        if (schoolClass.isUpperLevel()){
            urlStr = "http://www.ffg-dbr.de/plaene/stundenplan/iiplank"+Integer.toString(schoolClass.getGrade())+".xml";
        } else {
            urlStr = "http://www.ffg-dbr.de/plaene/stundenplan/iiplank"+Integer.toString(schoolClass.getGrade())+Character.toUpperCase(schoolClass.getName())+".xml";
        }
        try {
            // Aufbauen der Verbindung
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Vorbereiten des Ausgabestreams und des XMLPullParsers
                    InputStream responseData = urlConnection.getInputStream();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();
                    if (responseData != null) {
                        // Eingabe der Daten in den XMLPullParser
                        xpp.setInput(responseData, "ISO-8859-1");
                        int eventType = xpp.getEventType();
                        // Dies sind Zwischenspeicher für die Daten während des parsens.
                        String text = "";
                        DownloadLesson lesson = new DownloadLesson();
                        Integer period = 1;
                        // Realm wird zum schreiben geöffnet, um die DownloadLessons direkt abspeichern zu können.
                        realm.beginTransaction();
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            String tagName = xpp.getName();
                            if (eventType == XmlPullParser.START_TAG) {
                                if (tagName.contains("tag")) {
                                    Matcher dayNumber = Pattern.compile("[0-9]+").matcher(tagName);
                                    lesson = new DownloadLesson();
                                    if (dayNumber.find()) {
                                        lesson.setDay(Integer.parseInt(dayNumber.group()) + 1);
                                        lesson.setPeriod(period);
                                    } else {
                                        Log.e(TAG, "No day number was found in XML tag '" + tagName + "'");
                                    }
                                }
                            } else if (eventType == XmlPullParser.TEXT) {
                                text = xpp.getText();
                            } else if (eventType == XmlPullParser.END_TAG) {
                                if (tagName.equals("stunde")) {
                                    period = Integer.parseInt(text);
                                } else if (tagName.equals("fach")) {
                                    lesson.setName(text);
                                } else if (tagName.equals("lehrer")) {
                                    lesson.setTeacher(text);
                                } else if (tagName.equals("raum")) {
                                    lesson.setRoom(text);
                                } else if (tagName.equals("gruppe")) {
                                    if (!isStringBlank(text)) {
                                        lesson.setName(text);
                                    }
                                } else if (tagName.contains("tag")) {
                                    Matcher dayNumber = Pattern.compile("[0-9]+").matcher(tagName);
                                    if (dayNumber.find()) {
                                        if (Integer.parseInt(dayNumber.group()) <= 5) {
                                            realm.copyToRealm(lesson);
                                        }
                                    }
                                } else if (tagName.equals("datum")) {
                                    // Hier wird das Hochlade-Datum des Stundenplans abgespeichert.
                                    this.settingsManager.setScheduleUploadDate(CommonUtils.toDate(text));
                                } else if (tagName.equals("gueltigab")) {
                                    // Hier wird das Fültigkeits-Datum des Stundenplanes abgespeichert, welches den Tag des In-Kraft-tretens beschreibt.
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

    /*
     * Hier wird der Download des Stundenplans asynchron im Hintergrund durchgeführt.
     * Dabei wird gleich auch die Connectivität geprüft, um keine sinnlosen Request zu senden.
     */
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

    /*
     * Dies hier ist die nutzbare Funktion zum download des Stundenplans
     * Sie startet einen AsyncTask, welcher dann den Download durchführt.
     */
    public void downloadScheduleFromServerAsync(DownloadScheduleListener listener) {
        new DownloadScheduleAsyncTask(this, listener).execute();
    }
}
