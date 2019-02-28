package com.jr.sibi.todo.alarmservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;


public class AlarmScheduler {

    public void setTodoAlarm(Context context, long alarmTime, Uri reminderTask) {
        PendingIntent operation = TodoAlarmService.getTodoPendingIntent(context, reminderTask);
        setAlarm(context,alarmTime,operation);
    }

    public void setTodoRepeatAlarm(Context context, long alarmTime, Uri reminderTask, long RepeatTime) {
        PendingIntent operation = TodoAlarmService.getTodoPendingIntent(context, reminderTask);
        setRepeatAlarm(context,alarmTime,RepeatTime,operation);
    }

    public void cancelTodoAlarm(Context context, Uri todoTask) {
        PendingIntent operation = TodoAlarmService.getTodoPendingIntent(context, todoTask);
        cancelAlarm(context,operation);
    }

    public void setAlarm(Context context, long alarmTime, PendingIntent operation){
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);
//        if (Build.VERSION.SDK_INT >= 23) {
//            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, operation);
       /* } else */ if (Build.VERSION.SDK_INT >= 19) {
            manager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, operation);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, operation);
        }
    }

    public void setRepeatAlarm(Context context, long alarmTime, long RepeatTime,PendingIntent operation) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, RepeatTime, operation);
    }

    public void cancelAlarm(Context context, PendingIntent operation) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);
        manager.cancel(operation);
    }


}