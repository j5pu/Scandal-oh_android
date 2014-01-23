package com.bizeu.escandaloh.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.bizeu.escandaloh.MainActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

public class EscandaloAdapter extends ArrayAdapter<Escandalo> {

	public static int ROUTE_IMAGE = 5;
	
    Context mContext; 
    Activity acti;
    int layoutResourceId;    
    ArrayList<Escandalo> data;
    private int available_height;	    
    private Escandalo escanda;
    private Bitmap bitma;


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
       
                
        // Cambiamos el alto por c�digo
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
				// si a�n no hemos mostrado ning�n mensaje, indicamos que hubo un error (s�lo una vez)
				if (!MyApplication.TIMEOUT_PHOTO_SHOWN){
					Toast toast = Toast.makeText(mContext, "Hubo alg�n error obteniendo las fotos. Compruebe su conexi�n", Toast.LENGTH_LONG);
					toast.show();
					MyApplication.TIMEOUT_PHOTO_SHOWN = true;
					Log.v("WE","Error obteniendo foto (listener del shuterbug --> �timeout?");
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
        
        // La fecha tendr� el formato: dd-mm-aaaa
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
        holder.imgPicture.setTag(R.string.url_foto, (String) escanda.getRouteImgBig());
        holder.imgMicro.setTag(R.string.uri_audio, escanda.getUriAudio());
        holder.imgShare.setTag(R.string.url_foto, (String) escanda.getRouteImgBig());	
        holder.imgShare.setTag(R.string.title, (String) escanda.getTitle());
 	        
        // Listener para el microfono
        holder.imgMicro.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Escuchar audio desde carrusel",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				// Paramos si hubiera alg�n audio reproduci�ndose
				Audio.getInstance(mContext).releaseResources();
				
				// Lo reproducimos				
				new PlayAudioTask().execute((String)v.getTag(R.string.uri_audio));		
			}
		});
        	        
        // Listeners para la foto
        holder.imgPicture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Ver foto en detalle desde carrusel",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				// Evitamos que se pulse dos o m�s veces en las fotos (para que no se abra m�s de una vez)
				if (!MyApplication.PHOTO_CLICKED){
					MyApplication.PHOTO_CLICKED = true;
					
					// Paramos si hubiera alg�n audio reproduci�ndose
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
				
				final View mView = v;
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado prolongadamente",  // Event action (required)
				                   "Guardar foto en galer�a desde carrusel",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				// Paramos si hubiera alg�n audio reproduci�ndose
				Audio.getInstance(mContext).releaseResources();
				
				// Guardamos la foto en la galer�a	
				final CharSequence[] items = {"Guardar foto en la galer�a"};
				 AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			        builder.setItems(items, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int item) {
			            	
			                if (items[item].equals("Guardar foto en la galer�a")) {
			                	new SaveImageTask(mContext).execute((String) mView.getTag(R.string.url_foto));     			   
			                } 			                
			            }
			        });
			        builder.show();
			        
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
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Ver comentarios desde carrusel",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				// Paramos si hubiera alg�n audio reproduci�ndose
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
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Compartir esc�ndalo desde carrusel",   // Event label
				                   null)            // Event value
				      .build()
				  );
		
				// Paramos si hubiera alg�n audio reproduci�ndose
				Audio.getInstance(mContext).releaseResources();
				
				// Compartimos la foto
				Uri screenshotUri = Uri.parse((String)v.getTag(R.string.url_foto));	
				new ShareImageTask(mContext, (String) v.getTag(R.string.title)).execute(screenshotUri.toString()); 
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
	 *
	 */
	private class PlayAudioTask extends AsyncTask<String,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(String... params) {
	    	
	    	Audio.getInstance(mContext).startPlaying("http://scandaloh.s3.amazonaws.com/" + params[0]);							
	        return false;
	    }	
	}
	
	
	/**
	 * Guarda una foto en la galer�a
	 *
	 */
	private class SaveImageTask extends AsyncTask<String, String, String> {
	    private Context context;
	    private ProgressDialog pDialog;

	    public SaveImageTask(Context context) {
	        this.context = context;
	    }

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();

	        pDialog = new ProgressDialog(context);
	        pDialog.setMessage("Guardando ...");
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();		     
	    }

	    @Override
	    protected String doInBackground(String... args) {
	    	
	        try {
		    	// Obtenemos la foto desde la url
            	bitma = ImageUtils.getBitmapFromURL(args[0]);

	        } catch (Exception e) {
	            e.printStackTrace();
	            e.printStackTrace();
	             // Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
				                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
				                       e),                                                             // The exception.
				                       false).build());
	        }

	        return null;
	    }

	    @Override
	    protected void onPostExecute(String args) {
	    	// Quitamos el progress dialog
	        pDialog.dismiss();  
	        
	        // Guardamos la foto en la galer�a
			ImageUtils.saveBitmapIntoGallery(bitma, mContext);	
	    }
	}	
	
	
	
	/**
	 * Comparte un esc�ndalo
	 *
	 */
	private class ShareImageTask extends AsyncTask<String, String, String> {
	    private Context context;
	    private ProgressDialog pDialog;
	    URL myFileUrl;
	    String title;
	    Bitmap bmImg = null;
	    Intent share;
	    File file;

	    public ShareImageTask(Context context, String title) {
	        this.context = context;
	        this.title = title;
	    }

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();

	        pDialog = new ProgressDialog(context);
	        pDialog.setMessage("Preparando para compartir ...");
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();		     
	    }

	    @Override
	    protected String doInBackground(String... args) {
	    	
	    	// Obtenemos la foto desde la url de amazon
	        try {
	            myFileUrl = new URL(args[0]);
	            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
	            conn.setDoInput(true);
	            conn.connect();
	            InputStream is = conn.getInputStream();
	            bmImg = BitmapFactory.decodeStream(is);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            String path = myFileUrl.getPath();
	            String idStr = path.substring(path.lastIndexOf('/') + 1);
	            File filepath = Environment.getExternalStorageDirectory();
	            File dir = new File(filepath.getAbsolutePath()+ "/Sc�ndalOh/");
	            dir.mkdirs();
	            String fileName = idStr;
	            // Guardamos la ruta de la foto para m�s adelante eliminarla
	            MyApplication.FILES_TO_DELETE.add(filepath.getAbsolutePath() + "/Sc�ndalOh/" + idStr);
	            file = new File(dir, fileName);
	            FileOutputStream fos = new FileOutputStream(file);
	            bmImg.compress(CompressFormat.JPEG, 100, fos);
	            fos.flush();
	            fos.close();

	        } catch (Exception e) {
	            e.printStackTrace();
	             // Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
				                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
				                       e),                                                             // The exception.
				                       false).build());
	        }

	        return null;
	    }

	    @Override
	    protected void onPostExecute(String args) {
	    	// Quitamos el progress dialog
	        pDialog.dismiss();  
	        
	        // Ejecutamos el intent de compartir
	        share = new Intent(Intent.ACTION_SEND);		        
	        share.putExtra(Intent.EXTRA_SUBJECT, "Deber�as ver esto. �Qu� esc�ndalo!");
	        share.putExtra(Intent.EXTRA_TEXT, title);
	        share.putExtra(Intent.EXTRA_TITLE, title);	        
	        share.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file));
	        share.setType("image/jpeg");
	        acti.startActivityForResult(Intent.createChooser(share, "Compartir Sc�ndalOh! con..."), MainActivity.SHARING);
	    }
	}	
	
}
