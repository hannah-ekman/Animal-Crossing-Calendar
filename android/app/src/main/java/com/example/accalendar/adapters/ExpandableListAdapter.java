package com.example.accalendar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.example.accalendar.R;
import com.example.accalendar.utils.Art;
import com.example.accalendar.utils.ClassUtils;
import com.example.accalendar.utils.DeepSea;
import com.example.accalendar.utils.Fish;
import com.example.accalendar.utils.Villager;
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
    private ArrayList<Object> items;
    private ArrayList<Boolean> isNorth;
    private int filterCount = 0;
    private ArrayList<Object> itemsCopy;
    private Map<String, Object> caught;
    private Map<String, Object> donated;
    private Map<String, Integer> monthInts = new HashMap<>();
    private Map<String, Integer> timeInts = new HashMap<>();
    private String query = "";
    private int tabColor, buttonOnDrawable, buttonOffDrawable;
    private ArrayList<Button> buttons = new ArrayList<>();

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, HashMap<String, Boolean>> listChildData,
                                 RecyclerviewAdapter adapter, ArrayList<Object> items, ArrayList<Boolean> isNorth,
                                 ArrayList<Object> itemsCopy, Map<String, Object> caught, Map<String, Object> donated,
                                 int tabColor, int buttonOnDrawable, int buttonOffDrawable) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.adapter = adapter;
        this.items = items;
        this.itemsCopy = itemsCopy;
        this.isNorth = isNorth;
        this.caught = caught;
        this.donated = donated;
        this.tabColor = tabColor;
        this.buttonOnDrawable = buttonOnDrawable;
        this.buttonOffDrawable = buttonOffDrawable;

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

        timeInts.put("24", 0);
        timeInts.put("1", 1);
        timeInts.put("2", 2);
        timeInts.put("3", 3);
        timeInts.put("4", 4);
        timeInts.put("5", 5);
        timeInts.put("6", 6);
        timeInts.put("7", 7);
        timeInts.put("8", 8);
        timeInts.put("9", 9);
        timeInts.put("10", 10);
        timeInts.put("11", 11);
        timeInts.put("12", 12);
        timeInts.put("13", 13);
        timeInts.put("14", 14);
        timeInts.put("15", 15);
        timeInts.put("16", 16);
        timeInts.put("17", 17);
        timeInts.put("18", 18);
        timeInts.put("19", 19);
        timeInts.put("20", 20);
        timeInts.put("21", 21);
        timeInts.put("22", 22);
        timeInts.put("23", 23);
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
                button.setBackground(_context.getResources().getDrawable(buttonOffDrawable));
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean tf = !filters.get(key);
                        filters.put(key, tf);
                        System.out.println(filters);
                        if (tf) {
                            v.setBackground(_context.getResources().getDrawable(buttonOnDrawable));
                            filterCount += 1;
                            filter();
                        }
                        else {
                            v.setBackground(_context.getResources().getDrawable(buttonOffDrawable));
                            filterCount -= 1;
                            filter();
                        }
                    }
                });
                listChild.addView(button);
                buttons.add(button);
            }
            views.put(groupPosition, convertView);

        }
        return views.get(groupPosition);
    }

    public void clear() {
        filterCount = 0;
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            b.setBackground(_context.getResources().getDrawable(buttonOffDrawable));
        }
    }

    public void filter() {
        items.clear();
        System.out.println(query);
        if(filterCount == 0) {
            if (query.equals("")) {
                items.addAll(itemsCopy);
            } else {
                for (int i = 0; i<itemsCopy.size(); i++) {
                    ClassUtils.Trackable t = (ClassUtils.Trackable) itemsCopy.get(i);
                    String fishName = t.name;
                    System.out.println(fishName.toLowerCase().contains(query));
                    if(fishName.contains(query)) {
                        items.add(t);
                    }
                }
            }
            adapter.notifyDataSetChanged();
            return;
        }
        for(int i = 0; i<itemsCopy.size(); i++) {
            ClassUtils.Trackable t = (ClassUtils.Trackable) itemsCopy.get(i);
            String fishName = t.name;
            fishName = fishName.toLowerCase();
            boolean overallValid = true;
            if(fishName.contains(query)){
                for(Map.Entry<String, HashMap<String, Boolean>> filterGroup : _listDataChild.entrySet()) {
                    String group = filterGroup.getKey();
                    Map<String, Boolean> filters = filterGroup.getValue();
                    boolean isValid = false;
                    boolean hasFilter = false;
                    for(Map.Entry<String, Boolean> filter : filters.entrySet()) {
                        boolean tf = filter.getValue();
                        String value = filter.getKey();
                        if (tf) {
                            hasFilter = true;
                            if (group.equals("Times") || group.equals("Months")) {
                                ArrayList<ClassUtils.Available> times = ((ClassUtils.Catchable) t).times;
                                if(group.equals("Months") && isNorth.get(0))
                                    times = ((ClassUtils.Catchable) t).north;
                                else if (group.equals("Months"))
                                    times = ((ClassUtils.Catchable) t).south;

                                for (int j = 0; j < times.size(); j++) {
                                    ClassUtils.Available time = times.get(j);
                                    if (group.equals("Months")) {
                                        if (time.start < time.end)
                                            if (time.start <= monthInts.get(value) && time.end >= monthInts.get(value))
                                                isValid = true;
                                    } else { // this is a time
                                        if (value.equals("All Day")) {
                                            if (time.start == 0 && time.end == 24)
                                                isValid = true;
                                        } else if (value.equals("4 AM - 9 PM")) {
                                            if (time.start == 4 && time.end == 21)
                                                isValid = true;
                                        } else if (value.equals("4 PM - 9 AM")) {
                                            if (time.start == 16 && time.end == 9)
                                                isValid = true;
                                        } else if (value.equals("9 AM - 4 PM")) {
                                            if (time.start == 9 && time.end == 16)
                                                isValid = true;
                                        } else if (value.equals("9 PM - 4 AM")) {
                                            if (time.start == 21 && time.end == 4)
                                                isValid = true;
                                        }else if(time.start < time.end) {
                                            if (time.start <= timeInts.get(value) && time.end >= timeInts.get(value))
                                                isValid = true;
                                        } else {
                                            if (time.start <= timeInts.get(value) || time.end >= timeInts.get(value))
                                                isValid = true;
                                        }
                                    }
                                }
                            } else if (group.equals("Blathers") || group.equals("Residents")) {
                                if(caught.containsKey(t.name) && donated.containsKey(t.name)) {
                                    boolean hasCaught = (boolean) caught.get(t.name);
                                    boolean hasDonated = (boolean) donated.get(t.name);
                                    if ((value.equals("Caught") || value.equals("Resident")) && hasCaught)
                                        isValid = true;
                                    else if ((value.equals("Not Caught") || value.equals("Not Resident")) && !hasCaught)
                                        isValid = true;
                                    else if ((value.equals("Donated") || value.equals("Dreamie")) && hasDonated)
                                        isValid = true;
                                    else if ((value.equals("Not Donated") || value.equals("Not Dreamie")) && !hasDonated)
                                        isValid = true;
                                } else {
                                    if (value.equals("Not Caught") || value.equals("Not Resident"))
                                        isValid = true;
                                    else if (value.equals("Not Donated") || value.equals("Not Dreamie"))
                                        isValid = true;
                                }
                            } else if (group.equals("Birthday Months")) {
                                Villager v = (Villager) t;
                                int curValue = v.birthday.month;
                                if (curValue == monthInts.get(value))
                                    isValid = true;
                            } else {
                                String itemValue = "";
                                if (t instanceof Fish)
                                    itemValue = ((Fish) t).get(group);
                                else if (t instanceof DeepSea)
                                    itemValue = ((DeepSea) t).get(group);
                                else if (t instanceof Art)
                                    itemValue = ((Art) t).get(group);
                                else if (t instanceof Villager)
                                    itemValue = ((Villager) t).get(group);
                                else if (t instanceof ClassUtils.Catchable)
                                    itemValue = ((ClassUtils.Catchable) t).get(group);
                                if (itemValue.equals(value))
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
                    items.add(t);
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
            convertView.setBackgroundResource(tabColor);
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
