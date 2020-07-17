package com.example.accalendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.accalendar.adapters.ExpandableListAdapter;
import com.example.accalendar.adapters.ListAdapter;
import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.example.accalendar.utils.ClassUtils;
import com.example.accalendar.utils.DocSnapToData;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseFirestore db;
    private static final String TAG = "profile";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Map<String, Object> userData = new HashMap<>();
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isEdit = false;
    private Button birthday, hemisphere;
    private ImageButton fruit, cancel;
    private String fruitName, profileUrl;
    private boolean isNorth = false;
    ArrayList<View> isNorthButtons, fruitButtons;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Uri profilePath;
    private ImageView profilePic;
    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    private static final int GALLERY = 1;
    private DocumentReference userRef;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        profilePic = findViewById(R.id.profilePic);
        final View.OnClickListener profileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateIconOptions();
            }
        };

        getUserInfo();
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final EditText name = findViewById(R.id.name);
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.println(hasFocus);
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        final EditText island = findViewById(R.id.islandName);
        island.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.println(hasFocus);
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        name.setInputType(InputType.TYPE_NULL);
        island.setInputType(InputType.TYPE_NULL);

        birthday = findViewById(R.id.birthday);
        final View.OnClickListener birthdayClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    dialog = new DatePickerDialog(Profile.this,
                            android.R.style.Theme_Holo_Light_Dialog, mDateSetListener,
                            year, month, day);
                } else {
                    dialog = new DatePickerDialog(Profile.this, mDateSetListener,
                            year, month, day);
                }
                dialog.getDatePicker().findViewById(getResources()
                        .getIdentifier("year","id","android")).setVisibility(View.GONE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        };

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                birthday.setText((month+1)+"/"+dayOfMonth);
            }
        };

        hemisphere = findViewById(R.id.hemisphere);
        final View.OnClickListener hemiClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateNorthPickList();
            }
        };

        fruit = findViewById(R.id.fruit);
        final View.OnClickListener fruitClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateFruitPickList();
            }
        };

        final ImageButton editButton = findViewById(R.id.imageButton);
        cancel = findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
                editButton.setImageResource(android.R.drawable.ic_menu_edit);
                name.setInputType(InputType.TYPE_NULL);
                island.setInputType(InputType.TYPE_NULL);
                birthday.setOnClickListener(null);
                hemisphere.setOnClickListener(null);
                fruit.setOnClickListener(null);
                profilePic.setOnClickListener(null);
                isEdit = !isEdit;
                cancel.setVisibility(View.INVISIBLE);
            }
        });

        editButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    uploadImage();
                    updateInformation();
                    editButton.setImageResource(android.R.drawable.ic_menu_edit);
                    name.setInputType(InputType.TYPE_NULL);
                    island.setInputType(InputType.TYPE_NULL);
                    birthday.setOnClickListener(null);
                    hemisphere.setOnClickListener(null);
                    fruit.setOnClickListener(null);
                    profilePic.setOnClickListener(null);
                    isEdit = !isEdit;
                    cancel.setVisibility(View.INVISIBLE);
                } else {
                    editButton.setImageResource(android.R.drawable.checkbox_on_background);
                    name.setInputType(InputType.TYPE_CLASS_TEXT);
                    island.setInputType(InputType.TYPE_CLASS_TEXT);
                    birthday.setOnClickListener(birthdayClick);
                    hemisphere.setOnClickListener(hemiClick);
                    fruit.setOnClickListener(fruitClick);
                    profilePic.setOnClickListener(profileClick);
                    isEdit = !isEdit;
                    cancel.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateInformation() {
        final String nameString, islandString, birthdayString;
        EditText name = findViewById(R.id.name);
        EditText island = findViewById(R.id.islandName);
        Button birthday = findViewById(R.id.birthday);
        nameString = name.getText().toString();
        birthdayString = birthday.getText().toString();
        islandString = island.getText().toString();
        userRef.update(new HashMap<String, Object>() {{
            put("username", nameString);
            put("island name", islandString);
            put("birthday", birthdayString);
            put("isNorthern", isNorth);
            put("fruit", fruitName);
        }});
        userData.put("username", nameString);
        userData.put("island name", islandString);
        userData.put("birthday", birthdayString);
        userData.put("isNorthern", isNorth);
        userData.put("fruit", fruitName);
    }

    private void uploadImage() {
        if (profilePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Saving...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("users/"+ user.getUid()+"/profile");
            ref.putFile(profilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(Profile.this, "Saved", Toast.LENGTH_SHORT).show();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "onSuccess: uri= "+ uri.toString());
                                    // update new url into firebase
                                    profileUrl = uri.toString();
                                    userRef.update(new HashMap<String, Object>() {{
                                        put("profilePic", profileUrl);
                                    }});
                                    userData.put("profilePic", profileUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Profile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            profilePath = null;
                            updateProfile();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Saving "+(int)progress+"%");
                        }
                    });
        }
        profilePath = null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY && resultCode != 0) {
            Uri mImageUri = data.getData();
            profilePath = data.getData();
            try {
                Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                if (getOrientation(getApplicationContext(), mImageUri) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getApplicationContext(), mImageUri));
                    if (rotateImage != null)
                        rotateImage.recycle();
                    rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
                    profilePic.setImageBitmap(rotateImage);
                } else
                    profilePic.setImageBitmap(Image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getOrientation(Context context, Uri photoUri) {
        Cursor cursor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            cursor = context.getContentResolver().query(photoUri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);
        } else {
            return 0;
        }

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    private void inflateIconOptions() {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popView = layoutInflater.inflate(R.layout.picker, null);
        ListView list = (ListView) popView.findViewById(R.id.pickList);

        final PopupWindow popWindow = new PopupWindow(popView, profilePic.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        ArrayList<View> buttons = new ArrayList<>();
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf");
        Button upload = new Button(this);
        upload.setBackgroundResource(R.drawable.fishclear);
        upload.setText("Upload");
        upload.setTypeface(typeface);
        upload.setTextColor(Color.WHITE);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Image != null  && !Image.isRecycled() )
                    Image.recycle();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                popWindow.dismiss();
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);
            }
        });
        buttons.add(upload);
        list.setAdapter(new ListAdapter(this, buttons));

        Button remove = new Button(this);
        remove.setText("Remove");
        remove.setBackgroundResource(R.drawable.fishclear);
        remove.setTypeface(typeface);
        remove.setTextColor(Color.WHITE);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userData.put("profilePic", "");
                userRef.update(new HashMap<String, Object>() {{
                    put("profilePic", "");
                }});
                profilePic.setImageResource(R.mipmap.logo);
                profilePath = null;
                profileUrl = "";
                popWindow.dismiss();
            }
        });
        buttons.add(remove);

        popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);

        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );

        popWindow.showAsDropDown(profilePic, 0, px);
    }

    private void inflateNorthPickList() {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popView = layoutInflater.inflate(R.layout.picker, null);
        ListView list = (ListView) popView.findViewById(R.id.pickList);

        if (isNorthButtons == null) {
            isNorthButtons = new ArrayList<>();
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf");
            String[] vals = {"North", "South"};
            for(int i = 0; i<vals.length; i++) {
                Button b = new Button(this);

                if (isNorth && vals[i].equals("North") || !isNorth && vals[i].equals("South")) {
                    b.setBackgroundResource(R.drawable.fish_filter_on_button);
                } else {
                    b.setBackgroundResource(R.drawable.fish_filter_off_button);
                }

                b.setText(vals[i]);
                b.setTypeface(typeface);
                b.setTextColor(Color.WHITE);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button clicked = (Button) v;
                        boolean clickNorth = clicked.getText().equals("North");
                        isNorth = clickNorth;
                        hemisphere.setText(clicked.getText());
                        for(int i = 0; i<isNorthButtons.size(); i++) {
                            Button b = (Button) isNorthButtons.get(i);
                            if (isNorth && b.getText().equals("North") || !isNorth && b.getText().equals("South")) {
                                b.setBackgroundResource(R.drawable.fish_filter_on_button);
                            } else {
                                b.setBackgroundResource(R.drawable.fish_filter_off_button);
                            }
                        }
                    }
                });
                isNorthButtons.add(b);
            }
        }
        list.setAdapter(new ListAdapter(this, isNorthButtons));
        PopupWindow popWindow = new PopupWindow(popView, hemisphere.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);

        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );

        popWindow.showAsDropDown(hemisphere, 0, px);
    }

    private void inflateFruitPickList() {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popView = layoutInflater.inflate(R.layout.picker, null);
        ListView list = (ListView) popView.findViewById(R.id.pickList);

        if (fruitButtons == null) {
            fruitButtons = new ArrayList<>();
            String[] vals = {"apple", "cherry", "orange", "peach", "pear"};
            for(int i = 0; i<vals.length; i++) {
                ImageButton b = new ImageButton(this);
                System.out.println(vals[i]+" "+fruitName);

                if (vals[i].equals(fruitName)) {
                    b.setBackgroundResource(R.drawable.fish_filter_on_button);
                } else {
                    b.setBackgroundResource(0);
                }

                setFruit(b, vals[i]);
                b.setTag(vals[i]);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageButton clicked = (ImageButton) v;
                        setFruit(fruit, clicked.getTag().toString());
                        fruitName = clicked.getTag().toString();
                        for(int i = 0; i<fruitButtons.size(); i++) {
                            ImageButton b = (ImageButton) fruitButtons.get(i);
                            b.setBackgroundResource(0);
                        }
                        clicked.setBackgroundResource(R.drawable.fish_filter_on_button);
                    }
                });
                fruitButtons.add(b);
            }
        }
        list.setAdapter(new ListAdapter(this, fruitButtons));
        PopupWindow popWindow = new PopupWindow(popView, hemisphere.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);

        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );

        popWindow.showAsDropDown(fruit, -hemisphere.getWidth()+fruit.getWidth(), px);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Fish.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    // mark the edittext view as unfocused if it was focused and the user tapped outside of it
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    Log.d("focus", "touchevent");
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void updateProfile() {
        EditText name = findViewById(R.id.name);
        Object username = userData.get("username");
        if (username != null)
            name.setText(username.toString());
        else
            name.setText("Name");
        EditText island = findViewById(R.id.islandName);
        Object islandName = userData.get("island name");
        if (islandName != null)
            island.setText(islandName.toString());
        else
            island.setText("Island Name");
        ImageButton fruit = findViewById(R.id.fruit);
        Object fruitName = userData.get("fruit");
        if (fruitName != null) {
            this.fruitName = fruitName.toString();
            setFruit(fruit, fruitName.toString());
        } else {
            setFruit(fruit, "peach");
        }
        Button birthday = findViewById(R.id.birthday);
        Object birthdayDate = userData.get("birthday");
        if (birthdayDate != null)
            birthday.setText(birthdayDate.toString());
        else
            birthday.setText("1/1");
        Button hemisphere = findViewById(R.id.hemisphere);
        Object isNorth = userData.get("isNorthern");
        if (isNorth != null) {
            this.isNorth = (boolean) isNorth;
            if ((boolean) isNorth)
                hemisphere.setText("North");
            else
                hemisphere.setText("South");
        } else {
            hemisphere.setText("North");
        }
        Object profileUrl = userData.get("profilePic");
        if(profileUrl != null && profileUrl != "") {
            this.profileUrl = profileUrl.toString();
            Glide.with(this).load(this.profileUrl).into(profilePic);
        } else {
            profilePic.setImageResource(R.mipmap.logo);
        }
    }

    private void setFruit(ImageButton fruit, String fruitName) {
        if (fruitName.equals("apple")) {
            fruit.setImageResource(R.drawable.ic_apple);
        } else if (fruitName.equals("cherry")) {
            fruit.setImageResource(R.drawable.ic_cherry);
        } else if (fruitName.equals("orange")) {
            fruit.setImageResource(R.drawable.ic_orange);
        } else if (fruitName.equals("peach")) {
            fruit.setImageResource(R.drawable.ic_peach);
        } else if (fruitName.equals("pear")) {
            fruit.setImageResource(R.drawable.ic_pear);
        }
    }

    private void getUserInfo() {
        userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        userData.putAll(doc.getData());
                        updateProfile();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bug) {
            Intent intent = new Intent(this, Bug.class);
            startActivity(intent);
        } else if (id == R.id.nav_fish) {
            Intent intent = new Intent(this, Fish.class);
            startActivity(intent);
        } else if (id == R.id.nav_fossil) {

        } else if (id == R.id.nav_signout) {
            signOut();
        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        System.out.println("singing out");
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.w(TAG, "Logged out");
                        Intent intent = new Intent(Profile.this,
                                MainActivity.class);
                        startActivity(intent);
                        FirebaseAuth.getInstance().signOut();
                    }
                });
    }
}
