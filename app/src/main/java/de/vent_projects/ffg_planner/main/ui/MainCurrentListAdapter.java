package de.vent_projects.ffg_planner.main.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Handler;

import de.vent_projects.ffg_planner.ListMath;
import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.TimeManager;
import de.vent_projects.ffg_planner.main.schedule.MainScheduleManager;
import de.vent_projects.ffg_planner.main.schedule.objects.MainLesson;
import de.vent_projects.ffg_planner.realm.AdapterRealmChangeListener;
import de.vent_projects.ffg_planner.realm.AdapterRealmChangeListenerInterface;
import de.vent_projects.ffg_planner.replacement.ReplacementManager;
import de.vent_projects.ffg_planner.replacement.objects.DateReplacement;
import de.vent_projects.ffg_planner.replacement.objects.Replacement;
import de.vent_projects.ffg_planner.schedule.objects.Break;
import de.vent_projects.ffg_planner.schedule.objects.PeriodTime;
import de.vent_projects.ffg_planner.schedule.ui.ListItems;
import de.vent_projects.ffg_planner.schedule.ui.ViewHolder;
import io.realm.RealmList;
import io.realm.RealmResults;

import static de.vent_projects.ffg_planner.ListMath.getAbsoluteLessonPosition;
import static de.vent_projects.ffg_planner.ListMath.getCountOfBreaks;
import static de.vent_projects.ffg_planner.ListMath.getResourceBreakPosition;
import static de.vent_projects.ffg_planner.ListMath.getResourceLessonPosition;

