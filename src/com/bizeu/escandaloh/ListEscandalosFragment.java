package com.bizeu.escandaloh;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;


public class ListEscandalosFragment extends SherlockFragment implements onAdsReadyListener{

	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	public final static String ID_VINO_SELECCIONADO = "Vino_seleccionado";
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    
	private ListView lView;
	//private PullToRefreshListView  lView;
	private FrameLayout banner;
	private View img_view;
	private ProgressBar loading;
	
    private int mActivatedPosition = ListView.INVALID_POSITION;
	private int first_visible_item_count;
	public static ArrayList<Escandalo> escandalos;
	private BannerView adM;
	AmazonS3Client s3Client;
	int mCurrentPage;
	private boolean getting_escandalos = true;
	private String category;
	private boolean any_error;
	private boolean connection_checked = false;
	private boolean there_are_more_escandalos;
	Escandalo escan_aux;
	EscandaloAdapter escanAdapter;
    private Callbacks tCallbacks = null;  
    private Context mContext;
	
	private GetEscandalos getEscandalosAsync;
	private GetNewEscandalos getNewEscandalosAsync;
	private UpdateNumComments updateNumCommentsAsync;

	
	public interface Callbacks {
		public void onRefreshFinished(); // Indica que se ha terminado de actualizar el carrusel para nuevos escandalos
	}
	  
	  
	  
	 /**
	  * onAttach
	 */
	 @Override
	 public void onAttach(Activity activity) {
		 super.onAttach(activity);
		 if (!(activity instanceof Callbacks)) {
			 throw new IllegalStateException("La actividad debe implementar los callbacks de los fragmentos");
		 }
		 else {
			 tCallbacks = (Callbacks) activity;
		 }
	 }
	
	 
	 /**
	  * onCreate
	  */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      
	      mContext = getActivity().getBaseContext();
	      
