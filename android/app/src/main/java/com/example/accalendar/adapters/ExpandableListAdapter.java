package com.example.accalendar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.example.accalendar.R;
import com.example.accalendar.views.FilterView;
import com.google.android.flexbox.FlexboxLayout;

import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles

    // child data in format of header title, child title
    private HashMap<String, HashMap<String, Boolean>> _listDataChild;
    private HashMap<Integer, View> views = new HashMap<>();
    private HashMap<Integer, View> groupViews = new HashMap<>();
    private RecyclerviewAdapter adapter;
    private Map<String, Object> fish;
    private ArrayList<Boolean> isNorth;
    private int filterCount = 0;
    private Map<String, Object> fishCopy;
    private Map<String, Object> caught;
    private Map<String, Integer> monthInts = new HashMap<>();
    private String query = "";

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, HashMap<String, Boolean>> listChildData,
                                 RecyclerviewAdapter adapter, Map<String, Object> fish, ArrayList<Boolean> isNorth,
                                 Map<String, Object> fishCopy, Map<String, Object> caught) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.adapter = adapter;
        this.fish = fish;
        this.isNorth = isNorth;
        this.fishCopy = fishCopy;
        this.caught = caught;

        monthInts.put("JAN", 1);
        monthInts.put("FEB", 2);
        monthInts.put("MAR", 3);
        monthInts.put("APR", 4);
        monthInts.put("MAY", 5);
        monthInts.put("JUN", 6);
        monthInts.put("JUL", 7);
        monthInts.put("AUG", 8);
        monthInts.put("SEP", 9);
        monthInts.put("OCT", 10);
        monthInts.put("NOV", 11);
        monthInts.put("DEC", 12);

    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition));
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        return views.get(groupPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    public View getChild(final int groupPosition) {
        View convertView;
        final HashMap<String, Boolean> filters = (HashMap<String, Boolean>) getChild(groupPosition, 1);
        if (!views.containsKey(groupPosition)) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandabblelistitem, null);
            FlexboxLayout listChild = convertView.findViewById(R.id.filterbox);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    7,
                    _context.getResources().getDisplayMetrics()
            );

            params.setMargins(px, px, px, px);
            for (Map.Entry<String, Boolean> filter : filters.entrySet()) {
                final String key = filter.getKey();
                Button button = new Button(this._context);
                button.setLayoutParams(params);
                button.setMinHeight(0);
                px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        51,
                        _context.getResources().getDisplayMetrics()
                );
                button.setMinWidth(px);
                button.setMinimumWidth(px);
                button.setMinimumHeight(0);
                px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        _context.getResources().getDisplayMetrics()
                );
                button.setPadding(px, px, px, px);
                Typeface typeface = Typeface.createFromAsset(_context.getAssets(), "fonts/josefin_sans_semibold.ttf");
                button.setTypeface(typeface);
                button.setTextSize(15);
                button.setText(key);
                button.setTextColor(Color.WHITE);
                button.setBackground(_context.getResources().getDrawable(R.drawable.fish_filter_off_button));
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean tf = !filters.get(key);
                        filters.put(key, tf);
                        System.out.println(filters);
                        if (tf) {
                            v.setBackground(_context.getResources().getDrawable(R.drawable.fish_filter_on_button));
                            filterCount += 1;
                            filter();
                        }
                        else {
                            v.setBackground(_context.getResources().getDrawable(R.drawable.fish_filter_off_button));
                            filterCount -= 1;
                            filter();
                        }
                    }
                });
                listChild.addView(button);
            }
            views.put(groupPosition, convertView);

        }
        return views.get(groupPosition);
    }

    private void filter() {
        fish.clear();
        System.out.println(query);
        if(filterCount == 0) {
            if (query.equals("")) {
                fish.putAll(fishCopy);
            } else {
                for (Map.Entry<String, Object> f : fishCopy.entrySet()) {
                    String fishName = f.getKey();
                    System.out.println(fishName.toLowerCase().contains(query));
                    if(fishName.contains(query)) {
                        fish.put(fishName, f.getValue());
                    }
                }
            }
            adapter.notifyDataSetChanged();
            return;
        }
        for(Map.Entry<String, Object> f : fishCopy.entrySet()) {
            Map<String, Object> fishData = (Map<String, Object>) f.getValue();
            String fishName = f.getKey();
            fishName = fishName.toLowerCase();
            boolean overallValid = true;
            if(fishName.contains(query)){
                for(Map.Entry<String, HashMap<String, Boolean>> filterGroup : _listDataChild.entrySet()) {
                    String group = filterGroup.getKey();
                    String key;
                    if (group == "Locations")
                        key = "location";
                    else if (group == "Times")
                        key = "times";
                    else if (group == "Shadow Sizes")
                        key = "shadow size";
                    else if (group == "Caught")
                        key = "caught";
                    else {
                        if (isNorth.get(0))
                            key = "north";
                        else
                            key = "south";
                    }
                    Map<String, Boolean> filters = filterGroup.getValue();
                    boolean isValid = false;
                    boolean hasFilter = false;
                    for(Map.Entry<String, Boolean> filter : filters.entrySet()) {
                        boolean tf = filter.getValue();
                        String value = filter.getKey();
                        if (tf) {
                            hasFilter = true;
                            if (key == "times" || key == "north" || key == "south") {
                                ArrayList<Map<String, Long>> times = (ArrayList<Map<String, Long>>) fishData.get(key);
                                for (int i = 0; i < times.size(); i++) {
                                    Map<String, Long> time = times.get(i);
                                    if (key == "north" || key == "south") { // this is a month
                                        if (time.get("start") <= monthInts.get(value) && time.get("end") >= monthInts.get(value))
                                            isValid = true;
                                    } else { // this is a time
                                        if (value.equals("All Day")) {
                                            if (time.get("start") == 0 && time.get("end") == 24)
                                                isValid = true;
                                        } else if (value.equals("4 AM - 9 PM")) {
                                            if (time.get("start") == 4 && time.get("end") == 21)
                                                isValid = true;
                                        } else if (value.equals("4 PM - 9 AM")) {
                                            if (time.get("start") == 16 && time.get("end") == 9)
                                                isValid = true;
                                        } else if (value.equals("9 AM - 4 PM")) {
                                            if (time.get("start") == 9 && time.get("end") == 16)
                                                isValid = true;
                                        } else {
                                            if (time.get("start") == 21 && time.get("end") == 4)
                                                isValid = true;
                                        }
                                    }
                                }
                            } else if (key == "caught") {
                                if(caught.containsKey(f.getKey())) {
                                    boolean hasCaught = (boolean) caught.get(f.getKey());
                                    if (value.equals("Caught") && hasCaught)
                                        isValid = true;
                                    else if (value.equals("Not Caught") && !hasCaught)
                                        isValid = true;
                                }
                            } else {
                                String fishValue = (String) fishData.get(key);
                                if (fishValue.equals(value))
                                    isValid = true;
                            }
                        }
                    }
                    if (!isValid && hasFilter){
                        overallValid = false;
                        break;
                    }
                }
                if (overallValid)
                    fish.put(f.getKey(), fishData);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void search(String query) {
        this.query = query.toLowerCase();
        filter();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (groupViews.get(groupPosition) == null) {
            String headerTitle = (String) getGroup(groupPosition);
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandablelistgroup, null);
            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.text_title);
            Typeface typeface = Typeface.createFromAsset(_context.getAssets(), "fonts/josefin_sans_semibold.ttf");
            lblListHeader.setTypeface(typeface);
            lblListHeader.setText(headerTitle);
            groupViews.put(groupPosition, convertView);
        }

        return groupViews.get(groupPosition);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
