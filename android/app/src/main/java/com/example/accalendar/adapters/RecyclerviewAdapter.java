package com.example.accalendar.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.accalendar.R;
import com.example.accalendar.utils.ClassUtils;
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
    private final Map<String, Object> items;
    private ArrayList<String> keys;
    private LayoutInflater mInflater;
    private final Context context;
    private final ArrayList<Boolean> isNorth;
    private DocumentReference docRef;
    private DocumentReference donatedRef;
    private Map<String, Object> caught;
    private final ClassUtils.ItemType itemType;
    private final ClassUtils.PopupHelper helper;
    private Map<String, Object> donated;
    private Map<String, Object> resident;
    private Map<String, Object> dreamie;
    private DocumentReference dreamieRef;

    public RecyclerviewAdapter(Context context, Map<String, Object> fish, ArrayList<Boolean> isNorth,
                               Map<String, Object> caught, DocumentReference docRef,
                               ClassUtils.ItemType itemType, ClassUtils.PopupHelper helper,
                               Map<String, Object> donated, DocumentReference donatedRef) {
        this.mInflater = LayoutInflater.from(context);
        this.items = fish;
        this.keys = new ArrayList<>(items.keySet());
        this.context = context;
        this.isNorth = isNorth;
        this.docRef = docRef;
        if (itemType == ClassUtils.ItemType.VILLAGERS || itemType == ClassUtils.ItemType.RESIDENT) {
            this.resident = caught;
            this.dreamie = donated;
            this.dreamieRef = donatedRef;
        } else {
            this.caught = caught;
            this.donated = donated;
            this.donatedRef = donatedRef;
        }

        this.itemType = itemType;
        this.helper = helper;
    }

    //inflates cell layout from xml
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view, parent, false);
        if (itemType == ClassUtils.ItemType.VILLAGERS || itemType == ClassUtils.ItemType.RESIDENT)
            return new ViewHolder(view, helper.getImageView(), helper.getResidentView(),
                    helper.getConstraintView(), helper.getDonatedId());
        else
            return new ViewHolder(view, helper.getImageView(),
                    helper.getConstraintView(), helper.getDonatedId());
    }

    //binds data to the ImageView to each cell
    //original post used textview, so I need to hammer this part out
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        this.keys = new ArrayList<>(items.keySet());
        final String name = keys.get(position);
        final Map<String, Object> entry = (Map<String, Object>) items.get(keys.get(position));
        String url = "";
        if(entry.containsKey("image"))
            url = (String) entry.get("image");
        //Picasso.get().setLoggingEnabled(true);
        //Picasso.get().load(url).fit().centerCrop().into(holder.myImageView);
        //Glide.with(holder.itemView.getContext()).load(url).centerCrop().into(holder.myImageView);
        Glide.with(context)
                .load(url)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .thumbnail(0.05f)
                .dontTransform()
                .centerCrop()
                .into(holder.myImageView);

        ArrayList<Map<String, Long>> months;
        int[] timeBools = null;
        boolean[] monthBools = null;
        //Only want to fill time/months if the type is fish or bug
        if (itemType == ClassUtils.ItemType.FISH || itemType == ClassUtils.ItemType.BUG) {
            timeBools = fillTimeBools((ArrayList<Map<String, Long>>) entry.get("times"));
            if (isNorth.get(0)) {
                months = ((ArrayList<Map<String, Long>>) entry.get("north"));
            } else {
                months = ((ArrayList<Map<String, Long>>) entry.get("south"));
            }
            monthBools = fillMonthBools(months);
        }
        if (caught != null) {
            if (caught.containsKey(name) && (Boolean) caught.get(name)) {
                holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, helper.getCaughtId()));
            } else {
                caught.put(name, false);
                holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, helper.getNotCaughtId()));
            }
        }

        if (donated != null) {
            if (donated.containsKey(name) && (Boolean) donated.get(name)) {
                Glide.with(context)
                        .load(helper.getDonatedIcon())
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .encodeFormat(Bitmap.CompressFormat.PNG)
                        .thumbnail(0.05f)
                        .centerCrop()
                        .into(holder.donatedView);
            } else {
                donated.put(name, false);
                Glide.with(context)
                        .load(R.drawable.transparent)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .encodeFormat(Bitmap.CompressFormat.PNG)
                        .thumbnail(0.05f)
                        .centerCrop()
                        .into(holder.donatedView);
            }
        }

        if (resident != null) {
            if (resident.containsKey(name) && (Boolean) resident.get(name)) {
                Glide.with(context)
                        .load(helper.getDonatedIcon())
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .encodeFormat(Bitmap.CompressFormat.PNG)
                        .thumbnail(0.05f)
                        .centerCrop()
                        .into(holder.residentView);
            } else {
                resident.put(name, false);
                Glide.with(context)
                        .load(R.drawable.transparent)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .encodeFormat(Bitmap.CompressFormat.PNG)
                        .thumbnail(0.05f)
                        .centerCrop()
                        .into(holder.residentView);
            }
        }

        if (dreamie != null) {
            if (dreamie.containsKey(name) && (Boolean) dreamie.get(name)) {
                Glide.with(context)
                        .load(helper.getDreamieIcon())
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .encodeFormat(Bitmap.CompressFormat.PNG)
                        .thumbnail(0.05f)
                        .centerCrop()
                        .into(holder.donatedView);
            } else {
                dreamie.put(name, false);
                Glide.with(context)
                        .load(R.drawable.transparent)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .encodeFormat(Bitmap.CompressFormat.PNG)
                        .thumbnail(0.05f)
                        .centerCrop()
                        .into(holder.donatedView);
            }
        }

        final int[] finalTimeBools = timeBools;
        final boolean[] finalMonthBools = monthBools;
        holder.myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fill popup with corresponding fish info
                Log.d(TAG, "onClick: clicked on: " + name);

                ArrayList<ClassUtils.IdKeyPair> idKeyPairs = helper.getIdKeyPairs();

                // fill values for each view we are inflating
                HashMap<String, Object> keyValuePairs = new HashMap<>();
                for (int i = 0; i < idKeyPairs.size(); i++) {
                    ClassUtils.IdKeyPair idKeyPair = idKeyPairs.get(i);
                    String key = idKeyPair.getKey();
                    if (key.equals("name"))
                        keyValuePairs.put(key, name);
                    else if (key.equals("checkbox"))
                        keyValuePairs.put(key, caught.get(name));
                    else if (key.equals("donated"))
                        keyValuePairs.put(key, donated.get(name));
                    else if (key.equals("months"))
                        keyValuePairs.put(key, finalMonthBools);
                    else if (key.equals("times"))
                        keyValuePairs.put(key, finalTimeBools);
                    else if (key.equals("price"))
                        keyValuePairs.put(key, ((Long) entry.get(key)).toString());
                    else if (key.equals("birthday"))
                        keyValuePairs.put(key, fillBirthday(entry));
                    else if (key.equals("resident"))
                        keyValuePairs.put(key, resident.get(name));
                    else if (key.equals("dreamie"))
                        keyValuePairs.put(key, dreamie.get(name));
                    else
                        keyValuePairs.put(idKeyPair.getKey(), entry.get(idKeyPair.getKey()));
                }

                // pass to the helper to handle the inflate for us
                if (itemType == ClassUtils.ItemType.VILLAGERS)
                    helper.fillViews(mInflater, keyValuePairs, resident, docRef, holder, context,
                            dreamieRef, dreamie, entry.get("month"), entry.get("day"));
                else if (itemType == ClassUtils.ItemType.RESIDENT)
                    helper.fillViews(mInflater, keyValuePairs, context);
                else
                    helper.fillViews(mInflater, keyValuePairs, caught, docRef, holder, context,
                            donatedRef, donated, null, null);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        if (keys.size() > 0)
            return keys.get(position).hashCode();
        else
            return 0;
    }

    private String fillBirthday(Map<String, Object> entry) {
        return entry.get("month") + "/" + entry.get("day");
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.residentView);
        Glide.with(context).clear(holder.donatedView);
        Glide.with(context).clear(holder.myImageView);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private boolean[] fillMonthBools(ArrayList<Map<String, Long>> months) {
        int monthStart, monthEnd;
        boolean[] monthBools = new boolean[12];
        for (int i = 0; i < 12; i++)
            monthBools[i] = false;
        for (int monthIdx = 0; monthIdx < months.size(); monthIdx++) {
            Map<String, Long> month = months.get(monthIdx);
            monthStart = month.get("start").intValue();
            monthEnd = month.get("end").intValue();
            for (int i = monthStart; i <= monthEnd; i++)
                monthBools[i - 1] = true;
        }
        return monthBools;
    }

    // Sets the times the fish is available to 1 and the rest to 0 -> used in the TimeView
    private int[] fillTimeBools(ArrayList<Map<String, Long>> times) {
        int startTime, endTime;
        int[] timeBools = new int[25];
        for (int i = 0; i < 25; i++)
            timeBools[i] = 0;
        // Iterate through each time in the array (this was to deal with the piranha lol)
        for (int timeIdx = 0; timeIdx < times.size(); timeIdx++) {
            Map<String, Long> time = times.get(timeIdx);
            startTime = time.get("start").intValue();
            endTime = time.get("end").intValue();
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

    //total number of cells
    // We have to define getItemCount since it's abstract
    @Override
    public int getItemCount() {
        return items.size();
    }

    //supposed to store and recycle view as it is scrolled off screen
    //implements ViewHolder stuff so it doesn't show up as an error
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myImageView;
        public ImageView donatedView;
        public FrameLayout myConstraintLayout;
        public ImageView residentView;

        ViewHolder(View itemView, int imageView, int residentView, int constraintView, int donatedView) {
            super(itemView);
            myImageView = itemView.findViewById(imageView);
            this.donatedView = itemView.findViewById(donatedView);
            myConstraintLayout = itemView.findViewById(constraintView);
            if (residentView != 0)
                this.residentView = itemView.findViewById(residentView);
        }

        ViewHolder(View itemView, int imageView, int constraintView, int donatedView) {
            super(itemView);
            myImageView = itemView.findViewById(imageView);
            this.donatedView = itemView.findViewById(donatedView);
            myConstraintLayout = itemView.findViewById(constraintView);
        }

        ViewHolder(View itemView, int imageView) {
            super(itemView);
            myImageView = itemView.findViewById(imageView);
        }
    }

}
