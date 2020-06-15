package com.example.accalendar;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.example.accalendar.utils.DocSnapToData;
import com.example.accalendar.views.FilterView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.rpc.Help;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.accalendar.adapters.ExpandableListAdapter;

public class Fish extends AppCompatActivity {

    RecyclerviewAdapter adapter;
    ExpandableListAdapter listAdapter;
    private FirebaseFirestore db;
    private Map<String, Object> fish = new LinkedHashMap<>();
    private ArrayList<Boolean> isNorth = new ArrayList<>();
    private Map<String, Object> caught = new HashMap<>();
    private static final String TAG = "fish";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ExpandableListView list_view;
    List<String> filterParent;
    HashMap<String, HashMap<String, Boolean>> filterChild;
    HashMap<String, Boolean> sort;
    ArrayList<Button> sortButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish);
        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.fish_grid);
        int numColumns = 4;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        isNorth.add(false);
        getFish();
        getUserInfo();
        recyclerView.setLayoutManager(new GridLayoutManager(this,  numColumns));
        DocumentReference fishRef = db.collection("users").document(user.getUid())
                .collection("fish").document("caught");
        adapter = new RecyclerviewAdapter(this, fish, isNorth, caught, fishRef);
        recyclerView.setAdapter(adapter);

        list_view = (ExpandableListView) findViewById(R.id.filter);
        createListData();
        createSortButtons();

        // Listview Group click listener
        list_view.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // TODO GroupClickListener work
                return false;
            }
        });

        // Listview Group expanded listener
        list_view.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams) list_view.getLayoutParams();
                View child = listAdapter.getChild(groupPosition);
                // width to use to measure with (so we don't have clipping)
                int desiredWidth = View.MeasureSpec.makeMeasureSpec(list_view.getWidth(),
                        View.MeasureSpec.EXACTLY);
                // measure the view since might not be drawn yet
                child.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                // add the height to the view's height to expand it
                param.height = (list_view.getHeight() + child.getMeasuredHeight());
                list_view.setLayoutParams(param);
                list_view.refreshDrawableState();
                list_view.refreshDrawableState();
            }
        });

        // Listview Group collasped listener
        list_view.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams) list_view.getLayoutParams();
                View child = listAdapter.getChild(groupPosition);
                int desiredWidth = View.MeasureSpec.makeMeasureSpec(list_view.getWidth(),
                        View.MeasureSpec.EXACTLY);
                child.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                // subtract the height form the view's height to collapse it
                param.height = (list_view.getHeight() - child.getMeasuredHeight());
                list_view.setLayoutParams(param);
                list_view.refreshDrawableState();
                list_view.refreshDrawableState();
            }
        });

        // Listview on child click listener
        list_view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                return false;
            }
        });
    }

    private void setExpandableHeight(ExpandableListAdapter mAdapter, ExpandableListView mExpandableListView, NestedScrollView scrollView) {
        int mInitialHeight = 0;
        for (Integer i = 0; i < mAdapter.getGroupCount(); i++) {
            View groupItem = mAdapter.getGroupView(i, false, null, mExpandableListView);
            groupItem.measure(mExpandableListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            mInitialHeight += groupItem.getMeasuredHeight()+3;
        }
        ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams) mExpandableListView.getLayoutParams();
        param.height = mInitialHeight;
        mExpandableListView.setLayoutParams(param);
        mExpandableListView.refreshDrawableState();
        scrollView.refreshDrawableState();
    }

    private void createSortButtons() {
        sort = new LinkedHashMap<>();
        sort.put("Default", true);
        sort.put("Name", false);
        sort.put("Price", false);
        FlexboxLayout listChild = findViewById(R.id.sort);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                7,
                getResources().getDisplayMetrics()
        );

        params.setMargins(px, px, px, px);
        for (Map.Entry<String, Boolean> filter : sort.entrySet()) {
            final String key = filter.getKey();
            Button button = new Button(this);
            button.setLayoutParams(params);
            button.setMinHeight(0);
            button.setMinimumHeight(0);
            px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10,
                    this.getResources().getDisplayMetrics()
            );
            button.setPadding(px, px, px, px);
            Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/josefin_sans_semibold.ttf");
            button.setTypeface(typeface);
            button.setTextSize(15);
            button.setText(key);
            button.setTextColor(Color.WHITE);
            boolean tf = sort.get(key);
            if (tf)
                button.setBackground(getResources().getDrawable(R.drawable.fish_filter_on_button));
            else
                button.setBackground(getResources().getDrawable(R.drawable.fish_filter_off_button));
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean tf = !sort.get(key);
                    System.out.println(sort);
                    if (tf) {
                        // remove other button selections
                        int i = 0;
                        for (Map.Entry<String, Boolean> s : sort.entrySet()) {
                            Button b = sortButtons.get(i);
                            b.setBackground(getResources().getDrawable(R.drawable.fish_filter_off_button));
                            sort.put(s.getKey(), false);
                            i++;
                        }
                        // then set this button to be selected
                        DocSnapToData.sortHashMapByIndex(fish, key);
                        adapter.notifyDataSetChanged();
                        sort.put(key, true);
                        v.setBackground(getResources().getDrawable(R.drawable.fish_filter_on_button));
                        // sort by the key here
                    }
                }
            });
            listChild.addView(button);
            sortButtons.add(button);
        }
    }

    private void createListData() {
        filterParent = new ArrayList<>();
        filterChild = new HashMap<>();

        // Adding child data
        filterParent.add("Locations");
        filterParent.add("Times");
        filterParent.add("Months");
        filterParent.add("Shadow Sizes");

        // Adding child data List one
        HashMap<String, Boolean> locations = new LinkedHashMap<>();
        locations.put("Pier", false);
        locations.put("Pond", false);
        locations.put("River", false);
        locations.put("River (Clifftop)", false);
        locations.put("River (Clifftop) Pond", false);
        locations.put("River (Mouth)", false);
        locations.put("Sea", false);
        locations.put("Sea (Raining)", false);


        // Adding child data List two
        HashMap<String, Boolean> times = new LinkedHashMap<>();
        times.put("All day", false);
        times.put("4 AM - 9 PM", false);
        times.put("9 PM - 4 AM", false);

        // Adding child data List three
        HashMap<String, Boolean> months = new LinkedHashMap<>();
        months.put("JAN", false);
        months.put("FEB", false);
        months.put("MAR", false);
        months.put("APR", false);
        months.put("MAY", false);
        months.put("JUN", false);
        months.put("JUL", false);
        months.put("AUG", false);
        months.put("SEP", false);
        months.put("OCT", false);
        months.put("NOV", false);
        months.put("DEC", false);

        // Adding child data for shadow size
        HashMap<String, Boolean> shadow = new LinkedHashMap<>();
        shadow.put("1", false);
        shadow.put("2", false);
        shadow.put("3", false);
        shadow.put("4", false);
        shadow.put("5", false);
        shadow.put("6", false);
        shadow.put("6 (Fin)", false);
        shadow.put("Narrow", false);

        filterChild.put(filterParent.get(0), locations); // Header, Child data
        filterChild.put(filterParent.get(1), times); // Header, Child data
        filterChild.put(filterParent.get(2), months); // Header, Child data
        filterChild.put(filterParent.get(3), shadow);

        listAdapter = new ExpandableListAdapter(getApplicationContext(), filterParent, filterChild);
        list_view.setAdapter(listAdapter);
        NestedScrollView scroll = findViewById(R.id.fishScroll);
        setExpandableHeight(listAdapter, list_view, scroll); // set the height of the list view
    }

    private void getFish() {
        DocumentReference docRef = db.collection("trackables").document("fish");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        fish.putAll(doc.getData());
                        DocSnapToData.sortHashMapByIndex(fish, "Default");
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getUserInfo() {
        final DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        isNorth.set(0, ((Boolean) doc.get("isNorthern")));
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        final DocumentReference fishRef = db.collection("users").document(user.getUid())
                .collection("fish").document("caught");
        fishRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        caught.putAll(doc.getData());
                        adapter.notifyDataSetChanged();
                    } else {
                        fishRef.set(new HashMap<>());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    /*
    Can create function to log which item was clicked for debugging purposes
    @Override
    public void onItemClick(){
        Log.i("TAG", "")
    }
     */
}
