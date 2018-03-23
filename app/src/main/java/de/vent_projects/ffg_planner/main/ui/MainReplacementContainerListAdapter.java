package de.vent_projects.ffg_planner.main.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.replacement.ReplacementManager;
import de.vent_projects.ffg_planner.replacement.objects.DateReplacement;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

public class MainReplacementContainerListAdapter extends RecyclerView.Adapter<MainReplacementContainerViewHolder> {
    private RealmResults<DateReplacement> dateReplacements;

    public MainReplacementContainerListAdapter(Context context) {
        this.dateReplacements = new ReplacementManager(context).getAllDateReplacements();
        this.dateReplacements.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<DateReplacement>>() {
            @Override
            public void onChange(RealmResults<DateReplacement> dateReplacements, OrderedCollectionChangeSet changeSet) {
                if (changeSet != null) {
                    if (changeSet.getDeletions() != null) {
                        for (int i = 0; i < changeSet.getDeletions().length; i++) {
                            notifyItemRemoved(changeSet.getDeletions()[i]);
                        }
                    }
                    if (changeSet.getInsertions() != null) {
                        for (int i = changeSet.getInsertions().length-1; i >= 0; i--) {
                            notifyItemInserted(changeSet.getInsertions()[0]);
                        }
                    }
                    if (changeSet.getChanges() != null) {
                        for (int i = 0; i < changeSet.getChanges().length; i++) {
                            notifyItemChanged(changeSet.getChanges()[i]);
                        }
                    }
                }
            }
        });
    }

    @Override
    public MainReplacementContainerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MainReplacementContainerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_replacement_container, parent, false));
    }

    @Override
    public void onBindViewHolder(MainReplacementContainerViewHolder holder, int position) {
        DateReplacement dateReplacement = dateReplacements.get(position);
        holder.bindTitle(dateReplacement.getDateString());
        holder.bindReplacements(dateReplacement.getReplacements());
        holder.bindInfo(dateReplacement.getInfo());
    }

    @Override
    public int getItemCount() {
        return dateReplacements == null ? 0 : dateReplacements.size();
    }
}
