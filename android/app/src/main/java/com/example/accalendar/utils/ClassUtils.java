package com.example.accalendar.utils;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
        TEXTVIEW, MONTHVIEW, TIMEVIEW, CHECKBOX
    }

    // holds the id of the view, the key for the view (used to match to the value of the view), and the type of view
    public static class IdKeyPair {
        final int id;
        final String key;
        final ViewType type;
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

        public PopupHelper(ArrayList<IdKeyPair> idKeyPairs, int mainView, int caughtId, int notCaughtId,
                           int mainViewBg, int imageView, int constraintView) {
            this.idKeyPairs = idKeyPairs;
            this.mainView = mainView;
            this.caughtId = caughtId;
            this.notCaughtId = notCaughtId;
            this.mainViewBg = mainViewBg;
            this.imageView = imageView;
            this.constraintView = constraintView;
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

        // inflates the popup and fills the views
        public void fillViews(LayoutInflater mInflater, final HashMap<String, Object> keyValuePairs,
                              final Map<String, Object> caught, final DocumentReference docRef,
                              final RecyclerviewAdapter.ViewHolder holder, final Context context) {
            View popView = mInflater.inflate(mainView, null);
            // go through each id we need to fill
            for(int i = 0; i < idKeyPairs.size(); i++) {
                IdKeyPair idKeyPair = idKeyPairs.get(i);
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
                    case CHECKBOX:
                        CheckBox checkBox = popView.findViewById(viewId);
                        checkBox.setChecked((Boolean) value);
                        checkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String name = (String) keyValuePairs.get("name");
                                boolean isChecked = ((CheckBox) view).isChecked();
                                Map<String, Object> checked = new HashMap<>();
                                checked.put(name, isChecked);
                                caught.put(name, isChecked);
                                docRef.update(checked);
                                if (isChecked) {
                                    holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, caughtId));
                                } else {
                                    holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, notCaughtId));
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
            PopupWindow popWindow = new PopupWindow(popView, (int) (metrics.density*325+0.5f),
                    (int) (metrics.density*350+0.5f), true);
            if (Build.VERSION.SDK_INT >= 21) {
                popWindow.setElevation(5.0f);
            }
            popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
            popWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);
        }
    }
}
