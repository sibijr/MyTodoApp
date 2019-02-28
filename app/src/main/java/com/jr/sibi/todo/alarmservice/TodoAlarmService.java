package com.jr.sibi.todo.alarmservice;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.jr.sibi.todo.R;
import com.jr.sibi.todo.dbhelper.DbContract;
import com.jr.sibi.todo.fragment.Todo.AddTodoActivity;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Created by sibi-4939 on 06/04/18.
 */

public class TodoAlarmService extends IntentService {
    private static final String TAG = TodoAlarmService.class.getSimpleName();

    Cursor cursor;
    //This is a deep link intent, and needs the task stack
    public static PendingIntent getTodoPendingIntent(Context context, Uri uri) {
        Intent action = new Intent(context, TodoAlarmService.class);
        action.setData(uri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        }else {
            return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public TodoAlarmService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this).build());
        }
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i("Alarm","Triggered");

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        Uri uri = intent.getData();

        //Display a notification to view the task details
        Intent action = new Intent(getApplicationContext(), AddTodoActivity.class);
        action.setData(uri);
        PendingIntent operation = TaskStackBuilder.create(getApplicationContext())
                .addNextIntentWithParentStack(action)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Grab the task description
        if(uri != null){
            cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
        }

        String description = "";
        String todoId = "";
        String status = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                description = DbContract.getColumnString(cursor, DbContract.TaskEntry.KEY_TITLE);
                todoId = DbContract.getColumnString(cursor, DbContract.TaskEntry._ID);
                status = DbContract.getColumnString(cursor, DbContract.TaskEntry.KEY_STATUS);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if(status.equals("false")) {
            String CHANNEL_ID = "my_todo_01";// The id of the channel.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
            builder.setSmallIcon(R.drawable.ic_notifications_on_white_24dp);
            builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_notifications_on_white_24dp));
            builder.setContentTitle(getApplicationContext().getString(R.string.todo_title));
            builder.setContentText(description);
            builder.setContentIntent(operation);
            builder.setAutoCancel(true);
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);

            Intent doneIntent = new Intent(getApplicationContext(), TaskReceiver.class);
            doneIntent.setData(uri);
            doneIntent.putExtra("notificationId", Integer.parseInt(todoId));

            PendingIntent pendingIntentDone = PendingIntent.getService(getApplicationContext(), Integer.parseInt(todoId), doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent buttonIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
            buttonIntent.putExtra("notificationId", Integer.parseInt(todoId));

            PendingIntent dismissIntent = PendingIntent.getService(getApplicationContext(), Integer.parseInt(todoId), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            builder.addAction(android.R.drawable.checkbox_on_background, "MARK AS DONE", pendingIntentDone);
            builder.addAction(android.R.drawable.ic_delete, "DISMISS", dismissIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getApplicationContext().getString(R.string.app_name);// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                manager.createNotificationChannel(mChannel);
            }

            manager.notify(Integer.parseInt(todoId), builder.build());
        }
    }


}
