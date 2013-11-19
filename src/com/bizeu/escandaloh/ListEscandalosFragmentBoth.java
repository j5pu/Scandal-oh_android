package com.bizeu.escandaloh;

import java.util.ArrayList;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.Connectivity;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;


public class ListEscandalosFragmentBoth extends SherlockFragment implements onAdsReadyListener{

	private final static String APP_ID = "d83c1504-0e74-4cd6-9a6e-87ca2c509506";
	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	public final static String ID_VINO_SELECCIONADO = "Vino_seleccionado";
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;
	private int first_visible_item_count;
	EscandaloAdapter escanAdapter;
	public static ArrayList<Escandalo> escandalos;
	private FrameLayout banner;
	private BannerView adM;
	AmazonS3Client s3Client;
	int mCurrentPage;
	Escandalo escan_aux;
	private PullToRefreshListView lView;
	private GetEscandalos escandalos_asyn ;
	private boolean getting_escandalos = true;

	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	      	super.onCreate(savedInstanceState);
	      	escandalos = new ArrayList<Escandalo>();
	 
	      	escandalos_asyn = null;
	 }
	

	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.list_escandalos, container, false);
	
		escanAdapter = new EscandaloAdapter(getActivity().getBaseContext(), R.layout.escandalo,
				escandalos);
		
		lView = (PullToRefreshListView) v.findViewById(R.id.list_escandalos);
		lView.setAdapter(escanAdapter); 
	
		lView.setOnRefreshListener(new OnRefreshListener() {
			
		    @Override
		    public void onRefresh() {
				if (Connectivity.isOnline(getActivity().getApplicationContext())){
			    	// Obtenemos si hay nuevos escandalos subidos (y los mostramos al principio)
			    	new GetNewEscandalos().execute();
				}
				else{
					Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No dispone de una conexi�n a internet", Toast.LENGTH_SHORT);
					toast.show();
				}  		
		    }
		});
		
		
	
		lView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
							
				// Comprobamos cuando el scroll termina de moverse
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					
					// Si no es el �ltimo (tiene uno detr�s)
					if (lView.getChildAt(1) != null){

						// Obtenemos la coordenada Y donde empieza el segundo escandalo
						final int[] location = new int[2];
						lView.getChildAt(1).getLocationOnScreen(location);
						
						// Si el primer escandalo ocupa m�s pantalla que el segundo mostrado, mostramos el primero			
						// Para versi�n menor a 11: no tenemos en cuenta el status bar
						if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.GINGERBREAD_MR1){
							// Si la coordenada Y del segundo escandalo es mayor que la mitad de la pantalla (diponible)
							if ((location[1] - getActionBarHeight() + MyApplication.ALTO_TABS) >= getAvailableHeightScreen() / 2) {
								lView.setSelection(first_visible_item_count);
							} 
							// Si no, mostramos el segundo
							else {
								lView.setSelection(first_visible_item_count + 1);
							}
						}
						// Para versi�n 11+: tenemos en cuenta el status bar
						else{
							if ((location[1] - (getActionBarHeight() + getStatusBarHeight() + MyApplication.ALTO_TABS)) >= getAvailableHeightScreen() / 2) {
								lView.setSelection(first_visible_item_count);
							} 
							else {
								lView.setSelection(first_visible_item_count + 1);
							}
						}						
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {			
	            
				// Si quedan 5 esc�ndalos para llegar al �ltimo, obtenemos los 10 siguientes
	            if (firstVisibleItem == escanAdapter.getCount() - 1){
	            	// Usamos el booleano como llave de paso (s�lo la primera vez entrar�). Cuando se obtengan los 10 esc�ndalos se volver� a abrir
	            	if (!getting_escandalos){
		            	new GetEscandalos().execute();
		            	getting_escandalos = true;
	            	}
	            }
	            
				
				// Guardamos en que posici�n est� el primer escandalo visible (actualmente) en la pantalla
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
		
		if (Connectivity.isOnline(getActivity().getApplicationContext())){
	    	escandalos_asyn = new GetEscandalos();
	    	escandalos_asyn.execute();	
		}
		else{
			Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No dispone de una conexi�n a internet", Toast.LENGTH_SHORT);
			toast.show();
		}		
	}
	
	
	
	/**
	 * onPause
	 */
	@Override
	public void onPause() {
	    super.onPause();
	   
	    //AdsSessionController.pauseTracking();
	    cancelGetEscandalos();
	}
	
	
	
	
    /**
     * 
     * @param activateOnItemClick
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
    	lView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }
    
    
    /**
     * 
     * @param position
     */
    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
        	lView.setItemChecked(mActivatedPosition, false);
        } else {
        	lView.setItemChecked(position, true);
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
	 * Devuelve el alto de pantalla disponible en p�xeles: screen height - (status bar height + action bar height) - tabs height
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
	 * Obtiene los siguientes 10 esc�ndalos anteriores a partir de uno dado
	 * @author Alejandro
	 *
	 */
	private class GetEscandalos extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected void onPreExecute(){
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			String url = null;
			
			// Usamos un servicio u otro dependiendo si es el primer listado de esc�ndalos o ya posteriores
			if (MyApplication.FIRST_TIME_BOTH){
				Log.v("WE","primera vez both");
				url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&country=" + MyApplication.code_selected_country;
			}
			else{
				// A partir del �ltimo ID obtenido
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/";
			}
				
			HttpClient httpClient = new DefaultHttpClient();
        
		    HttpGet getEscandalos = new HttpGet(url);
		    getEscandalos.setHeader("content-type", "application/json");        
		        
		    HttpResponse response = null;
		    try{
				// Hacemos la petici�n al servidor
		        response = httpClient.execute(getEscandalos);
		        String respStr = EntityUtils.toString(response.getEntity());
		        //Log.i("WE",respStr);
		        
		        JSONArray escandalosObject = null;
		        
		        // Si es la primera vez obtenemos los escandalos a partir de un JSONObject, sino obtenemos directamente el JSONArray
		        if (MyApplication.FIRST_TIME_BOTH){
		        	MyApplication.FIRST_TIME_BOTH = false;
		        	// Obtenemos el json
			        JSONObject respJson = new JSONObject(respStr);	                       
			            
			        escandalosObject = respJson.getJSONArray("objects");
		        }
		        else{
		        	escandalosObject = new JSONArray(respStr);
		        }
		        
		            
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
					        // A�adimos el escandalo al ArrayList
					        escandalos.add(new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
									R.drawable.loading), Integer.parseInt(comments_count), resource_uri, "http://scandaloh.s3.amazonaws.com/" + img, sound));
							escanAdapter.notifyDataSetChanged();	
						}
			        });		        	
		    	 }
		     }
		     catch(Exception ex){
		            Log.e("ServicioRest","Error!", ex);
		     }
		        	 
		     // Devolvemos el c�digo resultado
		     return (response.getStatusLine().getStatusCode());    	
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
	
			
			if (!isCancelled()){
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	Log.v("WE","escandalos recibidos");
		        }
		        else{
		        	Log.v("WE","escandalos NO recibidos");
		        }        
			}
			// Abrimos la llave
			getting_escandalos = false;
	    }
	}
	
	
	
	
	


	/**
	 * Obtiene (si hay) nuevos escandalos
	 *
	 */
	private class GetNewEscandalos extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected void onPreExecute(){
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			// A partir del id m�s nuevo obtenido (el primero del array)
			String url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/" + escandalos.get(0).getId() + "/" + MyApplication.code_selected_country+ "/new/";
				
			HttpClient httpClient = new DefaultHttpClient();
        
		    HttpGet getEscandalos = new HttpGet(url);
		    getEscandalos.setHeader("content-type", "application/json");        
		        
		    HttpResponse response = null;
		    try{
				// Hacemos la petici�n al servidor
		        response = httpClient.execute(getEscandalos);
		        String respStr = EntityUtils.toString(response.getEntity());
		        Log.i("WE",respStr);
		        
		        JSONArray escandalosObject = new JSONArray(respStr);
           
		        for (int i = escandalosObject.length()-1; i>=0; i--){
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
					        // A�adimos el escandalo al comienzo
					        escandalos.add(0,new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
									R.drawable.loading), Integer.parseInt(comments_count), resource_uri, "http://scandaloh.s3.amazonaws.com/" + img, sound));
							escanAdapter.notifyDataSetChanged();	
						}
			        });		
			               	
		    	 }
		     }
		     catch(Exception ex){
		            Log.e("ServicioRest","Error!", ex);
		     }
		        	 
		     // Devolvemos el c�digo resultado
		     return (response.getStatusLine().getStatusCode());    	
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
	
			
			if (!isCancelled()){
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	Log.v("WE","escandalos recibidos");
		        }
		        else{
		        	Log.v("WE","escandalos NO recibidos");
		        }        
			}
	        lView.onRefreshComplete();
	    }
	}
	



	
	private void cancelGetEscandalos(){	
		if (escandalos_asyn != null){
			escandalos_asyn.cancel(true);
		}
	}
}
