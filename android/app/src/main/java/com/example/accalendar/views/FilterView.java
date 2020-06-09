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
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.example.accalendar.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterView extends View {
    //circle and text colors
    private int rectangleCol, highlightCol;
    //paint for drawing custom view
    private Paint paint;
    // rectangle used for drawing
    private RectF rectF;
    // array used to decide if we fill the square
    private HashMap<String, Boolean> filters = new HashMap<>();
    private HashMap<String, HashMap<String, Float>> coordinates = new HashMap<>(); // JAN: x1: 0, y1: 0, x2: 0, y2: 0
    private final Context context;
    private int height = 0;
    private int padding = 25;
    private int buttonHeight = 100;
    private boolean isSorted;

    public FilterView(Context context, AttributeSet attrs){
        super(context, attrs);

        this.context = context;

        //paint object for drawing in onDraw
        paint = new Paint();

        // set paint font styles
        Typeface font = ResourcesCompat.getFont(context, R.font.josefin_sans_semibold);
        paint.setTypeface(font);
        paint.setStrokeWidth(4);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        paint.setTextSize((int) (metrics.density * 16 + 0.5f));
        paint.setTextAlign(Paint.Align.CENTER);

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.MonthView, 0, 0);
        rectF = new RectF();

        try {
            //get the dates and colors specified using the names in attrs.xml
            rectangleCol = a.getInteger(R.styleable.FilterView_filterRectangleColor, 0);//0 is default
            highlightCol = a.getInteger(R.styleable.FilterView_filterHighlightColor, 0);
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

    public HashMap<String, Boolean> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, Boolean> filters){
        this.filters = filters;

        // gets height of the rows to use in onMeasure to set the height of the view
        int w = getMeasuredWidth();
        List<String> filterStrings = new ArrayList<>(filters.keySet());
        float curWidth = 0;
        int rows = 0;
        for (int i = 0; i < filters.size(); i++) {
            String currentString = filterStrings.get(i);
            float buttonWidth = paint.measureText(currentString) + padding * 2 + padding; // + inner padding + outer margin
            if (curWidth + buttonWidth > w) {
                curWidth = buttonWidth;
                rows += 1;
            } else {
                curWidth += buttonWidth;
            }
        }
        height = ((rows+1)*(75+padding)) + padding;
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

        int h = resolveSizeAndState(height, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    private void drawButtons(Canvas canvas, int fromIdx, List<String> filterStrings,
                             float start, int rows, int cornersRadius, int toIdx) {
        float textYPos;
        // for each button in row
        for (int j = fromIdx; j < toIdx; j++) {
            if (filters.get(filterStrings.get(j)))
                paint.setColor(highlightCol);
            else
                paint.setColor(rectangleCol);
            String drawString = filterStrings.get(j);
            float drawWidth = paint.measureText(drawString); // drawWidth = the width of the text

            rectF.set(
                    start, // left
                    rows * buttonHeight + padding, // top
                    start + drawWidth + padding * 2, // right
                    (rows + 1) * buttonHeight // bottom
            );

            // Draw the button on the canvas
            canvas.drawRoundRect(
                    rectF, // rect
                    cornersRadius, // rx
                    cornersRadius, // ry
                    paint // Paint
            );
            HashMap<String, Float> coords = new HashMap<>();
            coords.put("x1", start);
            coords.put("y1", (float) (rows * buttonHeight + padding));
            coords.put("x2", start + drawWidth + padding * 2);
            coords.put("y2", (float) (rows + 1) * buttonHeight);

            coordinates.put(filterStrings.get(j), coords);

            //draw the text in the center of the button
            paint.setColor(Color.WHITE);
            // centers text horizontally
            // essentially (a+b)/2 with a = start + padding, b = start + drawWidth + padding.
            // think of it like a line segment on a coordinate plane
            // a*-----*b, the length of the line segment would be drawWidth + padding,
            // the location of a is start + padding. So the actual location of b is a + length of segment
            float textXPos = (start*2 + drawWidth + padding*2)/2;
            // vertical center of button - distance to baseline + padding
            textYPos = ((rows + 1) * (buttonHeight) + (rows) * (buttonHeight) + padding)/2f - (int) (paint.ascent())/2f;
            canvas.drawText(drawString, textXPos, textYPos, paint);

            start+=drawWidth+padding*2+padding;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touched_x = event.getX();
        float touched_y = event.getY();
        boolean touched;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touched = true;
                break;
            case MotionEvent.ACTION_MOVE:
                touched = true;
                break;
            case MotionEvent.ACTION_UP:
                touched = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                touched = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                touched = false;
                break;
            default:
                touched = false;
        }

        if (touched) {
            for (HashMap.Entry <String, HashMap<String, Float>> coord : coordinates.entrySet()) {
                HashMap<String, Float> xy = coord.getValue();
                if (touched_x <= xy.get("x2") && touched_x >= xy.get("x1") &&
                        touched_y <= xy.get("y2") && touched_y >= xy.get("y1")) {
                    System.out.println(coord.getKey()+" "+filters);
                    boolean tf = filters.get(coord.getKey());
                    filters.put(coord.getKey(), !tf);
                    invalidate();
                    requestLayout();
                }
            }
        }

        return true; // processed
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (filters != null) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            // Set color to the rectangle's color to draw
            paint.setColor(Color.WHITE);



            // Define the corners radius of rounded rectangle
            int cornersRadius = 20;
            // Set the rectangle to use for drawing the rounded rectangles
            rectF.set(
                    0, // left
                    0, // top
                    getWidth(), // right
                    getHeight() // bottom
            );

            // draw background rectangle
            canvas.drawRect(rectF, paint);

            int w = getWidth();
            List<String> filterStrings = new ArrayList<>(filters.keySet());
            float curWidth = 0;
            int fromIdx = 0;
            int rows = 0;
            // Go through each key and get the width and add it to a accumulator.
            // If the accumulator > width: draw the row of buttons (not including the one that overfilled the width)
            for (int i = 0; i < filters.size(); i++) {
                String currentString = filterStrings.get(i);
                float buttonWidth = paint.measureText(currentString) + padding * 2 + padding;
                if (curWidth + buttonWidth + padding > w) {
                    // leftover = amount of space left over in the row of buttons. We can then center
                    // the buttons by splitting this space in half and starting the row at leftover/2
                    float leftover = w - curWidth + padding;
                    float start = leftover / 2f;
                    drawButtons(canvas, fromIdx, filterStrings, start, rows, cornersRadius, i);
                    // reset everything and increase # of rows
                    fromIdx = i;
                    curWidth = buttonWidth;
                    rows += 1;
                } else {
                    curWidth += buttonWidth;
                }
            }

            float leftover = w - curWidth + padding;
            float start = leftover / 2f;
            drawButtons(canvas, fromIdx, filterStrings, start, rows, cornersRadius, filters.size());
        }
    }
}
