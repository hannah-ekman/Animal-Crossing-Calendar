package com.example.accalendar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    EditText email2, password2;
    Button signup;
    String TAG = "Signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        email2 = (EditText) findViewById(R.id.email2);
        password2 = (EditText) findViewById(R.id.password2);

        AddUser();

    }

    public void AddUser() {
        signup = (Button) findViewById(R.id.signup_button);
        //Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/thicc.ttf");
        //signup.setTypeface(typeface);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.createUserWithEmailAndPassword(email2.getText().toString(), password2.getText().toString())
                        .addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    addUserToFirebase(user);
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(signup.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });
    }

    private void addUserToFirebase(final FirebaseUser user) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("villagers", new HashMap<>());
        userData.put("bugs", new HashMap<>());
        userData.put("fish", new HashMap<>());
        userData.put("fossils", new HashMap<>());
        userData.put("furniture", new HashMap<>());
        userData.put("recipes", new HashMap<>());
        userData.put("gallery", new HashMap<>());
        userData.put("isTimeTravel", false);
        userData.put("dateOffset", 0);

        docRef.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent homepage_redirect = new Intent(signup.this, NewProfile.class);
            startActivity(homepage_redirect);
        } else {

        }
    }

    //just fixes the back button exiting the app
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(signup.this, MainActivity.class);
        startActivity(intent);
    }

}
