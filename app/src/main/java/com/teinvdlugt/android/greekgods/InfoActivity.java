package com.teinvdlugt.android.greekgods;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity implements InfoActivityInterface {
    public static final String INFO_EXTRA = "info";
    private static final String BACK_STACK = "backStack";
    private static final String POS_IN_BACK_STACK = "posInBackStack";

    private ArrayList<Info> backStack;
    private int posInBackStack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            backStack = (ArrayList<Info>) savedInstanceState.getSerializable(BACK_STACK);
            posInBackStack = savedInstanceState.getInt(POS_IN_BACK_STACK, 0);
        }
        if (backStack == null || backStack.isEmpty()) {
            Info info = (Info) getIntent().getSerializableExtra(INFO_EXTRA);
            backStack = new ArrayList<>();
            backStack.add(info);
            posInBackStack = 0;
            showInfo();
        }
    }

    private void showInfo() {
        Info info = backStack.get(posInBackStack);
        switch (info.infoType) {
            case Info.INFO_TYPE_PERSON:
                PersonFragment fragment = new PersonFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                fragment.setPerson(this, info.id);
                break;
            case Info.INFO_TYPE_RELATION:
                RelationFragment fragment1 = new RelationFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment1)
                        .commit();
                fragment1.setRelation(this, info.id);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onClickPerson(int personId) {
        Info info = new Info(personId, Info.INFO_TYPE_PERSON);
        backStack.removeAll(backStack.subList(posInBackStack + 1, backStack.size()));
        backStack.add(info);
        posInBackStack++;
        showInfo();
    }

    @Override
    public void onClickRelation(int relationId) {
        Info info = new Info(relationId, Info.INFO_TYPE_RELATION);
        backStack.removeAll(backStack.subList(posInBackStack + 1, backStack.size()));
        backStack.add(info);
        posInBackStack++;
        showInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        if (backStack.get(posInBackStack).infoType != Info.INFO_TYPE_PERSON) {
            menu.removeItem(R.id.menu_view_in_family_tree);
        }
        if (posInBackStack == 0) {
            menu.removeItem(R.id.back_button);
        }
        if (backStack == null || posInBackStack == backStack.size() - 1) {
            menu.removeItem(R.id.forward_button);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_view_in_family_tree:
                FamilyTreeActivity.openActivity(this, backStack.get(posInBackStack).id);
                return true;
            case R.id.back_button:
                posInBackStack--;
                showInfo();
                return true;
            case R.id.forward_button:
                posInBackStack++;
                showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BACK_STACK, backStack);
        outState.putInt(POS_IN_BACK_STACK, posInBackStack);
    }

    public static void openActivity(Context context, int id, int infoType) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(InfoActivity.INFO_EXTRA, new Info(id, infoType));
        context.startActivity(intent);
    }
}
