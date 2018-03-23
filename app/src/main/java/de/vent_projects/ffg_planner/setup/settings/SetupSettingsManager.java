package de.vent_projects.ffg_planner.setup.settings;

import android.content.Context;

import de.vent_projects.ffg_planner.main.settings.MainSettingsManager;
import de.vent_projects.ffg_planner.settings.SettingsConfigurationManager;
import de.vent_projects.ffg_planner.settings.SettingsManager;
import de.vent_projects.ffg_planner.settings.objects.SchoolClass;

public class SetupSettingsManager extends SettingsManager implements SettingsConfigurationManager {
    
    public SetupSettingsManager(Context context) {
        super(context);
    }

    // SchoolClass and configuration

    @Override public SchoolClass getSchoolClass() {
        if (!getSharedPreferences().contains("setup_school_class") || getSharedPreferences().getString("setup_school_class", null) == null) return null;
        return new SchoolClass(getSharedPreferences().getString("setup_school_class", ""));
    }
    @Override public void setSchoolClass(SchoolClass schoolClass) {
        getSharedPreferencesEditor().putString("setup_school_class", schoolClass == null ? null : schoolClass.toString()).apply();
    }

    @Override public String getLanguage() {
        return getSharedPreferences().getString("setup_language", null);
    }
    @Override public void setLanguage(String language) {
        getSharedPreferencesEditor().putString("setup_language", language).apply();
    }

    @Override public String getBelieve() {
        return getSharedPreferences().getString("setup_believe", null);
    }
    @Override public void setBelieve(String believe) {
        getSharedPreferencesEditor().putString("setup_believe", believe).apply();
    }

    /* @Override public boolean isSpanish() {
        return getSharedPreferences().getBoolean("setup_is_spanish", false);
    }
    @Override public void setSpanish(boolean isSpanish) {
        getSharedPreferencesEditor().putBoolean("setup_is_spanish", isSpanish).apply();
    }*/

    @Override public String getThirdLanguage() {
        return getSharedPreferences().getString("setup_third_language", "none");
    }
    @Override public void setThirdLanguage(String thirdLanguage) {
        getSharedPreferencesEditor().putString("setup_third_language", thirdLanguage).apply();
    }

    public void removeConfiguration() {
        getSharedPreferencesEditor()
                .remove("setup_school_class")
                .remove("setup_language")
                .remove("setup_believe")
                .remove("setup_is_spanish")
                .apply();
    }

    public void saveConfigration() {
        MainSettingsManager settingsManager = new MainSettingsManager(getContext());
        settingsManager.setSchoolClass(this.getSchoolClass());
        settingsManager.setLanguage(this.getLanguage());
        settingsManager.setBelieve(this.getBelieve());
        // settingsManager.setSpanish(this.isSpanish());
        settingsManager.setThirdLanguage(this.getThirdLanguage());
        this.removeConfiguration();
    }
}
