package com.jr.sibi.todo.alarmservice;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.jr.sibi.todo.R;
import com.jr.sibi.todo.dbhelper.DbContract;

/**
 * Created by sibi-4939 on 10/05/18.
 */

public class TaskReceiver extends IntentService {

    private static final String TAG = TaskReceiver.class.getSimpleName();

    public TaskReceiver() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri uri = intent.getData();
        Context context = getApplicationContext();

        if(uri != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbContract.TaskEntry.KEY_STATUS, "true");
            context.getContentResolver().update(uri,contentValues,null,null);
            Intent intent2 = new Intent("ListViewDataUpdated");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
            Toast.makeText(context, context.getString(R.string.notification_task_done), Toast.LENGTH_SHORT).show();
        }



        int notificationId = intent.getIntExtra("notificationId", 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}
