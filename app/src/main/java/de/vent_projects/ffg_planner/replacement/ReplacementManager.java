package de.vent_projects.ffg_planner.replacement;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.vent_projects.ffg_planner.CommonUtils;
import de.vent_projects.ffg_planner.TimeManager;
import de.vent_projects.ffg_planner.main.schedule.MainScheduleManager;
import de.vent_projects.ffg_planner.main.schedule.objects.MainSubject;
import de.vent_projects.ffg_planner.main.settings.MainSettingsManager;
import de.vent_projects.ffg_planner.realm.RealmManager;
import de.vent_projects.ffg_planner.replacement.objects.DateReplacement;
import de.vent_projects.ffg_planner.replacement.objects.Replacement;
import de.vent_projects.ffg_planner.settings.objects.SchoolClass;
import io.realm.Realm;
import io.realm.RealmResults;

import static de.vent_projects.ffg_planner.CommonUtils.isValidDateString;
import static de.vent_projects.ffg_planner.CommonUtils.makeOnlyDate;

public class ReplacementManager {
    private Context context;

    public ReplacementManager(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return this.context;
    }

    protected Realm getRealm() {
        return RealmManager.getRealm();
    }

    // GET REPLACEMENTS

    public RealmResults<DateReplacement> getAllDateReplacements() {
        return getRealm().where(DateReplacement.class).greaterThanOrEqualTo("date", new TimeManager(this.context).getCalendar().getTime()).beginGroup().isNotEmpty("replacements").or().isNotEmpty("info").endGroup().findAllSorted("date");
    }

    public DateReplacement getDateReplacement(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return getRealm().where(DateReplacement.class).equalTo("date", makeOnlyDate(calendar).getTime()).findFirst();
    }

    public DateReplacement getDateReplacementAsync(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return getRealm().where(DateReplacement.class).equalTo("date", makeOnlyDate(calendar).getTime()).findFirstAsync();
    }

    // DOWNLOAD REPLACEMENTS

