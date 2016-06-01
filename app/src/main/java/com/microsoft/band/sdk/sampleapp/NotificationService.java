package com.microsoft.band.sdk.sampleapp;

import android.app.IntentService;
import android.content.Intent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.view.View;

import com.microsoft.band.sdk.sampleapp.streaming.R;


public class NotificationService extends IntentService {

    public static String NotificationMessage = "message";
    private static final int NOTIFICATION_ID = 3445;

    public NotificationService() {
        super("NotificationService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String text = intent.getStringExtra(NotificationMessage);
        showText(text);

    }

    private void showText(final String text) {
        Intent intent = new Intent(this, BandStreamingAppActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BandStreamingAppActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker")
                .setContentText("Your child is awake!")
                .setContentTitle("WearA'more")
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}

/* How to use:

Message BabyAwake = new Message("The baby has awoken");
Notification Awake = new Awake(BabyAwake);

DMS dms = new DMS(Awake);
dms.onHandleIntent(intent);

 */
