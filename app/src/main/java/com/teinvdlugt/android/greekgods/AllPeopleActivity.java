package com.teinvdlugt.android.greekgods;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.ArrayList;
import java.util.List;

public class AllPeopleActivity extends AppCompatActivity implements AllPeopleRecyclerViewAdapter.OnPersonClickListener {

    private RecyclerView recyclerView;
    private AllPeopleRecyclerViewAdapter adapter;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                String[] columns = {"personId", "name"};
                Cursor c = db.query("people", columns, null, null, null, null, "name");
                int idColumn = c.getColumnIndex("personId");
                int nameColumn = c.getColumnIndex("name");

                List<Person> result = new ArrayList<>();

                c.moveToFirst();
                do {
                    Person p = new Person();
                    p.setId(c.getInt(idColumn));
                    p.setName(c.getString(nameColumn));
                    result.add(p);
                } while (c.moveToNext());
                c.close();
                db.close();

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
        Snackbar.make(recyclerView, "Well done, you clicked " + person.getName() + "'s name!", Snackbar.LENGTH_LONG).show();
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
