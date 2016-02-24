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

        FamilyTreeNode treeNode = (FamilyTreeNode) findViewById(R.id.family_tree_node);
        Person p = new Person();
        p.setName("Chaos");
        p.setId(1);
        p.setShortDescription("Het Niets");
        treeNode.setPerson(p);
    }
}
