package com.example.accalendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import java.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import com.example.accalendar.decorators.BirthdayDecorator;
import com.example.accalendar.decorators.CurrentDayDecorator;
import com.example.accalendar.decorators.EventDecorator;
import com.example.accalendar.decorators.ResourceDecorator;
import com.example.accalendar.decorators.SpecialDecorator;
import com.example.accalendar.decorators.TourneyDecorator;
import com.example.accalendar.adapters.ListAdapter;
import com.example.accalendar.utils.DocSnapToData;
import com.example.accalendar.utils.InflateLayouts;
import com.example.accalendar.utils.TargetDrawable;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
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
import android.widget.TimePicker;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "oops";
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long dateOffset = 0;
    private long timeOffset = 0;
    private boolean isChecked = false;
    private boolean isNorthern = true;
    private LocalDate timeTravelDate;
    private int mins = 0;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Map<String, Map<String, Long>> events = new HashMap<>();
    private Map<String, Map<String, Map<String, Long>>> specialDays = new HashMap<>();
    private Map<String, Map<ArrayList<Long>, Integer>> tourneys = new HashMap<>();
    private CurrentDayDecorator currentDecorator;
    private Map<String, Map<String, Object>> resources = new HashMap<>();
    private Map<String, HashMap<String, Object>> birthdays = new HashMap<>();
    private ArrayList<TargetDrawable> targets = new ArrayList<>();
    private Timer autoUpdate;

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

        timeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                        put("isTimeTravel", true);
                    }});
                    updateCalendarDate(dateOffset, timeOffset);
                    travelDate.setVisibility(View.VISIBLE);
                } else {
                    dateOffset = 0;
                    timeOffset = 0;
                    updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                        put("isTimeTravel", false);
                        put("dateOffset", dateOffset);
                        put("timeOffset", timeOffset);
                    }});
                    updateCalendarDate(dateOffset, timeOffset);
                    travelDate.setVisibility(View.INVISIBLE);
                }
            }
        });

        getUserFields("users", user.getUid(), new String[]{"isTimeTravel", "dateOffset",
                "timeOffset", "isNorthern"});
        final Calendar cal = Calendar.getInstance();
        travelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    dialog = new DatePickerDialog(CalendarActivity.this,
                            android.R.style.Theme_Material_Dialog, mDateSetListener,
                            year, month, day);
                } else {
                    dialog = new DatePickerDialog(CalendarActivity.this, mDateSetListener,
                            year, month, day);
                }
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                dateOffset = ChronoUnit.DAYS.between(LocalDate.now(), date);
                updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                    put("dateOffset", dateOffset);
                }});
                TimePickerDialog.OnTimeSetListener timeSet =  new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int curHour = cal.get(Calendar.HOUR_OF_DAY);
                        int curMin = cal.get(Calendar.MINUTE);
                        curMin += curHour * 60;
                        minute += hourOfDay * 60;
                        timeOffset = minute - curMin;
                        updateFirestore("users", user.getUid(), new HashMap<String, Object>() {{
                            put("timeOffset", timeOffset);
                        }});
                        updateCalendarDate(dateOffset, timeOffset);
                    }
                };
                TimePickerDialog timePicker = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    timePicker = new TimePickerDialog(CalendarActivity.this,
                            android.R.style.Theme_Material_Dialog, timeSet, cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE), false);
                } else {
                    timePicker = new TimePickerDialog(CalendarActivity.this, timeSet,
                            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
                }
                timePicker.show();
                updateCalendarDate(dateOffset, timeOffset);
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
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateCalendarDate(dateOffset, timeOffset);
                    }
                });
            }
        }, 0, 60000); // updates each 40 secs
    }

    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {
        LocalDate day = date.getDate();
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        ArrayList<View> eventViews = getCorrespondingEvents(day);
        if (eventViews.size() > 0) {
            View popView = layoutInflater.inflate(R.layout.calendar_popup, null);
            TextView title = popView.findViewById(R.id.cardtitle);
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin_sans_semibold.ttf");
            title.setTypeface(typeface);
            ListView list = popView.findViewById(R.id.eventList);
            ListAdapter listAdapter = new ListAdapter(popView.getContext(), eventViews);
            list.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
            String month = day.getMonth().name();
            title.setText(month.substring(0, 1) + month.substring(1, month.length()).toLowerCase() + " " +
                    day.getDayOfMonth() + ", " + day.getYear() + " Events");
            popView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.roundedpopup));
            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
            PopupWindow popWindow = new PopupWindow(popView,  (int) (metrics.density*325+0.5f),
                    (int) (metrics.density*300+0.5f), true);
            if (Build.VERSION.SDK_INT >= 21) {
                popWindow.setElevation(5.0f);
            }
            popWindow.setAnimationStyle(R.style.PopUpWindow_Animation);
            popWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);
        }
    }

    private ArrayList<View> getCorrespondingEvents(LocalDate date) {
        ArrayList<View> listViews = new ArrayList<>();
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin-sans.ttf");

        ArrayList<String> eventList = new ArrayList<>();
        int color = ResourcesCompat.getColor(getResources(), R.color.event, null);
        for (Map.Entry<String, Map<String, Long>> event : events.entrySet()) {
            if (EventDecorator.isEvent(event, date))
                eventList.add(event.getKey());
        }
        Drawable event = ResourcesCompat.getDrawable(getResources(),
                R.drawable.eventicon, null);
        InflateLayouts.fillEventListItem(listViews, layoutInflater, color, eventList, typeface, event);

        ArrayList<String> resourceList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.resources, null);
        for (Map.Entry<String, Map<String, Object>> resource : resources.entrySet()) {
            if (ResourceDecorator.isResourceEvent(resource, date)) {
                String resourceName = resource.getKey();
                boolean addS = resourceName.charAt(resourceName.length() - 1) != 's';
                resourceList.add(resourceName + " Begin" + (addS ? "s" : ""));
            }
        }
        Drawable resource = ResourcesCompat.getDrawable(getResources(),
                R.drawable.cherryblossomicon, null);
        InflateLayouts.fillEventListItem(listViews, layoutInflater, color, resourceList, typeface, resource);

        ArrayList<String> specialList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.special, null);
        for (Map.Entry<String, Map<String, Map<String, Long>>> special : specialDays.entrySet()) {
            Map<String, Map<String, Long>> specialCategories = special.getValue();
            for (Map.Entry<String, Map<String, Long>> category : specialCategories.entrySet()) {
                if (SpecialDecorator.isSpecialEvent(category, special.getKey(), date))
                    specialList.add(category.getKey());
            }
        }
        Drawable special = ResourcesCompat.getDrawable(getResources(),
                R.drawable.specialicon, null);
        InflateLayouts.fillEventListItem(listViews, layoutInflater, color, specialList, typeface, special);

        ArrayList<String> tourneyList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.tourney, null);
        for (Map.Entry<String, Map<ArrayList<Long>, Integer>> tourney : tourneys.entrySet()) {
            if (TourneyDecorator.isTourney(tourney, date))
                tourneyList.add(tourney.getKey());
        }
        Drawable tourney = ResourcesCompat.getDrawable(getResources(),
                R.drawable.fishicon, null);
        InflateLayouts.fillEventListItem(listViews, layoutInflater, color, tourneyList, typeface, tourney);

        ArrayList<String> birthdayList = new ArrayList<>();
        color = ResourcesCompat.getColor(getResources(), R.color.birthday, null);
        for (Map.Entry<String, HashMap<String, Object>> villager : birthdays.entrySet()) {
            HashMap<String, Object> birthday = villager.getValue();
            if (date.getDayOfMonth() == (Integer) birthday.get("day") &&
                    date.getMonth().getValue() == (Integer) birthday.get("month"))
                birthdayList.add(villager.getKey() + "'s Birthday");
        }
        Drawable birthday = ResourcesCompat.getDrawable(getResources(),
                R.drawable.gifticon, null);
        InflateLayouts.fillEventListItem(listViews, layoutInflater, color, birthdayList, typeface, birthday);
        return listViews;
    }

    private void updateCalendarDate(long offset, long timeOffset) {
        Calendar cal = Calendar.getInstance();
        int mins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE) + (int) timeOffset;
        LocalDate date = LocalDate.now().plus(offset, ChronoUnit.DAYS);
        if (mins >= 24 * 60) {
            mins -= 24 * 60;
            date = date.plus(1, ChronoUnit.DAYS);
        }
        if (timeTravelDate == null || !timeTravelDate.equals(date)) {
            timeTravelDate = date;
            this.mins = mins;
            MaterialCalendarView calendarView = findViewById(R.id.calendarView);
            if (currentDecorator != null) {
                calendarView.removeDecorator(currentDecorator);
                calendarView.invalidateDecorators();
            }
            currentDecorator = new CurrentDayDecorator(
                    ResourcesCompat.getColor(getResources(), R.color.selectedGreen, null), date);
            calendarView.addDecorator(currentDecorator);
            updateTimeTravelButton(date, mins);
            updateSeasonalResources(date);
            calendarView.setCurrentDate(CalendarDay.from(date));
        } else if (this.mins != mins) {
            this.mins = mins;
            updateTimeTravelButton(date, mins);
        }
    }

    private void updateTimeTravelButton(LocalDate date, int mins) {
        int hour = mins / 60;
        mins = mins % 60;
        boolean isPM = hour >= 12;
        if (isPM && hour != 12)
            hour = hour - 12;
        String time = hour + ":" + (mins < 10 ? "0" : "") + mins + " " + (isPM ? "PM" : "AM");
        TextView dateText = findViewById(R.id.travelDate);
        dateText.setText(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                + " "+ time);
    }


    private void updateSeasonalResources(LocalDate date) {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout list = findViewById(R.id.seasonalList);
        list.removeAllViews();
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/josefin-sans.ttf");

        for (Map.Entry<String, Map<String, Object>> resource : resources.entrySet()) {
            Map<String, Object> resourceInfo = resource.getValue();
            LocalDate start = LocalDate.of(date.getYear(),
                    ((Long) resourceInfo.get("start month")).intValue(),
                    ((Long) resourceInfo.get("start day")).intValue());
            LocalDate end = LocalDate.of(date.getYear(),
                    ((Long)resourceInfo.get("end month")).intValue(),
                    ((Long)resourceInfo.get("end day")).intValue());

            if (date.isEqual(start) || date.isEqual(end) ||
                    (date.isAfter(start) && date.isBefore(end))) {
                try {
                    TargetDrawable target = InflateLayouts.fillResourceListItem(layoutInflater, typeface,
                            resource.getKey(), end, (String) resourceInfo.get("icon"),
                            getApplicationContext(), list);
                    targets.add(target);
                } catch (Exception e) {
                    Log.d(TAG, "Error "+e.toString());
                }
            }
        }
    }

    private void updateFieldVars(DocumentSnapshot doc, String[] fields) {
        for (String field : fields) {
            if (doc.contains(field)) {
                switch (field) {
                    case "isTimeTravel":
                        Switch timeToggle = findViewById(R.id.timeToggle);
                        isChecked = (boolean) doc.get(field);
                        timeToggle.setChecked(isChecked);
                        break;
                    case "dateOffset":
                        dateOffset = (long) doc.get(field);
                        updateCalendarDate(dateOffset, timeOffset);
                        break;
                    case "isNorthern":
                        isNorthern = (boolean) doc.get(field);
                        getTourneys();
                        getSeasonalResources();
                        getSpecialDays();
                        getVillagers(user.getUid());
                        getYearlyEvents();
                        break;
                    case "timeOffset":
                        timeOffset = (long) doc.get(field);
                        updateCalendarDate(dateOffset, timeOffset);
                        break;
                    default:
                        break;
                }
            }
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

    private void getUserFields(String collection, String docId, final String[] fields) {
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

    private void getVillagers(final String uid) {
        final DocumentReference docRef = db.collection("users").document(uid)
                .collection("villagers").document("island");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> map = doc.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + map);
                        if (map != null) {
                            birthdays = DocSnapToData.mapBirthdays(map);
                            Drawable d = ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.gifticon, null);
                            MaterialCalendarView calendar = findViewById(R.id.calendarView);
                            calendar.addDecorator(new BirthdayDecorator(birthdays, d));
                        }
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

    private void getYearlyEvents() {
        DocumentReference docRef = db.collection("events").document("yearly events");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                        Map<String, Object> map = doc.getData();

                        if (map != null) {
                            events = DocSnapToData.mapEvents(map);

                            MaterialCalendarView calendar = findViewById(R.id.calendarView);
                            calendar.addDecorator(new EventDecorator(
                                    ResourcesCompat.getColor(getResources(), R.color.event, null),
                                    events));
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getSpecialDays() {
        DocumentReference docRef = db.collection("events").document("special days");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> map = doc.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + map);
                        if (map != null) {
                            specialDays = DocSnapToData.mapSpecialDays(map);

                            MaterialCalendarView calendar = findViewById(R.id.calendarView);
                            Drawable d = ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.specialicon, null);
                            calendar.addDecorator(new SpecialDecorator(specialDays, d));
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
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
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> map = doc.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + map);
                        if (map != null) {
                            tourneys = DocSnapToData.mapTourneys(map);

                            MaterialCalendarView calendar = findViewById(R.id.calendarView);
                            Drawable d = ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.fishicon, null);
                            calendar.addDecorator(new TourneyDecorator(tourneys, d));
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
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
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> map = doc.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + map);
                        if (map != null) {
                            resources = DocSnapToData.mapResources(map);
                            MaterialCalendarView calendar = findViewById(R.id.calendarView);
                            Drawable d = ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.cherryblossomicon, null);
                            calendar.addDecorator(new ResourceDecorator(resources, d));
                            updateCalendarDate(dateOffset, timeOffset);
                        }
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
            // Handle the camera action
        } else if (id == R.id.nav_bug) {
            Intent intent = new Intent(this, Bug.class);
            startActivity(intent);
        } else if (id == R.id.nav_fish) {
            Intent intent = new Intent(this, Fish.class);
            startActivity(intent);
        }else if (id == R.id.nav_fossil) {

        } else if (id == R.id.nav_signout) {
            signOut();
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
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