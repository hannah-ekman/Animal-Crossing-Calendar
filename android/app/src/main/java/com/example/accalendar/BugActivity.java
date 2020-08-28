package com.example.accalendar;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.example.accalendar.adapters.ExpandableListAdapter;
import com.example.accalendar.adapters.RecyclerviewAdapter;
import com.example.accalendar.utils.Bug;
import com.example.accalendar.utils.ClassUtils;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BugActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerviewAdapter adapter;
    ExpandableListAdapter listAdapter;
    private FirebaseFirestore db;
    private ArrayList<Object> bug = new ArrayList<>();
    private ArrayList<Object> bugCopy = new ArrayList<>();
    private ArrayList<Boolean> isNorth = new ArrayList<>();
    private Map<String, Object> caught = new HashMap<>();
    private Map<String, Object> donated = new HashMap<>();
    private static final String TAG = "bug";
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
    private HashMap<String, Boolean> locations, times, months, caughtFilter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug);
        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.bug_grid);
        int numColumns = 4;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        isNorth.add(false);
        getbug();
        getUserInfo();
        recyclerView.setLayoutManager(new GridLayoutManager(this, numColumns));
        setAdapter();
        recyclerView.setAdapter(adapter);
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
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
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
        View popView = layoutInflater.inflate(R.layout.bug_filter_popup, null);
        list_view = (ExpandableListView) popView.findViewById(R.id.filter);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf");
        TextView filter = popView.findViewById(R.id.filterbytext);
        filter.setTypeface(typeface);
        TextView sort = popView.findViewById(R.id.sortby);
        sort.setTypeface(typeface);
        Button clear = popView.findViewById(R.id.clearbug);
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
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.bug_price_value, "price", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.bug_location_value, "location", ClassUtils.ViewType.TEXTVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.bug_months, "months", ClassUtils.ViewType.MONTHVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.bug_times, "times", ClassUtils.ViewType.TIMEVIEW));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.donatedbug, "donated",
                ClassUtils.ViewType.DONATEDBUTTON, R.drawable.ic_museumicon, R.drawable.ic_whitemuseumicon));
        idKeyPairs.add(new ClassUtils.IdKeyPair(R.id.caughtbug, "checkbox",
                ClassUtils.ViewType.CAUGHTBUTTON, R.drawable.ic_caughtbugicon, R.drawable.ic_notcaughtbugicon));

        ClassUtils.PopupHelper helper = new ClassUtils.PopupHelper(idKeyPairs, R.layout.bug_popup,
                R.drawable.caught_bug, R.drawable.missing_card, R.drawable.bugpopup, R.id.fishimg,
                R.id.fishconstraint, R.id.fishdonated, R.drawable.caught);


        DocumentReference bugRef = db.collection("users").document(user.getUid())
                .collection("bug").document("caught");
        DocumentReference donatedRef = db.collection("users").document(user.getUid())
                .collection("bug").document("donated");
        adapter = new RecyclerviewAdapter(this, bug, isNorth, caught, bugRef, ClassUtils.ItemType.BUG,
                helper, donated, donatedRef);
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
            sort.put("Default", true);
            sort.put("Name", false);
            sort.put("Price", false);
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
                button.setBackground(getResources().getDrawable(R.drawable.bug_filter_on_button));
            else
                button.setBackground(getResources().getDrawable(R.drawable.bug_filter_off_button));
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean tf = !sort.get(key);
                    System.out.println(sort);
                    if (tf) {
                        // remove other button selections
                        int i = 0;
                        for (Map.Entry<String, Boolean> s : sort.entrySet()) {
                            Button b = sortButtons.get(i);
                            b.setBackground(getResources().getDrawable(R.drawable.bug_filter_off_button));
                            sort.put(s.getKey(), false);
                            i++;
                        }
                        // then set this button to be selected
                        //DocSnapToData.sortHashMapByIndex(bug, key);
                        //DocSnapToData.sortHashMapByIndex(bugCopy, key);
                        adapter.notifyDataSetChanged();
                        sort.put(key, true);
                        v.setBackground(getResources().getDrawable(R.drawable.bug_filter_on_button));
                    }
                }
            });
            listChild.addView(button);
            sortButtons.add(button);
        }
    }

    private void setFilters() {
        // Adding child data List one
        locations.put("Coconut Trees", false);
        locations.put("Trees", false);
        locations.put("Tree Stumps", false);
        locations.put("Shaking Trees", false);
        locations.put("Ground", false);
        locations.put("Ground (Rolling Snowballs)", false);
        locations.put("Underground", false);
        locations.put("Flowers", false);
        locations.put("Flowers (White)", false);
        locations.put("Rivers and Ponds", false);
        locations.put("Flying", false);
        locations.put("Flying (Hybrid Flowers)", false);
        locations.put("Flying (Light)", false);
        locations.put("Hitting Rocks", false);
        locations.put("Rocks and Bushes (Rain)", false);
        locations.put("Beach Rocks", false);
        locations.put("Villager's Heads", false);
        locations.put("Disguised (Shell)", false);
        locations.put("Disguised (Leaf)", false);
        locations.put("Rotten Food", false);
        locations.put("Trash Items", false);



        // Adding child data List two
        times.put("1", false);
        times.put("2", false);
        times.put("3", false);
        times.put("4", false);
        times.put("5", false);
        times.put("6", false);
        times.put("7", false);
        times.put("8", false);
        times.put("9", false);
        times.put("10", false);
        times.put("11", false);
        times.put("12", false);
        times.put("13", false);
        times.put("14", false);
        times.put("15", false);
        times.put("16", false);
        times.put("17", false);
        times.put("18", false);
        times.put("19", false);
        times.put("20", false);
        times.put("21", false);
        times.put("22", false);
        times.put("23", false);
        times.put("24", false);
        times.put("All Day", false);


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

        caughtFilter.put("Caught", false);
        caughtFilter.put("Not Caught", false);
        caughtFilter.put("Donated", false);
        caughtFilter.put("Not Donated", false);

        filterChild.put(filterParent.get(0), locations); // Header, Child data
        filterChild.put(filterParent.get(1), times); // Header, Child data
        filterChild.put(filterParent.get(2), months); // Header, Child data
        filterChild.put(filterParent.get(3), caughtFilter);
    }

    private void createListData(View v) {
        // check if we have created the adapter already so we don't overwrite the state
        if (listAdapter == null) {
            filterParent = new ArrayList<>();
            filterChild = new HashMap<>();

            // Adding child data
            filterParent.add("Locations");
            filterParent.add("Times");
            filterParent.add("Months");
            filterParent.add("Blathers");

            locations = new LinkedHashMap<>();
            times = new LinkedHashMap<>();
            months = new LinkedHashMap<>();
            caughtFilter = new LinkedHashMap<>();

            setFilters();

            listAdapter = new ExpandableListAdapter(getApplicationContext(), filterParent, filterChild,
                    adapter, bug, isNorth, bugCopy, caught, donated, R.color.bugBackground, R.drawable.bug_filter_on_button,
                    R.drawable.bug_filter_off_button);
        }
        if (listHeight <= 0 && v != null ) {
            setExpandableHeight(listAdapter, list_view); // set the height of the list view
        }
        if (v != null) {

            list_view.setAdapter(listAdapter);

            NestedScrollView scroll = v.findViewById(R.id.bugScroll);
            ConstraintLayout.LayoutParams param = (ConstraintLayout.LayoutParams) list_view.getLayoutParams();
            param.height = listHeight;
            list_view.setLayoutParams(param);
            list_view.refreshDrawableState();
            scroll.refreshDrawableState(); // refresh the height
        }
    }

    private void getbug() {
        DocumentReference docRef = db.collection("trackables").document("bug");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        for (String key : doc.getData().keySet()) {
                            Bug b = new Bug((HashMap<String, Object>) doc.getData().get(key), key);
                            bug.add(b);
                        }
                        //DocSnapToData.sortHashMapByIndex(bug, "Default");
                        bugCopy.addAll(bug);
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
        final DocumentReference bugRef = db.collection("users").document(user.getUid())
                .collection("bug").document("caught");
        bugRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        caught.putAll(doc.getData());
                        adapter.notifyDataSetChanged();
                    } else {
                        bugRef.set(new HashMap<>());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        final DocumentReference donatedRef = db.collection("users").document(user.getUid())
                .collection("bug").document("donated");
        donatedRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        donated.putAll(doc.getData());
                        adapter.notifyDataSetChanged();
                    } else {
                        donatedRef.set(new HashMap<>());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(BugActivity.INPUT_METHOD_SERVICE);
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
        } else if (id == R.id.nav_fish) {
            Intent intent = new Intent(this, FishActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bug) {

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
                        Intent intent = new Intent(BugActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        FirebaseAuth.getInstance().signOut();
                    }
                });
    }

}
