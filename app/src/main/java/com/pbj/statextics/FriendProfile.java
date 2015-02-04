package com.pbj.statextics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;


public class FriendProfile extends ActionBarActivity implements OnItemSelectedListener {
    public static final String PERSON = "PERSON";

    private ArrayList<Person> peopleList;
    private Person p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // Save people list for future redirections
        peopleList = (ArrayList<Person>) getIntent().getExtras().get(Rankings.PEOPLE_LIST);

        // Get the person for this friend profile
        p = (Person) getIntent().getExtras().get(PERSON);
        String name = p.getName();
        if(p.getID().equals("meeeeee")){
            name = "You";
        }

        ((TextView) findViewById(R.id.headerName)).setText(name);


        ListData[] values = new ListData[Person.Category.values().length];
        int i = 0;
        for (Person.Category c : Person.Category.values()) {
            values[i++] = new ListData(c.name, c.formula.calculateFor(p));
        }

        ArrayAdapter<ListData> adapter = new ArrayAdapter<ListData>(this,
                android.R.layout.simple_list_item_1, values);
        ((ListView) findViewById(R.id.listview)).setAdapter(adapter);

        // Set up back on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (p == null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            // Go Home
            case R.id.friend_profile_to_home:
                intent = new Intent(this, MainActivity.class);
                break;

            // Go to Rankings
            case R.id.friend_profile_to_rankings:
                intent = new Intent(this, Rankings.class);
                break;

            // Back Button
            case android.R.id.home:
                onBackPressed();
                return true;

            // Somehow picked something else
            default:
                return super.onOptionsItemSelected(item);
        }

        // Always add people list
        intent.putExtra(Rankings.PEOPLE_LIST, peopleList);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.putExtra(Rankings.PEOPLE_LIST, peopleList);
        if (NavUtils.shouldUpRecreateTask(this, intent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(intent)
                            // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, intent);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class ListData {
        public final String categoryName;
        public final double value;

        public ListData(String name, Double stat){
            categoryName = name;
            value = stat;
        }


        public String toString() {
            double val = Math.floor(this.value * 100)/100;
            return this.categoryName + " " + "[" +  val + "]";
        }
    }


    @Override
    public void onPause() {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        super.onPause();
        String allPeople = "";
        for (Person p: MainActivity.peopleList) {
            allPeople += p.getStringRepresentation() + ",";
        }

        SharedPreferences settings = getSharedPreferences("preferences", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("allPeople", allPeople);
        editor.commit();

        // For yourself.
        if (MessageListActivity.getUserPerson() != null) {
            SharedPreferences youSettings = getSharedPreferences("youPreferences", 0);
            SharedPreferences.Editor youEditor = youSettings.edit();
            youEditor.putString("you", MessageListActivity.getUserPerson().getStringRepresentation());
            youEditor.commit();
        }
    }
}
