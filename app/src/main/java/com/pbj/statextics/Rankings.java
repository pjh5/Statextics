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
import android.widget.Spinner;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class Rankings extends ActionBarActivity implements OnItemSelectedListener {
    public static final String PEOPLE_LIST = "PEOPLE_LIST";

    private static HashMap<String, String> descriptions = new HashMap<>();
    static {
        descriptions.put(Person.Category.MOST_PROFANE.name,
                "Definitely does not kiss their mother with their mouth.");
        descriptions.put(Person.Category.MOST_VAIN.name,
                "I don't think these friends are aware that the Earth revolves around the sun and not them.");
        descriptions.put(Person.Category.LONGEST_WORD.name,
                "These friends are trying to compensate for something with their large-word-knowing");
        descriptions.put(Person.Category.PARTY_ANIMAL.name,
                "This friend is probably most likely to get insurance on their liver");
        descriptions.put(Person.Category.THEY_TEXT_MORE.name,
                "You should probably text these people a bit more...");
        descriptions.put(Person.Category.THEY_TEXT_LESS.name,
                "I wouldn't be surprised if these people had shrines dedicated to you.");
        descriptions.put(Person.Category.LAUGHER.name,
                "They're not actually laughing out loud, just so you know.");
        descriptions.put(Person.Category.KNOW_NOTHING.name,
                "If you were on a trivia show, don't bother calling these people");
        descriptions.put(Person.Category.MISS_BASIC.name,
                "Don't get too close to these people, I hear basic-ness is contagious");
        descriptions.put(Person.Category.LOTS_OF_TEXTS.name,
                "The killer of limited text customers world-wide");
        descriptions.put(Person.Category.LONG_TEXTS.name,
                "They've probably spent an hour constructing each text");
    }

    private static final String[] categories = new String[Person.Category.values().length];
    static {
        int i = 0;
        for (Person.Category c : Person.Category.values()) {
            categories[i++] = c.name;
        }
    }

    Spinner spinner1;

    private ArrayList<Person> peopleList;
    private List<PersonWithRank> friends = new ArrayList<>();
    private ArrayAdapter<PersonWithRank> listAdapter;
    private static Person.Category selectedCategory = Person.Category.LONG_TEXTS;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_rankings);

        // Get friends from intent, arbitrary ranking at first
        peopleList = (ArrayList<Person>) getIntent().getExtras().get(PEOPLE_LIST);
        for (Person p : peopleList) {
            friends.add(new PersonWithRank(p, 0));
        }

        // Make a spinner and other stuff
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setAdapter(spinnerAdapter);
        spinner1.setOnItemSelectedListener(this);

        final ListView listview = (ListView) findViewById(R.id.listview);

        // Make and set Adapter
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friends);
        listview.setAdapter(listAdapter);

        // Arbitrarily Picked a Starting Category
        sortList();

        // Set up back on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (peopleList == null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_rankings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            // Go Home
            case R.id.rankings_to_home:
                intent = new Intent(this, MainActivity.class);
                break;

            // Go to Friends list
            case R.id.rankings_to_friends:
                intent = new Intent(this, FriendsActivity.class);
                break;

            // Back Button
            case android.R.id.home:
                onBackPressed();
                return true;


            // Somehow nothing picked
            default:
                return super.onOptionsItemSelected(item);
        }

        // Go where we decided to go
        intent.putExtra(PEOPLE_LIST, peopleList);
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
        // Display Description
        Toast.makeText(getApplicationContext(), descriptions.get(
                parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();

        // Select Category and Sort List
        for (Person.Category cat : Person.Category.values()) {
            if (cat.name.equals(parent.getItemAtPosition(position).toString())) {
                selectedCategory = cat;
            }
        }
        sortList();
    }


    private void sortList() {
        // Sort friends
        final String categoryString = selectedCategory.name;
        listAdapter.sort(new Comparator<PersonWithRank>() {

            @Override
            public int compare(PersonWithRank lhs, PersonWithRank rhs) {
                double comparison = rhs.p.getRatingFor(categoryString) - lhs.p.getRatingFor(categoryString);
                return comparison > 0 ? 1 : -1;
            }
        });

        // Update friends' ranks for appearance
        for (PersonWithRank p : friends) {
            p.value = p.p.getRatingFor(categoryString);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /**
     * Stores a Person and their CURRENT RANK
     */
    private class PersonWithRank {
        public final Person p;
        public double value;

        public PersonWithRank(Person p, double value) {
            this.p = p;

            this.value = value;

        }


        public String toString() {
            String display = p.getName();
            if (display == "" || display == null){
                display = p.getNumber();
            }

            return display + "  " + Math.floor(value * 100)/100 + " " + selectedCategory.description;
        }
    }

    public void goToMain(){
        startActivity(new Intent(this, MainActivity.class));
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
