package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.model.Search;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class SearchAdapter extends ArrayAdapter<Search> {

	Context mContext;
	int layoutResourceId;
	ArrayList<Search> data;
	private Search search;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param layoutResourceId
	 * @param data
	 */
	public SearchAdapter(Context context, int layoutResourceId, ArrayList<Search> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.mContext = context;
		this.data = data;
	}

	/**
	 * getView
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SearchHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, null);
			holder = new SearchHolder();
			holder.txtTitle = (TextView) convertView.findViewById(R.id.txt_history_action);
			holder.txtUserName = (TextView) convertView.findViewById(R.id.txt_history_text);
			holder.imgScandaloh = (FetchableImageView) convertView.findViewById(R.id.img_history_scandaloh);
			convertView.setTag(holder);
		}

		else {
			holder = (SearchHolder) convertView.getTag();
		}

		// Rellenamos los datos
		search = data.get(position);

		holder.txtTitle.setText(search.getTitle());
		holder.txtUserName.setText(search.getUserName());
		holder.imgScandaloh.setImage(MyApplication.DIRECCION_BUCKET + search.getUrlScandal(),
				mContext.getResources().getDrawable(R.drawable.logo_blanco));

		return convertView;
	}

	/**
	 * Clase Holder
	 * 
	 */
	static class SearchHolder {
		FetchableImageView imgScandaloh;
		TextView txtTitle;
		TextView txtUserName;

		public TextView getTitle() {
			return txtTitle;
		}
		
		public TextView getUserName(){
			return txtUserName;
		}

		public FetchableImageView getAvatar() {
			return imgScandaloh;
		}
	}

}
