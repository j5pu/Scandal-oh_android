package com.bizeu.escandaloh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.users.MainLoginActivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.zed.adserver.AdsSessionController;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;

public class MainActivity extends SherlockActivity implements onAdsReadyListener {

	private final static String APP_ID = "d83c1504-0e74-4cd6-9a6e-87ca2c509506";
	private static final int SHOW_CAMERA = 10;
    private static final int CREATE_ESCANDALO = 11;
	private File photo;
	public static ArrayList<Escandalo> escandalos;
	EscandaloAdapter escanAdapter;
	private int first_visible_item_count;
	private ListView list_escandalos;
	private Uri fileUri;
	private Uri mImageUri;
	Bitmap taken_photo;
	AmazonS3Client s3Client;
	private int escandalo_loading = 0 ;
	private byte[] bytes;
	private boolean logged = false;
	
	private FrameLayout banner;
	private BannerView adM;
	private SharedPreferences prefs;

	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Ten
		AdsSessionController.setApplicationId(getApplicationContext(),APP_ID);
        AdsSessionController.registerAdsReadyListener(this);

		// Action bar
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setLogo(R.drawable.corte_manga);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);

		escandalos = new ArrayList<Escandalo>();
		
		escanAdapter = new EscandaloAdapter(this, R.layout.escandalo,
				escandalos);
				
		list_escandalos = (ListView) findViewById(R.id.list_escandalos);
		list_escandalos.setAdapter(escanAdapter);
	
		list_escandalos.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
							
				// Comprobamos cuando el scroll termina de moverse
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					
					// Si no es el último (tiene uno detrás)
					if (list_escandalos.getChildAt(1) != null){

						// Obtenemos la coordenada Y donde empieza el segundo escandalo
						final int[] location = new int[2];
						list_escandalos.getChildAt(1).getLocationOnScreen(location);
						
						// Si el primer escandalo ocupa más pantalla que el segundo mostrado, mostramos el primero			
						// Para versión menor a 11: no tenemos en cuenta el status bar
						if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.GINGERBREAD_MR1){
							// Si la coordenada Y del segundo escandalo es mayor que la mitad de la pantalla (diponible)
							if ((location[1] - getActionBarHeight()) >= getAvailableHeightScreen() / 2) {
								list_escandalos.setSelection(first_visible_item_count);
							} 
							// Si no, mostramos el segundo
							else {
								list_escandalos.setSelection(first_visible_item_count + 1);
							}
						}
						// Para versión 11+: tenemos en cuenta el status bar
						else{
							if ((location[1] - (getActionBarHeight() + getStatusBarHeight())) >= getAvailableHeightScreen() / 2) {
								list_escandalos.setSelection(first_visible_item_count);
							} 
							else {
								list_escandalos.setSelection(first_visible_item_count + 1);
							}
						}						
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// Guardamos en que posición está el primer escandalo visible (actualmente) en la pantalla
				first_visible_item_count = firstVisibleItem;
			}
		});
		
	
				
		new GetEscandalos().execute();	
	}

	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
	    AdsSessionController.enableTracking();

		// Comprobamos si el usuario esta logueado
		SharedPreferences prefs = this.getSharedPreferences(
			      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		String user_uri = prefs.getString("user_uri", null); 
		if (user_uri != null){
			logged = true;
		}
		else{
			logged = false;
		}
		
		// Refrescamos el action bar
		this.supportInvalidateOptionsMenu();
	}
	
	

	/**
	 * onPause
	 */
	@Override
	protected void onPause() {
	    // TODO Auto-generated method stub
	    super.onPause();
	    AdsSessionController.pauseTracking();
	}
	
	
	


	
	/**
	 * It will be called when the ads are ready to be shown
	 */
	@Override
	public void onAdsReady(){ 
		/*
	       //The banner will be show inside this view.
        banner = (FrameLayout) findViewById(R.id.banner);
     
        //BannerView initialization
        adM = new BannerView( this, getApplicationContext());
 
        banner.removeAllViews();
 
        //Add the bannerView to the container view
        banner.addView( adM );
 
        //Set the visibility to VISIBLE.
        banner.setVisibility( FrameLayout.VISIBLE );	
        */	
	}
	 

	
	
	/**
	 * It will be called when any errors ocurred.
	 */
	@Override
	public void onAdsReadyFailed(){
		Log.e("ZedAdServerLog","Error loading ads");
	}
	
	
	/**
	 * onKeyDown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // TODO Auto-generated method stub
	    if (keyCode == KeyEvent.KEYCODE_BACK){
	        AdsSessionController.stopTracking();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	 
	
	/**
	 * onUserLeaveHint
	 */
	@Override
	protected void onUserLeaveHint() {
	    // TODO Auto-generated method stub
	    super.onUserLeaveHint();
	    AdsSessionController.detectHomeButtonEvent();
	}
	
	
	
	
	/**
	 * onCreateOptionsMenu
	 */
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);

		com.actionbarsherlock.view.MenuItem mi_photo = menu.findItem(R.id.take_photo);
		com.actionbarsherlock.view.MenuItem mi_logout = menu.findItem(R.id.logout);
		
		if (logged){
			mi_photo.setIcon(R.drawable.camara_azul);
			mi_logout.setVisible(true);
		}
		else{
			mi_photo.setIcon(R.drawable.mas);
			mi_logout.setVisible(false);
			mi_logout.setEnabled(false);
		}

		return true;
	}

	
	
	@Override
	public boolean onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		com.actionbarsherlock.view.MenuItem mi_photo = menu.findItem(R.id.take_photo);
		com.actionbarsherlock.view.MenuItem mi_logout = menu.findItem(R.id.logout);
		
		if (logged){
			mi_photo.setIcon(R.drawable.camara_azul);
			mi_logout.setVisible(true);
		}
		else{
			mi_photo.setIcon(R.drawable.mas);
			mi_logout.setVisible(false);
		}

	    return super.onPrepareOptionsMenu(menu);
	}
	
	
	/**
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {

			case R.id.take_photo:
			    // Si está logueado iniciamos la cámara
				if (logged){ 
					Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
					File photo;
					try{
				        photo = this.createFile("picture", ".png");
				        photo.delete();
				    }
				    catch(Exception e){
				        Log.v("WE", "Can't create file to take picture!");
				        return false;
				    }
					
				    mImageUri = Uri.fromFile(photo);
				    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
					startActivityForResult(takePictureIntent, SHOW_CAMERA);
				}
				// Si no, iniciamos la pantalla de login
				else{
					Intent i = new Intent(this, MainLoginActivity.class);
					startActivity(i);
				}
				
				break;
			case R.id.logout:
				if (logged){
					AlertDialog.Builder alert_logout = new AlertDialog.Builder(this);
					alert_logout.setTitle("Cerrar sesión usuario");
					alert_logout.setMessage("¿Seguro que desea cerrar la sesión actual?");
					alert_logout.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {  
				            public void onClick(DialogInterface dialogo1, int id) {  
								// Deslogueamos al usuario
								prefs.edit().putString("user_uri", null).commit();
								logged = false;
								// Refrescamos el action bar
								supportInvalidateOptionsMenu();
				            }  
				        });  
					alert_logout.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {  
				        	public void onClick(DialogInterface dialogo1, int id) {  
				            }  
				        });            
				     alert_logout.show(); 
				}
		}
		
		return true;
	}

	
	

	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == SHOW_CAMERA) {
			if (resultCode == RESULT_OK) {
				Intent i = new Intent(MainActivity.this, CreateEscandaloActivity.class);
				i.putExtra("photoUri", mImageUri.toString());
				startActivityForResult(i, CREATE_ESCANDALO);					
			}
			else if (resultCode == RESULT_CANCELED) {
		           
	        }		 
		}
		
		else if (requestCode == CREATE_ESCANDALO){
		}	
	}



	



	
	/**
	 * Devuelve el alto de pantalla disponible en píxeles: screen height - (status bar height + action bar height)
	 * @return
	 */
	private int getAvailableHeightScreen(){
		
		int screen_height = 0;
		int available_height = 0;

		// Screen height
		DisplayMetrics display = getResources().getDisplayMetrics();
        screen_height = display.heightPixels;

        // Available height
		available_height = screen_height - getActionBarHeight() - getStatusBarHeight();
		
		return available_height;
	}
	
	
	
	/**
	 * Devuelve el alto del status bar
	 * @return
	 */
	private int getStatusBarHeight(){
		int status_bar_height = 0;
		
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");	
		if (resourceId > 0) {
			status_bar_height = getResources().getDimensionPixelSize(resourceId);
		}
		return status_bar_height;
	}
	
	
	
	
	/**
	 * Devuelve el alto del action bar
	 * @return
	 */
	private int getActionBarHeight(){
		TypedValue tv = new TypedValue();
		int action_bar_height = 0;
		
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
           if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        	   action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        else if(getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
        	action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
		return action_bar_height;
	}
	
	
	
	
	/**
	 * Crea un archivo en una ruta con un formato específico
	 * @param part
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	private File createFile(String part, String ext) throws Exception
	{
	    File scandaloh_dir= Environment.getExternalStorageDirectory();
	    scandaloh_dir=new File(scandaloh_dir.getAbsolutePath()+"/Scandaloh/");
	    if(!scandaloh_dir.exists())
	    {
	    	scandaloh_dir.mkdir();
	    }
	    return File.createTempFile(part, ext, scandaloh_dir);
	}
	
	

	
	
	
	/**
	 * Obtiene los escandalos del servidor y los muestra en pantalla
	 * @author Alejandro
	 *
	 */
	private class GetEscandalos extends AsyncTask<Void,Integer,Integer> {
		 
		@Override
	    protected Integer doInBackground(Void... params) {
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	        String url = "http://192.168.1.48:8000/api/v1/photo/?limit=15";
	        	    	        
	        HttpGet getEscandalos = new HttpGet(url);
	        getEscandalos.setHeader("content-type", "application/json");        
	        
	        HttpResponse response = null;
	        try{
				// Hacemos la petición al servidor
	        	response = httpClient.execute(getEscandalos);
	        	String respStr = EntityUtils.toString(response.getEntity());
	         
	        	// Obtenemos el json
	            JSONObject respJson = new JSONObject(respStr);	            
	            
	            // Parseamos el json para obtener los escandalos
	            JSONArray escandalosObject = null;
	            
	            escandalosObject = respJson.getJSONArray("objects");
	            
	            for (int i=0 ; i < escandalosObject.length(); i++){
	            	JSONObject escanObject = escandalosObject.getJSONObject(i);
	            	
	            	final String category = escanObject.getString("category");
	            	String date = escanObject.getString("date");
	            	final String id = escanObject.getString("id");
	            	final String img = escanObject.getString("img_p");
	            	final String comments_count = escanObject.getString("comments_count");
	            	String latitude = escanObject.getString("latitude");
	            	String longitude = escanObject.getString("longitude");
	            	final String resource_uri = escanObject.getString("resource_uri");	        
	            	final String title = escanObject.getString("title");
	            	String user = escanObject.getString("user");
	            	String visits_count = escanObject.getString("visits_count");
	            	final String sound = escanObject.getString("sound");
	    	    	
		        	runOnUiThread(new Runnable() {
						@Override
						public void run() {
				            // Añadimos el escandalo al ArrayList
				        	escandalos.add(new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
									R.drawable.loading), Integer.parseInt(comments_count), resource_uri, img, sound));
							escanAdapter.notifyDataSetChanged();	
							
				        	new GetPictureFromAmazon().execute(img);
						}
		        	});		        	
	    	    }             
	        }
	        catch(Exception ex){
	                Log.e("ServicioRest","Error!", ex);
	        }
	        	 
	        // Devolvemos el código resultado
	        return (response.getStatusLine().getStatusCode());
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Si es codigo 2xx --> OK
	        if (result >= 200 && result <300){
	        	Log.v("WE","escandalos recibidos");
	        }
	        else{
	        	Log.v("WE","escandalos NO recibidos");
	        }   
	    }
	}
	
	
	
	
	
	/**
	 * Obtiene la imagen de Amazon y la muestra
	 * @author Alejandro
	 *
	 */
	private class GetPictureFromAmazon extends AsyncTask<String,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(String... params) {
	    	boolean result = false;
		            	
	        //Obtenemos la imagen de cache
	    	//bytes = Cache.getInstance(getBaseContext()).obtenImagenDeCache(params[0]);
	    	    	
	    	  //if (bytes.length == 0){
	    	    	s3Client = new AmazonS3Client( new BasicAWSCredentials("AKIAJ6GJKNGVTOB3AREA", "RSNSbgY+HJJTufi4Dq6yM/r4tWBdTzEos+lUmDQU") );
	    	    	// Hacemos la petición a Amazon y obtenemos la imagen
	    	    	S3ObjectInputStream content  = null;

	    	    	result = true;
						
					try {
						content = s3Client.getObject("scandaloh", params[0]).getObjectContent();
						bytes = IOUtils.toByteArray(content);
						/*
						content.close();
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 5;
						Bitmap bitm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
						
						bytes = ImageUtils.BitmapToBytes(bitm);
						*/
					} catch (Exception e) {
						Log.e("WE","Error al obtener imagen");
						e.printStackTrace();
						result = false;
					}

				    // Añadimos la foto a caché
				  //  Cache.getInstance(getBaseContext()).aniadeImagenAcache(params[0], bytes);  // La almacenamos en cache		   
	    	   // }
	    	   // else{}
	 							
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
			if (result) {
			    final Escandalo esc = escandalos.get(escandalo_loading);    
			    esc.setPicture(ImageUtils.BytesToBitmap(bytes));	
			    escandalos.set(escandalo_loading, esc);
			    escandalo_loading++;    
				escanAdapter.notifyDataSetChanged();
				Log.v("WE","Imagen añadida");
			}
		}
	}
	

}
