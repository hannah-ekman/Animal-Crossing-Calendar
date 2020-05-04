package com.example.accalendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import com.example.accalendar.decorators.BirthdayDecorator;
import com.example.accalendar.decorators.CenteredDotSpan;
import com.example.accalendar.decorators.CurrentDayDecorator;
import com.example.accalendar.decorators.EventDecorator;
import com.example.accalendar.decorators.ResourceDecorator;
import com.example.accalendar.decorators.SpecialDecorator;
import com.example.accalendar.decorators.TourneyDecorator;
import com.example.accalendar.listviewadapters.EventAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "oops";
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long dateOffset;
    private boolean isChecked;
    private boolean isNorthern;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Map<String, Map<String, Long>> events = new HashMap<>();
    private Map<String, Map<String, Map<String, Long>>> specialDays = new HashMap<>();
    private Map<String, Map<ArrayList<Long>, Integer>> tourneys = new HashMap<>();
    private CurrentDayDecorator currentDecorator;
    private Map<String, Map<String, Long>> resources = new HashMap<>();
    private Map<String, HashMap<String, Object>> birthdays = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Switch timeToggle = findViewById(R.id.timeToggle);
        timeToggle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf"));
        final TextView travelDate = findViewById(R.id.travelDate);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        getVillagers(user.getUid());
        timeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                        put("isTimeTravel", true);
                    }});
                    updateCalendarDate(dateOffset);
                    travelDate.setVisibility(View.VISIBLE);
                } else {
                    dateOffset = 0;
                    updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                        put("isTimeTravel", false);
                        put("dateOffset", dateOffset);
                    }});
                    updateCalendarDate(dateOffset);
                    travelDate.setVisibility(View.INVISIBLE);
                }
            }
        });

        getFirestore("users", user.getUid(), new String[]{"isTimeTravel", "dateOffset", "isNorthern"});
        getFirestore("events", "yearly events", new String[0]);

        travelDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(CalendarActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog, mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                LocalDate date = LocalDate.of(year, month+1, dayOfMonth);
                dateOffset = ChronoUnit.DAYS.between(LocalDate.now(), date);
                updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                    put("dateOffset", dateOffset);
                }});
                updateCalendarDate(dateOffset);
            }
        };

        MaterialCalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangedListener(this);

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {
        LocalDate day = date.getDate();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        ArrayList<View> eventViews = getCorrespondingEvents(day);
        if(eventViews.size() > 0) {
            View popView = layoutInflater.inflate(R.layout.calendar_popup, null);
            TextView title = popView.findViewById(R.id.cardtitle);
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf");
            title.setTypeface(typeface);
            ListView list = popView.findViewById(R.id.eventList);
            EventAdapter listAdapter = new EventAdapter(popView.getContext(), eventViews);
            list.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
            String month = day.getMonth().name();
            title.setText(month.substring(0, 1) + month.substring(1, month.length()).toLowerCase() + " " +
                    day.getDayOfMonth() + ", " + day.getYear() + " Events");
            popView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.roundedpopup));
            PopupWindow popWindow = new PopupWindow(popView, 900,
                    800, true);
            if (Build.VERSION.SDK_INT >= 21) {
                popWindow.setElevation(5.0f);
            }
            popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
            popWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);
        }
    }

    private ArrayList<View> getCorrespondingEvents(LocalDate date) {
        ArrayList<View> listViews = new ArrayList<>();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        ArrayList<String> eventList = new ArrayList<>();
        int color = ResourcesCompat.getColor(getResources(), R.color.event, null);
        for (Map.Entry <String, Map<String, Long>> event : events.entrySet()) {
            if (EventDecorator.isEvent(event, date))
                    eventList.add(event.getKey());
        }
        addViewToList(listViews, layoutInflater, color, eventList);

        ArrayList<String> resourceList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.resources, null);
        for (Map.Entry <String, Map<String, Long>> resource : resources.entrySet()) {
            if (ResourceDecorator.isResourceEvent(resource, date)) {
                String resourceName = resource.getKey();
                boolean addS = resourceName.charAt(resourceName.length()-1) != 's';
                resourceList.add(resourceName + " Begin" + (addS ? "s" : ""));
            }
        }
        addViewToList(listViews, layoutInflater, color, resourceList);

        ArrayList<String> specialList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.special, null);
        for (Map.Entry <String, Map<String, Map<String, Long>>> special : specialDays.entrySet()) {
            Map<String, Map<String, Long>> specialCategories = special.getValue();
            for (Map.Entry <String, Map<String, Long>> category : specialCategories.entrySet()) {
                if (SpecialDecorator.isSpecialEvent(category, special.getKey(), date))
                    specialList.add(category.getKey());
            }
        }
        addViewToList(listViews, layoutInflater, color, specialList);

        ArrayList<String> tourneyList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.tourney, null);
        for (Map.Entry <String, Map<ArrayList<Long>, Integer>> tourney : tourneys.entrySet()) {
            if (TourneyDecorator.isTourney(tourney, date))
                tourneyList.add(tourney.getKey());
        }
        addViewToList(listViews, layoutInflater, color, tourneyList);

        ArrayList<String> birthdayList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.birthday, null);
        for (Map.Entry <String, HashMap<String, Object>> villager : birthdays.entrySet()) {
            HashMap<String, Object> birthday = villager.getValue();
            if (date.getDayOfMonth() == (Integer) birthday.get("day") &&
                    date.getMonth().getValue() == (Integer) birthday.get("month"))
                birthdayList.add(villager.getKey()+"'s Birthday");
        }
        addViewToList(listViews, layoutInflater, color, birthdayList);
        return listViews;
    }

    private void addViewToList(ArrayList<View> listViews, LayoutInflater layoutInflater, int color,
                               ArrayList<String> eventList) {
        if (eventList.size() > 0) {
            View eventListItem = layoutInflater.inflate(R.layout.event_list_item, null);
            TextView eventSpan = eventListItem.findViewById(R.id.eventItemSpan);
            LinearLayout eventListView = eventListItem.findViewById(R.id.eventItemList);
            SpannableString eventString = new SpannableString(" ");
            eventString.setSpan(new DotSpan(15, color), 0, eventString.length(), 0);
            eventSpan.setText(eventString);
            for (String eventName : eventList) {
                TextView event = new TextView(eventListItem.getContext());
                event.setText(eventName);
                event.setTextColor(Color.DKGRAY);
                event.setTextSize(16);
                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin-sans.ttf");
                event.setTypeface(typeface);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                event.setLayoutParams(params);
                eventListView.addView(event);
            }
            listViews.add(eventListItem);
        }
    }

    private void updateCalendarDate(long offset) {
        LocalDate date = LocalDate.now().plus(offset, ChronoUnit.DAYS);
        MaterialCalendarView calendarView = findViewById(R.id.calendarView);
        if (currentDecorator != null) {
            calendarView.removeDecorator(currentDecorator);
            calendarView.invalidateDecorators();
        }
        currentDecorator = new CurrentDayDecorator(
                ResourcesCompat.getColor(getResources(), R.color.selectedGreen, null), date);
        calendarView.addDecorator(currentDecorator);
        TextView dateText = findViewById(R.id.travelDate);
        dateText.setText(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        updateSeasonalResources(date);
        calendarView.setCurrentDate(CalendarDay.from(date));
    }

    private void updateSeasonalResources(LocalDate date) {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout list = findViewById(R.id.seasonalList);
        list.removeAllViews();
        int color = Color.WHITE;

        for (Map.Entry<String, Map<String, Long>> resource : resources.entrySet()) {
            Map<String, Long> resourceInfo = resource.getValue();
            LocalDate start = LocalDate.of(date.getYear(),
                    resourceInfo.get("start month").intValue(),
                    resourceInfo.get("start day").intValue());
            LocalDate end = LocalDate.of(date.getYear(),
                    resourceInfo.get("end month").intValue(),
                    resourceInfo.get("end day").intValue());

            if (date.isEqual(start) || date.isEqual(end) ||
                    (date.isAfter(start) && date.isBefore(end))) {
                View resourceListItem = layoutInflater.inflate(R.layout.resource_list_item, null);
                TextView resourceSpan = resourceListItem.findViewById(R.id.resourceItemSpan);
                TextView resourceText = resourceListItem.findViewById(R.id.resourceItemText);
                SpannableString resourceString = new SpannableString(" ");
                resourceString.setSpan(new CenteredDotSpan(10, color), 0, resourceString.length(), 0);
                resourceText.setTextSize(16);
                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin-sans.ttf");
                resourceText.setTypeface(typeface);
                resourceSpan.setText(resourceString);
                resourceText.setText(resource.getKey() + " ending on: " +
                        end.getMonthValue() + "/" + end.getDayOfMonth());
                list.addView(resourceListItem);
            }
        }
    }

    private void updateFieldVars(DocumentSnapshot doc, String[] fields) {
            switch (doc.getId()) {
                case "yearly events":
                    Map<String, Object> map = doc.getData();
                    
                    if (map != null) {
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            Map<String, Long> dateInfo = (Map<String, Long>) entry.getValue();
                            events.put(entry.getKey(), dateInfo);
                        }

                        MaterialCalendarView calendar = findViewById(R.id.calendarView);
                        calendar.addDecorator(new EventDecorator(
                                ResourcesCompat.getColor(getResources(), R.color.event, null),
                                events));
                    }
                    break;
                default:
                    for (String field : fields) {
                        switch (field) {
                            case "isTimeTravel":
                                Switch timeToggle = findViewById(R.id.timeToggle);
                                isChecked = (boolean) doc.get(field);
                                timeToggle.setChecked(isChecked);
                                break;
                            case "dateOffset":
                                dateOffset = (long) doc.get(field);
                                updateCalendarDate(dateOffset);
                                break;
                            case "isNorthern":
                                isNorthern = (boolean) doc.get(field);
                                getTourneys();
                                getSeasonalResources();
                                getSpecialDays();
                            default:
                                break;
                        }
                    }
                    break;
            }

    }

    private void updateFirestore(String collection, String docId, Map<String, Object> docData) {
        db.collection(collection).document(docId)
                .update(docData)
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
    }

    private void getFirestore(String collection, String docId, final String[] fields) {
        DocumentReference docRef = db.collection(collection).document(docId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        updateFieldVars(document, fields);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void getBirthdays(DocumentSnapshot doc) {
        Map<String, Object> map = doc.getData();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<String, Object> eventSet = (Map<String, Object>) entry.getValue();
                final int month = ((Long) eventSet.get("month")).intValue();;
                final int day = ((Long) eventSet.get("day")).intValue();

                birthdays.put(entry.getKey(), new HashMap<String, Object>() {{
                    put("day", day);
                    put("month", month);
                }});
            }
            MaterialCalendarView calendar = findViewById(R.id.calendarView);
            calendar.addDecorator(new BirthdayDecorator(
                    ResourcesCompat.getColor(getResources(), R.color.birthday, null),
                    birthdays));
        }
    }

    private void getSpecialDayInfo(DocumentSnapshot doc) {
        Map<String, Object>  map = doc.getData();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<String, Map<String, Long>> specialDay =
                        (Map<String, Map<String, Long>>) entry.getValue();
                specialDays.put(entry.getKey(), specialDay);
            }

            MaterialCalendarView calendar = findViewById(R.id.calendarView);
            calendar.addDecorator(new SpecialDecorator(
                    ResourcesCompat.getColor(getResources(), R.color.special, null),
                    specialDays));
        }
    }

    private void getSpecialDays() {
        DocumentReference docRef = db.collection("events").document("special days");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        getSpecialDayInfo(document);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getTourneyInfo(DocumentSnapshot doc) {
        Map<String, Object>  map = doc.getData();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<ArrayList<Long>, Integer> tourney = new HashMap<>();
                ArrayList<Long> months = (ArrayList<Long>) ((Map)entry.getValue()).get("months");
                tourney.put(months, ((Long)((Map)entry.getValue()).get("saturday")).intValue());
                tourneys.put(entry.getKey(), tourney);
            }

            MaterialCalendarView calendar = findViewById(R.id.calendarView);
            calendar.addDecorator(new TourneyDecorator(
                    ResourcesCompat.getColor(getResources(), R.color.tourney, null),
                    tourneys));
        }

    }

    private void getTourneys() {
        DocumentReference docRef;
        if (isNorthern)
            docRef = db.collection("events").document("northern tourneys");
        else
            docRef = db.collection("events").document("southern tourneys");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        getTourneyInfo(document);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getResourceInfo(DocumentSnapshot doc) {
        Map<String, Object>  map = doc.getData();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<String, Long> dateInfo = (Map<String, Long>) entry.getValue();
                resources.put(entry.getKey(), dateInfo);
            }
            MaterialCalendarView calendar = findViewById(R.id.calendarView);
            calendar.addDecorator(new ResourceDecorator(
                    ResourcesCompat.getColor(getResources(), R.color.resources, null),
                    resources));
            updateCalendarDate(dateOffset);
        }
    }

    private void getSeasonalResources() {
        DocumentReference docRef;
        if (isNorthern)
            docRef = db.collection("events").document("northern resources");
        else
            docRef = db.collection("events").document("southern resources");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        getResourceInfo(document);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getVillagers(final String uid) {
        final DocumentReference docRef = db.collection("users").document(uid)
                .collection("villagers").document("island");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        getBirthdays(document);
                    } else {
                        Log.d(TAG, "No such document");
                        docRef.set(new HashMap<>());
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
            // Handle the camera action
        } else if (id == R.id.nav_bug) {

        } else if (id == R.id.nav_fish) {

        }else if (id == R.id.nav_fossil) {

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
                        Intent intent = new Intent(CalendarActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        FirebaseAuth.getInstance().signOut();
                    }
                });
    }

}
