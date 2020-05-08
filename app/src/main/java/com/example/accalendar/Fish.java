package com.example.accalendar;


import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Fish extends AppCompatActivity {

    RecyclerviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish);

        //data to populate RecyclerView here needs to be changed so we use images
        ArrayList<Integer> data = new ArrayList<>();
        data.add(R.drawable.fishicon);
        data.add(R.drawable.fossiliconpng);

        RecyclerView recyclerView = findViewById(R.id.fish_grid);
        int numColumns = 5;
        recyclerView.setLayoutManager(new GridLayoutManager(this,  numColumns));
        adapter = new RecyclerviewAdapter(this, data);
        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    /*
    Can create function to log which item was clicked for debugging purposes
    @Override
    public void onItemClick(){
        Log.i("TAG", "")
    }
     */
}
