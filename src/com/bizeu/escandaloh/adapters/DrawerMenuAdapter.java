package com.bizeu.escandaloh.adapters;

import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerMenuAdapter extends BaseAdapter {

    Context context;
    String[] options;
    LayoutInflater inflater;
    
    
    public DrawerMenuAdapter(Context context, String[] options) {
        this.context = context;
        this.options = options;
    }
    
	@Override
	public int getCount() {
        return options.length;
	}

	@Override
	public Object getItem(int position) {
        return options[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        TextView txtOption;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.menu_lateral_item, parent,false);
 
        txtOption = (TextView) mView.findViewById(R.id.txt_menu_lateral_item);
        txtOption.setText(options[position]);
 
        return mView;
	}
}
