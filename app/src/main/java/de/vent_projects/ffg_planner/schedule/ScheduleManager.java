package de.vent_projects.ffg_planner.schedule;

import android.content.Context;

import de.vent_projects.ffg_planner.realm.RealmManager;
import io.realm.Realm;

public class ScheduleManager {
    private Context context;


    public ScheduleManager(Context context) {
        this.context = context;
    }

    // GET CONTEXT

    protected Context getContext() {
        return this.context;
    }

    // GET REALM

    protected Realm getRealm() {
        return RealmManager.getRealm();
    }
}
