package de.vent_projects.ffg_planner.main.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.replacement.objects.Replacement;
import de.vent_projects.ffg_planner.schedule.ui.ListItems;
import de.vent_projects.ffg_planner.schedule.ui.ViewHolder;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmList;

public class MainReplacementInnerListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private RealmList<Replacement> replacements;

    public MainReplacementInnerListAdapter(RealmList<Replacement> replacements){
        this.replacements = replacements;
        this.replacements.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Replacement>>() {
            @Override
            public void onChange(RealmList<Replacement> replacements, OrderedCollectionChangeSet changeSet) {
                // TODO: Implement
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_lesson, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setReplacement(replacements.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return ListItems.REPLACEMENT;
    }

    @Override
    public int getItemCount() {
        return replacements == null || !replacements.isLoaded() ? 0 : replacements.size();
    }
}
