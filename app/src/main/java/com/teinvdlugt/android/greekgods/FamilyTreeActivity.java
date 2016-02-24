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

public class FamilyTreeActivity extends AppCompatActivity implements FamilyTreeLayout.OnPersonClickListener {

    private FamilyTreeLayout treeLayout;
    private List<Integer> backStack = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

        treeLayout = (FamilyTreeLayout) findViewById(R.id.family_tree_layout);
        treeLayout.setOnPersonClickListener(this);
        loadPerson(64);
    }

    @Override
    public void onClickPerson(Person person) {
        if (person.getId() == treeLayout.getPerson().getId()) {
            return;
        }

        backStack.add(treeLayout.getPerson().getId());
        loadPerson(person.getId());
    }

    @Override
    public void onBackPressed() {
        if (backStack != null && backStack.size() > 0) {
            int id = backStack.get(backStack.size() - 1);
            backStack.remove(backStack.size() - 1);
            loadPerson(id);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("DefaultLocale")
    private void loadPerson(final int personId) {
        new AsyncTask<Void, Void, Person>() {
            @Override
            protected Person doInBackground(Void... params) {
                // TODO Optimise everything
                Person person = getBasicPersonData(personId);

                // Parents
                List<Integer> parentIds = getParentIds(personId);
                List<Person> parents = new ArrayList<>();
                for (int parentId : parentIds)
                    parents.add(getBasicPersonData(parentId));
                person.setParents(parents);

                // Children
                List<Integer> relationIds = getRelationIds(personId);
                List<Integer> childrenIds = new ArrayList<>();
                for (int relationId : relationIds)
                    childrenIds.addAll(getChildrenIds(relationId));
                List<Person> children = new ArrayList<>();
                for (int childId : childrenIds)
                    children.add(getBasicPersonData(childId));
                person.setChildren(children);

                return person;
            }

            private List<Integer> getRelationIds(int personId) {
                List<Integer> result = new ArrayList<>();
                Cursor c = null;

                try {
                    SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                    String query = String.format(DBUtils.RELATIONS_OF_PERSON_QUERY, personId);
                    c = db.rawQuery(query, null);
                    c.moveToFirst();
                    int relationIdIndex = c.getColumnIndex("relatiod_id");
                    do {
                        result.add(c.getInt(relationIdIndex));
                    } while (c.moveToNext());
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally { if (c != null) c.close(); }

                return result;
            }

            private List<Integer> getChildrenIds(int relationId) {
                List<Integer> result = new ArrayList<>();
                Cursor c = null;

                try {
                    SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                    String query = String.format(DBUtils.BIRTHS_FROM_RELATION_QUERY, relationId);
                    c = db.rawQuery(query, null);
                    c.moveToFirst();
                    int personIdIndex = c.getColumnIndex("personId");
                    do {
                        result.add(c.getInt(personIdIndex));
                    } while (c.moveToNext());
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally { if (c != null) c.close(); }

                return result;
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
                    boolean bool = c.moveToFirst();
                    int nameIndex = c.getColumnIndex("name");
                    int shortDescIndex = c.getColumnIndex("shortDescription");
                    person.setName(c.getString(nameIndex));
                    person.setShortDescription(c.getString(shortDescIndex));
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
