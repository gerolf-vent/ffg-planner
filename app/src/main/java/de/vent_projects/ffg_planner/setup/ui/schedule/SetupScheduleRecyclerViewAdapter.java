package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.schedule.ui.ViewHolder;
import de.vent_projects.ffg_planner.setup.schedule.SetupScheduleManager;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;
import io.realm.RealmChangeListener;

public class SetupScheduleRecyclerViewAdapter extends android.support.v7.widget.RecyclerView.Adapter<SetupLessonViewHolder> {
    private ArrayList<SetupLesson> lessons;
    private ArrayList<SetupLesson> bLessons; // Only to solve an Realm issue (Save instances of blessons and their change listeners)
    private FragmentManager fragmentManager;

    public SetupScheduleRecyclerViewAdapter(Context context, int day, FragmentManager fragmentManager) {
        this.lessons = new ArrayList<>();
        this.bLessons = new ArrayList<>();
        this.fragmentManager = fragmentManager;

        SetupScheduleManager scheduleManager = new SetupScheduleManager(context);
        for (int i = 0; i < 8; i++) {
            SetupLesson lesson = scheduleManager.getLessonForDayAndPeriod(day, i + 1);
            lesson.addChangeListener(new RealmChangeListener<SetupLesson>() {
                @Override
                public void onChange(SetupLesson lesson) {
                    notifyItemChanged(indexOf(lesson));
                }
            });
            if (lesson.isABLesson()) {
                SetupLesson bLesson = scheduleManager.getLessonForDayAndPeriod(lesson.getDay(), lesson.getPeriod(), true);
                bLesson.addChangeListener(new RealmChangeListener<SetupLesson>() {
                    @Override
                    public void onChange(SetupLesson lesson) {
                        notifyItemChanged(indexOf(lesson));
                    }
                });
                bLessons.add(bLesson);
            }
            this.lessons.add(lesson);
        }
    }

    private int indexOf(SetupLesson lesson) {
        if (!lesson.isValid()) return -1;
        for (int i = 0; i < this.lessons.size(); i++) {
            if (SetupLesson.getID(this.lessons.get(i).getDay(), this.lessons.get(i).getPeriod(), false).equals(SetupLesson.getID(lesson.getDay(), lesson.getPeriod(), false))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public SetupLessonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_lesson, parent, false);
        return new SetupLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SetupLessonViewHolder holder, int position) {
        holder.setLesson(this.lessons.get(position));
        holder.setOnClickListener(new ViewHolder.OnClickListener() {
            @Override
            public void onClick(int day, int period) {
                SetupLessonDialog dialog = SetupLessonDialog.newInstance(day, period, null);
                dialog.show(fragmentManager, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }
}
