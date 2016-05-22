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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.HashMap;
import java.util.Map;

public class FamilyTreeLayout2 extends ViewGroup {
    public FamilyTreeLayout2(Context context) {
        super(context);
    }

    public FamilyTreeLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FamilyTreeLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface OnPersonClickListener {
        void onClickPerson(Person person);
    }

    private Person person;
    private int startX, startY;
    private float prevTouchX, prevTouchY;
    private OnPersonClickListener onPersonClickListener;

    public void setOnPersonClickListener(OnPersonClickListener onPersonClickListener) {
        this.onPersonClickListener = onPersonClickListener;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        calculateTree();
        invalidate();
        requestLayout();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (people == null || people.isEmpty()) return;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        removeAllViewsInLayout();

        for (final Person person : people.keySet()) {
            Rect personRect = people.get(person);

            if (personRect.intersects(l + startX, t + startY, r + startX, b + startY)) {
                View familyTreeNode = inflater.inflate(R.layout.family_tree_node, null, false);
                ((TextView) familyTreeNode.findViewById(R.id.name_textView)).setText(person.getName());
                ((TextView) familyTreeNode.findViewById(R.id.shortDescription_textView)).setText(person.getShortDescription());
                familyTreeNode.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onPersonClickListener != null)
                            onPersonClickListener.onClickPerson(person);
                    }
                });

                addView(familyTreeNode);

                familyTreeNode.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                familyTreeNode.layout(personRect.left - startX, personRect.top - startY,
                        personRect.right - startX, personRect.bottom - startY);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevTouchX = event.getX();
                prevTouchY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                startX += prevTouchX - event.getX();
                startY += prevTouchY - event.getY();
                prevTouchX = event.getX();
                prevTouchY = event.getY();
                invalidate();
                requestLayout();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private Map<Person, Rect> people;
    private static final int minRelationDistance = 50;

    private void calculateTree() {
        people = new HashMap<>();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dummy = inflater.inflate(R.layout.family_tree_node, null, false);

        Rect personRect = createPersonRect(person, 0, 0, dummy);
        people.put(person, personRect);
        if (person.getParents() != null && !person.getParents().isEmpty()) {
            int totalWidth = 0;
            for (Person parent : person.getParents()) {
                measureDummy(parent, dummy);
                totalWidth += dummy.getMeasuredWidth();
            }
            totalWidth += (person.getParents().size() - 1) * minRelationDistance;
            int top = -minRelationDistance - dummy.getMeasuredHeight();
            int left = -totalWidth / 2 + personRect.width() / 2;
            for (int i = 0; i < person.getParents().size(); i++) {
                Person parent = person.getParents().get(i);
                Rect rect = createPersonRect(parent, left, top, dummy);
                people.put(parent, rect);
                left = rect.right + minRelationDistance;
            }
        }

        if (person.getChildren() != null && !person.getChildren().isEmpty()) {
            int totalWidth = 0;
            for (Person child : person.getChildren()) {
                measureDummy(child, dummy);
                totalWidth += dummy.getMeasuredWidth();
            }
            totalWidth += (person.getChildren().size() - 1) * minRelationDistance;
            int top = personRect.bottom + minRelationDistance;
            int left = -totalWidth / 2 + personRect.width() / 2;
            for (int i = 0; i < person.getChildren().size(); i++) {
                Person child = person.getChildren().get(i);
                Rect rect = createPersonRect(child, left, top, dummy);
                people.put(child, rect);
                left = rect.right + minRelationDistance;
            }
        }
    }

    private void measureDummy(Person person, View dummy) {
        ((TextView) dummy.findViewById(R.id.name_textView)).setText(person.getName());

        TextView shortDescriptionTV = (TextView) dummy.findViewById(R.id.shortDescription_textView);
        if (person.getShortDescription() == null || person.getShortDescription().isEmpty())
            shortDescriptionTV.setVisibility(View.GONE);
        else {
            shortDescriptionTV.setVisibility(View.VISIBLE);
            shortDescriptionTV.setText(person.getShortDescription());
        }

        dummy.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    private Rect createPersonRect(Person person, int left, int top, View dummy) {
        measureDummy(person, dummy);
        return new Rect(left, top, left + dummy.getMeasuredWidth(), top + dummy.getMeasuredHeight());
    }
}
