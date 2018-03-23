package de.vent_projects.ffg_planner.main.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import de.vent_projects.ffg_planner.main.MainSections;

public class MainSwipeRefreshLayout extends SwipeRefreshLayout {
    OnChildScrollUpCallback callback = null;
    boolean isScrollCurrent = false;
    boolean isScrollSchedule = false;
    MainSections currentMode = MainSections.CURRENT;

    public MainSwipeRefreshLayout(Context context){
        super(context);
    }

    public MainSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return (isScrollCurrent && currentMode == MainSections.CURRENT) ||
                (isScrollSchedule && currentMode == MainSections.SCHEDULE) ||
                (callback == null || callback.canChildScrollUp(this, null));
    }

    @Override
    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        this.callback = callback;
    }

    public void setScrollCurrent(boolean scrollCurrent) {
        isScrollCurrent = scrollCurrent;
    }

    public void setScrollSchedule(boolean scrollSchedule) {
        isScrollSchedule = scrollSchedule;
    }

    public void setCurrentMode(MainSections currentMode) {
        this.currentMode = currentMode;
    }
}

