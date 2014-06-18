package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.model.Notification;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    Context mContext; 
    int layoutResourceId;  
    ArrayList<Notification> data;  
    private Notification notification;
	
    
	 /**
     * Constructor
     * @param context
     * @param layoutResourceId
     * @param data
     */
    public NotificationAdapter(Context context, int layoutResourceId, ArrayList<Notification> data) {
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

    	NotificationHolder holder;
    	
    	if (convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater(); 
            convertView = inflater.inflate(layoutResourceId, null);        
            holder = new NotificationHolder();
            holder.txtDate = (TextView) convertView.findViewById(R.id.txt_notification_date);
            holder.txtText = (TextView) convertView.findViewById(R.id.txt_notification_text);
            holder.imgAvatar = (FetchableImageView) convertView.findViewById(R.id.img_notification_avatar);
            holder.imgScandal = (FetchableImageView) convertView.findViewById(R.id.img_notification_scandal);
            holder.llCapaBlanca = (View) convertView.findViewById(R.id.ll_notification_capablanca);
            convertView.setTag(holder);
    	}
    	
    	else{
    		 holder = (NotificationHolder) convertView.getTag();
    	}
    	
        // Rellenamos los datos	
        notification = data.get(position);  
        
        // Texto
        holder.txtText.setText(notification.getText());
        
        // Fecha
        holder.txtDate.setText(notification.getDate());
        
        // Notificación leida: ponemos la capa blanca y el texto normal
        if (notification.isRead()){
        	holder.llCapaBlanca.setVisibility(View.VISIBLE);
        	holder.txtText.setTypeface(null, Typeface.NORMAL);
        }
        
        // Notificación no leída: quitamos la capa blanca y ponemos letra en bold
        else{
        	holder.llCapaBlanca.setVisibility(View.INVISIBLE);
        	holder.txtText.setTypeface(null, Typeface.BOLD);
        }
        
        // Avatar
        holder.imgAvatar.setImage(MyApplication.DIRECCION_BUCKET + notification.getAvatar(), mContext.getResources().getDrawable(R.drawable.avatar_defecto));

        // Si la notificación es de tipo usuario ocultamos el escándalo
        if (notification.getType() == 6){
        	holder.imgScandal.setVisibility(View.GONE);
        }
        // Si no, mostramos el escándalo
        else{
        	holder.imgScandal.setVisibility(View.VISIBLE);
            holder.imgScandal.setImage(MyApplication.DIRECCION_BUCKET + notification.getScandal(), mContext.getResources().getDrawable(R.drawable.avatar_defecto));
        }
        
         
        return convertView;
    }
    

    
    
    /**
     * Clase Holder
     *
     */
     static class NotificationHolder{
    	FetchableImageView imgAvatar;
    	FetchableImageView imgScandal;
    	View llCapaBlanca;
        TextView txtText;
        TextView txtDate;
        
        public TextView getText(){
        	return txtText;
        }
        
        public FetchableImageView getAvatar(){
        	return imgAvatar;
        }  
        
        public FetchableImageView getScandal(){
        	return imgScandal;
        }
        
        public TextView getDate(){
        	return txtDate;
        }
        
        public View getCapaBlanca(){
        	return llCapaBlanca;
        }
    }
    
}
