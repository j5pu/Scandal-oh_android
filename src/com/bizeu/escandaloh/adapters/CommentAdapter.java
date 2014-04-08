package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.bizeu.escandaloh.model.Comment;

public class CommentAdapter extends ArrayAdapter<Comment> {

    Context mContext; 
    int layoutResourceIdIzquierda;  
    int layoutResourceIdDerecha;
    ArrayList<Comment> data;  
    private Comment comment;
	
    
    /**
     * Constructor
     * @param context
     * @param layoutResourceId
     * @param data
     */
    public CommentAdapter(Context context, int layoutResourceIdIzquierda, int layoutResourceIdDerecha, ArrayList<Comment> data) {
        super(context, layoutResourceIdIzquierda, layoutResourceIdDerecha, data);
        this.layoutResourceIdIzquierda = layoutResourceIdIzquierda;
        this.layoutResourceIdDerecha = layoutResourceIdDerecha;
        this.mContext = context;
        this.data = data;
    }

    
    
    /**
     * getView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	CommentHolder holder ;

        comment = data.get(position);
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            
        // Si soy el usuario del comentario, éste aparecerá a la derecha
        if (MyApplication.user_name.equals(comment.getUsername())){
        	convertView = inflater.inflate(layoutResourceIdDerecha, parent, false);
        }
        // Si no, a la izquierda
        else{
        	convertView = inflater.inflate(layoutResourceIdIzquierda, parent, false);
        }
                           
        holder = new CommentHolder();
        holder.txtText = (TextView)convertView.findViewById(R.id.txt_comment_text);
        holder.txtUsername = (TextView)convertView.findViewById(R.id.txt_comment_username);
        holder.txtDate = (TextView)convertView.findViewById(R.id.txt_comment_date);
        holder.imgAvatar = (FetchableImageView)convertView.findViewById(R.id.img_comment_avatar);
        holder.imgUser = (ImageView)convertView.findViewById(R.id.img_comment_socialnetwork);                                 

        // Rellenamos los datos	        
        holder.txtText.setText(comment.getText());
        holder.txtUsername.setText(comment.getUsername());
        holder.imgAvatar.setImage(MyApplication.DIRECCION_BUCKET + comment.getAvatar(), mContext.getResources().getDrawable(R.drawable.avatar_defecto));
     
        // La fecha tendrá el formato: dd-mm-aaaa
        String date_without_time = (comment.getDate().split("T",2))[0];   
        String year = date_without_time.split("-",3)[0];
        String month = date_without_time.split("-",3)[1];
        String day = date_without_time.split("-",3)[2];
        String final_date = day + "-" + month + "-" + year;
        holder.txtDate.setText(final_date); 
        
        // El icono del usuario dependerá de la red social de éste
        // Scandaloh
        int social_net = Integer.parseInt(comment.getSocialNetwork());
        if (social_net == 0){
        	holder.imgUser.setImageResource(R.drawable.s_rosa);
        }
        // Facebook
        else if (social_net == 1){
        	holder.imgUser.setImageResource(R.drawable.facebook_rosa);
        }  
             
        return convertView;
    }
    

    
    
    /**
     * Clase Holder
     *
     */
     static class CommentHolder{
        TextView txtText;
        TextView txtUsername;
        TextView txtDate; 
        ImageView imgUser;
        ImageView imgClock;
        FetchableImageView imgAvatar;
        LinearLayout info_comment_user;
        LinearLayout info_comment_text;
        
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
