package com.jr.sibi.todo.fragment.Notes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jr.sibi.todo.R;
import com.jr.sibi.todo.dbhelper.DbContract;
import com.jr.sibi.todo.fragment.MainActivity;
import com.jr.sibi.todo.fragment.TodoUtil;

import java.util.Calendar;

/**
 * Created by sibi-4939 on 30/04/18.
 */

public class AddNotesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentNoteUri;
    private boolean mVehicleHasChanged = false;
    private EditText mTitleText,mContentText;
    private TextView mLastModTimeText;
    private Toolbar mToolbar;
    private String mTitle;
    private String mContent;
    private String mLastModTime;

    private static final String KEY_TITLE = "title_key";
    private static final String KEY_CONTENT = "content_key";
    private static final String KEY_LASTMODTIME = "time_key";

    private static final int EXISTING_VEHICLE_LOADER = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mVehicleHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_add_details);

        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();

        if (mCurrentNoteUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_note));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a reminder that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_note));
            getLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, this);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleText = (EditText) findViewById(R.id.title);
        mContentText = (EditText) findViewById(R.id.content);
        mLastModTimeText = (TextView) findViewById(R.id.lastModTime);


        //initialize values
        mTitle = "";
        mContent="";
        mLastModTime="--";

        mTitleText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitle = s.toString().trim();
                mTitleText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

                if(mContentText.getTextSize()>0 || mTitleText.getTextSize()>0){
                    mVehicleHasChanged=true;
                }else{
                    mVehicleHasChanged=true;
                }

            }
        });

        mContentText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mContent = s.toString().trim();
                mContentText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mContentText.getTextSize()>0 || mTitleText.getTextSize()>0){
                    mVehicleHasChanged=true;
                }else{
                    mVehicleHasChanged=true;
                }

            }
        });

        mLastModTimeText.setText("--");


        // To save state on device rotation
        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitleText.setText(savedTitle);
            mTitle = savedTitle;

            String savedContent = savedInstanceState.getString(KEY_CONTENT);
            mContentText.setText(savedContent);
            mContent = savedContent;

            String savedLastMod = savedInstanceState.getString(KEY_LASTMODTIME);
            mLastModTimeText.setText(savedLastMod);
            mLastModTime = savedLastMod;
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, mTitleText.getText());
        outState.putCharSequence(KEY_CONTENT, mContentText.getText());
        outState.putCharSequence(KEY_LASTMODTIME, mLastModTimeText.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.note_edit_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:

                if (mContentText.getText().toString().length() == 0){
                    mContentText.setError("Note Title cannot be blank!");
                }
                else {
                    saveNote();
                    finish();
                }
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the reminder hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                handleHome();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleHome(){
        if (!mVehicleHasChanged) {
            finish();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that
        // changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        finish();
                    }
                };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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

    public void saveNote(){

        if (mCurrentNoteUri == null) {

            ContentValues values = new ContentValues();

            Long time= System.currentTimeMillis();


            values.put(DbContract.NoteEntry.KEY_TITLE, mTitle);
            values.put(DbContract.NoteEntry.KEY_CONTENT, mContent);
            values.put(DbContract.NoteEntry.KEY_CREATED_TIME, time.toString());
            values.put(DbContract.NoteEntry.KEY_MODIFIED_TIME, time.toString());
            // This is a NEW reminder, so insert a new reminder into the provider,
            // returning the content URI for the new reminder.
            Uri newUri = getContentResolver().insert(DbContract.NoteEntry.NOTE_CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_note_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                mCurrentNoteUri=newUri;
                Toast.makeText(this, getString(R.string.editor_insert_note_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            ContentValues values = new ContentValues();

            Long time= System.currentTimeMillis();
            values.put(DbContract.NoteEntry.KEY_TITLE, mTitle);
            values.put(DbContract.NoteEntry.KEY_CONTENT, mContent);
            values.put(DbContract.NoteEntry.KEY_MODIFIED_TIME, time.toString());

            int rowsAffected = getContentResolver().update(mCurrentNoteUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_note_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_note_successful), Toast.LENGTH_SHORT).show();
            }
        }

        // Create toast to confirm new reminder
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();

    }

    // On pressing the back button
    @Override
    public void onBackPressed() {
        handleHome();

    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    DbContract.NoteEntry._ID,
                    DbContract.NoteEntry.KEY_TITLE,
                    DbContract.NoteEntry.KEY_CONTENT,
                    DbContract.NoteEntry.KEY_MODIFIED_TIME
            };

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,   // Parent activity context
                    mCurrentNoteUri,         // Query the content URI for the current reminder
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
            int titleColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_TITLE);
            int contentColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_CONTENT);
            int lastModColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_MODIFIED_TIME);


            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String content = cursor.getString(contentColumnIndex);
            String lastModeTime = cursor.getString(lastModColumnIndex);


            // Update the views on the screen with the values from the database
            mTitleText.setText(title);
            mContentText.setText(content);


            if(title.length()>0){
                mContentText.setSelection(mContentText.getText().length());
                mContentText.requestFocus();
            }else{
                mTitleText.setSelection(mTitleText.getText().length());
            }


            mContentText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContentText.setFocusable(true);
                    mContentText.setSelection(mContentText.getText().length());
                    mContentText.requestFocus();

                }
            });


            mLastModTimeText.setText(TodoUtil.getFormatedDateFromMilliSec(Long.parseLong(lastModeTime)));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void callParentActivity(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("CALL_FROM", R.id.nav_notes);
        startActivity(intent);
    }
}
