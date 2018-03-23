package de.vent_projects.ffg_planner.schedule.transfer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.vent_projects.ffg_planner.download.schedule.objects.DownloadLesson;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;

public class ScheduleMergeTransferer extends ScheduleTransferer {
    private static final String TAG = ScheduleMergeTransferer.class.getSimpleName();

    public ScheduleMergeTransferer(Context context, TransferContext transferContext) {
        super(context, transferContext);
    }

    protected void transfer() {
        ArrayList<SetupLesson> lessons = new ArrayList<>();
        for (int day = 2; day <= 6; day++) {
            for (int period = 1; period <= 8; period++) {
                SetupLesson lesson;
                if (this.downloadScheduleManager.isABLessonForDayAndPeriod(day, period)) {
                    lesson = getUniqueABLesson(day, period, new ArrayList<>(this.downloadScheduleManager.getAllALessonsForDayAndPeriod(day, period)), new ArrayList<>(this.downloadScheduleManager.getAllBLessonsForDayAndPeriod(day, period)));
                } else {
                    lesson = getUniqueLesson(day, period, new ArrayList<>(this.downloadScheduleManager.getAllLessonsForDayAndPeriod(day, period)), false);
                }
                lessons.add(lesson);
            }
        }
        this.setupScheduleManager.setLessons(lessons);
        this.setupScheduleManager.getAllLessons(); // Only to fix an Bug of Realm (refresh list of lessons)
    }

    private SetupLesson getUniqueLesson(int day, int period, List<DownloadLesson> lessons, boolean isBlesson) {
        boolean ignoredLessons = this.ignorePossibleSubjects(lessons);
        if (lessons.size() == 1) {
            return new SetupLesson(lessons.get(0), isBlesson);
        } else {
            SetupLesson lesson = new SetupLesson(day, period, isBlesson);
            if (!ignoredLessons && lessons.size() == 0) {
                lesson.makeFree();
            } else {
                lesson.markAsEmpty();
            }
            return lesson;
        }
    }

    private boolean ignorePossibleSubjects(List<DownloadLesson> lessons) {
        boolean ignoredLessons = false;
        for (int i = lessons.size() - 1; i >= 0; i--) {
            if (lessons.get(i) == null) continue;
            if (downloadScheduleManager.isLessonPossible(lessons.get(i))) {
                lessons.remove(i);
                ignoredLessons = true;
            }
        }
        return ignoredLessons;
    }

    private SetupLesson getUniqueABLesson(int day, int period, List<DownloadLesson> aLessons, List<DownloadLesson> bLessons) {
        SetupLesson aLesson = getUniqueLesson(day, period, aLessons, false);
        SetupLesson bLesson = getUniqueLesson(day, period, bLessons, true);
        aLesson.setBlesson(bLesson);
        return aLesson;
    }
}
