package com.example.accalendar.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.example.accalendar.R;

public class MonthView extends View {
    //circle and text colors
    private int rectangleCol, highlightCol;
    //paint for drawing custom view
    private Paint paint;
    // starting month the item becomes available
    private int startMonth;
    // last month the item is available
    private int endMonth;
    // rectangle used for drawing
    private RectF rectF;
    // array used to decide if we fill the square
    private boolean[] monthBools;
    private String[] months = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private final Context context;

    public MonthView(Context context, AttributeSet attrs){
        super(context, attrs);

        this.context = context;

        //paint object for drawing in onDraw
        paint = new Paint();

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.MonthView, 0, 0);
        rectF = new RectF();

        try {
            //get the dates and colors specified using the names in attrs.xml
            rectangleCol = a.getInteger(R.styleable.MonthView_rectangleColor, 0);//0 is default
            highlightCol = a.getInteger(R.styleable.MonthView_highlightColor, 0);
        } finally {
            a.recycle();
        }
    }

    // get and set functions

    public int getRectangleColor(){
        return rectangleCol;
    }

    public int getHighlightColor(){
        return highlightCol;
    }

    public void setMonths(boolean[] monthBools) {
        this.monthBools = monthBools;
        //redraw the view
        invalidate();
        requestLayout();
    }

    public void setRectangleColor(int newColor){
        //update the instance variable
        rectangleCol = newColor;
        //redraw the view
        invalidate();
        requestLayout();
    }
    public void setHighlightColor(int newColor){
        //update the instance variable
        highlightCol = newColor;
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
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        // Set color to the rectangle's color to draw
        paint.setColor(rectangleCol);

        // Set the rectangle to use for drawing the rounded rectangles
        rectF.set(
                0, // left
                0, // top
                getWidth(), // right
                getHeight() // bottom
        );

        // Define the corners radius of rounded rectangle
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int cornersRadius = (int) (metrics.density*10+0.5f);

        // Draw the rounded corners rectangle object on the canvas
        canvas.drawRoundRect(
                rectF, // rect
                cornersRadius, // rx
                cornersRadius, // ry
                paint // Paint
        );

        int w = getWidth();
        int h = getHeight();
        float interval = w/6f; // used to split the rectangle into 6 even boxes
        int textYPos;
        paint.setStrokeWidth(4);
        paint.setTextSize((int) (metrics.density*16+0.5f));
        paint.setTextAlign(Paint.Align.CENTER);
        // have to start at i = 1 because we don't want to draw a line on the edge of the rectangle (0,0) coord.
        // We only need 5 vertical lines inside the rectangle to create 6 boxes
        for (int i = 1; i<6; i++) {
            // set to the color of the highlighted boxes
            paint.setColor(highlightCol);
            // draw a vertical line for the ith box split
            canvas.drawLine(interval*i, 0, interval*i, h, paint);

            // if the item is available during this month (jan-jun)
            if(monthBools[i-1]) {
                // set the rectangle to be in the center of the top box + padding (
                rectF.set(
                        interval * (i - 1) + 10, // left
                        10, // top
                        interval * i - 10, // right
                        h / 2 - 10 // bottom
                );
                canvas.drawRoundRect(
                        rectF, // rect
                        cornersRadius, // rx
                        cornersRadius, // ry
                        paint // Paint
                );
            }
            // if the item is available during this month (jul-dec)
            if(monthBools[i-1+6]) {
                rectF.set(
                        interval * (i - 1) + 10, // left
                        h / 2 + 10, // top
                        interval * i - 10, // right
                        h - 5 - 8 // bottom
                );
                canvas.drawRoundRect(
                        rectF, // rect
                        cornersRadius, // rx
                        cornersRadius, // ry
                        paint // Paint
                );
            }
            // set text color to be white
            paint.setColor(Color.WHITE);
            // center the text vertically in the top box
            textYPos = (h / 4) - (int) (paint.descent() + paint.ascent()) / 2;
            canvas.drawText(months[i-1], (interval*i+interval*(i-1))/2, textYPos, paint);
            //center the text vertically in the bottom box
            textYPos = 3*(h / 4) - (int) (paint.descent() + paint.ascent()) / 2;
            canvas.drawText(months[i-1+6], (interval*i+interval*(i-1))/2, textYPos, paint);
        }
        // fill the last two month boxes
        paint.setColor(highlightCol);
        if(monthBools[5]) {
            rectF.set(
                    interval * (6 - 1) + 10, // left
                    10, // top
                    interval * 6 - 10, // right
                    h / 2 - 10 // bottom
            );
            canvas.drawRoundRect(
                    rectF, // rect
                    cornersRadius, // rx
                    cornersRadius, // ry
                    paint // Paint
            );
        }
        if(monthBools[11]) {
            rectF.set(
                    interval * (6 - 1) + 10, // left
                    h / 2 + 10, // top
                    interval * 6 - 10, // right
                    h - 10 // bottom
            );
            canvas.drawRoundRect(
                    rectF, // rect
                    cornersRadius, // rx
                    cornersRadius, // ry
                    paint // Paint
            );
        }
        // draw a horizontal line to cut the rectangle in half (creating 12 boxes)
        canvas.drawLine(0, h/2, w, h/2, paint);
        paint.setColor(Color.WHITE);
        textYPos = (h / 4) - (int) (paint.descent() + paint.ascent()) / 2;
        canvas.drawText(months[6-1], (interval*6+interval*(6-1))/2, textYPos, paint);
        textYPos = 3*(h / 4) - (int) (paint.descent() + paint.ascent()) / 2;
        canvas.drawText(months[6-1+6], (interval*6+interval*(6-1))/2, textYPos, paint);
    }
}
