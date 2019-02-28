package com.jr.sibi.todo.alarmservice;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.jr.sibi.todo.dbhelper.DbContract;

import java.util.Calendar;

import static android.support.v4.app.JobIntentService.enqueueWork;
import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;
import static com.jr.sibi.todo.alarmservice.BootUpService.JOB_ID;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {

            Log.i("BootCompleted", "Our BroadcastReceiver works! Fantastic!");
            Intent serviceIntent = new Intent(context, BootUpService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(serviceIntent);
//            }else{
//                context.startService(serviceIntent);
//            }

//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                enqueueWork(context, BootUpService.class, JOB_ID, serviceIntent);
//            }else{
//                context.startService(serviceIntent);
//            }
        }

    }
}