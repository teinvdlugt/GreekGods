package com.teinvdlugt.android.greekgods;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.ArrayList;
import java.util.List;

public class RelationFragment extends Fragment {

    private TextView peopleTV, offspringTV;
    private TextView descriptionTV, relationTypeTV;
    private int relationId;
    private Context context;
    private InfoActivityInterface activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relation, container, false);
        peopleTV = (TextView) view.findViewById(R.id.people_textView);
        offspringTV = (TextView) view.findViewById(R.id.offspring_textView);
        descriptionTV = (TextView) view.findViewById(R.id.description_textView);
        relationTypeTV = (TextView) view.findViewById(R.id.relationType_textView);
        return view;
    }

    public void setRelation(Context context, int relationId) {
        this.context = context;
        this.relationId = relationId;
        refresh();
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
                SQLiteDatabase db = context.openOrCreateDatabase("data", 0, null);
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
                    peopleTV.setVisibility(View.GONE);
                } else {
                    // Toolbar title
                    StringBuilder titleText = new StringBuilder();
                    for (int i = 0; i < people.size(); i++) {
                        if (i != 0) titleText.append(" & ");
                        titleText.append(people.get(i).getName());
                    }
                    getActivity().setTitle(getString(R.string.relation_colon, titleText));

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
                    MyClickableSpan cs = new MyClickableSpan(context) {
                        @Override
                        public void onClick(View widget) {
                            activity.onClickPerson(personId);
                        }
                    };
                    ssb.setSpan(cs, ssb.length() - people.get(i).getName().length(),
                            ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                return ssb;
            }
        }.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (InfoActivityInterface) context;
    }
}
