package com.jr.sibi.todo.dbhelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;


public class DbProvider extends ContentProvider {

    public static final String LOG_TAG = DbProvider.class.getSimpleName();
    private static final int REMINDER = 100;
    private static final int REMINDER_ID = 101;
    private static final int TODO = 102;
    private static final int TODO_ID = 103;
    private static final int CATEGORY = 104;
    private static final int CATEGORY_ID = 105;
    private static final int NOTE = 106;
    private static final int NOTE_ID = 107;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.REMAINDER_PATH, REMINDER);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.REMAINDER_PATH + "/#", REMINDER_ID);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.TODO_PATH, TODO);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.TODO_PATH + "/#", TODO_ID);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.CATEGORY_PATH, CATEGORY);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.CATEGORY_PATH + "/#", CATEGORY_ID);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.NOTE_PATH, NOTE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.NOTE_PATH + "/#", NOTE_ID);
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case TODO:
                cursor = database.query(DbContract.TaskEntry.TABLE+ " LEFT OUTER JOIN "+ DbContract.CategoryEntry.TABLE+" ON "+ DbContract.CategoryEntry.TABLE +"."+DbContract.CategoryEntry._ID+"="+DbContract.TaskEntry.TABLE+"."+DbContract.TaskEntry.KEY_CATEGORY_ID , projection, selection, selectionArgs,null, null, sortOrder);
                break;
            case TODO_ID:
                selection = DbContract.TaskEntry.TABLE+"."+DbContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(DbContract.TaskEntry.TABLE+ " LEFT OUTER JOIN "+ DbContract.CategoryEntry.TABLE+" ON "+ DbContract.CategoryEntry.TABLE +"."+DbContract.CategoryEntry._ID+"="+DbContract.TaskEntry.TABLE+"."+DbContract.TaskEntry.KEY_CATEGORY_ID, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            case CATEGORY:
                cursor = database.query(DbContract.CategoryEntry.TABLE, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            case NOTE:
                cursor = database.query(DbContract.NoteEntry.TABLE, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            case NOTE_ID:
                selection = DbContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(DbContract.NoteEntry.TABLE, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TODO:
                return DbContract.TaskEntry.TODO_CONTENT_LIST_TYPE;
            case TODO_ID:
                return DbContract.TaskEntry.TODO_CONTENT_ITEM_TYPE;
            case NOTE:
                return DbContract.NoteEntry.NOTE_CONTENT_LIST_TYPE;
            case NOTE_ID:
                return DbContract.NoteEntry.NOTE_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TODO:
                return insertTodo(uri, contentValues);
            case CATEGORY:
                return insertCategory(uri, contentValues);
            case NOTE:
                return insertNote(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    private Uri insertTodo(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(DbContract.TaskEntry.TABLE, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertCategory(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(DbContract.CategoryEntry.TABLE, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertNote(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(DbContract.NoteEntry.TABLE, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TODO:
                rowsDeleted = database.delete(DbContract.TaskEntry.TABLE, selection, selectionArgs);
                break;
            case TODO_ID:
                selection = DbContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(DbContract.TaskEntry.TABLE, selection, selectionArgs);
                break;
            case NOTE:
                rowsDeleted = database.delete(DbContract.NoteEntry.TABLE, selection, selectionArgs);
                break;
            case NOTE_ID:
                selection = DbContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(DbContract.NoteEntry.TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TODO:
                return updateTodo(uri, contentValues, selection, selectionArgs);
            case TODO_ID:
                selection = DbContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateTodo(uri, contentValues, selection, selectionArgs);
            case NOTE:
                return updateNote(uri, contentValues, selection, selectionArgs);
            case NOTE_ID:
                selection = DbContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateNote(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateTodo(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(DbContract.TaskEntry.TABLE, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(DbContract.NoteEntry.TABLE, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
