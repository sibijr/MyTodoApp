package com.jr.sibi.todo.dbhelper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract {

    private DbContract() {}

    public static final String CONTENT_AUTHORITY = "com.sibijr.db.authority";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String REMAINDER_PATH = "reminder-path";
    public static final String TODO_PATH = "todo-path";
    public static final String CATEGORY_PATH = "category-path";
    public static final String NOTE_PATH = "note-path";

    public static final class TaskEntry implements BaseColumns {

        public static final Uri TODO_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TODO_PATH);
        public static final String TODO_CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TODO_PATH;
        public static final String TODO_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TODO_PATH;

        public static final String TABLE = "tasks";
        public final static String _ID = BaseColumns._ID;
        public static final String KEY_TITLE = "title";
        public static final String KEY_DATE = "date";
        public static final String KEY_TIME = "time";
        public static final String KEY_MILLISEC = "millisec";
        public static final String KEY_STATUS = "status";
        public static final String KEY_CATEGORY_ID = "category_id";
    }

    public static final class CategoryEntry implements BaseColumns {
        public static final Uri CATEGORY_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CATEGORY_PATH);
        public static final String TABLE = "taskCategory";
        public final static String _ID = "c_id";
        public static final String KEY_CATEGORY = "category";
    }

    public static final class NoteEntry implements BaseColumns {

        public static final Uri NOTE_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, NOTE_PATH);
        public static final String NOTE_CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + NOTE_PATH;
        public static final String NOTE_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + NOTE_PATH;

        public static final String TABLE = "notes";
        public final static String _ID = BaseColumns._ID;
        public static final String KEY_TITLE = "title";
        public static final String KEY_CONTENT = "content";
        public static final String KEY_CREATED_TIME = "created_time";
        public static final String KEY_MODIFIED_TIME = "modified_time";
    }


    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString( cursor.getColumnIndex(columnName) );
    }
}
