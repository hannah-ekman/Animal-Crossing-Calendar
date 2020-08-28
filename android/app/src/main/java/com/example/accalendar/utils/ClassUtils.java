package com.example.accalendar.utils;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.accalendar.R;
import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.example.accalendar.views.MonthView;
import com.example.accalendar.views.TimeView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public enum ItemType {
        FISH, BUG, FOSSIL, ART, VILLAGERS, RESIDENT
    }

    public enum ViewType {
        TEXTVIEW, MONTHVIEW, TIMEVIEW, DONATEDBUTTON, CAUGHTBUTTON, IMAGEVIEW, DREAMIEBUTTON, RESIDENTBUTTON
    }

    // holds the id of the view, the key for the view (used to match to the value of the view), and the type of view
    public static class IdKeyPair {
        final int id;
        final String key;
        final ViewType type;
        int selectedBG;
        int unselectedBG;
        public IdKeyPair(int id, String key, ViewType type, int selectedBG, int unselectedBG) {
            this.id = id;
            this.key = key;
            this.type = type;
            this.selectedBG = selectedBG;
            this.unselectedBG = unselectedBG;
        }

        public IdKeyPair(int id, String key, ViewType type) {
            this.id = id;
            this.key = key;
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public String getKey() {
            return key;
        }

        public ViewType getType() {
            return type;
        }

        public int getSelectedBG() {
            return selectedBG;
        }

        public int getUnselectedBG() {
            return unselectedBG;
        }
    }

    public static class Available {
        public int start;
        public int end;
    }

    public static class Date {
        public int day;
        public int month;
    }

    public static abstract class Trackable {
        public String name;
        public String image;

        public void fillKeyValues(HashMap<String, Object> values) {
            values.put("name", name);
            values.put("image", image);
        }
    }

    public static abstract class Discoverable extends Trackable {
        public int price;

        @Override
        public void fillKeyValues(HashMap<String, Object> values) {
            super.fillKeyValues(values);
            values.put("price", String.valueOf(price));
        }
    }

    public static abstract class Catchable extends Discoverable {
        public String location;
        public ArrayList<Available> north;
        public ArrayList<Available> south;
        public ArrayList<Available> times;

        ArrayList<ClassUtils.Available> ParseTimeHash(ArrayList<HashMap<String, Object>> available) {
            ArrayList<ClassUtils.Available> parsed = new ArrayList<>();
            for(int i = 0; i < available.size(); i++) {
                ClassUtils.Available times = new ClassUtils.Available();
                times.start = ((Long) available.get(i).get("start")).intValue();
                times.end = ((Long) available.get(i).get("end")).intValue();
                parsed.add(times);
            }
            return parsed;
        }

        public String get(String value) {
            if ("Locations".equals(value)) {
                return location;
            }
            return null;
        }

        public void fillKeyValues(HashMap<String, Object> values, boolean isNorth) {
            super.fillKeyValues(values);
            values.put("location", location);
            if(isNorth)
                values.put("months", fillMonthBools(north));
            else
                values.put("months", fillMonthBools(south));
            values.put("times", fillTimeBools(times));
        }

        private boolean[] fillMonthBools(ArrayList<ClassUtils.Available> months) {
            int monthStart, monthEnd;
            boolean[] monthBools = new boolean[12];
            for (int i = 0; i < 12; i++)
                monthBools[i] = false;
            for (int monthIdx = 0; monthIdx < months.size(); monthIdx++) {
                monthStart = months.get(monthIdx).start;
                monthEnd = months.get(monthIdx).end;
                for (int i = monthStart; i <= monthEnd; i++)
                    monthBools[i - 1] = true;
            }
            return monthBools;
        }

        // Sets the times the fish is available to 1 and the rest to 0 -> used in the TimeView
        private int[] fillTimeBools(ArrayList<ClassUtils.Available> times) {
            int startTime, endTime;
            int[] timeBools = new int[25];
            for (int i = 0; i < 25; i++)
                timeBools[i] = 0;
            // Iterate through each time in the array (this was to deal with the piranha lol)
            for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
                startTime = times.get(timeIdx).start;
                endTime = times.get(timeIdx).end;
                // If startTime is before endTime -> fill all times from start to end with 1
                if (startTime <= endTime) {
                    for (int i = startTime; i <= endTime; i++)
                        timeBools[i] = 1;
                } else { // start is after end -> fill all times before start and times after end with 1
                    for (int i = 0; i <= endTime; i++)
                        timeBools[i] = 1;
                    for (int i = startTime; i < 25; i++)
                        timeBools[i] = 1;
                }
            }
            return timeBools;
        }
    }

    // handles inflating the popup and contains other useful info for the recyclerview
    // keeps all the ids of each view (R.id.blank) in a layout to fill in the popup later
    public static class PopupHelper {
        final ArrayList<IdKeyPair> idKeyPairs;
        final int mainView;
        final int caughtId;
        final int notCaughtId;
        final int mainViewBg;
        final int imageView;
        int residentView = 0;
        final int constraintView;
        int donatedId;
        int donatedIcon;
        int dreamieIcon;

        public PopupHelper(ArrayList<IdKeyPair> idKeyPairs, int mainView, int caughtId, int notCaughtId,
                           int mainViewBg, int imageView, int constraintView, int donatedId, int donatedIcon) {
            this.idKeyPairs = idKeyPairs;
            this.mainView = mainView;
            this.caughtId = caughtId;
            this.notCaughtId = notCaughtId;
            this.mainViewBg = mainViewBg;
            this.imageView = imageView;
            this.constraintView = constraintView;
            this.donatedId = donatedId;
            this.donatedIcon = donatedIcon;
        }

        public PopupHelper(ArrayList<IdKeyPair> idKeyPairs, int mainView, int caughtId, int notCaughtId,
                           int mainViewBg, int imageView, int residentView, int constraintView, int donatedId,
                           int donatedIcon, int dreamieIcon) {
            this.idKeyPairs = idKeyPairs;
            this.mainView = mainView;
            this.caughtId = caughtId;
            this.notCaughtId = notCaughtId;
            this.mainViewBg = mainViewBg;
            this.imageView = imageView;
            this.residentView = residentView;
            this.constraintView = constraintView;
            this.donatedId = donatedId;
            this.donatedIcon = donatedIcon;
            this.dreamieIcon = dreamieIcon;
        }

        public ArrayList<IdKeyPair> getIdKeyPairs() {
            return idKeyPairs;
        }

        public int getImageView() {
            return imageView;
        }

        public int getResidentView() { return residentView; }

        public int getDreamieIcon() {
            return dreamieIcon;
        }

        public int getConstraintView() {
            return constraintView;
        }

        public int getCaughtId() {
            return caughtId;
        }

        public int getNotCaughtId() {
            return notCaughtId;
        }

        public int getDonatedId() {
            return donatedId;
        }

        public int getDonatedIcon() {
            return donatedIcon;
        }

        // inflates the popup and fills the views
        public void fillViews(LayoutInflater mInflater, final HashMap<String, Object> keyValuePairs,
                               final Map<String, Object> caught, final DocumentReference docRef,
                               final RecyclerviewAdapter.ViewHolder holder, final Context context,
                               final DocumentReference donatedRef, final Map<String, Object> donated,
                               final Object month, final Object day) {
            View popView = mInflater.inflate(mainView, null);
            // go through each id we need to fill
            for(int i = 0; i < idKeyPairs.size(); i++) {
                final IdKeyPair idKeyPair = idKeyPairs.get(i);
                // get the corresponding value for the current key
                Object value = keyValuePairs.get(idKeyPair.getKey());
                int viewId = idKeyPair.getId();
                ViewType type = idKeyPair.getType();
                // depending on the type we handle it differently
                switch (type) {
                    case TEXTVIEW:
                        TextView text = popView.findViewById(viewId);
                        text.setText((String) value);
                        break;
                    case MONTHVIEW:
                        MonthView months = popView.findViewById(viewId);
                        months.setMonths((boolean[]) value);
                        break;
                    case TIMEVIEW:
                        TimeView times = popView.findViewById(viewId);
                        times.setTimes((int[]) value);
                        break;
                    case CAUGHTBUTTON:
                        final ImageButton button = popView.findViewById(viewId);
                        setButtonOnClick(button, caught, docRef, (Boolean) value, context,
                                idKeyPair, keyValuePairs.get("name").toString(), month, day,
                                holder.myConstraintLayout, caughtId, notCaughtId, false);
                        break;
                    case DONATEDBUTTON:
                        final ImageButton donateButton = popView.findViewById(viewId);
                        setButtonOnClick(donateButton, donated, donatedRef, (Boolean) value, context,
                                idKeyPair, keyValuePairs.get("name").toString(), month, day,
                                holder.donatedView, donatedIcon, 0,false);
                        break;
                    case IMAGEVIEW:
                        final ImageView image = popView.findViewById(viewId);
                        Glide.with(context).load(value).into(image);
                        break;
                    case DREAMIEBUTTON:
                        final ImageButton dreamieButton = popView.findViewById(viewId);
                        setButtonOnClick(dreamieButton, donated, donatedRef, (Boolean) value, context,
                                idKeyPair, keyValuePairs.get("name").toString(), month, day,
                                holder.donatedView, dreamieIcon, 0, false);
                        break;
                    case RESIDENTBUTTON:
                        final ImageButton residentButton = popView.findViewById(viewId);
                        setButtonOnClick(residentButton, caught, docRef, (Boolean) value, context,
                                idKeyPair, keyValuePairs.get("name").toString(), month, day,
                                holder.residentView, donatedIcon, 0, true);
                        break;
                    default:
                        break;

                }
            }
            displayPopUp(popView, context);
        }

        public void fillViews(LayoutInflater mInflater, final HashMap<String, Object> keyValuePairs,
                              final Context context) {
            View popView = mInflater.inflate(mainView, null);
            // go through each id we need to fill
            for(int i = 0; i < idKeyPairs.size(); i++) {
                final IdKeyPair idKeyPair = idKeyPairs.get(i);
                // get the corresponding value for the current key
                Object value = keyValuePairs.get(idKeyPair.getKey());
                int viewId = idKeyPair.getId();
                ViewType type = idKeyPair.getType();
                // depending on the type we handle it differently
                switch (type) {
                    case TEXTVIEW:
                        TextView text = popView.findViewById(viewId);
                        text.setText((String) value);
                        break;
                    case MONTHVIEW:
                        MonthView months = popView.findViewById(viewId);
                        months.setMonths((boolean[]) value);
                        break;
                    case TIMEVIEW:
                        TimeView times = popView.findViewById(viewId);
                        times.setTimes((int[]) value);
                        break;
                    case IMAGEVIEW:
                        final ImageView image = popView.findViewById(viewId);
                        Glide.with(context).load(value).into(image);
                        break;
                    case DREAMIEBUTTON:
                    case RESIDENTBUTTON:
                        final ImageButton button = popView.findViewById(viewId);
                        button.setVisibility(View.GONE);
                        break;
                    default:
                        break;

                }
            }
            displayPopUp(popView, context);
        }

        public void displayPopUp(View popView, Context context) {
            popView.setBackground(ContextCompat.getDrawable(context, mainViewBg));
            DisplayMetrics metrics = context.getResources().getDisplayMetrics(); // used to convert px to dp
            PopupWindow popWindow = new PopupWindow(popView, metrics.widthPixels,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            if (Build.VERSION.SDK_INT >= 21) {
                popWindow.setElevation(5.0f);
            }
            popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
            popWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);
        }

        public void setButtonOnClick(ImageButton b, final Map<String, Object> docData, final DocumentReference docRef,
                                     Boolean value, final Context context, final IdKeyPair idKeyPair, final String name,
                                     final Object month, final Object day, final ImageView bg, final int icon, final int unIcon,
                                     final boolean isSpecial) {
            setDrawable(value, context, b, idKeyPair);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = updateChecked(docData, name, isSpecial, month, day, docRef,
                            context, view, idKeyPair);
                    if (isChecked) {
                        Glide.with(context).load(icon).into(bg);
                    } else {
                        Glide.with(context).clear(bg);
                    }
                }
            });
        }

        public void setButtonOnClick(ImageButton b, final Map<String, Object> docData, final DocumentReference docRef,
                                     Boolean value, final Context context, final IdKeyPair idKeyPair, final String name,
                                     final Object month, final Object day, final View bg, final int icon, final int unIcon,
                                     final boolean isSpecial) {
            setDrawable(value, context, b, idKeyPair);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = updateChecked(docData, name, isSpecial, month, day, docRef,
                            context, view, idKeyPair);
                    if (isChecked) {
                        bg.setBackgroundResource(icon);
                    } else {
                        bg.setBackgroundResource(unIcon);
                    }
                }
            });
        }

        private boolean updateChecked(Map<String, Object> docData, String name, boolean isSpecial,
                                      final Object month, final Object day, DocumentReference docRef,
                                      Context context, View b, IdKeyPair idKeyPair) {
            Map<String, Object> checked = new HashMap<>();
            boolean isChecked = !(Boolean) docData.get(name);
            docData.put(name, isChecked);
            if (isChecked && isSpecial) {
                checked.put(name, new HashMap<String, Object>() {{
                    put("month", month);
                    put("day", day);
                }});
            } else if (isChecked)
                checked.put(name, true);
            else if (isSpecial)
                checked.put(name, FieldValue.delete());
            else
                checked.put(name, false);
            docRef.update(checked);
            setDrawable(isChecked, context, (ImageButton) b, idKeyPair);
            return isChecked;
        }

        private void setDrawable(Boolean value, Context context, ImageButton b, final IdKeyPair idKeyPair) {
            if (value) {
                Drawable d = ResourcesCompat.getDrawable(
                        context.getResources(), idKeyPair.getSelectedBG(), null);
                b.setBackground(d);
            } else {
                Drawable d = ResourcesCompat.getDrawable(
                        context.getResources(), idKeyPair.getUnselectedBG(), null);
                b.setBackground(d);
            }
        }
    }

    public abstract static class RightDrawableOnTouchListener implements View.OnTouchListener {
        Drawable drawable;
        private int fuzz = 10;

        public RightDrawableOnTouchListener(TextView view) {
            super();
            final Drawable[] drawables = view.getCompoundDrawables();
            if (drawables != null && drawables.length == 4)
                this.drawable = drawables[2];
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
         */
        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
                final int x = (int) event.getX();
                final int y = (int) event.getY();
                final Rect bounds = drawable.getBounds();
                if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
                        && y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
                    return onDrawableTouch(event);
                }
            }
            return false;
        }

        public abstract boolean onDrawableTouch(final MotionEvent event);

    }
}
