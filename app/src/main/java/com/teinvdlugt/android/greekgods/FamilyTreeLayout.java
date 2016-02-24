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
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.teinvdlugt.android.greekgods.models.Person;

public class FamilyTreeLayout extends LinearLayout {

    private Person person;
    private FamilyTreeNode personView, parent1View, parent2View;

    public void setPerson(Person person) {
        this.person = person;
        removeAllViews();
        personView = new FamilyTreeNode(getContext());
        parent1View = new FamilyTreeNode(getContext());
        parent2View = new FamilyTreeNode(getContext());
        personView.setPerson(person);
        parent1View.setPerson(person.getParent1());
        parent2View.setPerson(person.getParent2());

        LayoutParams wrapParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams marginParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        marginParams.bottomMargin = marginParams.topMargin = marginParams.leftMargin = marginParams.rightMargin = 16;

        LinearLayout parentsLayout = new LinearLayout(getContext());
        parentsLayout.addView(parent1View, marginParams);
        parentsLayout.addView(parent2View, marginParams);
        addView(parentsLayout, wrapParams);
        addView(personView, marginParams);
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public FamilyTreeLayout(Context context) {
        super(context);
        init();
    }

    public FamilyTreeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
}
