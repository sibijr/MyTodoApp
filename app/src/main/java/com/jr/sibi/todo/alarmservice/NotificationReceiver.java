package com.jr.sibi.todo.alarmservice;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by sibi-4939 on 09/05/18.
 */

public class NotificationReceiver  extends IntentService {


    private static final String TAG = NotificationReceiver.class.getSimpleName();


    public NotificationReceiver() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", 0);

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}
