package com.example.accalendar;


import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accalendar.utils.DocSnapToData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Fish extends AppCompatActivity {

    RecyclerviewAdapter adapter;
    private FirebaseFirestore db;
    private Map<String, Object> fish = new HashMap<>();
    private static final String TAG = "fish";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish);
        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.fish_grid);
        int numColumns = 5;
        getFish();
        recyclerView.setLayoutManager(new GridLayoutManager(this,  numColumns));
        adapter = new RecyclerviewAdapter(this, fish);
        recyclerView.setAdapter(adapter);
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
                        fish.putAll(doc.getData()); // Need to use putAll to actually change the data vs just changing the reference
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

    /*
    Can create function to log which item was clicked for debugging purposes
    @Override
    public void onItemClick(){
        Log.i("TAG", "")
    }
     */
}
