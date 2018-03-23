package de.vent_projects.ffg_planner.main.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import de.vent_projects.ffg_planner.R;

public class MainScheduleFragment extends Fragment {
    private int day = -1;

    public MainScheduleFragment(){}

    public static MainScheduleFragment newInstance(int day) {
        MainScheduleFragment fragment = new MainScheduleFragment();
        fragment.day = day;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (this.day < 0 &&
                savedInstanceState != null &&
                savedInstanceState.containsKey("day"))
            this.day = savedInstanceState.getInt("day");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("day", this.day);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_schedule, container, false);
        RecyclerView list_subjects = rootView.findViewById(R.id.recycler_view_lessons);
        if (list_subjects != null){
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false){
                @Override
                protected int getExtraLayoutSpace(RecyclerView.State state) {
                    return 50000;
                }

                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            list_subjects.setLayoutManager(layoutManager);
            list_subjects.setNestedScrollingEnabled(false);
        }
        ScrollView scrollView = rootView.findViewById(R.id.scrollview_schedule);
        if (scrollView != null) {
            scrollView.setTag("scrollview_schedule_" + this.day);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        if (getView() != null) {
            RecyclerView list_subjects = getView().findViewById(R.id.recycler_view_lessons);
            if (list_subjects != null)
                list_subjects.setAdapter(new MainScheduleListAdapter(getContext(), this.day));
        }
        super.onStart();
    }
}