    private void downloadReplacementFromServer(DownloadReplacementListener listener){
        Calendar calendar = new TimeManager(this.context).getCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Log.d("Download", this.getSubjectPattern());

        HashMap<String, String> links = new HashMap<>();
        try {
            URL url = new URL("http://www.ffg-dbr.de/plaene/vertretungsplan/vplan_links.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream responseData;
                    responseData = urlConnection.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(responseData));
                    String x;
                    x = r.readLine();
                    String responseString = "";
                    while(x != null){
                        responseString += x;
                        x = r.readLine();
                    }
                    Pattern patternData = Pattern.compile("<td><a(.*?)a></td>");
                    Pattern patternHref = Pattern.compile("vplan....-..-..\\.xml");
                    Pattern patternDate = Pattern.compile("..\\...\\.....");
                    Matcher matches = patternData.matcher(responseString);
                    Matcher href;
                    Matcher date;
                    String match;
                    while(matches.find()){
                        match = matches.group();
                        href = patternHref.matcher(match);
                        if (href.find()) {
                            date = patternDate.matcher(match);
                            if (date.find()){
                                links.put(date.group(), href.group());
                            }
                        }
                    }
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        try {
            for (Map.Entry<String, String> link : links.entrySet()) {
                if (!isValidDateString(link.getKey())) continue;
                Calendar then = CommonUtils.toCalendar(link.getKey());
                if (then != null && then.after(calendar)){
                    downloadReplacementForDateFromServer(link.getKey(), link.getValue());
                }
            }
            if (listener != null) listener.onFinished();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onError();
        }
    }

    private void downloadReplacementForDateFromServer(String date, String XMLFile) throws Exception {
        Log.d("Download", "Date: " + date + ", File: " + XMLFile);
        DateReplacement dateReplacement = new DateReplacement(date);
        String subjectPattern = this.getSubjectPattern();
        URL url = new URL("http://www.ffg-dbr.de/plaene/vertretungsplan/"+XMLFile);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseData = urlConnection.getInputStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                if (responseData != null) {
                    xpp.setInput(responseData, "UTF-8");
                    int eventType = xpp.getEventType();
                    String text = "";

                    String period = "";
                    String name = "";
                    boolean nameChanged = false;
                    String teacher = "";
                    boolean teacherChanged = false;
                    String room = "";
                    boolean roomChanged = false;
                    String info = "";
                    String schoolClass = "";
                    String dayInfo = "";

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String tagname = xpp.getName();
                        if (eventType == XmlPullParser.START_TAG) {
                            if (tagname.equals("aktion")) {
                                // Maybe empty all holders
                            } else if (tagname.equals("fach")) {
                                nameChanged = xpp.getAttributeValue(null, "fageaendert") != null;
                            } else if (tagname.equals("tehrer")) {
                                teacherChanged = xpp.getAttributeValue(null, "legeaendert") != null;
                            } else if (tagname.equals("raum")) {
                                roomChanged = xpp.getAttributeValue(null, "rageaendert") != null;
                            }
                        } else if (eventType == XmlPullParser.TEXT) {
                            text = xpp.getText();
                        } else if (eventType == XmlPullParser.END_TAG) {
                            switch (tagname) {
                                case "aktion":
                                    if (schoolClass.matches(subjectPattern) && !this.isReplacementWithInfoIgnored(info)) {
                                        if (period.contains("-")) {
                                            int minPeriod = Integer.parseInt(period.split("-")[0]);
                                            int maxPeriod = Integer.parseInt(period.split("-")[1]);
                                            for (int i = minPeriod; i <= maxPeriod; i++) {
                                                dateReplacement.addReplacement(new Replacement(date, i, name, nameChanged, teacher, teacherChanged, room, roomChanged, info, schoolClass));
                                            }
                                        } else {
                                            dateReplacement.addReplacement(new Replacement(date, Integer.parseInt(period), name, nameChanged, teacher, teacherChanged, room, roomChanged, info, schoolClass));
                                        }
                                    }
                                    break;
                                case "stunde": period = text; break;
                                case "fach": name = text; break;
                                case "lehrer": teacher = text; break;
                                case "raum": room = text; break;
                                case "info": info = text; break;
                                case "klasse": schoolClass = text; break;
                                case "titel": dateReplacement.setTitle(text); break;
                                case "aenderungk": dateReplacement.setSchoolClasses(text); break;
                                case "datum": dateReplacement.setLastChanges(text); break;
                                case "fussinfo": dayInfo += text.trim() + "\n"; break;
                            }
                        }
                        eventType = xpp.next();
                    }
                    dateReplacement.setInfo(dayInfo);
                    Realm realm = getRealm();
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(dateReplacement);
                    realm.commitTransaction();
                }
            }
        } catch (Exception e){
            urlConnection.disconnect();
            throw e;
        }
        urlConnection.disconnect();
    }

    private String getSubjectPattern() {
        RealmResults<MainSubject> names = new MainScheduleManager(this.context).getAllSubjects();
        SchoolClass schoolClass = new MainSettingsManager(this.context).getSchoolClass();
        String subjectPattern = "";
        if (schoolClass.isUpperLevel()) {
            subjectPattern += "(" + schoolClass.gradeToString() + "\\s*/\\s*([A-Z]-)?(";
            boolean hyphen = false;
            for (int i = 0; i < names.size(); i++) {
                if (hyphen) {
                    subjectPattern += "|";
                } else {
                    hyphen = true;
                }
                subjectPattern += names.get(i).getName();
            }
            subjectPattern += "))";
        } else {
            subjectPattern = ".*" + schoolClass.toString() + ".*";
        }
        return subjectPattern;
    }

    private boolean isReplacementWithInfoIgnored(String info) {
        return info.matches("(\\s*Känguru-Wettbewerb\\s*|\\s*Mathematikolympiade\\s*)");
    }

    private static class DownloadReplacementAsyncTask extends AsyncTask<Void, Void, Void> {
        private DownloadReplacementListener listener;
        private ReplacementManager replacementManager;
        private ConnectivityManager connectivityManager;

        public DownloadReplacementAsyncTask(ReplacementManager replacementManager, DownloadReplacementListener listener) {
            this.listener = listener;
            this.replacementManager = replacementManager;
            this.connectivityManager = (ConnectivityManager) replacementManager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        public Void doInBackground(Void ...voids) {
            if (connectivityManager == null ||
                    connectivityManager.getActiveNetworkInfo() == null ||
                    !connectivityManager.getActiveNetworkInfo().isConnected()) {
                if (this.listener != null) this.listener.onNoNetworkAvailable();
                return null;
            }

            this.replacementManager.downloadReplacementFromServer(this.listener);
            return null;
        }
    }

    public void downloadReplacementFromServerAsyc(DownloadReplacementListener listener) {
        new DownloadReplacementAsyncTask(this, listener).execute();
    }
}
