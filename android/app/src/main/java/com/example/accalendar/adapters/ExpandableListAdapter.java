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

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, HashMap<String, Boolean>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
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

    public View getChild(int groupPosition) {
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
                        if (tf)
                            v.setBackground(_context.getResources().getDrawable(R.drawable.fish_filter_on_button));
                        else
                            v.setBackground(_context.getResources().getDrawable(R.drawable.fish_filter_off_button));
                    }
                });
                listChild.addView(button);
            }
            views.put(groupPosition, convertView);

        }
        return views.get(groupPosition);
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
