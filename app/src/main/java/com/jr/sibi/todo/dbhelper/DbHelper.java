package com.jr.sibi.todo.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo.app.db";

    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        String createTodoTable = "CREATE TABLE " + DbContract.TaskEntry.TABLE + " ( "
                + DbContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbContract.TaskEntry.KEY_TITLE + " TEXT NOT NULL, "
                + DbContract.TaskEntry.KEY_DATE + " TEXT NOT NULL, "
                + DbContract.TaskEntry.KEY_TIME + " TEXT NOT NULL, "
                + DbContract.TaskEntry.KEY_MILLISEC + " TEXT NOT NULL, "
                + DbContract.TaskEntry.KEY_STATUS + " TEXT NOT NULL, "
                + DbContract.TaskEntry.KEY_CATEGORY_ID + " INTEGER NOT NULL " + " );";

        String createCategoryTable = "CREATE TABLE " + DbContract.CategoryEntry.TABLE + " ( "
                + DbContract.CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +  DbContract.CategoryEntry.KEY_CATEGORY + " TEXT NOT NULL " + " );";

        String createNoteTable = "CREATE TABLE " + DbContract.NoteEntry.TABLE + " ( "
                + DbContract.NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbContract.NoteEntry.KEY_TITLE + " TEXT NOT NULL, "
                + DbContract.NoteEntry.KEY_CONTENT + " TEXT NOT NULL, "
                + DbContract.NoteEntry.KEY_CREATED_TIME + " TEXT NOT NULL, "
                + DbContract.NoteEntry.KEY_MODIFIED_TIME + " TEXT NOT NULL " + " );";

        sqLiteDatabase.execSQL(createTodoTable);
        sqLiteDatabase.execSQL(createCategoryTable);
        sqLiteDatabase.execSQL(createNoteTable);



    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DbContract.TaskEntry.TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DbContract.CategoryEntry.TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DbContract.NoteEntry.TABLE);
        onCreate(sqLiteDatabase);
    }
}
