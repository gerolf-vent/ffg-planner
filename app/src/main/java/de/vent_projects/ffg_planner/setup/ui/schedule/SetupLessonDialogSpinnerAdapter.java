package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.download.schedule.objects.DownloadLesson;

public class SetupLessonDialogSpinnerAdapter extends ArrayAdapter<DownloadLesson> {
    SetupLessonDialogSpinnerAdapter(Context context, List<DownloadLesson> lessons) {
        super(context, R.layout.spinner_item_lesson, lessons);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        DownloadLesson lesson = getItem(position);
        ViewHolder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_lesson, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.text_name);
            holder.teacher = convertView.findViewById(R.id.text_teacher);
            holder.room = convertView.findViewById(R.id.text_room);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (lesson != null){
            holder.name.setText(lesson.getName());
            holder.teacher.setText(lesson.getTeacher().replaceAll("\\*", ""));
            holder.room.setText(lesson.getRoom());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    class ViewHolder{
        TextView name, teacher, room;
    }
}
