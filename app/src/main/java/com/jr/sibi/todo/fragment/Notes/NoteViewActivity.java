package com.jr.sibi.todo.fragment.Notes;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
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

public class NoteViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentNoteUri;
    private TextView mTitleText,mContentText;
    private TextView mLastModTimeText;
    private Toolbar mToolbar;

    private static final String KEY_TITLE = "title_key";
    private static final String KEY_CONTENT = "content_key";

    private static final int EXISTING_VEHICLE_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();


        setTitle(getString(R.string.editor_activity_title_view_note));
        getLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, this);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleText = (TextView) findViewById(R.id.titleView);
        mContentText = (TextView) findViewById(R.id.contentView);
        mContentText.setMovementMethod(new ScrollingMovementMethod());



        // To save state on device rotation
        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitleText.setText(savedTitle);


            String savedContent = savedInstanceState.getString(KEY_CONTENT);
            mContentText.setText(savedContent);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.note_view_menu, menu);
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
            case R.id.editNote:
                Intent intent = new Intent(getApplicationContext(), AddNotesActivity.class);
                // Set the URI on the data field of the intent
                intent.setData(mCurrentNoteUri);
                startActivity(intent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.discard:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                callParentActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_note);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the reminder.
                deleteNote();
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

    private void deleteNote() {
        // Only perform the delete if this is an existing reminder.
        if (mCurrentNoteUri != null) {
            // Call the ContentResolver to delete the reminder at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentreminderUri
            // content URI already identifies the reminder that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_note_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_note_successful), Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }


    // On pressing the back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();

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


            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String content = cursor.getString(contentColumnIndex);


            // Update the views on the screen with the values from the database
            mTitleText.setText(title);
            mContentText.setText(content);
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
