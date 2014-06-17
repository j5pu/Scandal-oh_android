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
import com.bizeu.escandaloh.model.Search;
import com.bizeu.escandaloh.util.Utils;
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
			holder.txtTitle = (TextView) convertView.findViewById(R.id.txt_search_title);
			holder.txtUserName = (TextView) convertView.findViewById(R.id.txt_search_username);
			holder.imgScandaloh = (FetchableImageView) convertView.findViewById(R.id.img_search_scandaloh);
			holder.txtDate = (TextView) convertView.findViewById(R.id.txt_search_date);
			holder.txtLikes = (TextView) convertView.findViewById(R.id.txt_search_likes);
			holder.txtDislikes = (TextView) convertView.findViewById(R.id.txt_search_dislikes);
			holder.txtNumComments = (TextView) convertView.findViewById(R.id.txt_search_numcomm);
			holder.viewLinea = (View) convertView.findViewById(R.id.view_search_linea_color);
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
		holder.txtLikes.setText(search.getLikes());
		holder.txtDislikes.setText(search.getDislikes());
		holder.txtNumComments.setText(search.getNumComments());
		holder.txtDate.setText(Utils.changeDateFormat(search.getDate()));
		
		// Color de la linea dependiendo de si el escándalo es de humor o denuncia
		if (search.getCategory().equals("1")){
			holder.viewLinea.setBackgroundColor(mContext.getResources().getColor(R.color.morado));
		}
		else{
			holder.viewLinea.setBackgroundColor(mContext.getResources().getColor(R.color.azul));
		}	

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
		TextView txtDate;
		TextView txtLikes;
		TextView txtDislikes;
		TextView txtCategory;
		TextView txtNumComments;
		View viewLinea;

		public TextView getTitle() {
			return txtTitle;
		}
		
		public TextView getUserName(){
			return txtUserName;
		}

		public FetchableImageView getAvatar() {
			return imgScandaloh;
		}
		
		public TextView getDate(){
			return txtDate;
		}
		
		public TextView getLikes(){
			return txtLikes;
		}
		
		public TextView getDislikes(){
			return txtDislikes;
		}
		
		public TextView getCategory(){
			return txtCategory;
		}
		
		public TextView getNumComments(){
			return txtNumComments;
		}
		
		public View getLinea(){
			return viewLinea;
		}
	}

}
