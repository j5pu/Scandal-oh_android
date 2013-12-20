package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applidium.shutterbug.FetchableImageView;
import com.applidium.shutterbug.FetchableImageView.FetchableImageViewListener;
import com.bizeu.escandaloh.DetailCommentsActivity;
import com.bizeu.escandaloh.DetailPhotoActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class EscandaloAdapter extends ArrayAdapter<Escandalo> {

		public static int ROUTE_IMAGE = 5;
		
	    Context mContext; 
	    Activity acti;
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
	        this.mContext = context;
	        this.data = data;
	        this.acti = (Activity)context;
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
	        	
	            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            mView = inflater.inflate(layoutResourceId, parent, false);       
	                        
	            holder = new EscandaloHolder();
	            holder.txtTitle = (TextView)mView.findViewById(R.id.txt_titulo);
	            holder.imgPicture = (FetchableImageView) mView.findViewById(R.id.img_foto);
	            holder.txtNumComments = (TextView)mView.findViewById(R.id.txt_numero_comentarios); 
	            holder.imgMicro = (ImageView)mView.findViewById(R.id.img_escandalo_microfono);
	            holder.txtNameUser = (TextView)mView.findViewById(R.id.txt_escandalo_name_user);
	            holder.txtDate = (TextView)mView.findViewById(R.id.txt_escandalo_date);
	            holder.imgShare = (ImageView)mView.findViewById(R.id.img_escandalo_compartir);
	            
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
            holder.imgPicture.setImage(escanda.getRouteImg(), R.drawable.cargando); 
            holder.imgPicture.setListener(new FetchableImageViewListener() {
				
				@Override
				public void onImageFetched(Bitmap bitmap, String url) {
					// TODO Auto-generated method stub				
				}
				
				@Override
				public void onImageFailure(String url) {
					// si aún no hemos mostrado ningún mensaje, indicamos que hubo un error (sólo una vez)
					if (!MyApplication.TIMEOUT_PHOTO_SHOWN){
						Toast toast = Toast.makeText(mContext, "Hubo algún error obteniendo las fotos. Compruebe su conexión", Toast.LENGTH_LONG);
						toast.show();
						MyApplication.TIMEOUT_PHOTO_SHOWN = true;
						Log.v("WE","Error obteniendo foto (listener del shuterbug --> ¿timeout?");
					}
				}
			});
            
	        holder.txtTitle.setText(escanda.getTitle());
	        holder.txtNumComments.setText(Integer.toString(escanda.getNumComments()));
	        
	        if (escanda.hasAudio()){
	        	holder.imgMicro.setVisibility(View.VISIBLE);
	        }
	        else{
	        	holder.imgMicro.setVisibility(View.GONE);
	        }
	        
	        holder.txtNameUser.setText(escanda.getUser());
	        
	        // La fecha tendrá el formato: dd-mm-aaaa
	        String date_without_time = (escanda.getDate().split("T",2))[0];   
	        String year = date_without_time.split("-",3)[0];
	        String month = date_without_time.split("-",3)[1];
	        String day = date_without_time.split("-",3)[2];
	        String final_date = day + "-" + month + "-" + year;
	        holder.txtDate.setText(final_date); 
	        
	        // Guardamos los datos necesarios en las vistas para luego recuperarlos al hacer click
	        holder.txtNumComments.setTag(R.string.id, escanda.getId());
	        holder.txtNumComments.setTag(R.string.url_foto, (String) escanda.getRouteImg());
	        holder.txtNumComments.setTag(R.string.user, (String) escanda.getUser());
	        holder.txtNumComments.setTag(R.string.title, (String) escanda.getTitle());
	        holder.imgPicture.setTag(R.string.uri_audio, escanda.getUriAudio());
	        holder.imgMicro.setTag(R.string.uri_audio, escanda.getUriAudio());
	        holder.imgShare.setTag(R.string.url_foto, (String) escanda.getRouteImg());	   
	 	        
	        // Listener para el microfono
	        holder.imgMicro.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					 // Mandamos el evento a Google Analytics
					 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					 easyTracker.send(MapBuilder
					      .createEvent("Acción UI",     // Event category (required)
					                   "Boton clickeado",  // Event action (required)
					                   "Escuchar audio desde carrusel",   // Event label
					                   null)            // Event value
					      .build()
					  );
					
					// Paramos si hubiera algún audio reproduciéndose
					Audio.getInstance(mContext).releaseResources();
					
					// Lo reproducimos				
					new PlayAudio().execute((String)v.getTag(R.string.uri_audio));		
				}
			});
	        	        
            // Listeners para la foto
            holder.imgPicture.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					 // Mandamos el evento a Google Analytics
					 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					 easyTracker.send(MapBuilder
					      .createEvent("Acción UI",     // Event category (required)
					                   "Boton clickeado",  // Event action (required)
					                   "Ver foto en detalle desde carrusel",   // Event label
					                   null)            // Event value
					      .build()
					  );
					
					// Evitamos que se pulse dos o más veces en las fotos (para que no se abra más de una vez)
					if (!MyApplication.PHOTO_CLICKED){
						MyApplication.PHOTO_CLICKED = true;
						
						// Paramos si hubiera algún audio reproduciéndose
						Audio.getInstance(mContext).releaseResources();
						
						Intent i = new Intent(mContext, DetailPhotoActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ImageView imView = (ImageView) v;
						Bitmap bitm = ((BitmapDrawable)imView.getDrawable()).getBitmap();
						byte[] bytes = ImageUtils.bitmapToBytes(bitm);
						i.putExtra("bytes", bytes);
						i.putExtra("uri_audio", v.getTag(R.string.uri_audio).toString());

						mContext.startActivity(i);
					}
				}
			});   
            
            holder.imgPicture.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					
					 // Mandamos el evento a Google Analytics
					 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					 easyTracker.send(MapBuilder
					      .createEvent("Acción UI",     // Event category (required)
					                   "Boton clickeado prolongadamente",  // Event action (required)
					                   "Guardar foto en galería desde carrusel",   // Event label
					                   null)            // Event value
					      .build()
					  );
					
					// Paramos si hubiera algún audio reproduciéndose
					Audio.getInstance(mContext).releaseResources();
					
					// Guardamos la foto en la galería				
					ImageView imView = (ImageView) v;
					Bitmap bitm = ((BitmapDrawable)imView.getDrawable()).getBitmap();
					ImageUtils.saveBitmapIntoGallery(bitm, mContext);
					
					Toast toast = Toast.makeText(mContext, "foto guardada en la galería", Toast.LENGTH_LONG);
					toast.show();
					return true;
				}
			});
            
     
            // Listener para los comentarios  
            holder.txtNumComments.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					 // Mandamos el evento a Google Analytics
					 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					 easyTracker.send(MapBuilder
					      .createEvent("Acción UI",     // Event category (required)
					                   "Boton clickeado",  // Event action (required)
					                   "Ver comentarios desde carrusel",   // Event label
					                   null)            // Event value
					      .build()
					  );
					
					// Paramos si hubiera algún audio reproduciéndose
					Audio.getInstance(mContext).releaseResources();
					
					Intent i = new Intent(mContext, DetailCommentsActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra("id", v.getTag(R.string.id).toString());
					i.putExtra("route_image", (String) v.getTag(R.string.url_foto));
					i.putExtra("user", (String) v.getTag(R.string.user));
					i.putExtra("title", (String) v.getTag(R.string.title));
					mContext.startActivity(i);	
				}
			});

            
            // Listener para compartir
            holder.imgShare.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					 // Mandamos el evento a Google Analytics
					 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					 easyTracker.send(MapBuilder
					      .createEvent("Acción UI",     // Event category (required)
					                   "Boton clickeado",  // Event action (required)
					                   "Compartir escándalo desde carrusel",   // Event label
					                   null)            // Event value
					      .build()
					  );
			
					// Paramos si hubiera algún audio reproduciéndose
					Audio.getInstance(mContext).releaseResources();
					
					Uri screenshotUri = Uri.parse((String)v.getTag(R.string.url_foto));	
					Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
					shareIntent.setType("text/plain");		       
					shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Deberías ver esto. ¡Qué escándalo!");
					shareIntent.putExtra(Intent.EXTRA_TEXT, screenshotUri.toString());

					mContext.startActivity(Intent.createChooser(shareIntent, "Compartir scándalOh! con..."));		        
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
	        FetchableImageView imgPicture;
	        TextView txtNumComments;
	        LinearLayout lheight;
	        ImageView imgMicro;
	        ImageView imgNumComments;
	        ImageView imgShare;
	        ProgressBar loading;
	        TextView txtNameUser;
	        TextView txtDate;
	        
	        
	        public ImageView getPicture(){
	        	return imgPicture;
	        }
	        
	        public TextView getNumComments(){
	        	return txtNumComments;
	        }
	        
	        public ImageView getShare(){
	        	return imgShare;
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
			DisplayMetrics display = mContext.getResources().getDisplayMetrics();

	        int height = display.heightPixels;
			
			// Action bar height
	        TypedValue tv = new TypedValue();
	        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
	           if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
	        	   action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,mContext.getResources().getDisplayMetrics());
	        }
	        else if(mContext.getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
	        	action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,mContext.getResources().getDisplayMetrics());
	        }
			
			// Status bar height
			int resourceId = mContext.getResources().getIdentifier("status_bar_height",
					"dimen", "android");	
			if (resourceId > 0) {
				status_bar_height = mContext.getResources().getDimensionPixelSize(resourceId);
			}

			// Available height
			available_height = height - (action_bar_height*2) - status_bar_height ;// - MyApplication.ALTO_TABS;
			
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
		    	
		    	Audio.getInstance(mContext).startPlaying("http://scandaloh.s3.amazonaws.com/" + params[0]);							
		        return false;
		    }
			
		}
	
}
