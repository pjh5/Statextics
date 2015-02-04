package com.pbj.statextics;

/**
 * Created by cwang on 1/31/2015.
 */

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List adapter for storing SMS data
 *
 * @author itcuties
 *
 */
public class ListAdapter extends ArrayAdapter<SMSData> {

    // List context
    private final Context context;
    // List values
    private final List<SMSData> smsList;

    public ListAdapter(Context context, List<SMSData> smsList) {
        super(context, R.layout.activity_main, smsList);
        this.context = context;
        this.smsList = smsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.activity_message_list, parent, false);

        TextView senderNumber = (TextView) rowView.findViewById(R.id.smsNumberText);
        senderNumber.setText(smsList.get(position).getName() + "\n"
                + smsList.get(position).getNumber() + "\n"
                + smsList.get(position).getBody() + "\n"
                + smsList.get(position).getId() + "\n"
                + smsList.get(position).getTime() + "\n"
                + smsList.get(position).getFolderName());

        return rowView;
    }

}