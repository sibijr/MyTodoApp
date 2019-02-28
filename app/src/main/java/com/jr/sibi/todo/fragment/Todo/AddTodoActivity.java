package com.jr.sibi.todo.fragment.Todo;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.jr.sibi.todo.R;
import com.jr.sibi.todo.alarmservice.AlarmScheduler;
import com.jr.sibi.todo.dbhelper.DbContract;
import com.jr.sibi.todo.fragment.MainActivity;
import com.jr.sibi.todo.fragment.TodoConstants;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import static com.jr.sibi.todo.fragment.TodoConstants.ALL;
import static com.jr.sibi.todo.fragment.TodoConstants.CURRENT_CATEGORY;
import static com.jr.sibi.todo.fragment.TodoConstants.CURRENT_POS;
import static com.jr.sibi.todo.fragment.TodoConstants.FINISHED;
import static com.jr.sibi.todo.fragment.TodoConstants.NO_DUE_DATE;
import static com.jr.sibi.todo.fragment.TodoConstants.NO_DUE_TIME;

/**
 * Created by sibi-4939 on 05/04/18.
 */

public class AddTodoActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_VEHICLE_LOADER = 1;
    private Toolbar mToolbar;
    private EditText mTitleText;
    private TextView mDateText, mTimeText,mCategoryText ;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private Calendar mCalendar;
    private int mYear, mMonth, mHour, mMinute, mDay;
    private String mTitle;
    private String mTime;
    private String mDate;
    private String mCategory;
    private String mStatus;
    private int mCategoryId;

    private Uri mCurrentTodoUri;
    private int mCurrentPosition;
    private String mCurrentCategory;
    private boolean mVehicleHasChanged = false;

    // Values for orientation change
    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_DATE = "date_key";
    private static final String KEY_CATEGORY = "category_key";
    private static final String KEY_STATUS = "status_key";

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mVehicleHasChanged = true;
            return false;
        }
    };

    static ArrayList<String> items = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_add_details);

        Intent intent = getIntent();
        mCurrentTodoUri = intent.getData();


        if (mCurrentTodoUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_todo));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_todo));
            getLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, this);
        }

        updateCategoryList(null);
        // Initialize Views
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleText = (EditText) findViewById(R.id.todo_title);
        mDateText = (TextView) findViewById(R.id.set_date);
        mTimeText = (TextView) findViewById(R.id.set_time);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mCategoryText = (TextView) findViewById(R.id.set_category_type);

        // Initialize default values
        mStatus = "false";
        mCategoryId=0;

        mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.HOUR, 1);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);

        mDate = "";//mDay + "/" + mMonth + "/" + mYear;
        mTime = "";//mHour + ":" + mMinute;

        // Setup Reminder Title EditText
        mTitleText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitle = s.toString().trim();
                mTitleText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        // Setup TextViews using reminder values
        mDateText.setText(NO_DUE_DATE);
        mTimeText.setText(NO_DUE_TIME);
        mCategoryText.setText(items.get(0));

        // To save state on device rotation
        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitleText.setText(savedTitle);
            mTitle = savedTitle;

            String savedTime = savedInstanceState.getString(KEY_TIME);
            mTimeText.setText(savedTime);
            mTime = (savedTime.equals(TodoConstants.NO_DUE_TIME))?"":savedTime;

            String savedDate = savedInstanceState.getString(KEY_DATE);
            mDateText.setText(savedDate);
            mDate = (savedDate.equals(TodoConstants.NO_DUE_DATE))?"":savedDate;

            String savedCategory = savedInstanceState.getString(KEY_CATEGORY);
            mCategoryText.setText(savedCategory);
            mCategory = savedCategory;

            mStatus = savedInstanceState.getString(KEY_STATUS);
        }

        // Setup up active buttons
        if (mStatus.equals("false")) {
            mFAB1.setVisibility(View.VISIBLE);
            mFAB2.setVisibility(View.GONE);

        } else if (mStatus.equals("true")) {
            mFAB1.setVisibility(View.GONE);
            mFAB2.setVisibility(View.VISIBLE);
        }

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            mCurrentPosition=0;
        } else {
            mCurrentPosition= extras.getInt(CURRENT_POS);
            String CurrentCategory = extras.getString(CURRENT_CATEGORY,null);
            if(CurrentCategory!=null &&  !(CurrentCategory.equals(FINISHED) || CurrentCategory.equals(ALL))){
                mCategoryId = items.indexOf(CurrentCategory);
                mCategory = items.get(mCategoryId);
                mCategoryText.setText(mCategory);
            }
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, mTitleText.getText());
        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_DATE, mDateText.getText());
        outState.putCharSequence(KEY_CATEGORY, mCategoryText.getText());
        outState.putCharSequence(KEY_STATUS, mStatus);
    }

    // On clicking Time picker
    public void setTime(View v){
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setThemeDark(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    // On clicking Date picker
    public void setDate(View v){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    // Obtain time from time picker
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        if (minute < 10) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        mTimeText.setText(mTime);
        updateDate();
    }

    // Obtain date from date picker
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear ++;
        mDay = dayOfMonth;
        mMonth = monthOfYear;
        mYear = year;

        mDate = mDay + "/" + mMonth + "/" + mYear; //year + "-" + monthOfYear + "-" + dayOfMonth;
        mDateText.setText(mDate);
        mTimeText.setText(mTime);
        updateTime();
    }

    public void updateTime(){
        if(!mTime.equals("")){
            return;
        }
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        if (mMinute < 10) {
            mTime = mHour + ":" + "0" + mMinute;
        } else {
            mTime = mHour + ":" + mMinute;
        }
        mTimeText.setText(mTime);
    }

    public void updateDate(){
        if(!mDate.equals("")){
            return;
        }
        mCalendar = Calendar.getInstance();
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);

        mDate = mDay + "/" + mMonth + "/" + mYear; //year + "-" + monthOfYear + "-" + dayOfMonth;
        mDateText.setText(mDate);
    }

    // On clicking the active button
    public void selectFab1(View v) {
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mStatus = "true";
    }

    // On clicking the inactive button
    public void selectFab2(View v) {
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mStatus = "false";
    }




    // On clicking repeat type button
    public void selectCategoryType(View v){

        // Create List Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        String[] arr = new String[items.size()];
        arr = items.toArray(arr);
        builder.setItems(arr, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                mCategory = items.get(item);
                mCategoryId = item;
                mCategoryText.setText(mCategory);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void addCategoryType(View v){
        final EditText taskEditText = new EditText(this);
        taskEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        ContentValues values = new ContentValues();

                        values.put(DbContract.CategoryEntry.KEY_CATEGORY, task);
                        getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI, values);

                        updateCategoryList(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

    }

    public void updateCategoryList(String selectedCategory){
        items.clear();
        String[] projection = {
                DbContract.CategoryEntry.KEY_CATEGORY
        };
        Cursor cursor = getContentResolver().query(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,projection,null,null,null);
        try {
            if(cursor.isAfterLast()){
                ContentValues  values = new ContentValues();
                values.put(DbContract.CategoryEntry._ID,0);
                values.put(DbContract.CategoryEntry.KEY_CATEGORY,"Default");
                getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);
                cursor = getContentResolver().query(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,projection,null,null,null);
            }

            int categoryColumnIndex = cursor.getColumnIndex(DbContract.CategoryEntry.KEY_CATEGORY);
            while (cursor.moveToNext()) {
                String category = cursor.getString(categoryColumnIndex);
                items.add(category);
            }
            if(selectedCategory!=null){
                mCategory = selectedCategory;
                mCategoryId = items.indexOf(selectedCategory);
                mCategoryText.setText(mCategory);
            }
        } finally {
            cursor.close();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.add_remainder_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new reminder, hide the "Delete" menu item.
        if (mCurrentTodoUri == null) {
            MenuItem menuItem = menu.findItem(R.id.discard);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:

                if (mTitleText.getText().toString().length() == 0){
                    mTitleText.setError("Todo Title cannot be blank!");
                }

                else {
                    saveTodo();
                    callParentActivity();
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.discard:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the reminder hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mVehicleHasChanged) {
                    callParentActivity();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                callParentActivity();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_todo_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the reminder.
                deleteTodo();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTodo() {
        // Only perform the delete if this is an existing reminder.
        if (mCurrentTodoUri != null) {
            // Call the ContentResolver to delete the reminder at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentreminderUri
            // content URI already identifies the reminder that we want.

            new AlarmScheduler().cancelTodoAlarm(getApplicationContext(), mCurrentTodoUri);

            int rowsDeleted = getContentResolver().delete(mCurrentTodoUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_todo_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_todo_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    // On clicking the save button
    public void saveTodo(){

     /*   if (mCurrentTodoUri == null ) {
            // Since no fields were modified, we can return early without creating a new reminder.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
*/
        // Set up calender for creating the notification
        mCalendar.set(Calendar.MONTH, --mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
        mCalendar.set(Calendar.MINUTE, mMinute);
        mCalendar.set(Calendar.SECOND, 0);

        long selectedTimestamp =  mCalendar.getTimeInMillis();
        String milliSec="9999999999999"; //Sat 20 November 2286 23:16:39
        if(!mDate.equals("")) {
            milliSec = selectedTimestamp+"";
        }
        ContentValues values = new ContentValues();
        values.put(DbContract.TaskEntry.KEY_TITLE, mTitle);
        values.put(DbContract.TaskEntry.KEY_DATE, mDate);
        values.put(DbContract.TaskEntry.KEY_TIME, mTime);
        values.put(DbContract.TaskEntry.KEY_MILLISEC, milliSec);
        values.put(DbContract.TaskEntry.KEY_STATUS, mStatus);
        values.put(DbContract.TaskEntry.KEY_CATEGORY_ID, mCategoryId);



        if (mCurrentTodoUri == null) {
            // This is a NEW reminder, so insert a new reminder into the provider,
            // returning the content URI for the new reminder.
            Uri newUri = getContentResolver().insert(DbContract.TaskEntry.TODO_CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_todo_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                mCurrentTodoUri = newUri;
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_todo_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentTodoUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_todo_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_todo_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Create a new notification
        if (mStatus.equals("false") && !mDate.equals("")) {
            if(Calendar.getInstance().getTimeInMillis() < selectedTimestamp) {
                new AlarmScheduler().setTodoAlarm(getApplicationContext(), selectedTimestamp, mCurrentTodoUri);
            }
        }else{
            new AlarmScheduler().cancelTodoAlarm(getApplicationContext(), mCurrentTodoUri);
        }

        // Create toast to confirm new reminder
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();

    }

    // On pressing the back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }




    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                DbContract.TaskEntry._ID,
                DbContract.TaskEntry.KEY_TITLE,
                DbContract.TaskEntry.KEY_DATE,
                DbContract.TaskEntry.KEY_TIME,
                DbContract.TaskEntry.KEY_STATUS,
                DbContract.TaskEntry.KEY_CATEGORY_ID,
                DbContract.CategoryEntry.KEY_CATEGORY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentTodoUri,         // Query the content URI for the current reminder
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_TITLE);
            int dateColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_DATE);
            int timeColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_TIME);
            int activeColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_STATUS);
            int categoryIdColumnIndex = cursor.getColumnIndex(DbContract.TaskEntry.KEY_CATEGORY_ID);
            int categoryColumnIndex = cursor.getColumnIndex(DbContract.CategoryEntry.KEY_CATEGORY);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            String time = cursor.getString(timeColumnIndex);
            String active = cursor.getString(activeColumnIndex);
            Integer categoryId = cursor.getInt(categoryIdColumnIndex);
            String category = cursor.getString(categoryColumnIndex);

            mTitle=title;
            mDate=date;
            mTime=time;
            mCategory=category;

            if(date.equals("")){
                time = NO_DUE_TIME;
                date = NO_DUE_DATE;
            }

            // Update the views on the screen with the values from the database
            mTitleText.setText(title);
            mDateText.setText(date);
            mTimeText.setText(time);
            mCategoryText.setText(category);
            mCategoryId=categoryId;

            mTitleText.setSelection(mTitleText.getText().length());
            mTitleText.requestFocus();

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void callParentActivity(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("CALL_FROM", R.id.nav_todo);
        intent.putExtra("IS_FROM_ADD_TODO", "true");
        intent.putExtra(CURRENT_POS, mCurrentPosition);
        startActivity(intent);
    }
}
