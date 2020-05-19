package com.example.accalendar.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class EventAdapter extends ArrayAdapter<View> {
    public EventAdapter(Context context, ArrayList<View> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        View event = getItem(position);
        return event;
    }
}
