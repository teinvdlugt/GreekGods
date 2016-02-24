package com.teinvdlugt.android.greekgods;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.ArrayList;
import java.util.List;

public class FamilyTreeActivity extends AppCompatActivity {

    FamilyTreeLayout treeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

        treeLayout = (FamilyTreeLayout) findViewById(R.id.family_tree_layout);
        loadPerson(64);
    }

    @SuppressLint("DefaultLocale")
    private void loadPerson(final int personId) {
        new AsyncTask<Void, Void, Person>() {
            @Override
            protected Person doInBackground(Void... params) {
                Person person = getBasicPersonData(personId);
                List<Integer> parentIds = getParentIds(personId);
                List<Person> parents = new ArrayList<>();
                for (int parentId : parentIds) {
                    parents.add(getBasicPersonData(parentId));
                }
                person.setParents(parents);
                return person;
            }

            @Deprecated
            private List<Integer> getParentIds(int personId) {
                List<Integer> result = new ArrayList<>();
                Cursor c = null;

                try {
                    SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                    String query = String.format(DBUtils.PARENTS_QUERY, personId);
                    c = db.rawQuery(query, null);
                    c.moveToFirst();
                    do {
                        result.add(c.getInt(c.getColumnIndex("personId")));
                    } while (c.moveToNext());
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally { if (c != null) c.close(); }

                return result;
            }

            private Person getBasicPersonData(int id) {
                Person person = new Person(id);
                person.setId(id);
                Cursor c = null;
                try {
                    SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                    String[] columns = {"name", "shortDescription"};
                    String[] selectionArgs = {String.valueOf(id)};
                    c = db.query("people", columns, "personId=?", selectionArgs, null, null, null);
                    c.moveToFirst();
                    person.setName(c.getString(c.getColumnIndex("name")));
                    person.setShortDescription(c.getString(c.getColumnIndex("shortDescription")));
                } finally {
                    if (c != null) c.close();
                }

                return person;
            }

            @Override
            protected void onPostExecute(Person person) {
                treeLayout.setPerson(person);
            }
        }.execute();
    }
}
