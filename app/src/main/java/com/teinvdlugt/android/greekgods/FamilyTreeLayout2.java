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
    private double scale = 1;
    private Paint childRelationPaint, marriageRelationPaint;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (people == null || people.isEmpty()) return;
        removeAllViewsInLayout();
        for (Person person : people.keySet()) {
            Rect personRect = people.get(person);
            if (personRect.intersects((int) (l / scale + startX), (int) (t / scale + startY),
                    (int) (r / scale + startX), (int) (b / scale + startY))) {
                FamilyTreeNode view = new FamilyTreeNode(getContext());
                view.setPerson(person);
                addViewInLayout(view, -1, generateDefaultLayoutParams());
                view.layout((int) ((personRect.left - startX) * scale), (int) ((personRect.top - startY) * scale),
                        (int) ((personRect.right - startX) / scale), (int) ((personRect.bottom - startY) / scale));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Don't allow wrap_content, for the same reason as ScrollView
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    private float prevTouchX1 = -1, prevTouchY1 = -1;
    private float prevTouchX2 = -1, prevTouchY2 = -1;
    private int pointerId1 = -1, pointerId2 = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                prevTouchX1 = event.getX();
                prevTouchY1 = event.getY();
                pointerId1 = event.getPointerId(0);
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerId2 == -1) {
                    int index = event.getActionIndex();
                    pointerId2 = event.getPointerId(index);
                    prevTouchX2 = event.getX(index);
                    prevTouchY2 = event.getY(index);
                }
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    startX += (prevTouchX1 - event.getX()) / scale;
                    startY += (prevTouchY1 - event.getY()) / scale;
                    prevTouchX1 = event.getX();
                    prevTouchY1 = event.getY();
                } else {
                    int indexCurrent = event.getActionIndex();
                    int index1 = event.findPointerIndex(pointerId1);
                    int index2 = event.findPointerIndex(pointerId2);
                    if (indexCurrent != index1 && indexCurrent != index2
                            || index1 == -1 || index2 == -1
                            || prevTouchX1 == -1 || prevTouchY1 == -1
                            || prevTouchX2 == -1 || prevTouchY2 == -1) break;

                    double prevXDist = prevTouchX2 - prevTouchX1;
                    double prevYDist = prevTouchY2 - prevTouchY1;
                    double prevDistSqr = prevXDist * prevXDist + prevYDist * prevYDist;

                    double prevCenterPointX = (prevTouchX1 + prevTouchX2) / 2d;
                    double prevCenterPointY = (prevTouchY1 + prevTouchY2) / 2d;
                    double prevCenterPointXScaled = prevCenterPointX / scale + startX;
                    double prevCenterPointYScaled = prevCenterPointY / scale + startY;

                    prevTouchX1 = event.getX(index1);
                    prevTouchY1 = event.getY(index1);
                    prevTouchX2 = event.getX(index2);
                    prevTouchY2 = event.getY(index2);

                    double xDist = prevTouchX2 - prevTouchX1;
                    double yDist = prevTouchY2 - prevTouchY1;
                    double distSqr = xDist * xDist + yDist * yDist;
                    scale *= Math.sqrt(distSqr / prevDistSqr);

                    double centerPointX = (prevTouchX1 + prevTouchX2) / 2d;
                    double centerPointY = (prevTouchY1 + prevTouchY2) / 2d;
                    startX = (int) (prevCenterPointXScaled - centerPointX / scale);
                    startY = (int) (prevCenterPointYScaled - centerPointY / scale);
                }

                invalidate();
                requestLayout();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                int pointerIndex = event.getActionIndex();
                if (pointerIndex == event.findPointerIndex(pointerId1)) {
                    // Transfer the data of pointer 2 to pointer 1,
                    pointerId1 = pointerId2;
                    prevTouchX1 = prevTouchX2;
                    prevTouchY1 = prevTouchY2;
                }
                if (pointerIndex == event.findPointerIndex(pointerId1) ||
                        pointerIndex == event.findPointerIndex(pointerId2)) {
                    // Get rid of pointer 2
                    pointerId2 = -1;
                    prevTouchX2 = prevTouchY2 = -1;
                }
                return true; // TODO don't return?
            case MotionEvent.ACTION_UP:
                prevTouchX1 = prevTouchY1 = prevTouchX2 = prevTouchY2
                        = pointerId1 = pointerId2 = -1;
                return true; // TODO don't return?
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO use {@code scale}
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
