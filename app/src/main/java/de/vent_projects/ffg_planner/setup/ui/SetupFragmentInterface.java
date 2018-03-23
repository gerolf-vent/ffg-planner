package de.vent_projects.ffg_planner.setup.ui;

public interface SetupFragmentInterface {
    boolean canGoBack();
    boolean canGoNext();
    void goBack();
    void goNext();
    int getSubstepCount();
    int getSubstepPosition();
    String getTitle();
    boolean isResetable();
    void reset();
    void onFinish();
}
