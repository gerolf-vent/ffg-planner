package de.vent_projects.ffg_planner.main.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.vent_projects.ffg_planner.ListMath;
import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.main.schedule.MainScheduleManager;
import de.vent_projects.ffg_planner.main.schedule.objects.MainLesson;
import de.vent_projects.ffg_planner.schedule.objects.Break;
import de.vent_projects.ffg_planner.schedule.ui.ListItems;
import de.vent_projects.ffg_planner.schedule.ui.ViewHolder;
import io.realm.RealmResults;

public class MainScheduleListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final String TAG = MainScheduleListAdapter.class.getSimpleName();

    private RealmResults<MainLesson> lessons;
    private ArrayList<Break> breaks;
    private Context context;

    public MainScheduleListAdapter(@NonNull Context context, int day){
        Log.d(TAG, "Init adpater for day " + day);
        MainScheduleManager scheduleManager = new MainScheduleManager(context);
        this.lessons = scheduleManager.getLessonsForDay(day);
        this.breaks = scheduleManager.getDefaultBreaks();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Integer layoutResource;
        if (viewType == ListItems.BREAK){
            layoutResource = R.layout.list_item_break;
        } else {
            layoutResource = R.layout.list_item_lesson;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = this.getItemViewType(position);
        if (viewType == ListItems.LESSON){
            holder.setLesson(this.getLessonForViewPosition(position));
        } else if (viewType == ListItems.BREAK){
           holder.setBreak(this.getBreakForViewPosition(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ((position + 1) % 3 == 0) ? ListItems.BREAK : ListItems.LESSON;
    }

    @Override
    public int getItemCount() {
        return getListItemCount((this.lessons == null) ? 0 : this.lessons.size());
    }

    private int getListItemCount(int lessonCount){
        return lessonCount + ListMath.getCountOfBreaks(lessonCount);
    }

    private int getItemResourcePosition(int position, int viewType){
        if (viewType == ListItems.BREAK){
            if (this.breaks != null && position < getItemCount() && (position+1) % 3 == 0){
                return ListMath.getResourceBreakPosition(position);
            } else {
                return -1;
            }
        } else {
            if (this.lessons != null && position < getItemCount() && (position+1) % 3 != 0){
                return ListMath.getResourceLessonPosition(position);
            } else {
                return -1;
            }
        }
    }

    private MainLesson getLessonForViewPosition(int position){
        int resourcePosition = getItemResourcePosition(position, ListItems.LESSON);
        if (resourcePosition >= 0 && resourcePosition < lessons.size()){
            return lessons.get(resourcePosition);
        } else {
            return new MainLesson(0, position + 1, context.getString(R.string.word_error), "", "", false);
        }
    }

    private Break getBreakForViewPosition(int position){
        int resourcePosition = getItemResourcePosition(position, ListItems.BREAK);
        if (resourcePosition >= 0 && resourcePosition < breaks.size()){
            return breaks.get(resourcePosition);
        } else {
            return new Break(context.getString(R.string.word_error));
        }
    }
}
