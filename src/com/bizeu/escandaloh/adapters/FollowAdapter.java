package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.model.Follow;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FollowAdapter extends ArrayAdapter<Follow> {

	 Context mContext; 
	 int layoutResourceId;  
	 ArrayList<Follow> data;  
	 private Follow follow;
		

	 public FollowAdapter(Context context, int layoutResourceId, ArrayList<Follow> data) {
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

		 FollowHolder holder;
	    	
		 if (convertView == null){
			 LayoutInflater inflater = ((Activity)mContext).getLayoutInflater(); 
			 convertView = inflater.inflate(layoutResourceId, null);        
			 holder = new FollowHolder();
			 holder.txtUserName = (TextView) convertView.findViewById(R.id.txt_follow_username);
			 holder.imgAvatar = (FetchableImageView) convertView.findViewById(R.id.img_follow_avatar);
			 convertView.setTag(holder);
		 }
	    	
		 else{
			 holder = (FollowHolder) convertView.getTag();
		 }
	    	
		 // Rellenamos los datos	
		 follow = data.get(position);  
	        
		 holder.txtUserName.setText(follow.getName());	        
		 holder.imgAvatar.setImage(MyApplication.DIRECCION_BUCKET + follow.getUrlAvatar(), mContext.getResources().getDrawable(R.drawable.avatar_defecto));
	             
		 return convertView;
	 }
	    

	    
	    
	 /**
	  * Clase Holder
	  *
	  */
	 static class FollowHolder{
		 FetchableImageView imgAvatar;
		 TextView txtUserName;
	        
		 public TextView getUserName(){
			 return txtUserName;
		 }
	        
		 public FetchableImageView getAvatar(){
			 return imgAvatar;
		 }  
	 }
	    
	}

