package com.pbj.statextics;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageListActivity extends ListActivity {

    private static HashMap<String, Person> smsPeople = new HashMap<>();
    private static Person userPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        userPerson = new Person(tele.getLine1Number(), tele.getSimOperatorName(), "This is you, the user");

        // Read in all the messages
        Cursor c = getContentResolver().query(Uri.parse("content://sms/"), null, null ,null,null);
        if(c.moveToFirst()) {
            for(int i=0; i < c.getCount(); i++) {
                if (i % 1000 == 0) {
                    System.out.println(i + " texts analyzed");
                }
                // Make the SMSData object
                SMSData sms = new SMSData(
                        c.getString(c.getColumnIndexOrThrow("address")), // phone number
                        c.getString(c.getColumnIndexOrThrow("body")),
                        c.getString(c.getColumnIndexOrThrow("_id")),
                        c.getString(c.getColumnIndexOrThrow("date")), // time in milliseconds
                        c.getString(c.getColumnIndexOrThrow("type")).contains("1")?
                                SMSData.INBOX : SMSData.OUTBOX, // folder name
                        getContactName(getApplicationContext(),c.getString(c.getColumnIndexOrThrow("address"))));

                // Add new contact to map if necessary
                String contactID = getContactID(getApplicationContext(), sms.getNumber());
                if (!smsPeople.containsKey(contactID)) {
                    smsPeople.put(contactID, new Person(sms.getNumber(), sms.getName(), contactID));
                }

                // Give the text to the proper person
                smsPeople.get(contactID).update(sms);

                // If sent from me, add to myself
                if (sms.getFolderName().equals(SMSData.OUTBOX)) {
                    sms.switchFolder();
                    userPerson.update(sms);
                }

                // Move to next text
                c.moveToNext();
            }
        }
        c.close();

        // Move all people to a list of people
        MainActivity.peopleList = new ArrayList<>();
        for (String s: smsPeople.keySet()) {
            if (smsPeople.get(s).getTotalMessages(Person.RECEIVED_FROM_THEM) > 10) {
                MainActivity.peopleList.add(smsPeople.get(s));
            }
        }

        // Migrate to Rankings
        Intent intent = new Intent(this, Rankings.class);
        intent.putExtra(Rankings.PEOPLE_LIST, MainActivity.peopleList);
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_message_list, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        SMSData sms = (SMSData)getListAdapter().getItem(position);

        Toast.makeText(getApplicationContext(), sms.getBody(), Toast.LENGTH_SHORT).show();
        Log.d("onListItemClick", sms.getBody());

    }

    private String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    private String getContactID(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,new String[] { ContactsContract.Contacts.LOOKUP_KEY }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactID = null;
        if (cursor.moveToFirst()) {
            contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactID;
    }

    private String getMyProfile(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,new String[] { ContactsContract.Contacts.LOOKUP_KEY }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactID = null;
        if (cursor.moveToFirst()) {
            contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactID;
    }

    public static HashMap<String, Person> getSMSPeople(){

        if (smsPeople == null || smsPeople.size() == 0){
            if (MainActivity.peopleList.size() > 0) {
                smsPeople = new HashMap<>();

                for (int i = 0; i < MainActivity.peopleList.size(); i++) {
                    Person tempPerson = MainActivity.peopleList.get(i);
                    smsPeople.put(tempPerson.getID(), tempPerson);
                }

                return smsPeople;
            }
            return null;
        }
        return smsPeople;
    }

    public static Person getUserPerson(){
        return userPerson;
    }


    public static void setUserPerson(Person p) {
        userPerson = p;
    }

}
