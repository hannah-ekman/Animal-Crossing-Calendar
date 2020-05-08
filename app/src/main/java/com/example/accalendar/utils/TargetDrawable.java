package com.example.accalendar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.widget.TextView;

import com.example.accalendar.decorators.CenteredImageSpan;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class TargetDrawable implements Target {
    private Context context;
    private SpannableString string;
    private TextView spanText;

    public TargetDrawable(Context context, SpannableString string, TextView span){
        this.spanText = span;
        this.string = string;
        this.context = context;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Drawable d = new BitmapDrawable(context.getResources(), bitmap);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        CenteredImageSpan span = new CenteredImageSpan(d);
        string.setSpan(span, 0, string.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setText(string);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        e.printStackTrace();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
