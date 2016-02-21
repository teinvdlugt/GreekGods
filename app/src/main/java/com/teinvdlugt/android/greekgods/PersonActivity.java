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

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonActivity extends AppCompatActivity {
    public static final String PERSON_ID_EXTRA = "person_id";

    private TextView parentsTextView, relationsTextView;
    private int personId;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        personId = getIntent().getIntExtra(PERSON_ID_EXTRA, -1);
        parentsTextView = (TextView) findViewById(R.id.parents_textView);
        relationsTextView = (TextView) findViewById(R.id.relations_textView);

        if (personId != -1) {
            refresh();
        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    getString(R.string.something_went_wrong), Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void refresh() {
        new AsyncTask<Void, Void, Void>() {
            private String name;
            private List<String> parents;
            private Map<String, List<String>> relationsAndChildren = new HashMap<>();

            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                Cursor c = null;

                // Person's name
                try {
                    String[] nameColumns = {"name"};
                    String[] selectionArgs = {String.valueOf(personId)};
                    c = db.query("people", nameColumns, "personId=?", selectionArgs, null, null, null);
                    c.moveToFirst();
                    name = c.getString(c.getColumnIndex("name"));
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) c.close();
                }

                // Parents
                try {
                    String parentsQuery = String.format(DBUtils.PARENTS_QUERY, personId);
                    c = db.rawQuery(parentsQuery, null);
                    int nameColumn = c.getColumnIndex("name");
                    c.moveToFirst();
                    parents = new ArrayList<>();
                    do {
                        parents.add(c.getString(nameColumn));
                    } while (c.moveToNext());
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally {
                    if (c != null) c.close();
                }

                // Relations
                Map<String, Integer> relations = new HashMap<>();
                try {
                    String relationsQuery = String.format(DBUtils.RELATIONS_QUERY, personId);
                    c = db.rawQuery(relationsQuery, null);
                    int nameColumn = c.getColumnIndex("name");
                    int relationIdColumn = c.getColumnIndex("relatiod_id");
                    c.moveToFirst();
                    do {
                        relations.put(c.getString(nameColumn), c.getInt(relationIdColumn));
                    } while (c.moveToNext());
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally {
                    if (c != null) c.close();
                }

                // Children
                for (String partnerName : relations.keySet()) {
                    List<String> children = new ArrayList<>();
                    try {
                        String birthsQuery = String.format(DBUtils.BIRTHS_QUERY, relations.get(partnerName));
                        c = db.rawQuery(birthsQuery, null);
                        int nameColumn = c.getColumnIndex("name");
                        c.moveToFirst();
                        do {
                            children.add(c.getString(nameColumn));
                        } while (c.moveToNext());
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    } catch (CursorIndexOutOfBoundsException ignored) {
                    } finally {
                        if (c != null) c.close();
                    }

                    relationsAndChildren.put(partnerName, children);
                }

                db.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (name != null) {
                    setTitle(name);
                }
                if (parents == null || parents.isEmpty()) {
                    parentsTextView.setText(R.string.no_parents);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String parent : parents) {
                        sb.append(parent).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    parentsTextView.setText(sb);
                }
                if (relationsAndChildren == null || relationsAndChildren.isEmpty()) {
                    relationsTextView.setText(R.string.no_relations);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String partnerName : relationsAndChildren.keySet()) {
                        sb.append(partnerName).append("\n");
                        for (String childName : relationsAndChildren.get(partnerName)) {
                            sb.append("\t\t").append(childName).append("\n");
                        }
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    relationsTextView.setText(sb);
                }
            }
        }.execute();
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
