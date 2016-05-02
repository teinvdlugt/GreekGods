package com.teinvdlugt.android.greekgods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.HashMap;
import java.util.Map;

public class FamilyTreeLayout2 extends ViewGroup {
    public FamilyTreeLayout2(Context context) {
        super(context);
        init();
    }

    public FamilyTreeLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FamilyTreeLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        childRelationPaint = new Paint();
        childRelationPaint.setStrokeWidth(3);
        childRelationPaint.setColor(Color.BLACK);
        marriageRelationPaint = new Paint();
        marriageRelationPaint.setStrokeWidth(3);
        marriageRelationPaint.setColor(Color.RED);
    }

    private Person person;
    private int startX, startY;
    private Paint childRelationPaint, marriageRelationPaint;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (people == null || people.isEmpty()) return;
        removeAllViewsInLayout();
        for (Person person : people.keySet()) {
            Rect personRect = people.get(person);
            if (personRect.intersects(l + startX, t + startY, r + startX, b + startY)) {
                FamilyTreeNode view = new FamilyTreeNode(getContext());
                view.setPerson(person);
                addView(view);
                view.layout(personRect.left - startX, personRect.top - startY,
                        personRect.right - startX, personRect.bottom - startY);
            }
        }
    }

    private float prevTouchX, prevTouchY;

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (relations == null || relations.isEmpty()) return;
        for (Person[] persons : relations.keySet()) {
            Rect person1 = people.get(persons[0]);
            Rect person2 = people.get(persons[1]);
            int p1X = (person1.right - person1.left) / 2 - startX;
            int p1Y = (person1.bottom - person1.top) / 2 - startY;
            int p2X = (person2.right - person2.left) / 2 - startX;
            int p2Y = (person2.bottom - person2.top) / 2 - startY;
            canvas.drawLine(p1X, p1Y, p2X, p2Y,
                    relations.get(persons) == RELATION_TYPE_MARRIAGE ? marriageRelationPaint : childRelationPaint);
        }
    }

    public void setPerson(Person person) {
        this.person = person;
        calculateTree();
        invalidate();
        requestLayout();
    }

    private Map<Person, Rect> people;
    private Map<Person[], Integer> relations;
    private static final int RELATION_TYPE_MARRIAGE = 0;
    private static final int RELATION_TYPE_CHILD = 1;
    private int minRelationDistance = 50;

    private void calculateTree() {
        people = new HashMap<>();
        relations = new HashMap<>();
        FamilyTreeNode dummy = new FamilyTreeNode(getContext());

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
                relations.put(new Person[]{parent, person}, RELATION_TYPE_CHILD);
                for (int j = 0; j < person.getParents().size(); j++) {
                    if (i != j)
                        relations.put(new Person[]{parent, person.getParents().get(j)}, RELATION_TYPE_MARRIAGE);
                }
                left = rect.right + minRelationDistance;
            }
        }
    }

    private void measureDummy(Person person, FamilyTreeNode dummy) {
        dummy.setPerson(person);
        dummy.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    private Rect createPersonRect(Person person, int left, int top, FamilyTreeNode dummy) {
        measureDummy(person, dummy);
        return new Rect(left, top, left + dummy.getMeasuredWidth(), top + dummy.getMeasuredHeight());
    }

    public static class PersonConstructor {
        public static Person constructPerson(int personId) {
            return new Person();
        }
    }
}
