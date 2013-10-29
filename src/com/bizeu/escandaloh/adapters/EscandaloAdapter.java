package com.bizeu.escandaloh.adapters;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.bizeu.escandaloh.DetailCommentsActivity;
import com.bizeu.escandaloh.DetailPhotoActivity;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.model.Escandalo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EscandaloAdapter extends ArrayAdapter<Escandalo> {

	    Context context; 
	    int layoutResourceId;    
	    ArrayList<Escandalo> data;
	    private int available_height;	    
	    private Escandalo escanda;
	    private int pos;
	    
	    private ImageView foto_escandalo;
	    
	    
	    /**
	     * Constructor
	     * @param context
	     * @param layoutResourceId
	     * @param data
	     */
	    public EscandaloAdapter(Context context, int layoutResourceId, ArrayList<Escandalo> data) {
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
	        EscandaloHolder holder = null;

	        escanda = data.get(position);
	        
	        if(mView == null){
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            mView = inflater.inflate(layoutResourceId, parent, false);
         
	            available_height = getAvailableHeightScreen();            
	                        
	            holder = new EscandaloHolder();
	            holder.txtTitle = (TextView)mView.findViewById(R.id.txt_titulo);
	            holder.imgCategory = (ImageView)mView.findViewById(R.id.img_categoria);
	            holder.imgPicture = (ImageView)mView.findViewById(R.id.img_foto);
	            holder.txtNumComments = (TextView)mView.findViewById(R.id.txt_numero_comentarios); 
	        
	            mView.setTag(holder);
	        }
	        
	        else{
	            holder = (EscandaloHolder)mView.getTag();
	        }
	        
            
            // Cambiamos el alto por código
            holder.lheight = (LinearLayout)mView.findViewById(R.id.l_escandalo);
            LayoutParams params4 = holder.lheight.getLayoutParams();
            params4.height = available_height;
            holder.lheight.setLayoutParams(params4);
	        
	        
	        holder.imgPicture.setImageBitmap(escanda.getPicture());
	        holder.txtTitle.setText(escanda.getTitle());
	        holder.txtNumComments.setText(Integer.toString(escanda.getNumComments()));
	        
	        if (escanda.getCategory().equals(Escandalo.HAPPY)){
	        	holder.imgCategory.setImageResource(R.drawable.cara_riendose);
	        }
	        else{
	        	holder.imgCategory.setImageResource(R.drawable.cara_enfadado);
	        }
	        
	        
            // Listener para la foto
            holder.imgPicture.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {							
					Intent i = new Intent(context, DetailPhotoActivity.class);
					ImageView imView = (ImageView) v;
					Bitmap bitm = ((BitmapDrawable)imView.getDrawable()).getBitmap();
					Log.v("WE","route img: " + escanda.getRouteImg());
					i.putExtra("route_img", "/api/v1/photo/117/");
					//i.putExtra("bitmap", bitm);
					context.startActivity(i);
				}
			});
            
            // Listener para los comentarios  
            holder.txtNumComments.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(context, DetailCommentsActivity.class);
					i.putExtra("resource_uri", "/api/v1/photo/117/");
					context.startActivity(i);	
				}
			});
	        
	        return mView;
	    }
	    
	    
	    
	    
	    /**
	     * Clase Holder
	     * @author Alejandro
	     *
	     */
	    public static class EscandaloHolder{
	        TextView txtTitle;
	        ImageView imgCategory;
	        ImageView imgPicture;
	        TextView txtNumComments;
	        LinearLayout lheight;
	        
	        public ImageView getPicture(){
	        	return imgPicture;
	        }
	        
	        public TextView getNumComments(){
	        	return txtNumComments;
	        }
	        
	    }
	    
   
		/**
		 * Devuelve el alto disponible de la pantalla: alto total - ( alto action bar + alto status bar)
		 * @return
		 */
		private int getAvailableHeightScreen(){
			
			int action_bar_height = 0;
			int status_bar_height = 0;
			int available_height = 0;
			
			// Screen height
			DisplayMetrics display = context.getResources().getDisplayMetrics();

	        int height = display.heightPixels;
			
			// Action bar height
	        TypedValue tv = new TypedValue();
	        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
	           if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
	        	   action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
	        }
	        else if(context.getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
	        	action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
	        }
			
			// Status bar height
			int resourceId = context.getResources().getIdentifier("status_bar_height",
					"dimen", "android");	
			if (resourceId > 0) {
				status_bar_height = context.getResources().getDimensionPixelSize(resourceId);
			}

			// Available height
			available_height = height - action_bar_height - status_bar_height;
			
			return available_height;
		}
}
