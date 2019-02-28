package com.jr.sibi.todo.fragment.Notes;

import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jr.sibi.todo.R;
import com.jr.sibi.todo.dbhelper.DbContract;
import com.jr.sibi.todo.dbhelper.DbHelper;
import com.jr.sibi.todo.fragment.TodoConstants;

import java.util.ArrayList;

import static com.jr.sibi.todo.fragment.MainActivity.isFromAddTodo;
import static com.jr.sibi.todo.fragment.TodoConstants.CURRENT_POS;
import static com.jr.sibi.todo.fragment.TodoConstants.SORT_BY_CREATED;
import static com.jr.sibi.todo.fragment.TodoConstants.SORT_BY_MODIFIED;

/**
 * Created by sibi-4939 on 30/04/18.
 */

public class NotesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private FloatingActionButton mAddNoteButton;
    private Toolbar mToolbar;
    NotesCursorAdapter mCursorAdapter;
    DbHelper notexDbHelper = new DbHelper(getContext());
    ListView notesListView;
    private static final int VEHICLE_LOADER = 0;
    int currentPosition=0;

    private AdView mAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.notes_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("My Notes");

        notesListView = (ListView) getView().findViewById(R.id.list_notes);
        View emptyView = getView().findViewById(R.id.empty_notes_view);
        notesListView.setEmptyView(emptyView);

        mCursorAdapter = new NotesCursorAdapter(getContext(), null);
        mCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            @Override
            public Cursor runQuery(CharSequence constraint) {

                String strItemCode = constraint.toString();
                String[] projection = {
                        DbContract.NoteEntry._ID,
                        DbContract.NoteEntry.KEY_TITLE,
                        DbContract.NoteEntry.KEY_CONTENT,
                        DbContract.NoteEntry.KEY_CREATED_TIME,
                        DbContract.NoteEntry.KEY_MODIFIED_TIME

                };
                String sort = DbContract.NoteEntry.KEY_MODIFIED_TIME;
                if(strItemCode.equals(SORT_BY_CREATED)){
                    sort=DbContract.NoteEntry.KEY_CREATED_TIME;
                }
                Cursor cursor = getActivity().getContentResolver().query(DbContract.NoteEntry.NOTE_CONTENT_URI,projection,null,null,sort+" DESC");
                return cursor;
            }
        });
        notesListView.setAdapter(mCursorAdapter);

        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getContext(), NoteViewActivity.class);
                Uri currentVehicleUri = ContentUris.withAppendedId(DbContract.NoteEntry.NOTE_CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentVehicleUri);
                startActivity(intent);

            }
        });


        mAddNoteButton = (FloatingActionButton) getView().findViewById(R.id.fab_notes);

        mAddNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddNotesActivity.class);
                startActivity(intent);
            }
        });

        mAdView = getView().findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

       //getLoaderManager().initLoader(VEHICLE_LOADER, null,this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.love_todo_with_spinner, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        final ArrayList<String> sortList = getSortList();
        ArrayAdapter<String> adapter =  new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,sortList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String sort = sortList.get(position);
                currentPosition=position;
                mCursorAdapter.getFilter().filter(sort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Your code here
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onResume(){
        super.onResume();
        mCursorAdapter.notifyDataSetChanged();//in case our data was updated while this activity was paused
        final ArrayList<String> categoryList = getSortList();
        String Category = categoryList.get(currentPosition);
        mCursorAdapter.getFilter().filter(Category);
    }

    public ArrayList<String> getSortList() {
        ArrayList<String> items = new ArrayList<>();
        items.add(SORT_BY_CREATED);
        items.add(SORT_BY_MODIFIED);
        return  items;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                DbContract.NoteEntry._ID,
                DbContract.NoteEntry.KEY_TITLE,
                DbContract.NoteEntry.KEY_CONTENT,
                DbContract.NoteEntry.KEY_CREATED_TIME,
                DbContract.NoteEntry.KEY_MODIFIED_TIME
        };

        return new CursorLoader(getContext(),   // Parent activity context
                DbContract.NoteEntry.NOTE_CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       // mCursorAdapter.swapCursor(null);
    }
}
