package com.pbj.statextics;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendAdapter extends ArrayAdapter<String> {
    private final HashMap<String, Person> personMap;
    private final List<String> numberList;
    Typeface tf;

    // List context
    private final Context context;

    public FriendAdapter(Context inputContext,HashMap<String, Person> map) {
        super(inputContext, R.layout.activity_main, new ArrayList(map.keySet()));
        personMap = map;
        numberList = new ArrayList(map.keySet());
        context = inputContext;
        tf = Typeface.createFromAsset(context.getAssets(), "fonts/BLANCH_CONDENSED_LIGHT.otf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.activity_friends, parent, false);
        Button senderPerson = (Button) rowView.findViewById(R.id.friendPerson);


        Person p = personMap.get(numberList.get(position));
        senderPerson.setText((p.getName() == null || p.getName().equals("")?
                p.getNumber() : p.getName()));
        senderPerson.setContentDescription(p.getID());

        TextView textView = (TextView)rowView.findViewById(R.id.friendPerson);
        textView.setTypeface(tf);


        return rowView;
    }



}