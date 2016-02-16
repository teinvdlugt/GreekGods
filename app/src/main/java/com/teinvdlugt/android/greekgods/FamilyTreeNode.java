package com.teinvdlugt.android.greekgods;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Person;

public class FamilyTreeNode extends CardView {

    private Person person;
    private TextView title;
    private TextView description;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        title.setText(person.getName());
        description.setText(person.getShortDescription());
    }

    public FamilyTreeNode(Context context) {
        super(context);

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        title = new TextView(context);
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        description = new TextView(context);
        description.setTextSize(14);

        addView(ll);
    }

    public FamilyTreeNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
