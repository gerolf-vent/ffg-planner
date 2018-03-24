package de.vent_projects.ffg_planner.services;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.main.MainActivity;
import de.vent_projects.ffg_planner.main.MainSections;
import de.vent_projects.ffg_planner.replacement.DownloadReplacementListener;
import de.vent_projects.ffg_planner.replacement.ReplacementManager;
import de.vent_projects.ffg_planner.replacement.objects.DateReplacement;
import de.vent_projects.ffg_planner.replacement.objects.ReplacementChangesCalculator;

public class UpdateService extends ServiceManager {

    public UpdateService(Context context) {
        super(context);
    }

    private static void updateReplacement(final Context context) {
        final ReplacementManager replacementManager = new ReplacementManager(context);
        final List<DateReplacement> oldDateReplacements = replacementManager.getAllDateReplacements();
        replacementManager.downloadReplacementFromServerAsync(new DownloadReplacementListener() {
            @Override
            public void onFinished() {
                notifyUserAboutNewReplacement(context, oldDateReplacements, replacementManager.getAllDateReplacements());
            }

            @Override
            public void onNoNetworkAvailable() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private static void notifyUserAboutNewReplacement(Context context, List<DateReplacement> oldDateReplacements, List<DateReplacement> newDateReplacements) {
        ReplacementChangesCalculator replacementChangesCalculator = new ReplacementChangesCalculator(oldDateReplacements, newDateReplacements);
        if (replacementChangesCalculator.getAddedDateReplacements().size() > 0 || replacementChangesCalculator.getChangedDateReplacements().size() > 0) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "replacements");
            notificationBuilder.setSmallIcon(R.drawable.ic_school);
            notificationBuilder.setContentTitle(context.getString(R.string.notification_title_replacement_changed));
            notificationBuilder.setContentText(context.getString(R.string.notification_text_replacement_changed));
            Intent replacementIntent = new Intent(context, MainActivity.class);
            replacementIntent.putExtra("active_section", MainSections.REPLACEMENT.name());
            PendingIntent clickedReplacementIntent = PendingIntent.getActivities(context, 0, new Intent[]{replacementIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(clickedReplacementIntent);
            new NotificationManager(context).notify(123, notificationBuilder);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected Class<?> getServiceClass() {
        return UpdateService.class;
    }

    @Override
    protected Class<?> getJobClass() {
        return UpdateJob.class;
    }

    @Override
    @RequiresApi(21)
    protected JobInfo.Builder editJobInfo(JobInfo.Builder jobInfoBuilder) {
        jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        return jobInfoBuilder;
    }

    @Override
    protected int getFrequency() {
        return 3600000;
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    protected boolean needsReschedule() {
        return false;
    }

    @Override
    protected int getID() {
        return 10101010;
    }

    @RequiresApi(21)
    private static class UpdateJob extends JobService {

        @Override
        public boolean onStartJob(JobParameters params) {
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }

    }
}
