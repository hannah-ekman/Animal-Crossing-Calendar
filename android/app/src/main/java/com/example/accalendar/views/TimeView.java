package com.example.accalendar.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.example.accalendar.R;

public class TimeView extends View {
    // colors
    private int rectangleCol, backgroundCol;
    // paint for drawing custom view
    private Paint paint;
    // rectangle used for drawing
    private RectF rectF;
    // array used to decide if we fill the section
    private int[] timeBools;
    // array of times that the item is available
    private int[] times = new int[]{12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private final Context context;

    public TimeView(Context context, AttributeSet attrs){
        super(context, attrs);

        this.context = context;

        //paint object for drawing in onDraw
        paint = new Paint();

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TimeView, 0, 0);
        rectF = new RectF();

        try {
            //get the dates and colors specified using the names in attrs.xml
            rectangleCol = a.getInteger(R.styleable.TimeView_timeRectangleColor, 0);//0 is default
            backgroundCol = a.getInteger(R.styleable.TimeView_timeBackgroundColor, 0);
            int id = a.getInteger(R.styleable.TimeView_times, 0);

            if (id != 0) {
                timeBools = getResources().getIntArray(id);
            } else {
                timeBools = new int[25];
                for(int i = 0; i<25; i++)
                    timeBools[i] = 0;
            }
        } finally {
            a.recycle();
        }
    }

    // get and set functions

    public int[] getTimes() {
        return times;
    }

    public int getRectangleColor(){
        return rectangleCol;
    }

    public void setTimes(int[] timeBools) {
        this.timeBools = timeBools;
        //redraw the view
        invalidate();
        requestLayout();
    }

    public void setRectangleColor(int newColor){
        //update the instance variable
        rectangleCol=newColor;
        //redraw the view
        invalidate();
        requestLayout();
    }

    // copied from android docs https://developer.android.com/training/custom-views/custom-drawing
    // essentially gets the width and height of the space for the view to fill
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        // Set color to the rectangle's color to draw
        paint.setColor(rectangleCol);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics(); // used to convert to dp
        // Used so the rectangle doesn't fit all the way to the top of the view
        int rectHeight = 35;
        int rectTop = (int) (metrics.density*rectHeight+0.5f);

        // Used to pad the view so that the time text isn't cut off
        float boxPadding = metrics.density*12+0.5f;

        int w = getWidth();
        int h = getHeight();
        float interval = (w-boxPadding*2)/24f; // used to draw the hour lines evenly
        int textYPos; // for positioning the text in the view vertically
        paint.setStrokeWidth(5);
        paint.setTextSize((int) (metrics.density*12+0.5f));
        paint.setTextAlign(Paint.Align.CENTER);
        Typeface font = ResourcesCompat.getFont(context, R.font.josefin_sans_semibold);
        paint.setTypeface(font);
        int lineStart, lineEnd, lineTop, lineBottom, time,
                fontHeight = (int) (paint.descent()-paint.ascent()); // gets the height of the font
        String am = "AM", pm = "PM";
        boolean isAM;
        for(int i = 0; i<25; i++) {
            paint.setColor(rectangleCol);
            // If the entry is available during this hour: draw a rectangle to fill the hour
            if(i<24 && (timeBools[i] == 1 && timeBools[i+1] == 1)) {
                rectF.set(
                        interval*i+boxPadding, // left
                        rectTop, // top
                        interval*(i+1)+boxPadding, // right
                        getHeight() // bottom
                );
                canvas.drawRect(rectF, paint);
            }
            paint.setColor(backgroundCol);
            time = times[i];
            if(time/12f == time/12) {
                // Determine if the hour is AM or PM
                if(i == 0 || i == 24) {
                    isAM = true;
                } else {
                    isAM = false;
                }
                lineTop = rectHeight-5; // where the tick mark starts (12 will be highest, 6 2nd heighest, etc...)
                paint.setTextSize((int) (metrics.density*12+0.5f)); // set text to 12dp
                // get text position for the time (has to be placed above AM/PM, so subtract fontHeight)
                textYPos = (int) (metrics.density*(lineTop-5)+0.5f)-fontHeight;
                canvas.drawText(Integer.toString(times[i]), interval*i+boxPadding, textYPos, paint);
                // get text position for the AM/PM (place it right above the tick mark with a bit of padding)
                textYPos = (int) (metrics.density*(lineTop-5)+0.5f);
                canvas.drawText((isAM ? am : pm), interval*i+boxPadding, textYPos, paint);
                lineBottom = 0; // where the tick mark ends
            }else if(time/6f == time/6) {
                lineTop = rectHeight-5;
                paint.setTextSize((int) (metrics.density*12+0.5f));
                textYPos = (int) (metrics.density*(lineTop-5)+0.5f);
                canvas.drawText(Integer.toString(times[i]), interval*i+boxPadding, textYPos, paint);
                lineBottom = 0;
            } else if (time/3f == time/3) {
                lineTop = rectHeight-2;
                paint.setTextSize((int) (metrics.density*11+0.5f));
                textYPos = (int) (metrics.density*(lineTop-5)+0.5f);
                canvas.drawText(Integer.toString(times[i]), interval*i+boxPadding, textYPos, paint);
                lineBottom = 3;
            } else {
                lineTop = rectHeight+5;
                lineBottom = 5;
            }
            lineStart = (int) (metrics.density*lineTop+0.5f);
            lineEnd = (int) (metrics.density*lineBottom+0.5f);
            // draw a vertical line for the ith tick
            canvas.drawLine(interval*i+boxPadding, lineStart, interval*i+boxPadding, h-lineEnd, paint);
        }
    }
}
