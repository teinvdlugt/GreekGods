package com.teinvdlugt.android.greekgods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.teinvdlugt.android.greekgods.models.Person;

public class FamilyTreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

        FamilyTreeLayout treeLayout = (FamilyTreeLayout) findViewById(R.id.family_tree_layout);
        Person person = new Person();
        person.setName("Zeus");
        person.setShortDescription("Oppergod van Olympus");
        Person parent1 = new Person();
        parent1.setName("Rhea");
        parent1.setShortDescription("Titaan");
        Person parent2 = new Person();
        parent2.setName("Kronos");
        parent2.setShortDescription("Titaan");
        person.setParent1(parent1);
        person.setParent2(parent2);
        treeLayout.setPerson(person);
    }
}
