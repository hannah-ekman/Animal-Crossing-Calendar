package com.example.accalendar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.accalendar.R;
import com.example.accalendar.decorators.CenteredImageSpan;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;

public class InflateLayouts {
    public static TargetDrawable fillResourceListItem(LayoutInflater layoutInflater, Typeface typeface,
                                            String resourceName, LocalDate end, String url,
                                            final Context context, LinearLayout list) {
        View resourceListItem = layoutInflater.inflate(R.layout.resource_list_item, null);
        TextView resourceSpan = resourceListItem.findViewById(R.id.resourceItemSpan);
        TextView resourceText = resourceListItem.findViewById(R.id.resourceItemText);
        SpannableString resourceString = new SpannableString(" ");
        TargetDrawable target = new TargetDrawable(context, resourceString, resourceSpan);
        Picasso.get().load(url).into(target);
        resourceText.setTextSize(16);
        resourceText.setTypeface(typeface);
        resourceText.setText(resourceName + " ending on: " +
                end.getMonthValue() + "/" + end.getDayOfMonth());
        list.addView(resourceListItem);
        return target;
    }

    public static void fillEventListItem(ArrayList<View> listViews, LayoutInflater layoutInflater, int color,
                                         ArrayList<String> eventList, Typeface typeface, Drawable d) {
        if (eventList.size() > 0) {
            View eventListItem = layoutInflater.inflate(R.layout.event_list_item, null);
            TextView eventSpan = eventListItem.findViewById(R.id.eventItemSpan);
            LinearLayout eventListView = eventListItem.findViewById(R.id.eventItemList);
            SpannableString eventString = new SpannableString(" ");
            d.setBounds(0, 0, 50, 50);
            CenteredImageSpan span = new CenteredImageSpan(d);
            eventString.setSpan(span, 0, eventString.length(),
                    SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
            eventSpan.setText(eventString);
            for (String eventName : eventList) {
                TextView event = new TextView(eventListItem.getContext());
                event.setText(eventName);
                event.setTextColor(Color.DKGRAY);
                event.setTextSize(16);
                event.setTypeface(typeface);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                event.setLayoutParams(params);
                eventListView.addView(event);
            }
            listViews.add(eventListItem);
        }
    }
}


