package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.DetailCommentsActivity;
import com.bizeu.escandaloh.DetailPhotoActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EscandaloAdapter extends ArrayAdapter<Escandalo> {

	    Context context; 
	    int layoutResourceId;    
	    ArrayList<Escandalo> data;
	    private int available_height;	    
	    private Escandalo escanda;

    
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
	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            mView = inflater.inflate(layoutResourceId, parent, false);       
	                        
	            holder = new EscandaloHolder();
	            holder.txtTitle = (TextView)mView.findViewById(R.id.txt_titulo);
	            holder.imgCategory = (ImageView)mView.findViewById(R.id.img_categoria);
	            holder.imgPicture = (FetchableImageView) mView.findViewById(R.id.img_foto);
	            holder.txtNumComments = (TextView)mView.findViewById(R.id.txt_numero_comentarios); 
	            holder.imgNumComments = (ImageView)mView.findViewById(R.id.img_num_comentarios);
	            holder.imgMicro = (ImageView)mView.findViewById(R.id.img_escandalo_microfono);
	            				  
	            mView.setTag(holder);
	        }
	        
	        else{
	            holder = (EscandaloHolder)mView.getTag();
	        }
	                
	        // Cambiamos el alto por código
            available_height = getAvailableHeightScreen();   
            holder.lheight = (LinearLayout)mView.findViewById(R.id.l_escandalo);
            LayoutParams params4 = holder.lheight.getLayoutParams();
            params4.height = available_height;
            holder.lheight.setLayoutParams(params4);
	        	
            // Pide la imagen por url y la muestra cuando la obtenga. Mientras tanto muestra otra
            holder.imgPicture.setImage(escanda.getRouteImg(), R.drawable.previsualizacion_foto); 
            
	        holder.txtTitle.setText(escanda.getTitle());
	        holder.txtNumComments.setText(Integer.toString(escanda.getNumComments()));
	        
	        if (escanda.hasAudio()){
	        	holder.imgMicro.setVisibility(View.VISIBLE);
	        }
	        else{
	        	holder.imgMicro.setVisibility(View.GONE);
	        }

	        if (escanda.getCategory().equals(Escandalo.HAPPY)){
	        	holder.imgCategory.setImageResource(R.drawable.cara_riendose);
	        }
	        else{
	        	holder.imgCategory.setImageResource(R.drawable.cara_enfadado);
	        }
	        
	        // Guardamos el ID del escandalo para luego recuperarlo al hacer click sobre ellos
	        holder.txtNumComments.setTag(escanda.getId());
	        holder.imgNumComments.setTag(escanda.getId());
	        holder.imgPicture.setTag(escanda.getUriAudio());
	        holder.imgMicro.setTag(escanda.getUriAudio());        
	        
	        // Listener para el microfono
	        holder.imgMicro.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Paramos si hubiera algún audio reproduciéndose
					Audio.getInstance().closeAudio();
					// Lo reproducimos				
					new PlayAudio().execute((String)v.getTag());
					
				}
			});
	        	        
            // Listener para la foto
            holder.imgPicture.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {	            
					Intent i = new Intent(context, DetailPhotoActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ImageView imView = (ImageView) v;
					Bitmap bitm = ((BitmapDrawable)imView.getDrawable()).getBitmap();
					byte[] bytes = ImageUtils.BitmapToBytes(bitm);
					i.putExtra("bytes", bytes);
					i.putExtra("uri_audio", v.getTag().toString());
					context.startActivity(i);
				}
			});
            
            

			
     
            // Listeners para los comentarios  
            holder.txtNumComments.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {			
					Intent i = new Intent(context, DetailCommentsActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra("id", v.getTag().toString());
					context.startActivity(i);	
				}
			});
            
            holder.imgNumComments.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {					
					Intent i = new Intent(context, DetailCommentsActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra("id", v.getTag().toString());
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
	    static class EscandaloHolder{
	        TextView txtTitle;
	        ImageView imgCategory;
	        FetchableImageView imgPicture;
	       //ImageView imgPicture;
	        ImageView imgNumComments;
	        TextView txtNumComments;
	        LinearLayout lheight;
	        ImageView imgMicro;
	        ProgressBar loading;
	        
	        
	        public ImageView getPicture(){
	        	return imgPicture;
	        }
	        
	        public TextView getNumComments(){
	        	return txtNumComments;
	        }
	        
	        public ProgressBar getProgressBar(){
	        	return loading;
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
			available_height = height - action_bar_height - status_bar_height - MyApplication.ALTO_TABS;
			
			return available_height;
		}
		
		
		/**
		 * Reproduce el audio
		 * @author Alejandro
		 *
		 */
		private class PlayAudio extends AsyncTask<String,Integer,Boolean> {
			 
			@Override
		    protected Boolean doInBackground(String... params) {
		    	
		    	Audio.getInstance().startPlaying("http://scandaloh.s3.amazonaws.com/" + params[0]);							
		        return false;
		    }
			
		}
		
}
