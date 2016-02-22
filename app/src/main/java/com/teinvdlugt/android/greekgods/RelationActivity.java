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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.ArrayList;
import java.util.List;

public class RelationActivity extends AppCompatActivity {
    public static final String RELATION_ID_EXTRA = "relation_id";

    private TextView peopleTV, offspringTV;
    private TextView descriptionTV, relationTypeTV;
    private int relationId;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relation);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        relationId = getIntent().getIntExtra(RELATION_ID_EXTRA, -1);
        peopleTV = (TextView) findViewById(R.id.people_textView);
        offspringTV = (TextView) findViewById(R.id.offspring_textView);
        descriptionTV = (TextView) findViewById(R.id.description_textView);
        relationTypeTV = (TextView) findViewById(R.id.relationType_textView);

        if (relationId != -1) {
            refresh();
        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    getString(R.string.something_went_wrong), Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void refresh() {
        new AsyncTask<Void, Void, Void>() {
            private List<Person> people = new ArrayList<>();
            private List<Person> offspring = new ArrayList<>();
            private String description, type;

            @SuppressLint("DefaultLocale")
            @Override
            protected Void doInBackground(Void... params) {
                int personId1 = -1, personId2 = -1;
                SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                Cursor c = null;

                try {
                    String[] columns = {"personId1", "personId2", "description", "relation_type"};
                    String selection = "relatiod_id=?";
                    String[] selectionArgs = {String.valueOf(relationId)};
                    c = db.query("relations", columns, selection, selectionArgs, null, null, null);
                    c.moveToFirst();
                    personId1 = c.getInt(c.getColumnIndex("personId1"));
                    personId2 = c.getInt(c.getColumnIndex("personId2"));
                    description = c.getString(c.getColumnIndex("description"));
                    type = c.getString(c.getColumnIndex("relation_type"));
                } catch (CursorIndexOutOfBoundsException | SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) c.close();
                }

                // Names of persons
                try {
                    String query = String.format(DBUtils.NAMES_OF_TWO_PEOPLE_QUERY, personId1, personId2);
                    c = db.rawQuery(query, null);
                    c.moveToFirst();
                    do {
                        Person p = new Person();
                        p.setName(c.getString(c.getColumnIndex("name")));
                        p.setId(c.getInt(c.getColumnIndex("personId")));
                        people.add(p);
                    } while (c.moveToNext());
                } catch (SQLiteException | CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) c.close();
                }

                // Children
                try {
                    String query = String.format(DBUtils.BIRTHS_FROM_RELATION_QUERY, relationId);
                    c = db.rawQuery(query, null);
                    c.moveToFirst();
                    int nameColumn = c.getColumnIndex("name");
                    int idColumn = c.getColumnIndex("personId");
                    do {
                        Person p = new Person();
                        p.setName(c.getString(nameColumn));
                        p.setId(c.getInt(idColumn));
                        offspring.add(p);
                    } while (c.moveToNext());
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally {
                    if (c != null) c.close();
                }

                db.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (people == null || people.isEmpty()) {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                    peopleTV.setVisibility(View.GONE);
                } else {
                    // Toolbar title
                    StringBuilder titleText = new StringBuilder();
                    for (int i = 0; i < people.size(); i++) {
                        if (i != 0) titleText.append(" & ");
                        titleText.append(people.get(i).getName());
                    }
                    setTitle(getString(R.string.relation_colon, titleText));

                    peopleTV.setText(formatClickablePersonList(people));
                    peopleTV.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (offspring == null || offspring.isEmpty()) {
                    offspringTV.setText(R.string.no_offspring);
                } else {
                    offspringTV.setText(formatClickablePersonList(offspring));
                    offspringTV.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (description == null) {
                    descriptionTV.setText(R.string.no_description_available);
                } else {
                    descriptionTV.setText(description);
                }

                if ("marriage".equals(type)) {
                    relationTypeTV.setText(R.string.relation_type_marriage);
                } else if ("affair".equals(type)) {
                    relationTypeTV.setText(R.string.relation_type_affair);
                } else if ("single".equals(type)) {
                    relationTypeTV.setText(R.string.relation_type_single);
                } else if (type == null) {
                    relationTypeTV.setText(R.string.unknown);
                } else {
                    relationTypeTV.setText(type);
                }
            }

            private SpannableStringBuilder formatClickablePersonList(List<Person> people) {
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                for (int i = 0; i < people.size(); i++) {
                    if (i != 0) ssb.append("\n");
                    ssb.append(people.get(i).getName());
                    final int personId = people.get(i).getId();
                    ClickableSpan cs = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            PersonActivity.openActivity(RelationActivity.this, personId);
                        }
                    };
                    ssb.setSpan(cs, ssb.length() - people.get(i).getName().length(),
                            ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                return ssb;
            }
        }.execute();
    }

    public static void openActivity(Context context, int relationId) {
        Intent intent = new Intent(context, RelationActivity.class);
        intent.putExtra(RELATION_ID_EXTRA, relationId);
        context.startActivity(intent);
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
