package de.vent_projects.ffg_planner.schedule.transfer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import de.vent_projects.ffg_planner.download.schedule.DownloadScheduleManager;
import de.vent_projects.ffg_planner.main.settings.MainSettingsManager;
import de.vent_projects.ffg_planner.setup.schedule.SetupScheduleManager;
import de.vent_projects.ffg_planner.setup.settings.SetupSettingsManager;

public abstract class ScheduleTransferer {
    private static final String TAG = ScheduleTransferer.class.getSimpleName();

    protected SetupScheduleManager setupScheduleManager;
    protected DownloadScheduleManager downloadScheduleManager;

    public ScheduleTransferer(Context context, TransferContext transferContext) {
        this.setupScheduleManager = new SetupScheduleManager(context);
        this.downloadScheduleManager = new DownloadScheduleManager(context, transferContext == TransferContext.SETUP ? new SetupSettingsManager(context) : new MainSettingsManager(context));
    }

    protected abstract void transfer();

    public void transferAsync(ScheduleTransferListener listener) {
        new ScheduleTransformAsyncTask(this, listener).execute();
    }

    private static class ScheduleTransformAsyncTask extends AsyncTask<Void, Void, Void> {
        private ScheduleTransferListener listener;
        private ScheduleTransferer scheduleTransferer;

        public ScheduleTransformAsyncTask(ScheduleTransferer scheduleTransferer, ScheduleTransferListener listener) {
            this.scheduleTransferer = scheduleTransferer;
            this.listener = listener;
        }

        public Void doInBackground(Void ...voids) {
            this.scheduleTransferer.transfer();
            Log.d(TAG, "Transfer finished");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "onPostExecute()");
            if (listener != null) listener.onFinished();
        }
    }
}
