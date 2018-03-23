package de.vent_projects.ffg_planner.schedule;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.schedule.objects.LessonInterface;
import de.vent_projects.ffg_planner.settings.SettingsManager;

public class ScheduleNameManager {
    private SettingsManager settingsManager;

    public ScheduleNameManager(Context context) {
        this.settingsManager = new SettingsManager(context);
    }

    private Context getContext() {
        return this.settingsManager.getContext();
    }

    private String getString(int resourceID) {
        return getContext().getString(resourceID);
    }

    private String[] getStringArray(int resourceID) {
        return getContext().getResources().getStringArray(resourceID);
    }

    // GET FULL NAMES OF SUBJECTS, TEACHERS AND ROOMS

    private String getFullSubjectNameOfLesson(LessonInterface lesson) {
        String suffix = "";

        if (lesson.getName().toLowerCase().endsWith("fö")) {
            suffix = getString(R.string.subject_suffix_remedial);
        }

        String[] subjectAbbreviations = getStringArray(R.array.subject_abbreviations);
        String[] subjectNames = getStringArray(R.array.subject);

        for (int i = 0; i < subjectAbbreviations.length; i++) {
            Pattern pattern = Pattern.compile("^" + subjectAbbreviations[i] + "(a[1-9]|h[1-9]|[1-9]|fö|)$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(lesson.getName());
            if (matcher.find()){
                return subjectNames[i] + suffix;
            }
        }
        return lesson.getName();
    }
    public String getDisplayedSubjectNameOfLesson(LessonInterface lesson) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getFullSubjectNameOfLesson(lesson));
        if (lesson.isABLesson()) {
            stringBuilder.append(" / ").append(getFullSubjectNameOfLesson(lesson.getBlesson()));
        }
        return stringBuilder.toString();
    }

    public String getFullTeacherNameOfLesson(LessonInterface lesson) {
        String prefix = "";
        String suffix = "";
        if (lesson.getTeacher().contains("(")) {
            prefix = "(";
            suffix = ")";
        }
        String teacher = lesson.getTeacher().replaceAll("[()*]", "");
        try {
            JSONObject teacherNames = new JSONObject(this.getTeacherNamesJSON());
            if (teacherNames.has(teacher)) {
                return prefix + teacherNames.getString(teacher) + suffix;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prefix + teacher + suffix;
    }
    public String getDisplayedTeacherNameOfLesson(LessonInterface lesson) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getFullTeacherNameOfLesson(lesson));
        if (lesson.isABLesson()) {
            stringBuilder.append(" / ").append(getFullTeacherNameOfLesson(lesson.getBlesson()));
        }
        return stringBuilder.toString();
    }

    public String getDisplayedRoomNameOfLesson(LessonInterface lesson) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lesson.getRoom());
        if (lesson.isABLesson()) {
            stringBuilder.append(" / ").append(lesson.getBlesson().getRoom());
        }
        return stringBuilder.toString();
    }

    // GET & SET TEACHER NAMES

    private String getTeacherNamesJSON() {
        return this.settingsManager.getSharedPreferences().getString("teacher_names", "{}");
    }
    private void setTeacherNamesJSON(String json) {
        this.settingsManager.getSharedPreferencesEditor().putString("teacher_names", json);
    }

    // DOWNLOAD FULL NAMES OF TEACHERS

    /* Must be called by an async threat */
    public void downloadTeacherNamesFromServer() {
        // TODO: Implement
    }
}
