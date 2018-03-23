package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.List;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.download.schedule.DownloadScheduleManager;
import de.vent_projects.ffg_planner.download.schedule.objects.DownloadLesson;
import de.vent_projects.ffg_planner.setup.schedule.SetupScheduleManager;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;
import de.vent_projects.ffg_planner.setup.settings.SetupSettingsManager;
import io.realm.RealmResults;

import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;

public class SetupLessonDialog extends DialogFragment implements DialogInterface.OnDismissListener, Toolbar.OnMenuItemClickListener {
    private static final String TAG = SetupLessonDialog.class.getSimpleName();

    private SetupScheduleManager setupScheduleManager;
    private DownloadScheduleManager downloadScheduleManager;

    private SetupLesson lesson;
    private SetupLesson lessonBefore;
    private RealmResults<DownloadLesson> options;

    private int day, period;
    private Boolean isBlesson;

    private boolean cachedIsFree;
    private String cachedName;
    private String cachedTeacher;
    private String cachedRoom;

    private SetupLessonDialogSpinner spinnerOptions;
    private CheckBox checkBoxSimilar;
    private CheckBox checkBoxFree;
    private EditText editTextName;
    private EditText editTextTeacher;
    private EditText editTextRoom;

    public static SetupLessonDialog newInstance(int day, int period, Boolean isBlesson) {
        SetupLessonDialog dialog = new SetupLessonDialog();
        Bundle args = new Bundle();
        args.putInt("day", day);
        args.putInt("period", period);
        if (isBlesson != null) {
            args.putBoolean("is_blesson", isBlesson);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey("day") || !getArguments().containsKey("period")) {
            // TODO: throw error
            return new AlertDialog.Builder(getActivity()).setMessage("Error").create();
        }

        this.day = getArguments().getInt("day");
        this.period = getArguments().getInt("period");

        this.setupScheduleManager = new SetupScheduleManager(getContext());
        this.downloadScheduleManager = new DownloadScheduleManager(getContext(), new SetupSettingsManager(getContext()));

        if (!this.downloadScheduleManager.isABLessonForDayAndPeriod(this.day, this.period)) {
            this.isBlesson = null;
            this.lesson = this.setupScheduleManager.getLessonForDayAndPeriod(this.day, this.period);
        } else {
            this.isBlesson = getArguments().getBoolean("is_blesson", false);
            this.lesson = this.setupScheduleManager.getLessonForDayAndPeriod(this.day, this.period, this.isBlesson);
        }

        if (this.period % 2 == 0) {
            this.lessonBefore = this.setupScheduleManager.getLessonForDayAndPeriod(this.day, this.period - 1, this.downloadScheduleManager.isABLessonForDayAndPeriod(this.day, this.period - 1) && this.isBlesson);
        }

        if (this.isBlesson == null) {
            this.options = this.downloadScheduleManager.getAllLessonsForDayAndPeriod(this.day, period);
        } else if (!this.isBlesson) {
            this.options = this.downloadScheduleManager.getAllALessonsForDayAndPeriod(this.day, period);
        } else {
            this.options = this.downloadScheduleManager.getAllBLessonsForDayAndPeriod(this.day, period);
        }

        View view = View.inflate(getActivity(), R.layout.dialog_setup_lesson, null);

        // CHECK BOX SIMILAR

        this.checkBoxSimilar = view.findViewById(R.id.check_box_similar);
        if (this.checkBoxSimilar != null) {
            if ((this.period % 2) != 0 || this.lessonBefore == null || this.lessonBefore.isMarkedAsEmpty()){
                this.checkBoxSimilar.setEnabled(false);
            }
            this.checkBoxSimilar.setText(String.format(getResources().getString(R.string.dialog_text_similar_to_lesson), this.period - 1));
            this.checkBoxSimilar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    onCheckBoxChanged(false, checkBoxFree.isChecked(), true, isChecked);
                }
            });
        }

        // CHECK BOX FREE

        this.checkBoxFree = view.findViewById(R.id.check_box_free);
        if (this.checkBoxFree != null) {
            this.checkBoxFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    onCheckBoxChanged(true, isChecked, false, checkBoxSimilar.isChecked());
                }
            });
        }

        // OTHER VIEWS

        this.editTextName = view.findViewById(R.id.edit_text_name);
        this.editTextTeacher = view.findViewById(R.id.edit_text_teacher);
        this.editTextRoom = view.findViewById(R.id.edit_text_room);

        // SPINNER OPTIONS

        this.spinnerOptions = view.findViewById(R.id.spinner_options);
        if (this.options.size() > 1 || (this.options.size() == 1 && downloadScheduleManager.isLessonPossible(this.options.get(0)))){
            this.spinnerOptions.setAdapter(new SetupLessonDialogSpinnerAdapter(getActivity(), this.options));
            this.spinnerOptions.setSelection(0, false);
            this.spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View spinnerView, int position, long id) {
                    DownloadLesson lesson = options.get(position);
                    if (lesson != null && lesson.isValid() && !checkBoxFree.isChecked()) {
                        if (editTextName != null) editTextName.setText(lesson.getName());
                        if (editTextTeacher != null) editTextTeacher.setText(lesson.getTeacher().replaceAll("\\*", ""));
                        if (editTextRoom != null) editTextRoom.setText(lesson.getRoom());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }else {
            this.spinnerOptions.setAdapter(null);
            this.spinnerOptions.setVisibility(View.GONE);
        }

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                // SET VALUES
                if (!lesson.isMarkedAsEmpty()) {
                    if (lesson.isFree()) {
                        checkBoxFree.setChecked(true);
                    } else {
                        editTextName.setText(lesson.getName());
                        editTextTeacher.setText(lesson.getTeacher());
                        editTextRoom.setText(lesson.getRoom());
                    }
                } else {
                    spinnerOptions.setSelection(0);
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

            }
        });

        setHasOptionsMenu(true);

        View titleView = View.inflate(getActivity(), R.layout.dialog_setup_lesson_title, null);
        Toolbar toolbar = titleView.findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(getTitle());
            if (this.options.size() == 1 && !downloadScheduleManager.isLessonPossible(this.options.get(0))) {
                toolbar.inflateMenu(R.menu.activity_setup);
                toolbar.setOnMenuItemClickListener(this);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveLesson();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCustomTitle(titleView);
        builder.setOnDismissListener(this);
        return builder.create();
    }

    // MENU

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            if (this.lesson.isMarkedAsEmpty()) {
                this.checkBoxSimilar.setChecked(false);
                this.checkBoxFree.setChecked(false);
                this.editTextName.setText("");
                this.editTextTeacher.setText("");
                this.editTextRoom.setText("");
            } else {
                if (this.options.size() == 1) {
                    SetupLesson lesson = new SetupLesson(this.options.get(0), this.isBlesson != null && this.isBlesson);
                    this.checkBoxSimilar.setChecked(false);
                    if (lesson.isFree()) {
                        this.checkBoxFree.setChecked(true);
                    } else {
                        this.checkBoxFree.setChecked(false);
                        this.editTextName.setText(lesson.getName());
                        this.editTextTeacher.setText(lesson.getTeacher());
                        this.editTextRoom.setText(lesson.getRoom());
                    }
                }
            }
            return true;
        }
        return false;
    }

    private String getTitle() {
        return getResources().getString(R.string.word_lesson) + " " + Integer.toString(this.period) + (this.isBlesson == null ? "" : this.isBlesson ? " B" : " A");
    }

    private void saveLesson() {
        if (this.checkBoxFree == null || this.editTextName == null || this.editTextTeacher == null || this.editTextRoom == null) {
            Log.e(TAG, "Not enough input fields available");
            return;
        }
        if (!lesson.isValid()) {
            Log.e(TAG, "Lesson is not valid");
            return;
        }

        boolean isFree = this.checkBoxFree.isChecked();

        String name = this.editTextName.getText().toString();
        String teacher = this.editTextTeacher.getText().toString();
        String room = this.editTextRoom.getText().toString();

        setupScheduleManager.beginTransaction();
        if (isFree || isStringBlank(name) || name.equals(getString(R.string.word_free))) {
            this.lesson.makeFree();
        } else {
            this.lesson.setName(name);
            this.lesson.setTeacher(teacher.equals("-") ? "" : teacher);
            this.lesson.setRoom(room.equals("-") ? "" : room);
        }
        this.lesson.unmarkAsEmpty();
        setupScheduleManager.commitTransaction();

        /*if (this.isBlesson) {
            final SetupLesson setupLesson = setupScheduleManager.getLessonForDayAndPeriod(this.day, this.period);
            synchronized (setupLesson) {
                setupLesson.notifyAll();
            }
        }*/

        SetupSettingsManager settingsManager = new SetupSettingsManager(getContext());
        if (settingsManager.getSchoolClass().isUpperLevel() && !this.lesson.isFree()) {
            List<DownloadLesson> similarLessons = downloadScheduleManager.getLessonsWithSimilarSubjectName(this.lesson.getName());
            if (similarLessons != null) {
                for (DownloadLesson lesson : similarLessons) {
                    if (setupScheduleManager.getLessonForDayAndPeriod(lesson.getDay(), lesson.getPeriod()).isMarkedAsEmpty()) {
                        setupScheduleManager.setLesson(new SetupLesson(lesson, false));
                    }
                }
            }
        } else {
            SetupLesson lessonAfter = null;
            if (this.period % 2 != 0) {
                lessonAfter = setupScheduleManager.getLessonForDayAndPeriod(this.day, this.period + 1, isBlesson != null && isBlesson);
            }
            if (lessonAfter != null && lessonAfter.isMarkedAsEmpty()) {
                setupScheduleManager.beginTransaction();
                lessonAfter.setName(this.lesson.getName());
                lessonAfter.setTeacher(this.lesson.getTeacher());
                lessonAfter.setRoom(this.lesson.getRoom());
                lessonAfter.unmarkAsEmpty();
                setupScheduleManager.commitTransaction();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (this.isBlesson != null && !this.isBlesson){
            SetupLessonDialog newDialog = SetupLessonDialog.newInstance(this.day, this.period, true);
            newDialog.show(getFragmentManager(), null);
        }
        super.onDismiss(dialog);
    }

    // ON CHECKBOX CHANGES (WITH CORRECTION)

    private boolean isCachingInputSuppressed;

    private void onCheckBoxChanged(boolean freeChanged, boolean isFree, boolean similarChanged, boolean isSimilar) {
        if (this.editTextName == null ||
                this.editTextTeacher == null ||
                this.editTextRoom == null ||
                this.spinnerOptions == null ||
                this.checkBoxFree == null) return;

        if (isFree || isSimilar) {
            this.editTextName.setEnabled(false);
            this.editTextTeacher.setEnabled(false);
            this.editTextRoom.setEnabled(false);
            this.spinnerOptions.setEnabled(false);
        } else {
            this.editTextName.setEnabled(true);
            this.editTextTeacher.setEnabled(true);
            this.editTextRoom.setEnabled(true);
            this.spinnerOptions.setEnabled(true);
        }

        if (freeChanged) {
            if (isFree) {
                if (!isSimilar && !this.isCachingInputSuppressed) {
                    this.cachedName = this.editTextName.getText().toString();
                    this.cachedTeacher = this.editTextTeacher.getText().toString();
                    this.cachedRoom = this.editTextRoom.getText().toString();
                }

                this.editTextName.setText(getString(R.string.word_free));
                this.editTextTeacher.setText("-");
                this.editTextRoom.setText("-");
            } else {
                this.editTextName.setText(this.cachedName);
                this.editTextTeacher.setText(this.cachedTeacher);
                this.editTextRoom.setText(this.cachedRoom);
            }
        }

        if (similarChanged) {
            if (isSimilar) {
                this.cachedIsFree = isFree;
                if (!this.cachedIsFree) {
                    this.cachedName = this.editTextName.getText().toString();
                    this.cachedTeacher = this.editTextTeacher.getText().toString();
                    this.cachedRoom = this.editTextRoom.getText().toString();
                }

                this.checkBoxFree.setEnabled(false);

                if (this.lessonBefore != null) {
                    if (this.lessonBefore.isFree()) {
                        this.checkBoxFree.setChecked(true);
                    } else {
                        this.checkBoxFree.setChecked(false);

                        String name = this.lessonBefore.getName();
                        String teacher = this.lessonBefore.getTeacher();
                        String room = this.lessonBefore.getRoom();
                        this.editTextName.setText(name);
                        this.editTextTeacher.setText(isStringBlank(teacher) ? "-" : teacher);
                        this.editTextRoom.setText(isStringBlank(room) ? "-" : room);
                    }
                }
            } else {
                this.isCachingInputSuppressed = true;
                this.checkBoxFree.setEnabled(true);

                if (this.cachedIsFree) {
                    this.checkBoxFree.setChecked(true);
                } else {
                    this.checkBoxFree.setChecked(false);
                    this.editTextName.setText(this.cachedName);
                    this.editTextTeacher.setText(this.cachedTeacher);
                    this.editTextRoom.setText(this.cachedRoom);
                }
                this.isCachingInputSuppressed = false;
            }
        }
    }
}
