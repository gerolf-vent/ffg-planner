package de.vent_projects.ffg_planner.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Calendar;
import java.util.List;

public abstract class ServiceManager extends Service {
    protected Context context;

    public ServiceManager(Context context) {
        this.context = context;
    }

    public void manageService() {
        if (Build.VERSION.SDK_INT >= 21) {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler == null) {
                // TODO: Error
                return;
            }
            boolean jobRunning = false;
            List<JobInfo> runningJobs = scheduler.getAllPendingJobs();
            for (JobInfo info : runningJobs) {
                if (info.getId() == this.getID()) {
                    jobRunning = true;
                }
            }
            if (!jobRunning) {
                ComponentName job = new ComponentName(context, getJobClass());
                JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(this.getID(), job)
                        .setPeriodic(this.getFrequency());
                this.editJobInfo(jobInfoBuilder);
                scheduler.schedule(jobInfoBuilder.build());
            }
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), this.getFrequency(), this.getServiceIntent());
                alarmManager.cancel(this.getServiceIntent());
            }
        }
    }

    public static void manageServices() {

    }

    // SERVICE

    private PendingIntent getServiceIntent() {
        return PendingIntent.getService(context, this.getID(), new Intent(context, getServiceClass()), 0);
    }

    protected abstract Class<?> getServiceClass();

    // JOB

    protected abstract Class<?> getJobClass();

    @RequiresApi(21)
    protected abstract JobInfo.Builder editJobInfo(JobInfo.Builder jobInfoBuilder);

    // BOTH

    protected abstract int getID();

    protected abstract int getFrequency();

    protected abstract boolean isEnabled();

    protected abstract boolean needsReschedule();
}
