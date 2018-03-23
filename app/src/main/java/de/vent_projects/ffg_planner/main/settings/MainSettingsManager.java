package de.vent_projects.ffg_planner.main.settings;

import android.content.Context;

import java.util.Date;

import de.vent_projects.ffg_planner.settings.SettingsConfigurationManager;
import de.vent_projects.ffg_planner.settings.SettingsManager;
import de.vent_projects.ffg_planner.settings.objects.SchoolClass;

public class MainSettingsManager extends SettingsManager implements SettingsConfigurationManager {

    public MainSettingsManager(Context context) {
        super(context);
    }

    // SchoolClass and configuration

    @Override public SchoolClass getSchoolClass() {
        if (!getSharedPreferences().contains("main_school_class") || getSharedPreferences().getString("main_school_class", null) == null) return null;
        return new SchoolClass(getSharedPreferences().getString("main_school_class", ""));
    }
    @Override public void setSchoolClass(SchoolClass schoolClass) {
        getSharedPreferencesEditor().putString("main_school_class", schoolClass == null ? null : schoolClass.toString()).apply();
    }

    @Override public String getLanguage() {
        return getSharedPreferences().getString("main_language", null);
    }
    @Override public void setLanguage(String language) {
        getSharedPreferencesEditor().putString("main_language", language).apply();
    }

    @Override public String getBelieve() {
        return getSharedPreferences().getString("main_believe", null);
    }
    @Override public void setBelieve(String believe) {
        getSharedPreferencesEditor().putString("main_believe", believe).apply();
    }

    /* @Override public boolean isSpanish() {
        return getSharedPreferences().getBoolean("main_is_spanish", false);
    }
    @Override public void setSpanish(boolean isSpanish) {
        getSharedPreferencesEditor().putBoolean("main_is_spanish", isSpanish).apply();
    }*/

    @Override public String getThirdLanguage() {
        return getSharedPreferences().getString("main_third_language", "none");
    }
    @Override public void setThirdLanguage(String thirdLanguage) {
        getSharedPreferencesEditor().putString("main_third_language", thirdLanguage).apply();
    }

    // Schedule validity and upload date

    public Date getScheduleUploadDate() {
        long date = getSharedPreferences().getLong("main_upload_date", 0);
        return date != 0 ? new Date(date) : null;
    }
    public void setScheduleUploadDate(Date date) {
        if (date == null) {
            getSharedPreferencesEditor().remove("main_upload_date").apply();
        } else {
            getSharedPreferencesEditor().putLong("main_upload_date", date.getTime()).apply();
        }
    }

    public Date getScheduleValidityDate() {
        long date = getSharedPreferences().getLong("main_validity_date", 0);
        return date != 0 ? new Date(date) : null;
    }
    public void setScheduleValidityDate(Date date) {
        if (date == null) {
            getSharedPreferencesEditor().remove("main_validity_date").apply();
        } else {
            getSharedPreferencesEditor().putLong("main_validity_date", date.getTime()).apply();
        }
    }
}
