package com.jr.sibi.todo.fragment.Todo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.jr.sibi.todo.R;
import com.jr.sibi.todo.alarmservice.AlarmScheduler;
import com.jr.sibi.todo.dbhelper.DbContract;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sibi-4939 on 08/04/18.
 */

public class TodoCursorAdapter extends CursorAdapter {

    private TextView mTitleText, mDateAndTimeText, mCategoryText,mSeparator;
    private ImageView mActiveImage , mThumbnailImage;
    private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;
    private TextDrawable mDrawableBuilder;
    private CheckBox checkBox;
    private Cursor cursor;

    public TodoCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        this.cursor = cursor;
        return LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        final Cursor cursor1 = cursor;

        Boolean showSubHeader = false;
        mTitleText = (TextView) view.findViewById(R.id.recycle_title);
        mDateAndTimeText = (TextView) view.findViewById(R.id.recycle_date_time);
        mCategoryText = (TextView) view.findViewById(R.id.recycle_category);
        mActiveImage = (ImageView) view.findViewById(R.id.active_image);
        checkBox = (CheckBox) view.findViewById(R.id.task_delete);
        mSeparator = (TextView) view.findViewById(R.id.separator);

        int titleColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_TITLE);
        int dateColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_DATE);
        int timeColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_TIME);
        int millisecColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_MILLISEC);
        int statusColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_STATUS);
        int categoryIndex = cursor.getColumnIndex(DbContract.CategoryEntry.KEY_CATEGORY);
        int idIndex = cursor.getColumnIndex(DbContract.TaskEntry._ID);

        final String title = cursor.getString(titleColumnIndex);
        String date = cursor.getString(dateColumnIndex);
        String time = cursor.getString(timeColumnIndex);
        String millisec = cursor.getString(millisecColumnIndex);
        String status = cursor.getString(statusColumnIndex);
        String category = cursor.getString(categoryIndex);
        final Integer id = cursor.getInt(idIndex);

//        cursor.moveToPrevious();
//        String preMilliSec = cursor.getString(millisecColumnIndex);
//        cursor.moveToNext();
        String preMilliSec = null;
        if(cursor.isFirst()){
            showSubHeader = true;
        }else{
            cursor.moveToPrevious();
            preMilliSec = cursor.getString(millisecColumnIndex);
            cursor.moveToNext();
        }
        String displayHead = "Set Header";

        if(preMilliSec==null){
            showSubHeader=true;
            displayHead = getDateString(Long.parseLong(millisec));
        }else{
            String currentDisplay = getDateString(Long.parseLong(millisec));
            String prevDisplay = getDateString(Long.parseLong(preMilliSec));
            if(!currentDisplay.equals(prevDisplay)){
                showSubHeader=true;
                displayHead = currentDisplay;
            }
        }

        if(showSubHeader && !status.equals("true")){
            mSeparator.setVisibility(View.VISIBLE);
            mSeparator.setText(displayHead);
            if(displayHead.equals("Over Due")){
                mSeparator.setTextColor(Color.RED);
            }
        }else {
            mSeparator.setVisibility(View.GONE);
        }

        String dateTime = (date.equals(""))?"": getDate(Long.parseLong(millisec), "dd/MM/yyyy  hh:mm aa");

        setTodoTitle(title,status);
        setTodoDateTime(dateTime);
        setTodoCategory(category);
        if(status.equals("true")){
            checkBox.setChecked(true);
        }else {
            checkBox.setChecked(false);
        }
        checkBox.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String status = "false";
                if (((CheckBox) v).isChecked()) {
                    status="true";
                }
                markAsTodoAsDone(view,status,context,title,cursor1,id);
            }

        });



    }

    private String getDateString(long smsTimeInMilis){
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);
        Calendar now = Calendar.getInstance();
        if(smsTimeInMilis == 9999999999999l){
            return "No Due";
        } if (now.get(Calendar.YEAR) > smsTime.get(Calendar.YEAR) || (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR) && now.get(Calendar.MONTH) > smsTime.get(Calendar.MONTH)) || (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR) && now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH) && now.get(Calendar.DATE) > smsTime.get(Calendar.DATE))) {
            return "Over Due";
        }else if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) && smsTime.get(Calendar.MONTH) == now.get(Calendar.MONTH) && smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            return "Today";
        } else if (smsTime.get(Calendar.DATE) - now.get(Calendar.DATE)  == 1 && smsTime.get(Calendar.MONTH) == now.get(Calendar.MONTH) && smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) ){
            return "Tomorrow";
        }else if (smsTime.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) && smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)){
            return "This week";
        }else if (smsTime.get(Calendar.WEEK_OF_YEAR) - now.get(Calendar.WEEK_OF_YEAR) == 1 && smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)){
            return "Next week";
        }else if (smsTime.get(Calendar.MONTH) == now.get(Calendar.MONTH) && smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)){
            return "This Month";
        } else if (smsTime.get(Calendar.MONTH) - now.get(Calendar.MONTH) ==1 && smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            return "Next Month";
        }else if (smsTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)){
            return "This Year";
        } else if (smsTime.get(Calendar.YEAR) - now.get(Calendar.YEAR) ==1) {
            return "Next Year";
        } else {
            return "Later";
        }
    }


    private void markAsTodoAsDone(View view,final String status,final Context context,final String title,final Cursor cursor1,final Integer id ) {
            Animation fadeout = AnimationHelper.slideOutRight(context);
            deleteOnAnimationComplete(fadeout,status,context,title,cursor1,id);
            animate(view, fadeout);

    }

    // This is our listener for when the delete animate completes
    // We then update our data set (removing the fruit)
    private void deleteOnAnimationComplete(Animation fadeout,final String status,final Context context,final String title,final Cursor cursor1,final Integer id ) {
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                ContentValues values = new ContentValues();
                values.put(DbContract.TaskEntry.KEY_STATUS,status);
                String[] selectionArgs = new String[]{id.toString()};
                String selection = DbContract.TaskEntry._ID + "=?";
                context.getContentResolver().update(DbContract.TaskEntry.TODO_CONTENT_URI,values,selection,selectionArgs);
                setTodoTitle(title,status);
                cursor1.requery();
                notifyDataSetChanged();
            }
        });
    }

    // actually do the animate on our row
    private static void animate(View view, Animation animation) {
        view.startAnimation(animation);
    }


    // Set reminder title view
    public void setTodoTitle(String title,String status) {
        mTitleText.setText(title);
        if(status.contentEquals("true")){
            mTitleText.setPaintFlags(mTitleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            mTitleText.setPaintFlags( mTitleText.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    // Set date and time views
    public void setTodoDateTime(String datetime) {
        if (datetime.equals("")){
            datetime = "No Due";
        }
        mDateAndTimeText.setText(datetime);
    }

    public void setTodoCategory(String category){
        mCategoryText.setText(category);
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


}
