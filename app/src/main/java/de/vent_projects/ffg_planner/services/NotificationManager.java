package de.vent_projects.ffg_planner.services;

import android.app.NotificationChannel;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import de.vent_projects.ffg_planner.R;

public class NotificationManager {
    private Context context;
    private android.app.NotificationManager notificationManager;

    public NotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notify(int ID, NotificationCompat.Builder notificationBuilder) {
        this.notificationManager.notify(ID, notificationBuilder.build());
    }

    public void registerNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            registerReplacementsNotificationChannel();
        }
    }

    @RequiresApi(26)
    private void registerReplacementsNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel("replacements", context.getString(R.string.word_replacements), android.app.NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(false);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
