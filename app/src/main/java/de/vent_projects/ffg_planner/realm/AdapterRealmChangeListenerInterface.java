package de.vent_projects.ffg_planner.realm;

import io.realm.RealmObject;

public interface AdapterRealmChangeListenerInterface<T extends RealmObject> {
    void onDeleted(int position);
    void onInserted(T realmObject, int position);
    void onChanged(T realmObject, int position);
}
