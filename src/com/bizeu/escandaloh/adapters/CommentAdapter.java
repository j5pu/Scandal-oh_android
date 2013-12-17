package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.model.Comment;

public class CommentAdapter extends ArrayAdapter<Comment> {

    Context context; 
    int layoutResourceId;    
    ArrayList<Comment> data;  
    CommentHolder holder;
    private Comment comment;
    private String user_name_owner;
	
    /**
     * Constructor
     * @param context
     * @param layoutResourceId
     * @param data
     */
    public CommentAdapter(Context context, int layoutResourceId, ArrayList<Comment> data, String user_name) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.user_name_owner = user_name;
    }

    
    /**
     * getView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mView = convertView;
        holder = null;

        comment = data.get(position);
        
        if(mView == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            mView = inflater.inflate(layoutResourceId, parent, false);
                                   
            holder = new CommentHolder();
            holder.txtText = (TextView)mView.findViewById(R.id.txt_comment_text);
            holder.txtUsername = (TextView)mView.findViewById(R.id.txt_comment_username);
            holder.txtDate = (TextView)mView.findViewById(R.id.txt_comment_date);
            holder.imgUser = (ImageView)mView.findViewById(R.id.img_comment_user);
            holder.imgClock = (ImageView)mView.findViewById(R.id.img_comment_clock);
            
              
            mView.setTag(holder);
        }
        
        else{
            holder = (CommentHolder)mView.getTag();
            
            // Si soy el usuario del comentario aparecerá en azul el nombre y la fecha
            if (user_name_owner.equals(comment.getUsername())){
            	holder.txtUsername.setTextColor(context.getResources().getColor(R.color.azul));
            	holder.txtDate.setTextColor(context.getResources().getColor(R.color.azul));
            	holder.imgUser.setImageResource(R.drawable.user_azul);
            	holder.imgClock.setImageResource(R.drawable.reloj_azul);
            }
        }
        	        
        holder.txtText.setText(comment.getText());
        holder.txtUsername.setText(comment.getUsername());
        
        // La fecha tendrá el formato: dd-mm-aaaa
        String date_without_time = (comment.getDate().split("T",2))[0];   
        String year = date_without_time.split("-",3)[0];
        String month = date_without_time.split("-",3)[1];
        String day = date_without_time.split("-",3)[2];
        String final_date = day + "-" + month + "-" + year;
        holder.txtDate.setText(final_date); 
        
        
        // Si soy el usuario del comentario aparecerá en azul el nombre y la fecha
        if (MyApplication.resource_uri.equals(comment.getResourceuri())){
        	holder.txtUsername.setTextColor(context.getResources().getColor(R.color.azul));
        	holder.txtDate.setTextColor(context.getResources().getColor(R.color.azul));
        	holder.imgUser.setImageResource(R.drawable.user_azul);
        	holder.imgClock.setImageResource(R.drawable.reloj_azul);
        }
        else{
        	holder.txtUsername.setTextColor(context.getResources().getColor(R.color.gris_oscuro));
        	holder.txtDate.setTextColor(context.getResources().getColor(R.color.gris_oscuro));
        	holder.imgUser.setImageResource(R.drawable.user);
        	holder.imgClock.setImageResource(R.drawable.reloj);
        }
             
        return mView;
    }
    
    
    
    
    /**
     * Clase Holder
     * @author Alejandro
     *
     */
    public static class CommentHolder{
        TextView txtText;
        TextView txtUsername;
        TextView txtDate; 
        ImageView imgUser;
        ImageView imgClock;
        
        public TextView getText(){
        	return txtText;
        }
        
        public TextView getUsername(){
        	return txtUsername;
        }
        
        public TextView getDate(){
        	return txtDate;
        }     
    }
    
}
