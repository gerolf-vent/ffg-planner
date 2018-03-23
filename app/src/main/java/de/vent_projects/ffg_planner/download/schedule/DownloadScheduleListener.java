package de.vent_projects.ffg_planner.download.schedule;

/**
 * Dies ist ein Interface für den Download des Stundenplanes, um relativ
 * detailierte Meldungen für den Nutzer anzeigen zu können.
 */

public interface DownloadScheduleListener {
    void onFinished();
    void onNoNetworkAvailable();
    void onBadNetwork();
    void onNetworkError(int errorCode, String errorMessage);
    void onParseError();
    void onUnknownError();
}
