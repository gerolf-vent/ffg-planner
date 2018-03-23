package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.view.View;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.schedule.objects.LessonInterface;
import de.vent_projects.ffg_planner.schedule.ui.ListItems;
import de.vent_projects.ffg_planner.schedule.ui.ViewHolder;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;

import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class SetupLessonViewHolder extends ViewHolder {

    public SetupLessonViewHolder(View view) {
        super(view, ListItems.LESSON);
    }

    @Override
    protected void setName(LessonInterface lesson) {
        if (this.textViewName != null) {
            this.textViewName.setText(getNameForLesson((SetupLesson) lesson));
        }
    }

    private String getNameForLesson(SetupLesson lesson) {
        if (lesson == null) return getContext().getString(R.string.word_error);

        String name = "";
        if (lesson.isMarkedAsEmpty()) {
            name += getContext().getString(R.string.list_name_choose_please);
        } else if (lesson.isFree()) {
            name += getContext().getString(R.string.word_free);
        } else {
            name += lesson.getName();
        }

        if (lesson.isABLesson()) {
            name += " / " + getNameForLesson(lesson.getBlesson());
        }

        return name;
    }

    @Override
    protected void setTeacher(LessonInterface lesson) {
        if (this.textViewTeacher != null) {
            this.textViewTeacher.setText(getTeacherForLesson((SetupLesson) lesson));
        }
    }

    private String getTeacherForLesson(SetupLesson lesson) {
        if (lesson == null) return "-";

        String teacher = "";
        if (lesson.isMarkedAsEmpty() || lesson.isFree() || isStringBlank(lesson.getTeacher())) {
            teacher += "-";
        } else {
            teacher += lesson.getTeacher();
        }

        if (lesson.isABLesson()) {
            teacher += " / " + getTeacherForLesson(lesson.getBlesson());
        }

        return teacher;
    }

    @Override
    protected void setRoom(LessonInterface lesson) {
        if (this.textViewRoom != null) {
            this.textViewRoom.setText(getRoomForLesson((SetupLesson) lesson));
        }
    }

    private String getRoomForLesson(SetupLesson lesson) {
        if (lesson == null) return "-";

        String room = "";
        if (lesson.isMarkedAsEmpty() || lesson.isFree() || isStringBlank(lesson.getRoom())) {
            room += "-";
        } else {
            room += lesson.getRoom();
        }

        if (lesson.isABLesson()) {
            room += " / " + getRoomForLesson(lesson.getBlesson());
        }

        return room;
    }
}
