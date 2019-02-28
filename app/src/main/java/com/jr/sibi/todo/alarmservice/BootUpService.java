package com.jr.sibi.todo.alarmservice;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.jr.sibi.todo.dbhelper.DbContract;

import java.util.Calendar;

public class BootUpService extends JobIntentService {

    public static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BootUpService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //Toast.makeText(getApplicationContext(), "Our BroadcastReceiver works! Fantastic!", Toast.LENGTH_LONG).show();

        Log.i("Boot_complete", "Start");
        String[] projection = {
                DbContract.TaskEntry._ID,
                DbContract.TaskEntry.KEY_MILLISEC
        };
        String status = "false";
        String[] selectionArgs;
        String selection;

        Long currentTime = Calendar.getInstance().getTimeInMillis();

        selectionArgs = new String[]{currentTime.toString(),status};
        selection = DbContract.TaskEntry.KEY_MILLISEC + ">? and "+ DbContract.TaskEntry.KEY_MILLISEC + "!=9999999999999 and "+ DbContract.TaskEntry.KEY_STATUS + "=?";

        Cursor cursor = getApplicationContext().getContentResolver().query(DbContract.TaskEntry.TODO_CONTENT_URI, projection, selection, selectionArgs, DbContract.TaskEntry.KEY_MILLISEC + " ASC");
        if(cursor != null){
            while (cursor.moveToNext()) {
                int idColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry._ID);
                int milliSecColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_MILLISEC);

                // Extract out the value from the Cursor for the given column index
                String id = cursor.getString(idColumnIndex);
                String milliSec = cursor.getString(milliSecColumnIndex);

                Log.i("Boot_complete", "id : "+id +" millic : "+milliSec);

                Uri currentVehicleUri = ContentUris.withAppendedId(DbContract.TaskEntry.TODO_CONTENT_URI, Long.parseLong(id));
                new AlarmScheduler().setTodoAlarm(getApplicationContext(), Long.parseLong(milliSec), currentVehicleUri);
            }
            cursor.close();
        }
    }

}
