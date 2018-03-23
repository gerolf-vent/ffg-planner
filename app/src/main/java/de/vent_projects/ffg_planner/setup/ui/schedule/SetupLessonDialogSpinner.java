package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import de.vent_projects.ffg_planner.R;


public class SetupLessonDialogSpinner extends AppCompatSpinner {

    public SetupLessonDialogSpinner(Context context) {
        super(context);
    }

    public SetupLessonDialogSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SetupLessonDialogSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        if (getOnItemSelectedListener() != null)
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
    }

    @Override
    public void setSelection(int position, boolean animate) {
        super.setSelection(position, animate);
        if (getOnItemSelectedListener() != null)
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        View view = getSelectedView();
        if (view != null) {
            view.setEnabled(enabled);
            TextView name = view.findViewById(R.id.text_name);
            TextView teacher = view.findViewById(R.id.text_teacher);
            TextView room = view.findViewById(R.id.text_room);
            if (name != null) name.setEnabled(enabled);
            if (teacher != null) teacher.setEnabled(enabled);
            if (room != null) room.setEnabled(enabled);
        }
    }
}
