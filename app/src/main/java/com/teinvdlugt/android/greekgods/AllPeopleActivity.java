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

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AllPeopleActivity extends AppCompatActivity implements AllPeopleRecyclerViewAdapter.Listener {

    private AllPeopleRecyclerViewAdapter adapter;
    private String searchQuery;
    private RecyclerView recyclerView;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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
                } catch (SQLiteException e) {
                    return null;
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
        InfoActivity.openActivity(this, person.getId(), Info.INFO_TYPE_PERSON);
    }

    @Override
    public void onClickRefreshDatabase() {
        fillDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_people, menu);

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
        if (item.getItemId() == R.id.action_refresh_database) {
            fillDatabase();
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


    private void fillDatabase() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Snackbar.make(recyclerView, R.string.refreshing_database, Snackbar.LENGTH_INDEFINITE).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (checkNotConnected()) return null;
                String authorsSqlStatements = downloadFile("http://teinvdlugt.netai.net/authors.sql");
                String birthsSqlStatements = downloadFile("http://teinvdlugt.netai.net/births.sql");
                String bookMentionsBirthSqlStatements = downloadFile("http://teinvdlugt.netai.net/book_mentions_birth.sql");
                String booksSqlStatements = downloadFile("http://teinvdlugt.netai.net/books.sql");
                String peopleSqlStatements = downloadFile("http://teinvdlugt.netai.net/people.sql");
                String relationsSqlStatements = downloadFile("http://teinvdlugt.netai.net/relations.sql");

                SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                DBUtils.dropTables(db);
                DBUtils.createTables(db);

                if (authorsSqlStatements != null) {
                    String[] statements = authorsSqlStatements.replaceAll("kcv.authors", "authors").split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                if (birthsSqlStatements != null) {
                    String[] statements = birthsSqlStatements.replaceAll("kcv.births", "births").split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                if (bookMentionsBirthSqlStatements != null) {
                    String[] statements = bookMentionsBirthSqlStatements.replaceAll("kcv.book_mentions_birth", "book_mentions_birth").split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                if (booksSqlStatements != null) {
                    String[] statements = booksSqlStatements.replaceAll("kcv.books", "books").split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                if (peopleSqlStatements != null) {
                    String[] statements = peopleSqlStatements.replaceAll("kcv.people", "people").split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                if (relationsSqlStatements != null) {
                    String[] statements = relationsSqlStatements.replaceAll("kcv.relations", "relations").split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }

                db.close();
                return null;
            }

            private String downloadFile(String URL) {
                try {
                    java.net.URL url = new URL(URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    int response = conn.getResponseCode();
                    if (response >= 400) return "" + response;
                    InputStream is = conn.getInputStream();
                    return read(is);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private String read(InputStream inputStream) throws IOException {
                InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }

            private boolean checkNotConnected() {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                return networkInfo == null || !networkInfo.isConnected();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Snackbar.make(recyclerView, R.string.refresh_database_done, Snackbar.LENGTH_LONG).show();
                refresh();
            }
        }.execute();
    }
}
