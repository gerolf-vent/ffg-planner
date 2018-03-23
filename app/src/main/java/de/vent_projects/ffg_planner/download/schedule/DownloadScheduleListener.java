package de.vent_projects.ffg_planner.download.schedule;

public interface DownloadScheduleListener {
    void onFinished();
    void onNoNetworkAvailable();
    void onBadNetwork();
    void onNetworkError(int errorCode, String errorMessage);
    void onParseError();
    void onUnknownError();
}