	      // Obtenemos el tipo de categoria
	      if (getArguments() != null) {
			category = getArguments().getString(MainActivity.CATEGORY);
			Log.v("WE","La categoria es: " + category);	
		  } 
	      else{
	    	  // Primera vez que aparecen escándalos, el primero es HAPPY
	    	  category = MainActivity.HAPPY;
	      }
	 }
	

	 
	 /**
	  * onCreateView
	  */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.list_escandalos, container, false);
		
		connection_checked = false;	
		there_are_more_escandalos = true;
		
	    // Indicamos a las otras categorias que serán su primera vez la próxima vez que se les pulse
	    if (category.equals(MainActivity.HAPPY)){
	      	Log.v("WE","oncreateview HAPPY");
	    	MyApplication.FIRST_TIME_ANGRY = true;
	    	MyApplication.FIRST_TIME_BOTH = true;
	    }
	    else if (category.equals(MainActivity.ANGRY)){
	      	Log.v("WE","oncreateview ANGRY");
	    	MyApplication.FIRST_TIME_HAPPY = true;
	    	MyApplication.FIRST_TIME_BOTH = true;
	    }
	    if (category.equals(MainActivity.BOTH)){
	      	Log.v("WE","oncreateview BOTH");
	    	MyApplication.FIRST_TIME_HAPPY = true;
	    	MyApplication.FIRST_TIME_ANGRY = true;
	    }
		
      	getting_escandalos = true;
      	
      	lView = (ListView) v.findViewById(R.id.list_escandalos);
      	loading = (ProgressBar) v.findViewById(R.id.prog_list_escandalos);

      	/*
		lView.setOnRefreshListener(new OnRefreshListener() {
			
		    @Override
		    public void onRefresh() {
		    	
		    	// Si no se están obteniendo otros escándalos
		    	if (!getting_escandalos){
			    	// Si hay conexión
					if (Connectivity.isOnline(getActivity().getApplicationContext())){
						// Obtenemos si hay nuevos escandalos subidos (y los mostramos al principio)
						getting_escandalos = true;
						getNewEscandalosAsync = new GetNewEscandalos();
						getNewEscandalosAsync.execute();
					}

					else{
						Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No dispone de una conexión a internet", Toast.LENGTH_SHORT);
						toast.show();
					} 
		    	}
		    	// Si se están obteniendo otros indicamos que ha terminado el pull to push
		    	else{
		    		Log.v("WE","ENTRA EN ELSE WIIIII :DDDDD");
		    	}			
		    }
		});
		*/	
		
      	escandalos = new ArrayList<Escandalo>();    	
		escanAdapter = new EscandaloAdapter(getActivity(), R.layout.escandalo,
				escandalos);
				
		lView.setAdapter(escanAdapter); 

		lView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {		
				
				// Comprobamos cuando el scroll termina de moverse
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					// Si no es el último (tiene uno detrás)
					if (lView.getChildAt(1) != null){

						// Obtenemos la coordenada Y donde empieza el segundo escandalo
						final int[] location = new int[2];
						lView.getChildAt(1).getLocationOnScreen(location);
						
						// Si el primer escandalo ocupa más pantalla que el segundo mostrado, mostramos el primero			
						// Para versión menor a 11: no tenemos en cuenta el status bar
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
						// Para versión 11+: tenemos en cuenta el status bar
						else{
							if ((location[1] - (getActionBarHeight() + getStatusBarWithTabsHeight() + MyApplication.ALTO_TABS)) >= getAvailableHeightScreen() / 2) {
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
				
				// Si quedan 5 escándalos para llegar al último y hay más escandalos: obtenemos los siguientes 10
	            if (firstVisibleItem == escanAdapter.getCount() - 5 && there_are_more_escandalos){
	            	
	            	// Si no hemos comprobado ya que no disponga de conexión (para que no aparezcan 20 mil mensajes de no tiene conexión)
	            	if (!connection_checked){
	            		// Si hay conexión
						if (Connectivity.isOnline(getActivity().getApplicationContext())){
							// Abrimos la llave
							connection_checked = false;
			            	// Usamos el booleano como llave de paso (sólo la primera vez entrará). Cuando se obtengan los 10 escándalos se volverá a abrir
			            	if (!getting_escandalos && escandalos.size() >0){
			            		Log.v("WE","Activado!!!");
				            	getting_escandalos = true;
				            	getEscandalosAsync = new GetEscandalos();
			            		getEscandalosAsync.execute();
			            	}
						}

						else{
							// Cerramos la llave
							connection_checked = true;
							Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT);
							toast.show();
						} 
	            	}		    	
	            }
	            				
				// Guardamos en que posición está el primer escandalo visible (actualmente) en la pantalla
				first_visible_item_count = firstVisibleItem;                
			}
		});
			
		return v;
	}
	
	
	/**
	 * onViewCreated
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	
		//lView.onRefreshComplete();
	    if (category.equals(MainActivity.HAPPY)){
	      	Log.v("WE","onviewcreated HAPPY");
	    }
	    else if (category.equals(MainActivity.ANGRY)){
	      	Log.v("WE","onviewcreated ANGRY");
	    }
	    if (category.equals(MainActivity.BOTH)){
	      	Log.v("WE","onviewcreated BOTH");
	    }
		
		if (savedInstanceState != null && savedInstanceState
				.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
			
		
		// Ten
		//AdsSessionController.setApplicationId(getActivity().getApplicationContext(),APP_ID);
        //AdsSessionController.registerAdsReadyListener(this);       		
		
		if (Connectivity.isOnline(getActivity().getApplicationContext())){
			getEscandalosAsync = new GetEscandalos();
	    	getEscandalosAsync.execute();	
		}
		else{
			Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No dispones de conexión a internet", Toast.LENGTH_SHORT);
			toast.show();
			
			// Quitamos el progressbar y mostramos la lista de escandalos
			loading.setVisibility(View.GONE);
			lView.setVisibility(View.VISIBLE);
			
			// Abrimos la llave
			getting_escandalos = false;
		}	
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
	 * onStop
	 */
	@Override
	public void onStop(){
		super.onStop();
	}
	
	
	/**
	 * onDestroyView
	 */
	@Override
	public void onDestroyView(){
		super.onDestroyView();
	    cancelGetEscandalos();

        lView.setAdapter(null);
        escandalos.clear();
        escandalos = null;
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
     * setActivatedPosition
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
		available_height = screen_height - getActionBarHeight() - getStatusBarWithTabsHeight() ;
		
		return available_height;
	}
	
	
	
	/**
	 * Devuelve el alto del status bar
	 * @return
	 */
	private int getStatusBarWithTabsHeight(){
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
				
		LinearLayout linear_action_bar = (LinearLayout) getActivity().findViewById(R.id.ll_main_action_bar_1);
		return linear_action_bar.getHeight() * 2;	
		/*
		 
		TypedValue tv = new TypedValue();
		int action_bar_height = 0;
		
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
           if (getActivity().getApplicationContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        	   action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        else if(getActivity().getApplicationContext().getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
        	Log.v("WE","Entra else action bar");

        	//action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        */
     
		//return action_bar_height;
	}
	
	
	
	
	
	
	
	
	

	
	
	

	/**
	 * Obtiene los siguientes 10 escándalos anteriores a partir de uno dado
	 * @author Alejandro
	 *
	 */
	private class GetEscandalos extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected void onPreExecute(){
			Log.v("WE","Entra en getescandalos");
			any_error = false;
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			String url = null;

			// HAPPY
			if (category.equals(MainActivity.HAPPY)){
				// Usamos un servicio u otro dependiendo si es el primer listado de escándalos o ya posteriores
				if (MyApplication.FIRST_TIME_HAPPY){
					Log.v("WE","primera vez happy");
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=1&country=" + MyApplication.code_selected_country;
				}
				else{
					// A partir del último ID obtenido
					Log.v("WE","ultimo id obtenido happy");
					// Si no hay escándalos consideramos que es la primera vez (BUG: si se pulsa muy rápido en las pestañas)
					if (escandalos.size() == 0){
						url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=1&country=" + MyApplication.code_selected_country;

					}
					else{
						url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/?category__id=1";
					}
				}
			}
			
			// ANGRY
			else if (category.equals(MainActivity.ANGRY)){
				// Usamos un servicio u otro dependiendo si es el primer listado de escándalos o ya posteriores
				if (MyApplication.FIRST_TIME_ANGRY){
					Log.v("WE","primera vez angry");
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=2&country=" + MyApplication.code_selected_country;
				}
				else{
					Log.v("WE","ultimo id obtenido happy");
					if (escandalos.size() == 0){
						url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=2&country=" + MyApplication.code_selected_country;

					}
					else{
						url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/?category__id=2";
					}
				}
			}
			
			// BOTH
			else if (category.equals(MainActivity.BOTH)){
				// Usamos un servicio u otro dependiendo si es el primer listado de escándalos o ya posteriores
				if (MyApplication.FIRST_TIME_BOTH){
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&country=" + MyApplication.code_selected_country;
				}
				else{
					if (escandalos.size() == 0){
						url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=1&country=" + MyApplication.code_selected_country;

					}
					else{
                        url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/";
					}
				}
			}
			
	    	HttpResponse response = null;
			
		    try{
				
		    	HttpClient httpClient = new DefaultHttpClient();
		    	HttpGet getEscandalos = new HttpGet(url);
		   		getEscandalos.setHeader("content-type", "application/json");        		    

				// Hacemos la petición al servidor
		        response = httpClient.execute(getEscandalos);
		        String respStr = EntityUtils.toString(response.getEntity());
		        //Log.i("WE",respStr);
		        
		        JSONArray escandalosObject = null;
		        
		        // Si es la primera vez obtenemos los escandalos a partir de un JSONObject, sino obtenemos directamente el JSONArray
		       	        
		        // HAPPY
		        if (category.equals(MainActivity.HAPPY)) {
			        if (MyApplication.FIRST_TIME_HAPPY){
			        	MyApplication.FIRST_TIME_HAPPY = false;
			        	// Obtenemos el json
				        JSONObject respJson = new JSONObject(respStr);	                       			            
				        escandalosObject = respJson.getJSONArray("objects");
			        }
			        else{
			        	// Si no hay escándalos obtenemos los primeros
			        	if (escandalos.size() == 0){
					        JSONObject respJson = new JSONObject(respStr);	                       			            
					        escandalosObject = respJson.getJSONArray("objects");
			        	}
			        	else{
				        	escandalosObject = new JSONArray(respStr);
				        	
				        	// Si no hay más escandalos,lo indicamos
				        	if (escandalosObject.length() == 0){
				        		Log.v("WE","No hay mas happys");
				        		there_are_more_escandalos = false;
				        	}
			        	}
			        }
		        }
		        
		        // ANGRY
		        else if (category.equals(MainActivity.ANGRY)) {
			        if (MyApplication.FIRST_TIME_ANGRY){
			        	MyApplication.FIRST_TIME_ANGRY = false;
			        	// Obtenemos el json
				        JSONObject respJson = new JSONObject(respStr);	                       
				            
				        escandalosObject = respJson.getJSONArray("objects");
			        }
			        else{
			        	if (escandalos.size() == 0){
					        JSONObject respJson = new JSONObject(respStr);	                       			            
					        escandalosObject = respJson.getJSONArray("objects");
			        	}
			        	else{
				        	escandalosObject = new JSONArray(respStr);
				        	
				        	// Si no hay más escandalos,lo indicamos
				        	if (escandalosObject.length() == 0){
				        		Log.v("WE","No hay mas angrys");
				        		there_are_more_escandalos = false;
				        	}
			        	}
			        }
		        }
		        
		        // BOTH
		        else if (category.equals(MainActivity.BOTH)) {
			        if (MyApplication.FIRST_TIME_BOTH){
			        	MyApplication.FIRST_TIME_BOTH = false;
			        	// Obtenemos el json
				        JSONObject respJson = new JSONObject(respStr);	                       
				            
				        escandalosObject = respJson.getJSONArray("objects");
			        }
			        else{
			        	if (escandalos.size() == 0){
					        JSONObject respJson = new JSONObject(respStr);	                       			            
					        escandalosObject = respJson.getJSONArray("objects");
			        	}
			        	else{
				        	escandalosObject = new JSONArray(respStr);
				        	
				        	// Si no hay más escandalos,lo indicamos
				        	if (escandalosObject.length() == 0){
				        		Log.v("WE","No hay mas boths");
				        		there_are_more_escandalos = false;
				        	}
			        	}
			        }
		        }
		        	      
		        
		        // Obtenemos los datos de los escándalos
		        for (int i=0 ; i < escandalosObject.length(); i++){
		        	JSONObject escanObject = escandalosObject.getJSONObject(i);
		            	
		            final String category = escanObject.getString("category");
		            final String date = escanObject.getString("date");
		            final String id = escanObject.getString("id");
		            final String img = escanObject.getString("img_p");
		            final String comments_count = escanObject.getString("comments_count");
		            String latitude = escanObject.getString("latitude");
		            String longitude = escanObject.getString("longitude");
		            final String resource_uri = escanObject.getString("resource_uri");	       
		            final String title = new String(escanObject.getString("title").getBytes("ISO-8859-1"), HTTP.UTF_8);
		            final String user = escanObject.getString("user");
		            String visits_count = escanObject.getString("visits_count");
		            final String sound = escanObject.getString("sound");
		            final String username = escanObject.getString("username");
	            			           
		            if (escandalos != null){
			            getActivity().runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                        // Añadimos el escandalo al ArrayList
	                        	  escandalos.add(new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
	          							R.drawable.loading), Integer.parseInt(comments_count), resource_uri, "http://scandaloh.s3.amazonaws.com/" + img, sound, username, date));	        	       
	                        }
			            }); 
		            }          
		    	 }
		     }
		     catch(Exception ex){
		            Log.e("ServicioRest","Error obteniendo escándalos", ex);
		            // Hubo algún error inesperado
		            any_error = true;
		            
					// Mandamos la excepcion a Google Analytics
					EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build());                                                 // False indicates a fatal exception			                       
		     }
		       
		    // Si hubo algún error devolvemos 666
		    if (any_error){
		    	return 666;
		    }
		    else{
		    	// Devolvemos el código resultado
			    return (response.getStatusLine().getStatusCode());   
		    }
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Quitamos el progresbar y mostramos la lista de escandalos
			loading.setVisibility(View.GONE);
			lView.setVisibility(View.VISIBLE);
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666){
				Toast toast = Toast.makeText(getActivity().getBaseContext(), "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hubo ningún error inesperado
			if (!isCancelled()){
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	escanAdapter.notifyDataSetChanged();
		        }
		        else{
		        }        
			}
			// Abrimos la llave
			getting_escandalos = false;
			//lView.onRefreshComplete();
			
			// Indicamos a la actividad que ha terminado de actualizar
			tCallbacks.onRefreshFinished();
	    }
	}
	
	
	
	
	


	/**
	 * Obtiene (si hay) nuevos escandalos
	 *
	 */
	private class GetNewEscandalos extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected void onPreExecute(){
			any_error = false;
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			// A partir del id más nuevo obtenido (el primero del array)
			String url = null;
			// HAPPY
			if (category.equals(MainActivity.HAPPY)){
				Log.v("WE","nuevos happy");
				url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(0).getId() + "/" + MyApplication.code_selected_country+ "/new/?category__id=1";
			}
			// ANGRY
			if (category.equals(MainActivity.ANGRY)){
				Log.v("WE","nuevos angry");
				
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/" + escandalos.get(0).getId() + "/" + MyApplication.code_selected_country+ "/new/?category__id=2";
			}
			// BOTH
			if (category.equals(MainActivity.BOTH)){
				Log.v("WE","nuevos both");			
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/" + escandalos.get(0).getId() + "/" + MyApplication.code_selected_country+ "/new/";
			}
	
			HttpResponse response = null;
			
			try{
				 HttpClient httpClient = new DefaultHttpClient();
        
				 HttpGet getEscandalos = new HttpGet(url);
				 getEscandalos.setHeader("content-type", "application/json");        
		           
				// Hacemos la petición al servidor
		        response = httpClient.execute(getEscandalos);
		        String respStr = EntityUtils.toString(response.getEntity());
		        Log.i("WE",respStr);
		        
		        JSONArray escandalosObject = new JSONArray(respStr);
           
		        for (int i = 0; i<escandalosObject.length(); i++){
		        	JSONObject escanObject = escandalosObject.getJSONObject(i);
		            	
		            final String category = escanObject.getString("category");
		            final String date = escanObject.getString("date");
		            final String id = escanObject.getString("id");
		            final String img = escanObject.getString("img_p");
		            final String comments_count = escanObject.getString("comments_count");
		            String latitude = escanObject.getString("latitude");
		            String longitude = escanObject.getString("longitude");
		            final String resource_uri = escanObject.getString("resource_uri");	        
		            final String title = escanObject.getString("title");
		            final String user = escanObject.getString("user");
		            String visits_count = escanObject.getString("visits_count");
		            final String sound = escanObject.getString("sound");
		            final String username = escanObject.getString("username");
	            		         
		            if (escandalos != null){
				        getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
						        // Añadimos el escandalo al comienzo
						        escandalos.add(0,new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
										R.drawable.loading), Integer.parseInt(comments_count), resource_uri, "http://scandaloh.s3.amazonaws.com/" + img, sound, username, date));		
							}
				        });
		            }		               	
		    	 }
		     }
		     catch(Exception ex){
		            Log.e("ServicioRest","Error!", ex);
		            // Hubo algún error inesperado
		            any_error = true;
					// Mandamos la excepcion a Google Analytics
					EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build());  
		     }		        	 
		    
	    	// Si hubo algún error devolvemos 666
		    if (any_error){
		    	return 666;
		    }
		    else{
		    	// Devolvemos el código resultado
		    	return (response.getStatusLine().getStatusCode());   	
		    }
	    }
	
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Si hubo algún error inesperado
			if (result == 666){
				Toast toast = Toast.makeText(getActivity().getBaseContext(), "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			
			else{
				if (!isCancelled()){
					// Si es codigo 2xx --> OK
			        if (result >= 200 && result <300){
			        	updateNumCommentsAsync = new UpdateNumComments();
			        	updateNumCommentsAsync.execute();
			        }
			        else{
			        }        
				}
			}
	    }
	}
	
	
	
	
	/**
	 * Actualiza el número de comentarios de cada escándalo
	 *
	 */
	private class UpdateNumComments extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected void onPreExecute(){
			any_error = false;
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			// La url dependerá de si estamos en una categoria en concreta o en la de Todas
			String url = null;
			// HAPPY
			if (category.equals(MainActivity.HAPPY) || category.equals(MainActivity.ANGRY)){
				Log.v("WE","num comentarios happy");
				url = MyApplication.SERVER_ADDRESS + "api/v1/comment/count/" + escandalos.get(escandalos.size()-1).getId() + "/" + escandalos.get(0).getId() + "/";
			}
			
			// BOTH
			if (category.equals(MainActivity.BOTH)){
				Log.v("WE","num comentarios both");			
				url = MyApplication.SERVER_ADDRESS + "api/v1/comment/count/" + escandalos.get(escandalos.size()-1).getId() + "/" + escandalos.get(0).getId() + "/all/";
			}
	
			HttpResponse response = null;
			
		    try{
			
		    	HttpClient httpClient = new DefaultHttpClient();
		    	HttpGet getEscandalos = new HttpGet(url);
		   		getEscandalos.setHeader("content-type", "application/json");        		    

				// Hacemos la petición al servidor
		        response = httpClient.execute(getEscandalos);
		        String respStr = EntityUtils.toString(response.getEntity());
		        Log.i("WE",respStr);
		        		        
		        JSONArray commentsArray = new JSONArray(respStr);		        	      
		        
		        // Recorremos todos los escándalos y obtenemos sus nº de comentarios
		        for (int i=0 ; i < commentsArray.length(); i++){
		        	JSONObject numCommentObject = commentsArray.getJSONObject(i);
		        	
		        	final String id_escandalo = numCommentObject.getString("photo_id");
		        	final String num_comments = numCommentObject.getString("num_comments");
	            	
		        	final int posicion = i;
		        	
		            if (escandalos != null){
			            getActivity().runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                        //Modificamos el número de comentarios del escándalo
	                         Escandalo escaAux = escandalos.get(posicion);
	                         escaAux.setNumComments(Integer.parseInt(num_comments));
	                         escandalos.set(posicion, escaAux);
	                        }
			            }); 
		            }          
		    	 }  	 	    	 
		     }
		     
		     catch(Exception ex){
		            Log.e("ServicioRest","Error actualizando número de comentarios", ex);
		            // Hubo algún error inesperado
		            any_error = true;
		            
					// Mandamos la excepcion a Google Analytics
					EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build());                                                 // False indicates a fatal exception			                       
		     }
		       
		    // Si hubo algún error devolvemos 666
		    if (any_error){
		    	return 666;
		    }
		    else{
			     // Devolvemos el código resultado
			     return (response.getStatusLine().getStatusCode());   
		    }
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666){
				Toast toast = Toast.makeText(getActivity().getBaseContext(), "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hubo ningún error inesperado
			if (!isCancelled()){
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	escanAdapter.notifyDataSetChanged();
		        }
		        else{
		        }        
			}
			// Abrimos la llave
			getting_escandalos = false;
			//lView.onRefreshComplete();
			
			// Indicamos a la actividad que ha terminado de actualizar
			tCallbacks.onRefreshFinished();
	    }
	}



	/**
	 * Cancela si hubiese alguna hebra obteniendo escándalos
	 */
	private void cancelGetEscandalos(){	
			
		if (getEscandalosAsync != null){
			if (getEscandalosAsync.getStatus() == AsyncTask.Status.PENDING || getEscandalosAsync.getStatus() == AsyncTask.Status.RUNNING){
				getEscandalosAsync.cancel(true);
			}
		} 
		
		if (getNewEscandalosAsync != null){
			if (getNewEscandalosAsync.getStatus() == AsyncTask.Status.PENDING || getNewEscandalosAsync.getStatus() == AsyncTask.Status.RUNNING){
				getNewEscandalosAsync.cancel(true);
			}
		} 		
	}
	
	
	/**
	 * Comprueba y obtiene si hay nuevos escándalos
	 */
	public void updateList(){
		
		// Si no se están obteniendo otros escándalos
	    if (!getting_escandalos){
	    	
		    // Si hay conexión
			if (Connectivity.isOnline(getActivity().getApplicationContext())){
				
				// Paramos si se estuviesen actualizando el nº de comentarios
				if (updateNumCommentsAsync != null){
					if (updateNumCommentsAsync.getStatus() == AsyncTask.Status.PENDING || updateNumCommentsAsync.getStatus() == AsyncTask.Status.RUNNING){
						updateNumCommentsAsync.cancel(true);
					}
				}
				
				// Colocamos el carrusel en el primer escándalo
				lView.setSelection(0);
					
				// Obtenemos los escándalos:
				// Si no hay ninguno mostrado obtenemos los primeros, si hay alguno obtenemos si hay nuevos escándalos subidos
				getting_escandalos = true;
					
				if (escandalos.size() > 0){			
					getNewEscandalosAsync = new GetNewEscandalos();
					getNewEscandalosAsync.execute();
				}
				else{
					getEscandalosAsync = new GetEscandalos();
				   	getEscandalosAsync.execute();
				}
			}
				
			// No hay conexión
			else{				
				Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No dispones de conexión a internet", Toast.LENGTH_SHORT);
				toast.show();
				// Indicamos a la actividad que ha terminado de actualizar
				tCallbacks.onRefreshFinished();	
			} 
	    }
	    else{
			// Indicamos a la actividad que ha terminado de actualizar
			tCallbacks.onRefreshFinished();	
	    }
	}
	
	
	/**
	 * Actualiza el número de comentarios de los escándalos
	 */
	public void updateComments(){
		
		// Actualizamos nº de comentarios si no se están obteniendo otros escándalos
	    if (!getting_escandalos){
	    	
		    // Si hay conexión
			if (Connectivity.isOnline(getActivity().getApplicationContext())){							
				if (escandalos.size() > 0){			
					updateNumCommentsAsync = new UpdateNumComments();
					updateNumCommentsAsync.execute();
				}
			}
	    }
	}
}
