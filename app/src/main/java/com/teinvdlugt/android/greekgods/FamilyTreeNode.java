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
