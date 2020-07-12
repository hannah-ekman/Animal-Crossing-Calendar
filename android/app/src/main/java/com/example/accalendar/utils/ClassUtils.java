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
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.accalendar.R;
import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.example.accalendar.views.MonthView;
import com.example.accalendar.views.TimeView;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public enum ItemType {
        FISH, BUG, FOSSIL, ART
    }

    public enum ViewType {
        TEXTVIEW, MONTHVIEW, TIMEVIEW, DONATEDBUTTON, CAUGHTBUTTON
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

    // handles inflating the popup and contains other useful info for the recyclerview
    // keeps all the ids of each view (R.id.blank) in a layout to fill in the popup later
    public static class PopupHelper {
        final ArrayList<IdKeyPair> idKeyPairs;
        final int mainView;
        final int caughtId;
        final int notCaughtId;
        final int mainViewBg;
        final int imageView;
        final int constraintView;
        int donatedId;
        int donatedIcon;

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

        public ArrayList<IdKeyPair> getIdKeyPairs() {
            return idKeyPairs;
        }

        public int getImageView() {
            return imageView;
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
                              final DocumentReference donatedRef, final Map<String, Object> donated) {
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
                        MonthView month = popView.findViewById(viewId);
                        month.setMonths((boolean[]) value);
                        break;
                    case TIMEVIEW:
                        TimeView times = popView.findViewById(viewId);
                        times.setTimes((int[]) value);
                        break;
                    case CAUGHTBUTTON:
                        final ImageButton button = popView.findViewById(viewId);
                        if ((Boolean) value) {
                            Drawable d = ResourcesCompat.getDrawable(
                                    context.getResources(), idKeyPair.getSelectedBG(), null);
                            button.setBackground(d);
                        } else {
                            Drawable d = ResourcesCompat.getDrawable(
                                    context.getResources(), idKeyPair.getUnselectedBG(), null);
                            button.setBackground(d);
                        }
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String name = (String) keyValuePairs.get("name");
                                Map<String, Object> checked = new HashMap<>();
                                boolean isChecked = !(Boolean) caught.get(name);
                                caught.put(name, isChecked);
                                checked.put(name, isChecked);
                                docRef.update(checked);
                                if (isChecked) {
                                    holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, caughtId));
                                    Drawable d = ResourcesCompat.getDrawable(
                                            context.getResources(), idKeyPair.getSelectedBG(), null);
                                    view.setBackground(d);
                                } else {
                                    holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, notCaughtId));
                                    Drawable d = ResourcesCompat.getDrawable(
                                            context.getResources(), idKeyPair.getUnselectedBG(), null);
                                    view.setBackground(d);
                                }
                            }
                        });
                        break;
                    case DONATEDBUTTON:
                        final ImageButton donateButton = popView.findViewById(viewId);
                        if ((Boolean) value) {
                            Drawable d = ResourcesCompat.getDrawable(
                                    context.getResources(), idKeyPair.getSelectedBG(), null);
                            donateButton.setBackground(d);
                        } else {
                            Drawable d = ResourcesCompat.getDrawable(
                                    context.getResources(), idKeyPair.getUnselectedBG(), null);
                            donateButton.setBackground(d);
                        }
                        donateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String name = (String) keyValuePairs.get("name");
                                Map<String, Object> checked = new HashMap<>();
                                boolean isChecked = !(Boolean) donated.get(name);
                                donated.put(name, isChecked);
                                checked.put(name, isChecked);
                                donatedRef.update(checked);
                                if (isChecked) {
                                    holder.donatedView.setImageResource(donatedIcon);
                                    Drawable d = ResourcesCompat.getDrawable(
                                            context.getResources(), idKeyPair.getSelectedBG(), null);
                                    view.setBackground(d);
                                } else {
                                    holder.donatedView.setImageResource(0);
                                    Drawable d = ResourcesCompat.getDrawable(
                                            context.getResources(), idKeyPair.getUnselectedBG(), null);
                                    view.setBackground(d);
                                }
                            }
                        });
                        break;
                    default:
                        break;

                }
            }
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
