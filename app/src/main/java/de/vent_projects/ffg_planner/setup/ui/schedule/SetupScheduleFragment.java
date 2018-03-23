package de.vent_projects.ffg_planner.setup.ui.schedule;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.download.schedule.DownloadScheduleManager;
import de.vent_projects.ffg_planner.main.schedule.MainScheduleManager;
import de.vent_projects.ffg_planner.schedule.transfer.ScheduleMergeTransferer;
import de.vent_projects.ffg_planner.schedule.transfer.ScheduleTransferListener;
import de.vent_projects.ffg_planner.schedule.transfer.TransferContext;
import de.vent_projects.ffg_planner.setup.schedule.SetupScheduleManager;
import de.vent_projects.ffg_planner.setup.schedule.objects.SetupLesson;
import de.vent_projects.ffg_planner.setup.settings.SetupSettingsManager;
import de.vent_projects.ffg_planner.setup.ui.SetupFragment;
import de.vent_projects.ffg_planner.setup.ui.SetupFragmentInterface;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SetupScheduleFragment extends SetupFragment implements SetupFragmentInterface {
    private static final String TAG = SetupScheduleFragment.class.getSimpleName();

    private ViewPager viewPager;
    private SetupScheduleManager scheduleManager;
    private RealmResults<SetupLesson> lessons;

    public SetupScheduleFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.viewPager = view.findViewById(R.id.view_pager);

        if (this.viewPager != null) {
            this.viewPager.setOffscreenPageLimit(5);
            this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    requestTitleRefresh();
                    requestNavigationRefresh();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        this.scheduleManager = new SetupScheduleManager(getContext());
        this.scheduleManager.purgeSchedule();

        this.lessons = scheduleManager.getAllLessons();
        this.lessons.addChangeListener(new RealmChangeListener<RealmResults<SetupLesson>>() {
            @Override
            public void onChange(RealmResults<SetupLesson> setupLessons) {
                requestNavigationRefresh();
            }
        });

        requestTitleRefresh();

        reset(true);

        super.onViewCreated(view, savedInstanceState);
    }

    // SETUP FRAGMENT INTERFACE

    @Override
    public SetupFragmentInterface getSetupFragmentInterface() {
        return this;
    }

    @Override
    public boolean canGoBack() {
        return true;
    }

    @Override
    public boolean canGoNext() {
        return !(this.getSubstepPosition() == this.getSubstepCount() - 1 && scheduleManager != null && !scheduleManager.areAllLessonsNonEmpty());
    }

    @Override
    public void goBack() {
        if (this.viewPager != null) {
            this.viewPager.setCurrentItem(this.viewPager.getCurrentItem() - 1, true);
        }
    }

    @Override
    public void goNext() {
        if (this.viewPager != null) {
            this.viewPager.setCurrentItem(this.viewPager.getCurrentItem() + 1, true);
        }
    }

    @Override
    public int getSubstepCount() {
        return 5;
    }

    @Override
    public int getSubstepPosition() {
        return this.viewPager == null ? 0 : this.viewPager.getCurrentItem();
    }

    @Override
    public String getTitle() {
        return getContext() == null ? null : getResources().getStringArray(R.array.week_days)[getSubstepPosition()];
    }

    @Override
    public boolean isResetable() {
        return true;
    }

    @Override
    public void reset() {
        reset(false);
    }

    public void reset(final boolean resetViewPager) {
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getContext().getString(R.string.progress_adjust_schedule));
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        DownloadScheduleManager downloadScheduleManager = new DownloadScheduleManager(getContext(), new SetupSettingsManager(getContext()));
        downloadScheduleManager.removeUnselectedSubjects();

        ScheduleMergeTransferer scheduleTransferer = new ScheduleMergeTransferer(getContext(), TransferContext.SETUP);
        scheduleTransferer.transferAsync(new ScheduleTransferListener() {
            @Override
            public void onFinished() {
                if (resetViewPager) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setAdapter(new SetupSchedulePagerAdapter(getFragmentManager()));
                        }
                    });
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onFinish() {
        SetupSettingsManager setupSettingsManager = new SetupSettingsManager(getContext());
        setupSettingsManager.saveConfigration();

        MainScheduleManager mainScheduleManager = new MainScheduleManager(getContext());
        mainScheduleManager.replaceSchedule(scheduleManager.getAllLessons());
    }
}
