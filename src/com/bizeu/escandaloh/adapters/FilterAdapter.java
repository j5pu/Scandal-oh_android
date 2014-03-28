package com.bizeu.escandaloh.adapters;

import java.util.List;
import java.util.Map;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


public class FilterAdapter extends BaseExpandableListAdapter {
	 
    private Activity context;
    private Map<String, List<String>> filterCollection;
    private List<String> filter_header;
 
    public FilterAdapter(Activity context, List<String> filter_header, Map<String, List<String>> filterCollection) {
        this.context = context;
        this.filterCollection = filterCollection;
        this.filter_header = filter_header;
    }
 
    public Object getChild(int groupPosition, int childPosition) {
        return filterCollection.get(filter_header.get(groupPosition)).get(childPosition);
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        final String laptop = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();
 
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.filter_item, null);
        }
 
        TextView item = (TextView) convertView.findViewById(R.id.txt_filter_type);

        item.setText(laptop);
        return convertView;
    }
 
    public int getChildrenCount(int groupPosition) {
        return filterCollection.get(filter_header.get(groupPosition)).size();
    }
 
    public Object getGroup(int groupPosition) {
        return filter_header.get(groupPosition);
    }
 
    public int getGroupCount() {
        return filter_header.size();
    }
 
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String header_name = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.filter_group,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.txt_filter_header);
        item.setText(header_name);
        return convertView;
    }
 
    public boolean hasStableIds() {
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}