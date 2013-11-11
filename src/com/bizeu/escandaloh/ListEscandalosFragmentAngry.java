package com.bizeu.escandaloh;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.ImageUtils;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;


public class ListEscandalosFragmentAngry extends SherlockFragment implements onAdsReadyListener{

	private final static String APP_ID = "d83c1504-0e74-4cd6-9a6e-87ca2c509506";
	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	public final static String ID_VINO_SELECCIONADO = "Vino_seleccionado";
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;
	private int first_visible_item_count;
	EscandaloAdapter escanAdapter;
	public static ArrayList<Escandalo> escandalos;
	private byte[] bytes;
	private int escandalo_loading = 0 ;
	private ListView list_escandalos;
	private FrameLayout banner;
	private BannerView adM;
	AmazonS3Client s3Client;
	int mCurrentPage;
	Escandalo escan_aux;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	      	super.onCreate(savedInstanceState); 
	 }
	

	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.list_escandalos, container, false);
		
		escandalo_loading = 0;
		escandalos = new ArrayList<Escandalo>();	
		escanAdapter = new EscandaloAdapter(getActivity().getBaseContext(), R.layout.escandalo,
				escandalos);
		
		list_escandalos = (ListView) v.findViewById(R.id.list_escandalos);
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
							if ((location[1] - getActionBarHeight() + MyApplication.ALTO_TABS) >= getAvailableHeightScreen() / 2) {
								list_escandalos.setSelection(first_visible_item_count);
							} 
							// Si no, mostramos el segundo
							else {
								list_escandalos.setSelection(first_visible_item_count + 1);
							}
						}
						// Para versión 11+: tenemos en cuenta el status bar
						else{
							if ((location[1] - (getActionBarHeight() + getStatusBarHeight() + MyApplication.ALTO_TABS)) >= getAvailableHeightScreen() / 2) {
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
				
		
		
				
		return v;
	}
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (savedInstanceState != null && savedInstanceState
				.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}		
		
		// Ten
		//AdsSessionController.setApplicationId(getActivity().getApplicationContext(),APP_ID);
        //AdsSessionController.registerAdsReadyListener(this);       		
		
		new GetEscandalos().execute();		
	}
	
	
	
	/**
	 * onPause
	 */
	@Override
	public void onPause() {
	    super.onPause();
	    //AdsSessionController.pauseTracking();
	}
	
	
	
	
    /**
     * 
     * @param activateOnItemClick
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
    	list_escandalos.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }
    
    
    /**
     * 
     * @param position
     */
    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
        	list_escandalos.setItemChecked(mActivatedPosition, false);
        } else {
        	list_escandalos.setItemChecked(position, true);
        }
        mActivatedPosition = position;
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
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // TODO Auto-generated method stub
	    if (keyCode == KeyEvent.KEYCODE_BACK){
	        AdsSessionController.stopTracking();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	 */
	
	/**
	 * onUserLeaveHint
	 */
	/*
	@Override
	protected void onUserLeaveHint() {
	    // TODO Auto-generated method stub
	    super.onUserLeaveHint();
	    AdsSessionController.detectHomeButtonEvent();
	}
	*/

	



	
	/**
	 * Devuelve el alto de pantalla disponible en píxeles: screen height - (status bar height + action bar height) - tabs height
	 * @return
	 */
	private int getAvailableHeightScreen(){
		
		int screen_height = 0;
		int available_height = 0;	

		// Screen height
		DisplayMetrics display = getResources().getDisplayMetrics();
        screen_height = display.heightPixels;

        // Available height
		available_height = screen_height - getActionBarHeight() - getStatusBarHeight() - MyApplication.ALTO_TABS ;
		
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
           if (getActivity().getApplicationContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        	   action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        else if(getActivity().getApplicationContext().getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
        	action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
		return action_bar_height;
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
	        String url = "http://192.168.1.31:8000/api/v1/photo/?limit=3&category__id=2";
	        	    	        
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
            	
		        	getActivity().runOnUiThread(new Runnable() {
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
			
			//escanAdapter.notifyDataSetChanged();
			
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
