package de.vent_projects.ffg_planner.schedule.ui;

import android.content.Context;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.replacement.objects.Replacement;
import de.vent_projects.ffg_planner.schedule.ScheduleNameManager;
import de.vent_projects.ffg_planner.schedule.objects.Break;
import de.vent_projects.ffg_planner.schedule.objects.LessonInterface;

import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class ViewHolder extends RecyclerView.ViewHolder {
    protected TextView textViewPeriod;
    protected TextView textViewTime;
    protected TextView textViewName;
    protected TextView textViewTeacher;
    protected TextView textViewRoom;
    protected TextView textViewInfo;

    protected LessonInterface lesson;
    protected ScheduleNameManager scheduleNameManager;

    protected OnClickListener listener;

    private boolean isBackgroundSet = false;

    public ViewHolder(View view, int viewType) {
        super(view);
        if (viewType == ListItems.BREAK){
            this.textViewName = view.findViewById(R.id.text_name);
            this.textViewInfo = view.findViewById(R.id.text_info);
        } else if (viewType == ListItems.INFO){
            this.textViewInfo = view.findViewById(R.id.text_info);
        } else {
            this.textViewPeriod = view.findViewById(R.id.text_period);
            this.textViewTime = view.findViewById(R.id.text_time);
            this.textViewName = view.findViewById(R.id.text_name);
            this.textViewTeacher = view.findViewById(R.id.text_teacher);
            this.textViewRoom = view.findViewById(R.id.text_room);
            this.textViewInfo = view.findViewById(R.id.text_info);
            if (this.textViewInfo != null && viewType == ListItems.LESSON) {
                this.textViewInfo.setVisibility(View.GONE);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onClick(lesson.getDay(), lesson.getPeriod());
                }
            });
        }

        this.scheduleNameManager = new ScheduleNameManager(view.getContext());
    }

    protected Context getContext() {
        return this.itemView.getContext();
    }

    public void setLesson(LessonInterface lesson) {
        if (lesson == null) {
            return;
        }
        setPeriod(lesson);
        setName(lesson);
        setTeacher(lesson);
        setRoom(lesson);
        removeInfo();
        removeBackground();
        this.lesson = lesson;
    }

    public void setReplacement(Replacement replacement) {
        this.setLesson(replacement);
        this.setInfo(replacement.getInfo());
    }

    public void setBreak(Break aBreak) {
        this.setName(aBreak.getName());
        this.setInfo(aBreak.getTime());
    }

    protected void setPeriod(LessonInterface lesson) {
        if (this.textViewPeriod != null) {
            this.textViewPeriod.setText(String.format(Locale.ENGLISH,"%d", lesson.getPeriod()));
        }
        if (this.textViewTime != null) {
            this.textViewTime.setText(lesson.getTime().toString());
        }
    }

    protected void setName(LessonInterface lesson) {
        if (this.textViewName != null) {
            this.textViewName.setText(lesson.isFree() ? getContext().getString(R.string.word_free) : this.scheduleNameManager.getDisplayedSubjectNameOfLesson(lesson));
        }
    }

    protected void setName(String name) {
        if (this.textViewName != null) {
            this.textViewName.setText(name);
        }
    }

    protected void setTeacher(LessonInterface lesson) {
        if (this.textViewTeacher != null) {
            this.textViewTeacher.setText(lesson.isFree() || isStringBlank(lesson.getTeacher()) ? "-" : this.scheduleNameManager.getDisplayedTeacherNameOfLesson(lesson));
        }
    }

    protected void setRoom(LessonInterface lesson) {
        if (this.textViewRoom != null) {
            this.textViewRoom.setText(lesson.isFree() || isStringBlank(lesson.getRoom()) ? "-" : this.scheduleNameManager.getDisplayedRoomNameOfLesson(lesson));
        }
    }

    protected void setInfo(String info) {
        if (this.textViewInfo != null) {
            this.textViewInfo.setVisibility(View.VISIBLE);
            this.textViewInfo.setText(isStringBlank(info) ? "-" : info);
        }
    }

    protected void removeInfo() {
        if (this.textViewInfo != null) {
            this.textViewInfo.setVisibility(View.GONE);
        }
    }

    public void makeStripedBackground(){
        if (this.itemView != null && !isBackgroundSet){
            try{
                BitmapDrawable background = new BitmapDrawable(this.itemView.getResources(), ((BitmapDrawable) this.itemView.getResources().getDrawable(R.drawable.background_stripes)).getBitmap()){
                    @Override
                    public int getMinimumWidth() {
                        return 0;
                    }

                    @Override
                    public int getMinimumHeight() {
                        return 0;
                    }
                };
                background.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                this.itemView.setBackground(background);
                this.isBackgroundSet = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void removeBackground(){
        if (this.itemView != null){
            this.itemView.setBackground(null);
            this.isBackgroundSet = false;
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onClick(int day, int period);
    }
}
