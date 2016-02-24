/* Greek Gods: an Android application which shows the family tree of the Greek Gods.
 * Copyright (C) 2016 Tein van der Lugt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.teinvdlugt.android.greekgods;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.ArrayList;
import java.util.List;

public class AllPeopleActivity extends AppCompatActivity implements AllPeopleRecyclerViewAdapter.OnPersonClickListener {

    private AllPeopleRecyclerViewAdapter adapter;
    private String searchQuery;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllPeopleRecyclerViewAdapter(this, new ArrayList<Person>(), this);
        recyclerView.setAdapter(adapter);
        refresh();
    }

    private void refresh() {
        new AsyncTask<Void, Void, List<Person>>() {
            @Override
            protected List<Person> doInBackground(Void... params) {
                if (searchQuery == null) searchQuery = "";

                List<Person> result = new ArrayList<>();

                SQLiteDatabase db = null;
                Cursor c = null;
                try {
                    db = openOrCreateDatabase("data", 0, null);
                    String[] columns = {"personId", "name"};
                    String selection = "name LIKE '" + searchQuery + "%'";
                    c = db.query("people", columns, selection, null, null, null, "name");
                    int idColumn = c.getColumnIndex("personId");
                    int nameColumn = c.getColumnIndex("name");

                    c.moveToFirst();
                    do {
                        Person p = new Person();
                        p.setId(c.getInt(idColumn));
                        p.setName(c.getString(nameColumn));
                        result.add(p);
                    } while (c.moveToNext());
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally {
                    if (c != null) c.close();
                    if (db != null) db.close();
                }

                return result;
            }

            @Override
            protected void onPostExecute(List<Person> persons) {
                adapter.setData(persons);
            }
        }.execute();
    }

    @Override
    public void onClickPerson(Person person) {
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra(PersonActivity.PERSON_ID_EXTRA, person.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint(getString(R.string.search_in_people));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                refresh();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Hide soft keyboard
            View focus = getCurrentFocus();
            if (focus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            invalidateOptionsMenu();
            searchQuery = "";
            refresh();
        } else {
            super.onBackPressed();
        }
    }
}
