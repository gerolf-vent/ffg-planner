package de.vent_projects.ffg_planner.main.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.TimeManager;
import de.vent_projects.ffg_planner.replacement.ReplacementManager;
import de.vent_projects.ffg_planner.replacement.objects.DateReplacement;
import io.realm.RealmChangeListener;

import static de.vent_projects.ffg_planner.CommonUtils.getAttentionInInfo;
import static de.vent_projects.ffg_planner.CommonUtils.getAttentionWithoutTitle;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class MainCurrentFragment extends Fragment {
    private Calendar calendar = null;
    private int position = -1;

    private ScrollView scrollView;
    private RecyclerView recyclerViewLessons;
    private TextView textViewAttention;
    private CardView cardViewAttention;
    private TextView textViewInfo;
    private CardView cardViewInfo;
    private View containerNothing;

    private ReplacementManager replacementManager;
    private DateReplacement dateReplacement;

    public MainCurrentFragment() {}

    public static MainCurrentFragment newInstance(int position){
        MainCurrentFragment fragment = new MainCurrentFragment();
        fragment.position = position;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (this.position < 0 &&
                savedInstanceState != null &&
                savedInstanceState.containsKey("position"))
            this.position = savedInstanceState.getInt("position");

        this.calendar = new TimeManager(getContext()).getCalendar();
        if (this.position != 0) this.calendar.add(Calendar.DAY_OF_MONTH, 1);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", position);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_current, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.scrollView = view.findViewById(R.id.scrollview_current);
        if (this.scrollView != null) {
            this.scrollView.setTag("scrollview_current_" + this.position);
        }

        this.recyclerViewLessons = view.findViewById(R.id.recycler_view_lessons);
        if (this.recyclerViewLessons != null){
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false){
                @Override
                protected int getExtraLayoutSpace(RecyclerView.State state) {
                    return 5000;
                }

                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            this.recyclerViewLessons.setLayoutManager(layoutManager);
            this.recyclerViewLessons.setNestedScrollingEnabled(false);
        }

        this.textViewAttention = view.findViewById(R.id.text_attention);
        this.cardViewAttention = view.findViewById(R.id.card_attention);

        this.textViewInfo = view.findViewById(R.id.text_info);
        this.cardViewInfo = view.findViewById(R.id.card_info);

        this.containerNothing = view.findViewById(R.id.container_nothing);

        this.replacementManager = new ReplacementManager(getContext());
        this.dateReplacement = this.replacementManager.getDateReplacement(this.calendar);
        if (this.dateReplacement != null) {
            bindInfo(this.dateReplacement.getInfo());
            this.dateReplacement.addChangeListener(new RealmChangeListener<DateReplacement>() {
                @Override
                public void onChange(DateReplacement dateReplacement) {
                    bindInfo(dateReplacement.getInfo());
                }
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        if (this.calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && this.calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            if (getView() != null) {
                if (this.recyclerViewLessons != null)
                    this.recyclerViewLessons.setAdapter(new MainCurrentListAdapter(getContext(), this.calendar, this.position == 0));
                showLessonList();
            }
        } else {
            showNothing();
        }
        super.onStart();
    }

    private void showLessonList() {
        if (getView() != null) {
            if (this.scrollView != null) this.scrollView.setVisibility(View.VISIBLE);
            if (this.containerNothing != null) this.containerNothing.setVisibility(View.GONE);
        }
    }

    private void showNothing() {
        if (getView() != null) {
            if (this.containerNothing != null) this.containerNothing.setVisibility(View.VISIBLE);
            if (this.scrollView != null) this.scrollView.setVisibility(View.GONE);
        }
    }

    private void bindInfo(String info) {
        if (getView() != null) {
            if (!isStringBlank(info)) {
                String attention = getAttentionInInfo(info);
                if (!isStringBlank(attention)) {
                    if (this.textViewAttention != null) {
                        this.textViewAttention.setText(getAttentionWithoutTitle(attention).trim());
                        if (this.cardViewAttention != null) this.cardViewAttention.setVisibility(View.VISIBLE);
                    } else {
                        if (this.cardViewAttention != null) this.cardViewAttention.setVisibility(View.GONE);
                    }
                } else {
                    if (this.cardViewAttention != null) this.cardViewAttention.setVisibility(View.GONE);
                }
                String onlyInfo = info.trim().replace(attention.trim(), "");
                if (!isStringBlank(onlyInfo)) {
                    this.textViewInfo.setText(onlyInfo);
                    if (this.cardViewInfo != null) this.cardViewInfo.setVisibility(View.VISIBLE);
                } else {
                    if (this.cardViewInfo != null) this.cardViewInfo.setVisibility(View.GONE);
                }
            } else {
                if (this.cardViewInfo != null) this.cardViewInfo.setVisibility(View.GONE);
                if (this.cardViewAttention != null) this.cardViewAttention.setVisibility(View.GONE);
            }
        }
    }
}
