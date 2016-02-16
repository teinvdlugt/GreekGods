package com.teinvdlugt.android.greekgods.models;

public class Relation {
    private String description;
    private Person person1, person2;

    public enum RelationType {CHILD, PARENT, HUSBAND, AFFAIR}
}