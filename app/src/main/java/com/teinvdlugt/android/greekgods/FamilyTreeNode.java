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
import android.util.TypedValue;
import android.view.Gravity;
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

    private void init(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int _16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        content.setPadding(_16dp, _16dp, _16dp, _16dp);
        title = new TextView(context);
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        description = new TextView(context);
        description.setTextSize(14);
        description.setGravity(Gravity.CENTER_HORIZONTAL);

        content.addView(title);
        content.addView(description);
        addView(content);
    }

    public FamilyTreeNode(Context context) {
        super(context);
        init(context);
    }

    public FamilyTreeNode(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FamilyTreeNode(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
}
