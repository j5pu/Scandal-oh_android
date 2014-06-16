package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.model.History;
import com.bizeu.escandaloh.util.Utils;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<History> {
	
	Context mContext;
	int layoutResourceId;
	ArrayList<History> data;
	private History history;
	
	
	/**
	 * Constructor
	 * 
	 * @param context
	 * @param layoutResourceId
	 * @param data
	 */
	public HistoryAdapter(Context context, int layoutResourceId, ArrayList<History> data) {
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

		HistoryHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, null);
			holder = new HistoryHolder();
			holder.txtDate = (TextView) convertView.findViewById(R.id.txt_history_date);
			holder.txtText = (TextView) convertView.findViewById(R.id.txt_history_text);
			holder.imgScandaloh = (FetchableImageView) convertView.findViewById(R.id.img_history_scandaloh);
			holder.txtUsername = (TextView) convertView.findViewById(R.id.txt_history_username);
			holder.txtLikes = (TextView) convertView.findViewById(R.id.txt_history_likes);
			holder.txtDislikes = (TextView) convertView.findViewById(R.id.txt_history_dislikes);
			holder.txtNumComm = (TextView) convertView.findViewById(R.id.txt_history_numcomm);
			holder.viewLinea = (View) convertView.findViewById(R.id.view_history_linea_color);
			convertView.setTag(holder);
		}

		else {
			holder = (HistoryHolder) convertView.getTag();
		}

		// Rellenamos los datos
		history = data.get(position);
		holder.txtDate.setText(Utils.changeDateFormat(history.getDate()));
		holder.txtText.setText(history.getText());
		holder.txtUsername.setText(history.getUserName());
		holder.txtLikes.setText(history.getLikes());
		holder.txtDislikes.setText(history.getDislikes());
		holder.txtNumComm.setText(history.getNumComments());
		holder.imgScandaloh.setImage(MyApplication.DIRECCION_BUCKET + history.getUrl(),
				mContext.getResources().getDrawable(R.drawable.logo_blanco));
		
		// Color de la linea dependiendo de si el escándalo es de humor o denuncia
		if (history.getCategory().equals("1")){
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
	static class HistoryHolder {
		FetchableImageView imgScandaloh;;
		TextView txtText;
		TextView txtDate;
		TextView txtNumComm;
		TextView txtLikes;
		TextView txtDislikes;
		TextView txtUsername;
		View viewLinea;
		
		public TextView getText(){
			return txtText;
		}
		
		public TextView getDate(){
			return txtDate;
		}

		public FetchableImageView getImg() {
			return imgScandaloh;
		}
		
		public TextView getNumComm(){
			return txtNumComm;
		}
		
		public TextView getLikes(){
			return txtLikes;
		}
		
		public TextView getDislikes(){
			return txtDislikes;
		}
		
		public TextView getUserName(){
			return txtUsername;
		}
		
		public View getLinea(){
			return viewLinea;
		}
	}

}
