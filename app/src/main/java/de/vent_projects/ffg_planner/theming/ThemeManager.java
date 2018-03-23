package de.vent_projects.ffg_planner.theming;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.settings.SettingsManager;

public class ThemeManager {
    private SettingsManager settingsManager;

    public ThemeManager(Context context) {
        this(new SettingsManager(context));
    }

    public ThemeManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public Themes getCurrentTheme() {
        return this.settingsManager.getCurrentTheme();
    }

    public void setCurrentTheme(Themes theme) {
        this.settingsManager.setCurrentTheme(theme);
    }

    public void applyTheme(Activity activity) {
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(activity, R.anim.fade_in, R.anim.fade_out);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static int getActivityThemeID(Themes theme) {
        switch (theme) {
            case DARK: return R.style.AppTheme_Dark;
        }
        return R.style.AppTheme;
    }

    public int getActivityThemeID() {
        return getActivityThemeID(getCurrentTheme());
    }

    public static int getActivityWithToolbarThemeID(Themes theme) {
        switch (theme) {
            case DARK: return R.style.AppTheme_Dark_NoActionBar;
        }
        return R.style.AppTheme_NoActionBar;
    }

    public int getActivityWithToolbarThemeID() {
        return getActivityWithToolbarThemeID(getCurrentTheme());
    }

}
