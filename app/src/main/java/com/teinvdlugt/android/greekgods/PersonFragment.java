package com.teinvdlugt.android.greekgods;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Book;
import com.teinvdlugt.android.greekgods.models.Parents;
import com.teinvdlugt.android.greekgods.models.Person;
import com.teinvdlugt.android.greekgods.models.Relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonFragment extends Fragment {

    private TextView parentsTextView, relationsTextView;
    private TextView descriptionTV;
    private int personId;
    private Context context;
    private InfoActivityInterface activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person, container, false);
        parentsTextView = (TextView) view.findViewById(R.id.parents_textView);
        relationsTextView = (TextView) view.findViewById(R.id.relations_textView);
        descriptionTV = (TextView) view.findViewById(R.id.description_textView);
        return view;
    }

    public void setPerson(Context context, int personId) {
        this.context = context;
        this.personId = personId;
        refresh();
    }

    private void refresh() {
        new AsyncTask<Void, Void, Void>() {
            private String name;
            private String description, shortDescription;
            //private Map<Relation, List<String>> parents;
            private List<Parents> parentses;
            private Map<Relation, List<Person>> relationsAndChildren = new HashMap<>();

            @SuppressLint("DefaultLocale")
            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase db = context.openOrCreateDatabase("data", 0, null);
                Cursor c = null;

                // Person's name and description
                try {
                    String[] nameColumns = {"name", "shortDescription", "description"};
                    String[] selectionArgs = {String.valueOf(personId)};
                    c = db.query("people", nameColumns, "personId=?", selectionArgs, null, null, null);
                    c.moveToFirst();
                    name = c.getString(c.getColumnIndex("name"));
                    description = c.getString(c.getColumnIndex("description"));
                    shortDescription = c.getString(c.getColumnIndex("shortDescription"));
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) c.close();
                }

                // Parents
                parentses = new ArrayList<>();
                Cursor c2 = null;
                try {
                    String birthsQuery = String.format(DBUtils.BIRTHS_OF_PERSON_QUERY, personId);
                    //String parentRelationsQuery = String.format(DBUtils.PARENTS_RELATIONS_QUERY, personId);
                    c = db.rawQuery(birthsQuery, null);
                    int birthIdColumn = c.getColumnIndex("birth_id");
                    int relationColumn = c.getColumnIndex("relationId");
                    c.moveToFirst();
                    do {
                        Parents parents = new Parents();

                        // Get names of parents
                        int relationId = c.getInt(relationColumn);
                        parents.relation = new Relation(relationId);
                        String relationNamesQuery = String.format(DBUtils.NAMES_OF_RELATION_QUERY, relationId);
                        c2 = db.rawQuery(relationNamesQuery, null);
                        int nameColumn = c2.getColumnIndex("name");
                        c2.moveToFirst();
                        do {
                            parents.names.add(c2.getString(nameColumn));
                        } while (c2.moveToNext());

                        // Get books that mention this birth
                        int birthId = c.getInt(birthIdColumn);
                        String booksQuery = String.format(DBUtils.BOOKS_OF_BIRTH_QUERY, birthId);
                        c2 = db.rawQuery(booksQuery, null);
                        int idColumn = c2.getColumnIndex("book_id");
                        nameColumn = c2.getColumnIndex("name");
                        c2.moveToFirst();
                        do {
                            Book book = new Book();
                            book.id = c2.getInt(idColumn);
                            book.name = c2.getString(nameColumn);
                            parents.books.add(book);
                        } while (c2.moveToNext());
                        c2.close();

                        parentses.add(parents);
                    } while (c.moveToNext());
                } catch (SQLiteException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) c.close();
                    if (c2 != null) c2.close();
                }

                // Relations
                try {
                    String relationsQuery = String.format(DBUtils.RELATIONS_OF_PERSON_QUERY, personId);
                    c = db.rawQuery(relationsQuery, null);
                    int personNameColumn = c.getColumnIndex("name");
                    int personIdColumn = c.getColumnIndex("personId");
                    int relationIdColumn = c.getColumnIndex("relatiod_id");
                    c.moveToFirst();
                    do {
                        Person partner = new Person();
                        partner.setId(c.getInt(personIdColumn));
                        partner.setName(c.getString(personNameColumn));
                        Relation relation = new Relation();
                        relation.setId(c.getInt(relationIdColumn));
                        relation.setPerson1(partner);

                        List<Person> children = new ArrayList<>();
                        try {
                            String birthsQuery = String.format(DBUtils.BIRTHS_FROM_RELATION_QUERY, relation.getId());
                            c2 = db.rawQuery(birthsQuery, null);
                            int nameColumn = c2.getColumnIndex("name");
                            int idColumn = c2.getColumnIndex("personId");
                            c2.moveToFirst();
                            do {
                                Person child = new Person();
                                child.setName(c2.getString(nameColumn));
                                child.setId(c2.getInt(idColumn));
                                children.add(child);
                            } while (c2.moveToNext());
                        } catch (SQLiteException e) {
                            e.printStackTrace();
                        } catch (CursorIndexOutOfBoundsException ignored) {
                        } finally {
                            if (c2 != null) c2.close();
                        }

                        relationsAndChildren.put(relation, children);
                    } while (c.moveToNext());
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } catch (CursorIndexOutOfBoundsException ignored) {
                } finally {
                    if (c != null) c.close();
                    if (c2 != null) c2.close();
                }

                db.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (name != null) {
                    getActivity().setTitle(name);
                }
                /*if (shortDescription != null && getSupportActionBar() != null) {
                    TODO getSupportActionBar().setSubtitle(shortDescription);
                }*/
                if (description != null) {
                    descriptionTV.setText(description);
                } else {
                    descriptionTV.setText(R.string.no_description_available);
                }
                if (parentses == null || parentses.isEmpty()) {
                    parentsTextView.setText(R.string.no_parents);
                } else {
                    setParentTexts();
                }
                if (relationsAndChildren == null || relationsAndChildren.isEmpty()) {
                    relationsTextView.setText(R.string.no_relations);
                } else {
                    setRelationsAndChildrenTexts();
                }
            }

            private void setParentTexts() {
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                for (Parents parents : parentses) {
                    final int relationId = parents.relation.getId();
                    MyClickableSpan cs = new MyClickableSpan(context) {
                        @Override
                        public void onClick(View widget) {
                            activity.onClickRelation(relationId);
                        }
                    };
                    StringBuilder relationText = new StringBuilder();
                    // Parents names
                    for (int i = 0; i < parents.names.size(); i++) {
                        if (i != 0) relationText.append(" & ");
                        relationText.append(parents.names.get(i));
                    }
                    // Book names
                    if (!parents.books.isEmpty()) {
                        relationText.append(" (");
                        for (Book book : parents.books) {
                            relationText.append(book.name).append(", ");
                        }
                        relationText.replace(relationText.length() - 2, relationText.length(), ")");
                    }
                    ssb.append(relationText);
                    ssb.setSpan(cs, ssb.length() - relationText.length(),
                            ssb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    ssb.append("\n");
                }
                ssb.delete(ssb.length() - 1, ssb.length());
                parentsTextView.setText(ssb);
                parentsTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            private void setRelationsAndChildrenTexts() {
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                for (Relation relation : relationsAndChildren.keySet()) {
                    final int relationId = relation.getId();
                    MyClickableSpan cs = new MyClickableSpan(context) {
                        @Override
                        public void onClick(View widget) {
                            activity.onClickRelation(relationId);
                        }
                    };

                    String text;
                    if (relation.getPerson1().getId() == personId) {
                        text = getString(R.string.single_relation_text, name);
                    } else {
                        text = name + " & " + relation.getPerson1().getName();
                    }

                    ssb.append(text);
                    ssb.setSpan(cs, ssb.length() - text.length(),
                            ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    ssb.append("\n");

                    List<Person> children = relationsAndChildren.get(relation);
                    for (int i = 0; i < children.size(); i++) {
                        Person child = children.get(i);
                        final int childId = child.getId();
                        MyClickableSpan cs2 = new MyClickableSpan(context) {
                            @Override
                            public void onClick(View widget) {
                                activity.onClickPerson(childId);
                            }
                        };

                        if (i == children.size() - 1)
                            ssb.append("\u2514\t");
                        else
                            ssb.append("\u251C\t");
                        ssb.setSpan(new ForegroundColorSpan(Color.BLACK), ssb.length() - 2, ssb.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ssb.append(child.getName());
                        ssb.setSpan(cs2, ssb.length() - child.getName().length(),
                                ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ssb.append("\n");
                    }
                }
                ssb.delete(ssb.length() - 1, ssb.length());
                relationsTextView.setText(ssb);
                relationsTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (InfoActivityInterface) context;
    }
}
