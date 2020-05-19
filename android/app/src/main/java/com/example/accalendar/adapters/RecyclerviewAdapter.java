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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accalendar.R;
import com.example.accalendar.views.MonthView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {
    private static final String TAG = "Recyclerview";
    private final Map<String, Object> fish;
    private ArrayList<String> keys;
    //populate this array with the r.drawable files
    private LayoutInflater mInflater;
    private final Context context;
    private final ArrayList<Boolean> isNorth;

    public RecyclerviewAdapter(Context context, Map<String, Object> fish, ArrayList<Boolean> isNorth){
        this.mInflater = LayoutInflater.from(context);
        this.fish = fish;
        this.context = context;
        this.isNorth = isNorth;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
        this.keys = new ArrayList<>(fish.keySet());
        final Map<String, Object> entry = (Map<String, Object>) fish.get(keys.get(position));
        String url = (String) entry.get("image");
        Picasso.get().load(url).into(holder.myImageView);
        final int startMonth;
        final int endMonth;
        if (isNorth.get(0)) {
            startMonth = ((Map<String, Long>) entry.get("north")).get("start month").intValue();
            endMonth = ((Map<String, Long>) entry.get("north")).get("end month").intValue();
        } else {
            startMonth = ((Map<String, Long>) entry.get("south")).get("start month").intValue();
            endMonth = ((Map<String, Long>) entry.get("south")).get("end month").intValue();
        }
        holder.myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fill popup with corresponding fish info
                Log.d(TAG, "onClick: clicked on: " + keys.get(position));
                View popView = mInflater.inflate(R.layout.fish_popup, null);
                TextView fishName = popView.findViewById(R.id.cardtitle);
                fishName.setText(keys.get(position));
                TextView price = popView.findViewById(R.id.fish_price_value);
                price.setText(((Long) entry.get("price")).toString());
                TextView shadow = popView.findViewById(R.id.fish_shadow_value);
                shadow.setText(((Long) entry.get("shadow size")).toString());
                TextView location = popView.findViewById(R.id.fish_location_value);
                location.setText((String) entry.get("location"));
                MonthView monthInfo = popView.findViewById(R.id.fish_months);
                monthInfo.setStartAndEndMonths(startMonth, endMonth);
                popView.setBackground(ContextCompat.getDrawable(context, R.drawable.roundedpopup));
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                PopupWindow popWindow = new PopupWindow(popView, (int) (metrics.density*325+0.5f),
                        (int) (metrics.density*300+0.5f), true);
                if (Build.VERSION.SDK_INT >= 21) {
                    popWindow.setElevation(5.0f);
                }
                popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
                popWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);
            }
        });

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

        ViewHolder(View itemView){
            super(itemView);
            myImageView = itemView.findViewById(R.id.fishimg);
            //itemView.setOnClickListener(this);
        }
    }

}
