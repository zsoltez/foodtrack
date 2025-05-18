package com.example.foodtrack.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.foodtrack.DashboardActivity;

public class NotificationHelper {
    private static final String LOG_TAG = NotificationHelper.class.getName();
    private static final String CHANNEL_ID = "foodtrack_daily";
    private final int NOTIFICATION_ID = 0;

    private NotificationManager mNotifyManager;
    private Context mContext;


    public NotificationHelper(Context context) {
        this.mContext = context;
        this.mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel
                (CHANNEL_ID, "Napi emlékeztető", NotificationManager.IMPORTANCE_HIGH);

        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setDescription("Értesítés az étkezések rögzítéséről");

        mNotifyManager.createNotificationChannel(channel);
    }

    private boolean isAllowedToSendNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(LOG_TAG, "Értesítés engedély nélkül – nem küldhető.");
                return false; // Nincs engedély -> ne küldjön értesítést
            }
        }
        return true;
    }

    public void send(String message) {
        if (!isAllowedToSendNotification()) return;

        Intent intent = new Intent(mContext, DashboardActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Foodtrack emlékeztető")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent);

        mNotifyManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void calorieLimitNotification(String message) {
        if (!isAllowedToSendNotification()) return;

        Intent intent = new Intent(mContext, DashboardActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Foodtrack kalória jelentés")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent);

        mNotifyManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancel() {
        mNotifyManager.cancel(NOTIFICATION_ID);
    }
}