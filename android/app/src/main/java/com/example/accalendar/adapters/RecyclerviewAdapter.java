package com.example.accalendar.adapters;

import android.app.Activity;
import android.content.ClipData;
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
import com.example.accalendar.utils.Art;
import com.example.accalendar.utils.Bug;
import com.example.accalendar.utils.ClassUtils;
import com.example.accalendar.utils.Fish;
import com.example.accalendar.utils.Fossil;
import com.example.accalendar.utils.Villager;
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
    private final ArrayList<Object> items;
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

    public RecyclerviewAdapter(Context context, ArrayList<Object> items, ArrayList<Boolean> isNorth,
                               Map<String, Object> caught, DocumentReference docRef,
                               ClassUtils.ItemType itemType, ClassUtils.PopupHelper helper,
                               Map<String, Object> donated, DocumentReference donatedRef) {
        this.mInflater = LayoutInflater.from(context);
        this.items = items;
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
        final ClassUtils.Trackable t = (ClassUtils.Trackable) items.get(position);
        glide(t.image, holder.myImageView);

        if (caught != null) {
            if (caught.containsKey(t.name) && (Boolean) caught.get(t.name)) {
                holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, helper.getCaughtId()));
            } else {
                caught.put(t.name, false);
                holder.myConstraintLayout.setBackground(ContextCompat.getDrawable(context, helper.getNotCaughtId()));
            }
        }

        if (donated != null) {
            if (donated.containsKey(t.name) && (Boolean) donated.get(t.name)) {
                glide(helper.getDonatedIcon(), holder.donatedView);
            } else {
                donated.put(t.name, false);
                glide(R.drawable.transparent, holder.donatedView);
            }
        }

        if (resident != null) {
            if (resident.containsKey(t.name) && (Boolean) resident.get(t.name)) {
                glide(helper.getDonatedIcon(), holder.residentView);
            } else {
                resident.put(t.name, false);
                glide(R.drawable.transparent, holder.residentView);
            }
        }

        if (dreamie != null) {
            if (dreamie.containsKey(t.name) && (Boolean) dreamie.get(t.name)) {
                glide(helper.getDreamieIcon(), holder.donatedView);
            } else {
                dreamie.put(t.name, false);
                glide(R.drawable.transparent, holder.donatedView);
            }
        }
        holder.myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fill popup with corresponding fish info
                Log.d(TAG, "onClick: clicked on: " + t.name);

                // fill values for each view we are inflating
                HashMap<String, Object> keyValuePairs = new HashMap<>();

                if (t instanceof ClassUtils.Discoverable) {
                    keyValuePairs.put("checkbox", caught.get(t.name));
                    keyValuePairs.put("donated", donated.get(t.name));
                    switch (itemType){
                        case ART:
                            ((Art) t).fillKeyValues(keyValuePairs);
                            break;
                        case BUG:
                            ((Bug) t).fillKeyValues(keyValuePairs, isNorth.get(0));
                            break;
                        case FISH:
                            ((Fish) t).fillKeyValues(keyValuePairs, isNorth.get(0));
                            break;
                        case FOSSIL:
                            ((Fossil) t).fillKeyValues(keyValuePairs);
                            break;
                        default:
                            break;
                    }
                }

                if (t instanceof Villager) {
                    keyValuePairs.put("resident", resident.get(t.name));
                    keyValuePairs.put("dreamie", dreamie.get(t.name));
                    ((Villager) t).fillKeyValues(keyValuePairs);
                }

                // pass to the helper to handle the inflate for us
                if (itemType == ClassUtils.ItemType.VILLAGERS) {
                    Villager v = (Villager) t;
                    helper.fillViews(mInflater, keyValuePairs, resident, docRef, holder, context,
                            dreamieRef, dreamie, v.birthday.month , v.birthday.day);
                } else if (itemType == ClassUtils.ItemType.RESIDENT)
                    helper.fillViews(mInflater, keyValuePairs, context);
                else
                    helper.fillViews(mInflater, keyValuePairs, caught, docRef, holder, context,
                            donatedRef, donated, null, null);
            }
        });
    }

    private void glide(int d, ImageView v) {
        Glide.with(context)
                .load(d)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .thumbnail(0.5f)
                .centerCrop()
                .into(v);
    }

    private void glide(String s, ImageView v) {
        Glide.with(context)
                .load(s)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .thumbnail(0.05f)
                .centerCrop()
                .into(v);
    }

    @Override
    public long getItemId(int position) {
        if (items.size() > 0)
            return items.get(position).hashCode();
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.residentView != null)
            Glide.with(context).clear(holder.residentView);
        if (holder.donatedView != null)
            Glide.with(context).clear(holder.donatedView);
        if (holder.myImageView != null)
            Glide.with(context).clear(holder.myImageView);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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
