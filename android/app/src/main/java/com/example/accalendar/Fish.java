package com.example.accalendar;


import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.example.accalendar.utils.DocSnapToData;
import com.example.accalendar.views.FilterView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Fish extends AppCompatActivity {

    RecyclerviewAdapter adapter;
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
                // TODO GroupExpandListener work
            }
        });

        // Listview Group collasped listener
        list_view.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                // TODO GroupCollapseListener work
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

    private void createListData() {
        filterParent = new ArrayList<>();
        filterChild = new HashMap<>();

        // Adding child data
        filterParent.add("Location");
        filterParent.add("Time");
        filterParent.add("Month");

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

        filterChild.put(filterParent.get(0), locations); // Header, Child data
        filterChild.put(filterParent.get(1), times); // Header, Child data
        filterChild.put(filterParent.get(2), months); // Header, Child data

        ExpandableListAdapter listAdapter = new com.example.accalendar.adapters.ExpandableListAdapter(getApplicationContext(), filterParent, filterChild);
        list_view.setAdapter(listAdapter);
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
                        HashMap<String, Object> sorted = DocSnapToData.sortHashMapByIndex(doc.getData());
                        fish.putAll(sorted); // Need to use putAll to actually change the data vs just changing the reference
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
