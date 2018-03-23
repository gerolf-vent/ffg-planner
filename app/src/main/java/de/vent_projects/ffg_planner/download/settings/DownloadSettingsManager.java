package de.vent_projects.ffg_planner.download.settings;

import android.content.Context;

import java.util.Date;

import de.vent_projects.ffg_planner.settings.SettingsManager;

/**
 * Dieser SettingsManager kümmert sich um die Einstellungen des Downloadbereiches.
 * Er ist ein Wrapper für die SharedPreferences und bietet spezielle Abfragemöglichkeiten.
 */

public class DownloadSettingsManager extends SettingsManager {

    public DownloadSettingsManager(Context context) {
        super(context);
    }

    // STUNDENPLAN DATEN

    // Diese Funktionen sind für das Hochlade-Datum zuständig.
    public Date getScheduleUploadDate() {
        long date = getSharedPreferences().getLong("download_upload_date", 0);
        return date != 0 ? new Date(date) : null;
    }
    public void setScheduleUploadDate(Date date) {
        if (date == null) {
            getSharedPreferencesEditor().remove("download_upload_date").apply();
        } else {
            getSharedPreferencesEditor().putLong("download_upload_date", date.getTime()).apply();
        }
    }

    // Diese Funktionen sind für das Gültigkeits-Datum zuständig, welches das Inkrafttreten des Stundenplans angibt.
    public Date getScheduleValidityDate() {
        long date = getSharedPreferences().getLong("download_validity_date", 0);
        return date != 0 ? new Date(date) : null;
    }
    public void setScheduleValidityDate(Date date) {
        if (date == null) {
            getSharedPreferencesEditor().remove("download_validity_date").apply();
        } else {
            getSharedPreferencesEditor().putLong("download_validity_date", date.getTime()).apply();
        }
    }

    // TODO: Eine Funktion für das löschen von Hochlade- und Gültigkeits-Datum implementieren und im DownloadScheduleManager einfügen
}
