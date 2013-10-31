package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.model.Comment;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CommentAdapter extends ArrayAdapter<Comment> {

    Context context; 
    int layoutResourceId;    
    ArrayList<Comment> data;  
    CommentHolder holder;
    private Comment comment;
	
    /**
     * Constructor
     * @param context
     * @param layoutResourceId
     * @param data
     */
    public CommentAdapter(Context context, int layoutResourceId, ArrayList<Comment> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
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
        
            mView.setTag(holder);
        }
        
        else{
            holder = (CommentHolder)mView.getTag();
        }
        	        
        holder.txtText.setText(comment.getText());
        holder.txtUsername.setText(comment.getUsername());
        holder.txtDate.setText(comment.getDate());      
        
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
