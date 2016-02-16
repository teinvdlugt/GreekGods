package com.teinvdlugt.android.greekgods.models;


import java.util.List;

public class Person {
    private String name;
    private String description, shortDescription;
    private List<Relation> relations;

    public Person(String name, List<Relation> relations) {
        this.name = name;
        this.relations = relations;
    }

    public Person() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
}
