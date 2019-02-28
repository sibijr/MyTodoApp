package com.jr.sibi.todo.fragment.Notes;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.jr.sibi.todo.R;
import com.jr.sibi.todo.dbhelper.DbContract;
import com.jr.sibi.todo.fragment.TodoUtil;

/**
 * Created by sibi-4939 on 30/04/18.
 */

public class NotesCursorAdapter extends CursorAdapter {

    private TextView mTitleText, mContentText, mDateText;

    public NotesCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_notes, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mTitleText = (TextView) view.findViewById(R.id.recycle_title);
        mContentText = (TextView) view.findViewById(R.id.recycle_content);
        mDateText = (TextView) view.findViewById(R.id.recycle_modified_date_time);

        int titleColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_TITLE);
        int contentColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_CONTENT);
        int createdColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_CREATED_TIME);
        int modifiedColumnIndex = cursor.getColumnIndex(DbContract.NoteEntry.KEY_MODIFIED_TIME);



        String title = cursor.getString(titleColumnIndex);
        String content = cursor.getString(contentColumnIndex);
        String created = cursor.getString(createdColumnIndex);
        String modified = cursor.getString(modifiedColumnIndex);

        String date = "Modified : "+ TodoUtil.getFormatedDateFromMilliSec(Long.parseLong(modified)) + " / Created : " + TodoUtil.getFormatedDateFromMilliSec(Long.parseLong(created));

        mTitleText.setText(title);
        mContentText.setText(content);
        mDateText.setText(date);

    }
}
