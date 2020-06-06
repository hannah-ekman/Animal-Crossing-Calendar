package com.example.accalendar.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.accalendar.R;
import com.example.accalendar.views.MonthView;
import com.example.accalendar.views.TimeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {
    private static final String TAG = "Recyclerview";
    private final Map<String, Object> fish;
    private ArrayList<String> keys;
    //populate this array with the r.drawable files
    private LayoutInflater mInflater;
    private final Context context;
    private final ArrayList<Boolean> isNorth;
    private DocumentReference docRef;
    private Map<String, Object> caught;

    public RecyclerviewAdapter(Context context, Map<String, Object> fish, ArrayList<Boolean> isNorth,
                                Map<String, Object> caught, DocumentReference docRef) {
        this.mInflater = LayoutInflater.from(context);
        this.fish = fish;
        this.context = context;
        this.isNorth = isNorth;
        this.caught = caught;
        this.docRef = docRef;
    }

    //inflates cell layout from xml
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view, parent, false);
        return new ViewHolder(view);
    }

    //binds data to the ImageView to each cell
    //original post used textview, so I need to hammer this part out
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position){
        this.keys = new ArrayList<>(fish.keySet());
        final String name = keys.get(position);
        final Map<String, Object> entry = (Map<String, Object>) fish.get(keys.get(position));
        String url = (String) entry.get("image");
        Picasso.get().setLoggingEnabled(true);
        //Picasso.get().load(url).into(holder.myImageView);
        Glide.with(holder.itemView.getContext()).load(url).into(holder.myImageView);
        ArrayList<Map<String, Long>> months;
        final int[] timeBools = fillTimeBools((ArrayList<Map<String, Long>>) entry.get("times"));
        if (isNorth.get(0)) {
            months = ((ArrayList<Map<String, Long>>) entry.get("north"));
        } else {
            months = ((ArrayList<Map<String, Long>>) entry.get("south"));
        }
        if (!caught.containsKey(name)) {
            caught.put(name, false);
            holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.missing_card));
        } else if ((Boolean) caught.get(name)) {
            holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.caught_fish));
        } else {
            holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.missing_card));
        }
        final boolean[] monthBools = fillMonthBools(months);
        holder.myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fill popup with corresponding fish info
                Log.d(TAG, "onClick: clicked on: " + name);
                View popView = mInflater.inflate(R.layout.fish_popup, null);
                TextView fishName = popView.findViewById(R.id.cardtitle);
                fishName.setText(name);
                TextView price = popView.findViewById(R.id.fish_price_value);
                price.setText(((Long) entry.get("price")).toString());
                TextView shadow = popView.findViewById(R.id.fish_shadow_value);
                shadow.setText((String) entry.get("shadow size"));
                TextView location = popView.findViewById(R.id.fish_location_value);
                location.setText((String) entry.get("location"));
                MonthView monthInfo = popView.findViewById(R.id.fish_months);
                monthInfo.setMonths(monthBools);
                TimeView timeInfo = popView.findViewById(R.id.fish_times);
                timeInfo.setTimes(timeBools);
                CheckBox checkBox = popView.findViewById(R.id.checkbox_fish);
                checkBox.setChecked((Boolean) caught.get(name));
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isChecked = ((CheckBox) view).isChecked();
                        Map<String, Object> checked = new HashMap<>();
                        checked.put(name, isChecked);
                        caught.put(name, isChecked);
                        docRef.update(checked);
                        if (isChecked) {
                            holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.caught_fish));
                        } else {
                            holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.missing_card));
                        }
                    }
                });
                popView.setBackground(ContextCompat.getDrawable(context, R.drawable.fishpopup));
                DisplayMetrics metrics = context.getResources().getDisplayMetrics(); // used to convert px to dp
                PopupWindow popWindow = new PopupWindow(popView, (int) (metrics.density*325+0.5f),
                        (int) (metrics.density*350+0.5f), true);
                if (Build.VERSION.SDK_INT >= 21) {
                    popWindow.setElevation(5.0f);
                }
                popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
                popWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private boolean[] fillMonthBools(ArrayList<Map<String, Long>> months){
        int monthStart, monthEnd;
        boolean[] monthBools = new boolean[12];
        for(int i = 0; i<12; i++)
            monthBools[i] = false;
        for(int monthIdx = 0; monthIdx < months.size(); monthIdx++) {
            Map<String, Long> month = months.get(monthIdx);
            monthStart = month.get("start").intValue();
            monthEnd = month.get("end").intValue();
            for(int i = monthStart; i <= monthEnd; i++)
                monthBools[i-1] = true;
        }
        return monthBools;
    }

    // Sets the times the fish is available to 1 and the rest to 0 -> used in the TimeView
    private int[] fillTimeBools(ArrayList<Map<String, Long>> times) {
        int startTime, endTime;
        int[] timeBools = new int[25];
        for(int i = 0; i<25; i++)
            timeBools[i] = 0;
        // Iterate through each time in the array (this was to deal with the piranha lol)
        for(int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
            Map<String, Long> time = times.get(timeIdx);
            startTime = time.get("start").intValue();
            endTime = time.get("end").intValue();
            // If startTime is before endTime -> fill all times from start to end with 1
            if(startTime <= endTime) {
                for(int i = startTime; i <= endTime; i++)
                    timeBools[i] = 1;
            } else { // start is after end -> fill all times before start and times after end with 1
                for(int i = 0; i <= endTime; i++)
                    timeBools[i] = 1;
                for(int i = startTime; i < 25; i++)
                    timeBools[i] = 1;
            }
        }
        return timeBools;
    }

    //total number of cells
    // We have to define getItemCount since it's abstract
    @Override
    public int getItemCount(){
        return fish.size();
    }

    //supposed to store and recycle view as it is scrolled off screen
    //implements ViewHolder stuff so it doesn't show up as an error
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myImageView;
        ConstraintLayout myConstraintLayout;

        ViewHolder(View itemView){
            super(itemView);
            myImageView = itemView.findViewById(R.id.fishimg);
            myConstraintLayout = itemView.findViewById(R.id.fishconstraint);
            //itemView.setOnClickListener(this);
        }
    }

}
