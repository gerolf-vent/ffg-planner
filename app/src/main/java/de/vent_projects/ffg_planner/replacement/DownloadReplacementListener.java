package de.vent_projects.ffg_planner.replacement;

public interface DownloadReplacementListener {
    void onFinished();
    void onNoNetworkAvailable();
    void onError();
}
