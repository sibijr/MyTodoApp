package com.jr.sibi.todo.fragment.Todo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.jr.sibi.todo.R;
import com.jr.sibi.todo.dbhelper.DbContract;
import com.jr.sibi.todo.dbhelper.DbHelper;

import java.util.ArrayList;

import static com.jr.sibi.todo.fragment.MainActivity.isFromAddTodo;
import static com.jr.sibi.todo.fragment.TodoConstants.ALL;
import static com.jr.sibi.todo.fragment.TodoConstants.CURRENT_CATEGORY;
import static com.jr.sibi.todo.fragment.TodoConstants.CURRENT_POS;
import static com.jr.sibi.todo.fragment.TodoConstants.FINISHED;

/**
 * Created by sibi-4939 on 09/03/18.
 */

public class TodoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private FloatingActionButton mAddTodoButton;
    private Toolbar mToolbar;
    TodoCursorAdapter mCursorAdapter;
    DbHelper alarmReminderDbHelper = new DbHelper(getContext());
    ListView todoListView;
    ProgressDialog prgDialog;
    int currentPosition=0;

    private static final int VEHICLE_LOADER = 1;
    private AdView mAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.todo_fragment, container, false);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter("ListViewDataUpdated"));
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("My Todo");
        todoListView = (ListView) getView().findViewById(R.id.list_todo);
        View emptyView = getView().findViewById(R.id.empty_view);
        todoListView.setEmptyView(emptyView);

        mCursorAdapter = new TodoCursorAdapter(getContext(), null);
        mCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            @Override
            public Cursor runQuery(CharSequence constraint) {

                String strItemCode = constraint.toString();
                String[] projection = {
                        DbContract.TaskEntry._ID,
                        DbContract.TaskEntry.KEY_TITLE,
                        DbContract.TaskEntry.KEY_DATE,
                        DbContract.TaskEntry.KEY_TIME,
                        DbContract.TaskEntry.KEY_MILLISEC,
                        DbContract.TaskEntry.KEY_STATUS,
                        DbContract.CategoryEntry.KEY_CATEGORY

                };
                String status = "false";
                String[] selectionArgs;
                String selection;
                if(strItemCode.equals(FINISHED)){
                    status="true";
                    selectionArgs = new String[]{status};
                    selection = DbContract.TaskEntry.KEY_STATUS + "=?";
                }else if (strItemCode.equals(ALL)){
                    selectionArgs = new String[]{status};
                    selection = DbContract.TaskEntry.KEY_STATUS + "=?";
                }else{
                    selectionArgs = new String[]{strItemCode,status};
                    selection = DbContract.CategoryEntry.KEY_CATEGORY + "=? and "+ DbContract.TaskEntry.KEY_STATUS + "=?";
                }
                 Cursor cursor = getActivity().getContentResolver().query(DbContract.TaskEntry.TODO_CONTENT_URI,projection,selection,selectionArgs,DbContract.TaskEntry.KEY_MILLISEC+" ASC");
                return cursor;
            }
        });

        todoListView.setAdapter(mCursorAdapter);

        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getContext(), AddTodoActivity.class);
                Uri currentVehicleUri = ContentUris.withAppendedId(DbContract.TaskEntry.TODO_CONTENT_URI, id);
                intent.setData(currentVehicleUri);
                intent.putExtra(CURRENT_POS, currentPosition);
                startActivity(intent);

            }
        });

        mAddTodoButton = (FloatingActionButton) getView().findViewById(R.id.fab_todo);

        mAddTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddTodoActivity.class);
                intent.putExtra(CURRENT_POS, currentPosition);
                intent.putExtra(CURRENT_CATEGORY, getCategoryList().get(currentPosition));
                startActivity(intent);
            }
        });
        //getLoaderManager().initLoader(VEHICLE_LOADER, null,this);

        mAdView = getView().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


}

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.love_todo_with_spinner, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        final ArrayList<String> categoryList = getCategoryList();
        ArrayAdapter<String> adapter =  new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            String Category = categoryList.get(position);
            int currentPosition1 = getActivity().getIntent().getIntExtra(CURRENT_POS,-1);
            if(currentPosition1 != -1 && isFromAddTodo){
                isFromAddTodo=false;
                getActivity().getIntent().removeExtra(CURRENT_POS);
                spinner.setSelection(currentPosition1);
            }
            mCursorAdapter.getFilter().filter(Category);
            currentPosition=position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // Your code here
        }
    });

        super.onCreateOptionsMenu(menu,inflater);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<String> categoryList = getCategoryList();
            String Category = categoryList.get(currentPosition);
            mCursorAdapter.getFilter().filter(Category);

        }
    };

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter("ListViewDataUpdated"));
        mCursorAdapter.notifyDataSetChanged();//in case our data was updated while this activity was paused
        final ArrayList<String> categoryList = getCategoryList();
        String Category = categoryList.get(currentPosition);
        mCursorAdapter.getFilter().filter(Category);
    }


    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    public ArrayList<String> getCategoryList(){
        ArrayList<String> items =new ArrayList<>();
        items.add(ALL);
        String[] projection = {
                DbContract.CategoryEntry.KEY_CATEGORY
        };
        Cursor cursor = getActivity().getContentResolver().query(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,projection,null,null,null);
        try {
            if(cursor.isAfterLast()){
                ContentValues values = new ContentValues();
                values.put(DbContract.CategoryEntry._ID,0);
                values.put(DbContract.CategoryEntry.KEY_CATEGORY,"General");
                getActivity().getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);

                values = new ContentValues();
                values.put(DbContract.CategoryEntry._ID,1);
                values.put(DbContract.CategoryEntry.KEY_CATEGORY,"Personal");
                getActivity().getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);

                values = new ContentValues();
                values.put(DbContract.CategoryEntry._ID,2);
                values.put(DbContract.CategoryEntry.KEY_CATEGORY,"Shopping");
                getActivity().getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);

                values = new ContentValues();
                values.put(DbContract.CategoryEntry._ID,3);
                values.put(DbContract.CategoryEntry.KEY_CATEGORY,"Wishlist");
                getActivity().getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);

                values = new ContentValues();
                values.put(DbContract.CategoryEntry._ID,4);
                values.put(DbContract.CategoryEntry.KEY_CATEGORY,"Work");
                getActivity().getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);

                getActivity().getContentResolver().insert(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,values);
                cursor = getActivity().getContentResolver().query(DbContract.CategoryEntry.CATEGORY_CONTENT_URI,projection,null,null,null);
            }

            int categoryColumnIndex = cursor.getColumnIndex(DbContract.CategoryEntry.KEY_CATEGORY);
            while (cursor.moveToNext()) {
                String category = cursor.getString(categoryColumnIndex);
                items.add(category);
            }
            items.add(FINISHED);
        } finally {
            cursor.close();
        }
        return items;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                DbContract.TaskEntry._ID,
                DbContract.TaskEntry.KEY_TITLE,
                DbContract.TaskEntry.KEY_DATE,
                DbContract.TaskEntry.KEY_TIME,
                DbContract.TaskEntry.KEY_MILLISEC,
                DbContract.TaskEntry.KEY_STATUS,
                DbContract.CategoryEntry.KEY_CATEGORY

        };

        String[] selectionArgs = new String[]{"false"};
        String selection = DbContract.TaskEntry.KEY_STATUS + "=?";

        return new CursorLoader(getContext(),   // Parent activity context
                DbContract.TaskEntry.TODO_CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                selection,                   // No selection clause
                selectionArgs,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //mCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       // mCursorAdapter.swapCursor(null);

    }


}


