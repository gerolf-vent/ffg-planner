package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.vent_projects.ffg_planner.R;

public class SetupSchedulePageFragment extends Fragment {

    public SetupSchedulePageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_schedule_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (!getArguments().containsKey("day")) return;
        int day = getArguments().getInt("day");
        if (day < 2 || day > 6) return;

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new SetupScheduleRecyclerViewAdapter(getContext(), day, getFragmentManager()));
        }

        super.onViewCreated(view, savedInstanceState);
    }
}
