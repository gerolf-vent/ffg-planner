package de.vent_projects.ffg_planner.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.vent_projects.ffg_planner.theming.Themes;

public class SettingsManager {
    private Context context;

    public SettingsManager(Context context) {
        this.context = context;
    }

    // ACCESS TO CONTEXT

    public Context getContext() {
        return this.context;
    }

    // PREFERENCES AMD PREFERENCE EDITOR

    public SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }

    // COMMON SETTINGS

    // Theme

    public Themes getCurrentTheme() {
        return Themes.valueOf(getSharedPreferences().getString("current_theme", Themes.LIGHT.name()));
    }
    public void setCurrentTheme(Themes theme) {
        getSharedPreferencesEditor().putString("current_theme", theme.name()).apply();
    }
}
