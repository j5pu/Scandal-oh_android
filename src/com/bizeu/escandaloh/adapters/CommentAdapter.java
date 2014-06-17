package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.users.ProfileActivity;
import com.bizeu.escandaloh.util.ImageViewRounded;
import com.bizeu.escandaloh.util.Utils;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

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
        holder.imgAvatar = (ImageViewRounded)convertView.findViewById(R.id.img_comment_avatar);
        holder.imgUser = (ImageView)convertView.findViewById(R.id.img_comment_socialnetwork);                                 

        // Rellenamos los datos	 y asignamos onClick
        OnClickListener onClickLisPerfil = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Le llevamos al perfil del usuario
				Intent i = new Intent(mContext, ProfileActivity.class);
				i.putExtra(ProfileActivity.USER_ID, v.getTag().toString());
				mContext.startActivity(i);		
			}
		};
        
        holder.txtText.setText(comment.getText());
        holder.txtUsername.setText(comment.getUsername());
        holder.txtUsername.setTag(comment.getUserId());
        holder.txtUsername.setOnClickListener(onClickLisPerfil);
        holder.imgAvatar.setImage(MyApplication.DIRECCION_BUCKET + comment.getAvatar(), mContext.getResources().getDrawable(R.drawable.avatar_defecto));
        holder.imgAvatar.setTag(comment.getUserId());
        holder.imgAvatar.setOnClickListener(onClickLisPerfil);
        
        
        // La fecha tendrá el formato: dd-mm-aaaa
        holder.txtDate.setText(Utils.changeDateFormat(comment.getDate())); 
        
        // El icono del usuario dependerá de la red social de éste
        // Scandaloh
        int social_net = Integer.parseInt(comment.getSocialNetwork());
        if (social_net == 0){
        	holder.imgUser.setImageResource(R.drawable.s_circular_gris);
        }
        // Facebook
        else if (social_net == 1){
        	holder.imgUser.setImageResource(R.drawable.f_circular_gris);
        }  
        
        holder.imgUser.setTag(comment.getUserId());
        holder.imgUser.setOnClickListener(onClickLisPerfil);
             
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
        ImageViewRounded imgAvatar;
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
