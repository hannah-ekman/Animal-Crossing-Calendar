package com.example.accalendar.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<View> {
    public ListAdapter(Context context, ArrayList<View> views) {
        super(context, 0, views);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        View v = getItem(position);
        return v;
    }
}
