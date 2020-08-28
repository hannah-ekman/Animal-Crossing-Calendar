package com.example.accalendar;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.accalendar.utils.ClassUtils;
import com.example.accalendar.utils.Villager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.accalendar.adapters.ExpandableListAdapter;
import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Villagers extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerviewAdapter adapter;
    ExpandableListAdapter listAdapter;
    private FirebaseFirestore db;
    private ArrayList<Object> villagers = new ArrayList<>();
    private ArrayList<Object> villagersCopy = new ArrayList<>();
    private ArrayList<Boolean> isNorth = new ArrayList<>();
    private Map<String, Object> resident = new HashMap<>();
    private Map<String, Object> dreamie = new HashMap<>();
    private static final String TAG = "villagers";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ExpandableListView list_view;
    List<String> filterParent;
    HashMap<String, HashMap<String, Boolean>> filterChild;
    HashMap<String, Boolean> sort;
    ArrayList<Button> sortButtons = new ArrayList<>();
    private GoogleSignInClient mGoogleSignInClient;
    private int listHeight = 0;
    Drawable search;
    private HashMap<String, Boolean> personality, hobby, species, months, residentFilter, genders;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_villagers);
        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.villagers_grid);
        int numColumns = 4;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        isNorth.add(false);
        getVillagers();
        getUserInfo();
        final GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        recyclerView.setLayoutManager(layoutManager);
        setAdapter();
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached

                    Log.i("Yaeye!", "end called");

                    // Do something

                    loading = true;
                }
            }
        });
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final EditText editText = findViewById(R.id.search);
        editText.setSingleLine();
        // hide the keyboard when the view loses focus (user taps outside of edittext field
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.println(hasFocus);
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                addRightCancelDrawable(editText);
                if (listAdapter == null) {
                    createListData(null);
                }
                listAdapter.search(editText.getText().toString());
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        search = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search);
        search.setBounds(0, 0, search.getIntrinsicWidth(), search.getIntrinsicHeight());

        ImageButton filterButton = findViewById(R.id.imageButton);
        filterButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateFilter();
                setListViewListeners();
            }
        });
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

    private void setListViewListeners() {
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

    private void inflateFilter() {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popView = layoutInflater.inflate(R.layout.villager_filter_popup, null);
        list_view = (ExpandableListView) popView.findViewById(R.id.filter);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf");
        TextView filter = popView.findViewById(R.id.filterbytext);
        filter.setTypeface(typeface);
        TextView sort = popView.findViewById(R.id.sortby);
        sort.setTypeface(typeface);
        Button clear = popView.findViewById(R.id.clearvillagers);
        //adjust button size to match the filter buttons' sizes
        clear.setMinHeight(0);
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                51,
                getResources().getDisplayMetrics()
        );
        clear.setMinWidth(px);
        clear.setMinimumWidth(px);
        clear.setMinimumHeight(0);
        clear.setTypeface(typeface);
        px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                getResources().getDisplayMetrics()
        );
        clear.setPadding(px, px, px, px);
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setFilters();
                listAdapter.clear();
                listAdapter.filter();
            }
        });
        createListData(popView);
        createSortButtons(popView);
        popView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.whiterectangle));
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        PopupWindow popWindow = new PopupWindow(popView, width,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        if (Build.VERSION.SDK_INT >= 21) {
            popWindow.setElevation(5.0f);
        }
        popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
        View toolbar = findViewById(R.id.toolbar);
        popWindow.showAsDropDown(toolbar, 0, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addRightCancelDrawable(final EditText editText) {
        if (editText.getText().toString().equals("")) {
            editText.setCompoundDrawables(search, null, null, null);
        } else {
            Drawable cancel = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_close_clear_cancel);
            cancel.setBounds(0, 0, cancel.getIntrinsicWidth(), cancel.getIntrinsicHeight());
            editText.setCompoundDrawables(search, null, cancel, null);
            editText.setOnTouchListener(new ClassUtils.RightDrawableOnTouchListener(editText) {
                @Override
                public boolean onDrawableTouch(final MotionEvent event) {
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    editText.setText("");
                    editText.setCompoundDrawables(search, null, null, null);
                    return true;
                }
            });
        }

    }

    private void setAdapter() {
        ArrayList<ClassUtils.IdKeyPair> idKeyPairs = new ArrayList<>();
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.cardtitle, "name", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.birthday_value, "birthday", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.catchphrase_value, "catchphrase", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.hobby_value, "hobby", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.species_value, "species", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.personality_value, "personality", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.villagerImage, "image", ClassUtils.ViewType.IMAGEVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.gender_value, "gender", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.isResident, "resident",
                ClassUtils.ViewType.RESIDENTBUTTON, R.drawable.ic_island, R.drawable.ic_whiteisland));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.isDreamie, "dreamie",
                ClassUtils.ViewType.DREAMIEBUTTON, R.drawable.ic_dreamie, R.drawable.ic_dreamiewhite));

        ClassUtils.PopupHelper helper = new ClassUtils.PopupHelper(idKeyPairs, R.layout.villager_popup,
                0, 0, R.drawable.villagerpopup, R.id.fishimg, R.id.resident,
                R.id.fishconstraint, R.id.fishdonated, R.drawable.specialicon, R.drawable.caught);


        DocumentReference residentRef = db.collection("users").document(user.getUid())
                .collection("villagers").document("island");
        DocumentReference dreamieRef = db.collection("users").document(user.getUid())
                .collection("villagers").document("dreamie");
        adapter = new RecyclerviewAdapter(this, villagers, null, resident, residentRef,
                ClassUtils.ItemType.VILLAGERS, helper, dreamie, dreamieRef);
    }

    private void setExpandableHeight(ExpandableListAdapter mAdapter, ExpandableListView mExpandableListView) {
        int mInitialHeight = 0;
        for (Integer i = 0; i < mAdapter.getGroupCount(); i++) {
            View groupItem = mAdapter.getGroupView(i, false, null, mExpandableListView);
            groupItem.measure(mExpandableListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            mInitialHeight += groupItem.getMeasuredHeight() + 3;
        }
        listHeight = mInitialHeight;
    }

    private void createSortButtons(View v) {
        // check if we have created it before (so we remember state of the filter after we close the popup)
        if (sort == null) {
            sort = new LinkedHashMap<>();
            sort.put("Name", true);
            sort.put("Catchphrase", false);
        }
        sortButtons.clear(); // have to clear the button list everytime the view is inflated to get rid of the 'old' inflated buttons
        FlexboxLayout listChild = v.findViewById(R.id.sort);
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
                        //DocSnapToData.sortHashMapByIndex(villagers, key);
                       //DocSnapToData.sortHashMapByIndex(villagersCopy, key);
                        adapter.notifyDataSetChanged();
                        sort.put(key, true);
                        v.setBackground(getResources().getDrawable(R.drawable.fish_filter_on_button));
                    }
                }
            });
            listChild.addView(button);
            sortButtons.add(button);
        }
    }

    private void setFilters() {
        // Adding child data List one
        personality.put("Cranky", false);
        personality.put("Jock", false);
        personality.put("Lazy", false);
        personality.put("Normal", false);
        personality.put("Peppy", false);
        personality.put("Sisterly", false);
        personality.put("Smug", false);
        personality.put("Snooty", false);


        // Adding child data List two
        species.put("Alligator", false);
        species.put("Anteater", false);
        species.put("Bear", false);
        species.put("Bird", false);
        species.put("Bull", false);
        species.put("Cat", false);
        species.put("Chicken", false);
        species.put("Cow", false);
        species.put("Cub", false);
        species.put("Deer", false);
        species.put("Dog", false);
        species.put("Duck", false);
        species.put("Eagle", false);
        species.put("Elephant", false);
        species.put("Frog", false);
        species.put("Goat", false);
        species.put("Gorilla", false);
        species.put("Hamster", false);
        species.put("Hippo", false);
        species.put("Horse", false);
        species.put("Kangaroo", false);
        species.put("Koala", false);
        species.put("Lion", false);
        species.put("Monkey", false);
        species.put("Mouse", false);
        species.put("Octopus", false);
        species.put("Ostrich", false);
        species.put("Penguin", false);
        species.put("Pig", false);
        species.put("Rabbit", false);
        species.put("Rhino", false);
        species.put("Sheep", false);
        species.put("Squirrel", false);
        species.put("Tiger", false);
        species.put("Wolf", false);


        // Adding child data List three
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

        // Adding child data for hobby size
        hobby.put("Education", false);
        hobby.put("Fashion", false);
        hobby.put("Fitness", false);
        hobby.put("Music", false);
        hobby.put("Nature", false);
        hobby.put("Play", false);

        genders.put("Female", false);
        genders.put("Male", false);

        residentFilter.put("Resident", false);
        residentFilter.put("Not Resident", false);
        residentFilter.put("Dreamie", false);
        residentFilter.put("Not Dreamie", false);

        filterChild.put(filterParent.get(0), personality); // Header, Child data
        filterChild.put(filterParent.get(1), species); // Header, Child data
        filterChild.put(filterParent.get(2), months); // Header, Child data
        filterChild.put(filterParent.get(3), hobby);
        filterChild.put(filterParent.get(4), genders);
        filterChild.put(filterParent.get(5), residentFilter);
    }

    private void createListData(View v) {
        // check if we have created the adapter already so we don't overwrite the state
        if (listAdapter == null) {
            filterParent = new ArrayList<>();
            filterChild = new HashMap<>();

            // Adding child data
            filterParent.add("Personalities");
            filterParent.add("Species");
            filterParent.add("Birthday Months");
            filterParent.add("Hobbies");
            filterParent.add("Genders");
            filterParent.add("Residents");

            personality = new LinkedHashMap<>();
            species = new LinkedHashMap<>();
            months = new LinkedHashMap<>();
            hobby = new LinkedHashMap<>();
            residentFilter = new LinkedHashMap<>();
            genders = new LinkedHashMap<>();

            setFilters();

            listAdapter = new ExpandableListAdapter(getApplicationContext(), filterParent, filterChild,
                    adapter, villagers, isNorth, villagersCopy, resident, dreamie, R.color.fishBackground, R.drawable.fish_filter_on_button,
                    R.drawable.fish_filter_off_button);
        }
        if (listHeight <= 0 && v != null) {
            setExpandableHeight(listAdapter, list_view); // set the height of the list view
        }
        if (v != null) {

            list_view.setAdapter(listAdapter);

            NestedScrollView scroll = v.findViewById(R.id.villagerScroll);
            ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams) list_view.getLayoutParams();
            param.height = listHeight;
            list_view.setLayoutParams(param);
            list_view.refreshDrawableState();
            scroll.refreshDrawableState(); // refresh the height
        }
    }

    private void getVillagers() {
        CollectionReference collection = db.collection("villagers");
        Query q = collection.orderBy("index");

        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Villager v = new Villager(document.getData(), document.getId());
                        villagers.add(v);
                    }
                    for (int i = 0; i<villagers.size(); i++) {
                        Villager v = (Villager) villagers.get(i);
                        Glide.with(Villagers.this)
                                .load(v.image)
                                .dontAnimate()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .encodeFormat(Bitmap.CompressFormat.PNG)
                                .dontTransform()
                                .centerCrop()
                                .preload();
                    }
                    villagersCopy.addAll(villagers);
                    adapter.notifyDataSetChanged();
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
        final DocumentReference villagersRef = db.collection("users").document(user.getUid())
                .collection("villagers").document("island");
        villagersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        resident.putAll(doc.getData());
                        for (String key : resident.keySet()) {
                            resident.put(key, true);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        villagersRef.set(new HashMap<>());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        final DocumentReference dreamieRef = db.collection("users").document(user.getUid())
                .collection("villagers").document("dreamie");
        dreamieRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        dreamie.putAll(doc.getData());
                        adapter.notifyDataSetChanged();
                    } else {
                        dreamieRef.set(new HashMap<>());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Villagers.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            Intent intent = new Intent(this, BugActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_fish) {

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
                        Intent intent = new Intent(Villagers.this,
                                MainActivity.class);
                        startActivity(intent);
                        FirebaseAuth.getInstance().signOut();
                    }
                });
    }
}
