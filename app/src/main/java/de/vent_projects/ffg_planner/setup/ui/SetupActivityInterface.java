package de.vent_projects.ffg_planner.setup.ui;

public interface SetupActivityInterface {
    void requestNavigationRefresh();
    void notifyStepFinished();
    void notifyStepAborted();
    void requestTitleRefresh();
}
