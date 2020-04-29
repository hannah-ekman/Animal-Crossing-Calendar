package com.example.accalendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Boolean isNorth;
    Button next;
    EditText username;
    EditText island_name;
    Switch hemisphere_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);
        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.edit_name);
        island_name = findViewById(R.id.edit_island);
        hemisphere_switch = findViewById(R.id.hemisphere_switch);
        next = (Button) findViewById(R.id.nextbutton);
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
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        Intent intent = getIntent();
    }
}
