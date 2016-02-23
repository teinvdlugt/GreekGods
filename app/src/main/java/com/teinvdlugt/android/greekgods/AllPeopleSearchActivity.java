package com.teinvdlugt.android.greekgods;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.ArrayList;
import java.util.List;

public class AllPeopleSearchActivity extends AppCompatActivity implements AllPeopleRecyclerViewAdapter.OnPersonClickListener {

    private AllPeopleRecyclerViewAdapter adapter;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all_people);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllPeopleRecyclerViewAdapter(this, new ArrayList<Person>(), this);
        recyclerView.setAdapter(adapter);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    private void search(String query) {
        new AsyncTask<String, Void, List<Person>>() {
            @Override
            protected List<Person> doInBackground(String... params) {
                List<Person> result = new ArrayList<>();
                try {
                    String query = params[0];
                    SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                    Cursor c = db.query("people", new String[]{"personId", "name", "shortDescription"},
                            "name LIKE '" + query + "%'", null, null, null, null);
                    int nameColumn = c.getColumnIndex("name");
                    int idColumn = c.getColumnIndex("personId");
                    int shortDescColumn = c.getColumnIndex("shortDescription");
                    c.moveToFirst();
                    do {
                        Person p = new Person();
                        p.setId(c.getInt(idColumn));
                        p.setName(c.getString(nameColumn));
                        p.setShortDescription(c.getString(shortDescColumn));
                        result.add(p);
                    } while (c.moveToNext());
                    c.close();
                    db.close();
                    return result;
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    return result;
                }
            }

            @Override
            protected void onPostExecute(List<Person> persons) {
                adapter.setData(persons);
            }
        }.execute(query);
    }

    @Override
    public void onClickPerson(Person person) {
        PersonActivity.openActivity(this, person.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }
}
