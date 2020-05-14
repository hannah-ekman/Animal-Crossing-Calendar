package com.example.accalendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Boolean isNorth;
    Button next;
    EditText username;
    EditText island_name;
    Switch hemisphere_switch;
    String userid;
    String TAG = "newProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();

        username = findViewById(R.id.edit_name);
        island_name = findViewById(R.id.edit_island);
        hemisphere_switch = findViewById(R.id.hemisphere_switch);
        next = (Button) findViewById(R.id.nextbutton);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next(v);
            }
        });
        //need to findviewbyid for layout if we want to add error checking
        new_profile();
    }

    public void new_profile(){
        hemisphere_switch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            isNorth = true;
                        } else {
                            isNorth = false;
                        }
                    }
                });
    }

    public void next(View view){
        String user_name = username.getText().toString();
        String islandName = island_name.getText().toString();

        db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user_name);
        userData.put("island name", islandName);
        userData.put("isNorthern", isNorth);

        // Add a new document into the events collection
        db.collection("users").document(userid)
                .update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        Intent mainpage_redirect = new Intent(NewProfile.this, CalendarActivity.class);
        startActivity(mainpage_redirect);
    }

    @Override
    public void onBackPressed()
    {

        Intent intent=new Intent(NewProfile.this, signup.class);
        startActivity(intent);
    }
}