public class MainCurrentListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private int hiddenItems = 0;
    private RealmList<Replacement> replacements;
    private RealmResults<MainLesson> lessons;
    private ArrayList<Break> breaks;
    private Handler handler;

    public MainCurrentListAdapter(@NonNull Context context, Calendar calendar, boolean isToday){
        this.context = context;
        MainScheduleManager scheduleManager = new MainScheduleManager(context);
        ReplacementManager replacementManager = new ReplacementManager(context);
        this.breaks = scheduleManager.getDefaultBreaks();
        this.lessons = scheduleManager.getLessonsForDay(calendar.get(Calendar.DAY_OF_WEEK));
        this.lessons.addChangeListener(new AdapterRealmChangeListener<RealmResults<MainLesson>>(new AdapterRealmChangeListenerInterface<MainLesson>() {
            @Override
            public void onDeleted(int position) {
                // ignore
            }

            @Override
            public void onInserted(MainLesson lesson, int position) {
                notifyItemInserted(getAbsoluteLessonPosition(position));
            }

            @Override
            public void onChanged(MainLesson lesson, int position) {
                notifyItemChanged(getAbsoluteLessonPosition(position));
            }
        }));
        DateReplacement dateReplacement = replacementManager.getDateReplacement(calendar);
        if (dateReplacement != null) {
            this.replacements = dateReplacement.getReplacements();
        }
        if (this.replacements != null) {
            this.replacements.addChangeListener(new AdapterRealmChangeListener<RealmList<Replacement>>(new AdapterRealmChangeListenerInterface<Replacement>() {
                @Override
                public void onDeleted(int position) {
                    notifyDataSetChanged();
                }

                @Override
                public void onInserted(Replacement replacement, int position) {
                    notifyItemChanged(getAbsoluteLessonPosition(replacement.getPeriod() - 1));
                }

                @Override
                public void onChanged(Replacement replacement, int position) {
                    notifyItemChanged(getAbsoluteLessonPosition(replacement.getPeriod() - 1));
                }
            }));
        }
        if (isToday) {
            Calendar now = (new TimeManager(context)).getCalendar();
            //setCurrentLesson(getCurrentLesson(now));
            /*this.timer = new Timer();
            for (int i = 0; i < 8; i++) {
                TimePair lesson = Lesson.getLessonTimePair(i);
                if (lesson.hour > now.get(Calendar.HOUR) ||
                        lesson.hour == now.get(Calendar.HOUR) && lesson.minute > now.get(Calendar.MINUTE)) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            setCurrentLesson(getCurrentLesson(getCalendar(scheduleManager.getContext())));
                        }
                    }, getDate(now, lesson));
                }
            }*/
        }
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
        if (viewType == ListItems.REPLACEMENT){
            Replacement replacement = getReplacementForViewPosition(position);
            MainLesson lesson = getLessonForViewPosition(position);
            if (replacement != null){
                holder.setReplacement(replacement);
                holder.makeStripedBackground();
            } else {
                holder.setLesson(lesson);
                holder.removeBackground();
            }
        } else if (viewType == ListItems.BREAK){
            holder.setBreak(getBreakForViewPosition(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ((position+hiddenItems+1) % 3 == 0) ? ListItems.BREAK : ListItems.REPLACEMENT;
    }

    @Override
    public int getItemCount() {
        int sum = getAbsoluteItemCount()-hiddenItems;
        return (sum < 0) ? 0 : sum;
    }

    private int getAbsoluteItemCount(){
        int lessons = (this.lessons == null) ? 0 : this.lessons.size();
        return lessons + getCountOfBreaks(lessons);
    }

    private int getItemResourcePosition(int position, int viewType){
        if (viewType == ListItems.BREAK){
            if (this.breaks != null && position < this.getItemCount() && (position + hiddenItems + 1) % 3 == 0){
                return getResourceBreakPosition(position + hiddenItems);
            } else {
                return -1;
            }
        } else {
            if (this.lessons != null && position < this.getItemCount() && (position + hiddenItems + 1) % 3 != 0){
                return getResourceLessonPosition(position + hiddenItems);
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
            return new MainLesson(0, position+1, this.context.getString(R.string.word_error), "", "", false);
        }
    }
    private Break getBreakForViewPosition(int position){
        int resourcePosition = getItemResourcePosition(position, ListItems.BREAK);
        if (resourcePosition >= 0 && resourcePosition < breaks.size()){
            return breaks.get(resourcePosition);
        } else {
            return new Break(this.context.getString(R.string.word_error));
        }
    }

    private Replacement getReplacementForViewPosition(int position){
        if (this.replacements == null || this.replacements.size() == 0){
            return null;
        }
        for (int i = 0; i < this.replacements.size(); i++) {
            if (this.replacements.get(i).getPeriod() == getItemResourcePosition(position, ListItems.LESSON) + 1){
                return this.replacements.get(i);
            }
        }
        return null;
    }

    private void updateListView(int hiddenItems){
        int old = this.hiddenItems;
        if (hiddenItems > old){
            for (int i = old; i < hiddenItems; i++) {
                notifyItemRemoved(0);
                this.hiddenItems++;
            }
        } else if (hiddenItems < old) {
            for (int i = old; i > hiddenItems; i--) {
                notifyItemInserted(0);
                this.hiddenItems--;
            }
        }
    }

    private void setCurrentLesson(int currentLesson) {
        if (currentLesson < lessons.size()) {
            updateListView(getCountOfHiddenItems(currentLesson));
        } else {
            updateListView(0);
        }
    }

    private int getCountOfHiddenItems(int currentLesson) {
        return ListMath.getCountOfHiddenItems(lessons.size() - (currentLesson - 1));
    }

    private int getCurrentLesson(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        for (int i = 0; i < 8; i++) {
            PeriodTime periodTime = MainLesson.getTime(i).getNextPeriodTime();
            if (hour < periodTime.getHour() && minute < periodTime.getMinute()) return i;
        }
        return 1;
    }

    private Date getDate(Calendar calendar, PeriodTime periodTime) {
        Date startTime = new Date();
        startTime.setTime(calendar.getTimeInMillis());
        startTime.setHours(periodTime.getHour());
        startTime.setMinutes(periodTime.getMinute());
        startTime.setSeconds(1);
        return startTime;
    }
}
