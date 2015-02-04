package com.pbj.statextics;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;


import java.util.ArrayList;


public class FriendsActivity extends ListActivity {
    private ArrayList<String> peopleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_friends); //new
        View header = getLayoutInflater().inflate(R.layout.header, null);
        ListView listView = getListView();
        listView.addHeaderView(header);
        setListAdapter(new FriendAdapter(this, MessageListActivity.getSMSPeople()));

        //font manipulation occurs  here
        TextView tv = (TextView)findViewById(R.id.topheader);
        Typeface tfBold = Typeface.createFromAsset(getAssets(), "fonts/BLANCH_CONDENSED_LIGHT.otf");
        tv.setTypeface(tfBold);

        // Save people list for future
        peopleList = (ArrayList<String>) getIntent().getExtras().get(Rankings.PEOPLE_LIST);

        // Set up back on Action Bar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            // Go Home
            case R.id.friends_activity_to_home:
                intent = new Intent(this, MainActivity.class);
                break;

            // Go to Friends list
            case R.id.friends_activity_to_rankings:
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

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }
    public void goToFriendProfile(View view) {
        Intent intent = new Intent(this, FriendProfile.class);
        intent.putExtra(FriendProfile.PERSON,
                MessageListActivity.getSMSPeople().get(((Button) view).getContentDescription()));
        intent.putExtra(Rankings.PEOPLE_LIST, peopleList);
        startActivity(intent);
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
