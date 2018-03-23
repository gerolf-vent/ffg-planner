package de.vent_projects.ffg_planner.realm;

import java.util.List;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmObject;

public class AdapterRealmChangeListener<T extends List> implements OrderedRealmCollectionChangeListener<T> {
    private AdapterRealmChangeListenerInterface listenerInterface;

    public AdapterRealmChangeListener(AdapterRealmChangeListenerInterface listenerInterface) {
        this.listenerInterface = listenerInterface;
    }

    @Override
    public void onChange(T t, OrderedCollectionChangeSet changeSet) {
        if (changeSet != null && this.listenerInterface != null) {
            if (changeSet.getDeletions() != null) {
                for (int i = 0; i < changeSet.getDeletions().length; i++) {
                    this.listenerInterface.onDeleted(changeSet.getDeletions()[i]);
                }
            }
            if (changeSet.getInsertions() != null) {
                for (int i = 0; i < changeSet.getInsertions().length; i++) {
                    this.listenerInterface.onInserted((RealmObject) t.get(changeSet.getInsertions()[i]), changeSet.getInsertions()[i]);
                }
            }
            if (changeSet.getChanges() != null) {
                for (int i = 0; i < changeSet.getChanges().length; i++) {
                    this.listenerInterface.onInserted((RealmObject) t.get(changeSet.getChanges()[i]), changeSet.getChanges()[i]);
                }
            }
        }
    }

}
