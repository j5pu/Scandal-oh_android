package com.bizeu.escandaloh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bizeu.escandaloh.adapters.DrawerMenuAdapter;
import com.bizeu.escandaloh.adapters.ScandalohFragmentPagerAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.settings.SettingsActivity;
import com.bizeu.escandaloh.users.MainLoginActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;


public class MainActivity extends SherlockFragmentActivity implements OnClickListener{

	public static final int SHOW_CAMERA = 10;
    private static final int CREATE_ESCANDALO = 11;
    public static final int FROM_GALLERY = 12;
    public static final int SHARING = 13;
    public static final String FIRST_TIME = "First_time"; // Nos indica si pulsó el + para hacer una foto
	public static final String CATEGORY = "Category";
	public static final String ANGRY = "Denuncia";
	public static final String HAPPY = "Humor";
	public static final String BOTH = "Todas";
	private final static String APP_ID = "d83c1504-0e74-4cd6-9a6e-87ca2c509506";
	private LinearLayout ll_refresh;
	private LinearLayout ll_take_photo;
	private ImageView img_update_list;
	private ImageView img_take_photo;
	private ProgressBar progress_refresh;
	private Button but_happy;
	private Button but_angry;
	private ListView list_menu_lateral;
    DrawerLayout mDrawerLayout;
 
	private Uri mImageUri;
	AmazonS3Client s3Client;
	private SharedPreferences prefs;
	private Context mContext;
	ScandalohFragmentPagerAdapter adapter;
	ViewPager pager = null;
	ProgressBar loading;
	private boolean any_error;
	private GetEscandalos getEscandalosAsync;
	private GetNewEscandalos getNewEscandalosAsync;
	//private UpdateNumComments updateNumCommentsAsync;
	private String category;
	private boolean getting_escandalos = true;
	private boolean there_are_more_escandalos = true;
	public static ArrayList<Escandalo> escandalos;
    DrawerMenuAdapter mMenuAdapter;
    String[] options;
    ActionBarDrawerToggle mDrawerToggle;
    private boolean no_hay_escandalos;
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		no_hay_escandalos = false;
		mContext = this;
      	escandalos = new ArrayList<Escandalo>();  
		
		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup)findViewById(R.id.lay_pantalla_main));
		
		// Si el usuario no está logueado mostramos la pantalla de registro/login
		if (!MyApplication.logged_user){
	        Intent i = new Intent(MainActivity.this, MainLoginActivity.class);
	        i.putExtra(FIRST_TIME, true);
	        startActivity(i);
		}

		// ACTION BAR
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar, null);
		getSupportActionBar().setCustomView(view);
		
        // Activamos el logo del menu para el menu
        actBar.setHomeButtonEnabled(true);
        actBar.setDisplayHomeAsUpEnabled(true);
		
		/*
		// Si es 4.2+ deshabilitamos el botón home
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
	    	actBar.setDisplayShowHomeEnabled(false);
	    }
	    // Si no, le ponemos una imagen invisible (fallan los tabs si quitamos el home en estas versiones)
	    else{
	    	actBar.setIcon(R.drawable.noimage);
	    }
	    */
		
		loading = (ProgressBar) findViewById(R.id.loading_escandalos);
		img_update_list = (ImageView) findViewById(R.id.img_actionbar_updatelist);
		ll_refresh = (LinearLayout) findViewById(R.id.ll_main_refresh);
		ll_refresh.setOnClickListener(this);
		img_take_photo = (ImageView) findViewById(R.id.img_actionbar_takephoto);
		ll_take_photo = (LinearLayout) findViewById(R.id.ll_main_take_photo);
		ll_take_photo.setOnClickListener(this);
		progress_refresh = (ProgressBar) findViewById(R.id.prog_refresh_action_bar);
		but_happy = (Button) findViewById(R.id.but_action_bar_happy);
		but_happy.setOnClickListener(this);
		but_angry = (Button) findViewById(R.id.but_action_bar_angry);
		but_angry.setOnClickListener(this);
	    	
		// VIEWPAGER
        pager = (ViewPager) this.findViewById(R.id.pager);
        adapter = new ScandalohFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            	
            	Log.v("WE","adapter count: " + adapter.getCount() + " y position: " + position);
            	// Si quedan 4 escándalos más para llegar al último y aún quedan más escándalos (si hemos llegado 
            	// a los últimos no se pedirán más): obtenemos los siguientes 10
            	if (position == adapter.getCount() - 5 && there_are_more_escandalos){
            		// Usamos una llave de paso (sólo la primera vez entrará). Cuando se obtengan los 10 escándalos se volverá a abrir
            		if (!getting_escandalos){
            			getEscandalosAsync = new GetEscandalos();
            			getEscandalosAsync.execute();
            		}
            	}
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });  
        
        
        // MENU LATERAL
        mDrawerLayout = (DrawerLayout) findViewById(R.id.lay_pantalla_main);
        
        // Sombra del menu sobre la pantalla
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        options = new String[] {"Ajustes"};
        //TODO V2.0 options = new String[] {"Perfil", "Notificaciones", "País", "Ajustes", "Danos tu opinión"};
        
        mMenuAdapter = new DrawerMenuAdapter(MainActivity.this, options);
       
        list_menu_lateral = (ListView) findViewById(R.id.menu_lateral);
        list_menu_lateral.setAdapter(mMenuAdapter);
        list_menu_lateral.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {
 
            public void onDrawerClosed(View view) {
                // TODO Auto-generated method stub
                super.onDrawerClosed(view);
            }
 
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                // Set the title on the action when drawer open
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // PASO ENTRE ESCÁNDALOS
        // Le asignamos la animación al pasar entre escándalos (API 11+)
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
        
        // Separación entre escándalos
		pager.setPageMargin(3);
		
      	//TODO Asignamos categoria happy la primera vez
      	category = HAPPY;
      	but_happy.setClickable(false);
      	but_happy.setTypeface(null, Typeface.BOLD);
	
		// Si hay conexión
		if (Connectivity.isOnline(mContext)){
			// Obtenemos los 10 primeros escándalos
			getEscandalosAsync = new GetEscandalos();
			getEscandalosAsync.execute();
		}
		else{
			Toast toast = Toast.makeText(mContext, "No dispones de conexión a internet", Toast.LENGTH_SHORT);
			toast.show();
			// Quitamos el progresbar y mostramos la lista de escandalos
			loading.setVisibility(View.GONE);
			pager.setVisibility(View.VISIBLE);
		}
	}

    

 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart(){
		super.onStart();
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		/*
		// Actualizamos nº de comentarios si no se están obteniendo otros escándalos
	    if (!getting_escandalos){	
		    // Si hay conexión
			if (Connectivity.isOnline(mContext)){							
				if (escandalos.size() > 0){			
					updateNumCommentsAsync = new UpdateNumComments();
					updateNumCommentsAsync.execute();
				}
			}
	    }		
		*/
		// Activamos google analytics
		EasyTracker.getInstance(this).activityStart(this);  
		
	}

	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
		/*
		// Abrimos la llave para el caso de error del timeout al obtener fotos
		MyApplication.TIMEOUT_PHOTO_SHOWN = false;
			
	   // AdsSessionController.enableTracking();	
		*/
		
		// Si está logueado mostramos la cámara (con su selector)
		if (MyApplication.logged_user){		
			StateListDrawable states = new StateListDrawable();

			states.addState(new int[] {android.R.attr.state_pressed},
			    getResources().getDrawable(R.drawable.camara_pressed));
			states.addState(new int[] {android.R.attr.state_focused},
			    getResources().getDrawable(R.drawable.camara_pressed));
			states.addState(new int[] { },
			    getResources().getDrawable(R.drawable.camara));
			img_take_photo.setImageDrawable(states);
			
		}
		// Si no está logueado mostramos el símbolo para hacer login
		else{					
			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] {android.R.attr.state_pressed},
				    getResources().getDrawable(R.drawable.mas_pressed));
				states.addState(new int[] {android.R.attr.state_focused},
				    getResources().getDrawable(R.drawable.mas_pressed));
				states.addState(new int[] { },
				    getResources().getDrawable(R.drawable.mas));
			img_take_photo.setImageDrawable(states);
		}	
	}
	
	

	/**
	 * onPause
	 */
	@Override
	protected void onPause() {
	    super.onPause();   
	    Log.v("WE","Entra en onPause");
	}
	
	
	/**
	 * onStop
	 */
	@Override
	public void onStop(){
		super.onStop();
		Log.v("WE","Entra en onStop");
		// Paramos google analytics
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	
	/**
	 * onDestroy
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.v("WE","Entra en onDestroy");
	    cancelGetEscandalos();
	}
	
	
	/**
	 * onOptionsItemSelected
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
 
    	// Asignamos el botón home para el menu lateral
        if (item.getItemId() == android.R.id.home) {
 
            if (mDrawerLayout.isDrawerOpen(list_menu_lateral)) {
                mDrawerLayout.closeDrawer(list_menu_lateral);
            } else {
                mDrawerLayout.openDrawer(list_menu_lateral);
            }
        }
 
        return super.onOptionsItemSelected(item);
    }
	


	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == SHOW_CAMERA) {
			if (resultCode == RESULT_OK) {
				if (mImageUri != null){
					// Guardamos la foto en la galería
					Bitmap bitAux = ImageUtils.uriToBitmap(mImageUri, mContext);
					ImageUtils.saveBitmapIntoGallery(bitAux, mContext);
					
					Intent i = new Intent(MainActivity.this, CreateEscandaloActivity.class);
					i.putExtra("photo_from", SHOW_CAMERA);
					i.putExtra("photoUri", mImageUri.toString());
					startActivityForResult(i, CREATE_ESCANDALO);
				}
				else{
					Toast toast = Toast.makeText(mContext, "Hubo algún error con la cámara", Toast.LENGTH_LONG);
					toast.show();
				}			
			}
			else if (resultCode == RESULT_CANCELED) {	
	        }	
		}
			
		else if (requestCode == FROM_GALLERY) {
			if (data != null){
	            Uri selectedImageUri = data.getData();
	            Intent i = new Intent(MainActivity.this, CreateEscandaloActivity.class);
	            i.putExtra("photo_from", FROM_GALLERY);
	            i.putExtra("photoUri", ImageUtils.getRealPathFromURI(mContext,selectedImageUri));
	            startActivityForResult(i, CREATE_ESCANDALO);
			}
        }
	}
	


	/**
	 * onClick
	 */
	@Override
	public void onClick(View v) {	
		//EasyTracker easyTracker = EasyTracker.getInstance(mContext);
		  
		switch(v.getId()){	
		// Login/Subir escandalo
		case R.id.ll_main_take_photo:
			
			// Paramos si hubiera algún audio reproduciéndose
			Audio.getInstance(mContext).releaseResources();

			// Si dispone de conexión
			if (Connectivity.isOnline(mContext)){
				// Si está logueado iniciamos la cámara
				if (MyApplication.logged_user){ 
					
					// Creamos un menu para elegir entre hacer foto con la cámara o cogerla de la galería
					final CharSequence[] items = {"Hacer foto con la cámara", "Seleccionar foto de la galería"};
					 AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				        builder.setTitle("Añadir foto");
				        builder.setItems(items, new DialogInterface.OnClickListener() {
				            @Override
				            public void onClick(DialogInterface dialog, int item) {
				            	
				                if (items[item].equals("Hacer foto con la cámara")) {
				                	
				      			     // Mandamos el evento a Google Analytics
				            		 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				      			     easyTracker.send(MapBuilder.createEvent("Acción UI",     // Event category (required)
				      			                     "Selección realizada",  // Event action (required)
				      			                     "Hacer foto desde la cámara",   // Event label
				      			                     null)            // Event value
				      			        .build()
				      			     );
				                	
				                     if (checkCameraHardware(mContext)){
										Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
										File photo = null;
									    photo = createFileTemporary("picture", ".png");
									        //photo.delete();
									    if (photo != null){
										    mImageUri = Uri.fromFile(photo);
										    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
											startActivityForResult(takePictureIntent, SHOW_CAMERA);
											photo.delete();
									    }

									    	/*
									        Log.v("WE", "Can't create file to take picture!");
											// Mandamos la excepcion a Google Analytics
											easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
											                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
											                       e),                                                             // The exception.
											                       false).build()); 
											Toast toast = Toast.makeText(mContext, "No se pudo acceder a la cámara", Toast.LENGTH_SHORT);
											toast.show();
											*/
									    //}
										

									}
				                     
									// El dispositivo no dispone de cámara
									else{
										Toast toast = Toast.makeText(mContext, "Este dispositivo no dispone de cámara", Toast.LENGTH_LONG);
										toast.show();
									}
				                } 
				                
				                else if (items[item].equals("Seleccionar foto de la galería")) {
				                	
				      			     // Mandamos el evento a Google Analytics
				            	     EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				      			     easyTracker.send(MapBuilder.createEvent("Acción UI",     // Event category (required)
				      			                     "Selección realizada",  // Event action (required)
				      			                     "Subir foto desde la galería",   // Event label
				      			                     null)            // Event value
				      			        .build()
				      			     );
				      			     
				                	Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				                	startActivityForResult(i, FROM_GALLERY);
				                } 
				            }
				        });
				        builder.show();                 
				}
				// Si no, iniciamos la pantalla de login
				else{
					Intent i = new Intent(this, MainLoginActivity.class);
			        i.putExtra(FIRST_TIME, false); // Indicamos que no es la primera vez que saldrá esta pantalla (ha pulsado "+")
					startActivity(i);
				}
			}
			else{
				Toast toast = Toast.makeText(mContext, "No dispone de conexión a internet", Toast.LENGTH_LONG);
				toast.show();
			}
			
			break;
			
			
		// Actualizar carrusel: Le decimos al fragmento que actualice los escándalos (y suba el carrusel al primero)
		case R.id.ll_main_refresh:
			
			// Mandamos el evento a Google Analytics
			/*
			easyTracker.send(MapBuilder.createEvent("Acción UI",     // Event category (required)
			                   "Boton clickeado",  // Event action (required)
			                   "Actualizar lista escándalos",   // Event label
			                   null)            // Event value
			      .build()
			);
			*/
			
			// Cambiamos la imagen de actualizar por un loading
			progress_refresh.setVisibility(View.VISIBLE);
			img_update_list.setVisibility(View.GONE);
			
			// Nos colocamos en el primer escandalo
			pager.setCurrentItem(0);
			
			// Si no se están obteniendo otros escándalos
		    if (!getting_escandalos){   	
			    // Si hay conexión
				if (Connectivity.isOnline(mContext)){				
					// Paramos si se estuviesen actualizando el nº de comentarios
					/*
					if (updateNumCommentsAsync != null){
						if (updateNumCommentsAsync.getStatus() == AsyncTask.Status.PENDING || updateNumCommentsAsync.getStatus() == AsyncTask.Status.RUNNING){
							updateNumCommentsAsync.cancel(true);
						}
					}
					*/				
						
					// Obtenemos los escándalos:
					// Si no hay ninguno mostrado obtenemos los primeros, si hay alguno obtenemos si hay nuevos escándalos subidos
					getting_escandalos = true;
						
					if (escandalos.size() > 0){			
						getNewEscandalosAsync = new GetNewEscandalos();
						getNewEscandalosAsync.execute();
					}
					else{
						no_hay_escandalos = true; // Indicamos que no hay escándalos aún
						getEscandalosAsync = new GetEscandalos();
					   	getEscandalosAsync.execute();
					}
				}
					
				// No hay conexión
				else{				
					Toast toast = Toast.makeText(mContext, "No dispones de conexión a internet", Toast.LENGTH_SHORT);
					toast.show();
					// Indicamos a la actividad que ha terminado de actualizar
					refreshFinished();	
				} 
		    }
		    else{
				// Indicamos a la actividad que ha terminado de actualizar
				refreshFinished();	
		    }
										
	        break;	
	       
	        
	    // Selección de categoría
		case R.id.but_action_bar_happy: case R.id.but_action_bar_angry:
				// Si hay conexión
				if (Connectivity.isOnline(mContext)){
					// Inhabilitamos ambos botones
					but_happy.setClickable(false);
					but_angry.setClickable(false);
					// Abrimos llave de hay más escandalos
					there_are_more_escandalos = true;
				    // Quitamos los escándalos actuales
				    escandalos.clear();

				    // Seleccionado happy
					if (v.getId() == R.id.but_action_bar_happy){
						Log.v("WE","seleccionado happy");
						category = HAPPY;
				      	but_happy.setTypeface(null, Typeface.BOLD);
				      	but_angry.setTypeface(null, Typeface.NORMAL);
					}
					// Seleccionado angry
					else{
						Log.v("WE","Seleccionado angry");
						category = ANGRY;
				      	but_angry.setTypeface(null, Typeface.BOLD);
				      	but_happy.setTypeface(null, Typeface.NORMAL);
					}

					pager.setCurrentItem(0);
					adapter.clearFragments();
					adapter.notifyDataSetChanged();
				    // Obtenemos los 10 primeros escándalos para la categoría seleccionada
					// Mostramos el progressBar y ocultamos la lista de escandalos
					loading.setVisibility(View.VISIBLE);
					pager.setVisibility(View.GONE);
					getEscandalosAsync = new GetEscandalos();
					getEscandalosAsync.execute();					
				}
							
				// No hay conexión
				else{				
					Toast toast = Toast.makeText(mContext, "No dispones de conexión a internet", Toast.LENGTH_SHORT);
					toast.show();
				} 
				break;
			}    	
	}
	
	
	
	/**
	 * Obtiene los siguientes 10 escándalos anteriores a partir de uno dado
	 * @author Alejandro
	 *
	 */
	private class GetEscandalos extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected void onPreExecute(){
			getting_escandalos = true;
			Log.v("WE","Entra en getescandalos con category: " + category);
			any_error = false;
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			String url = null;
			
			// HAPPY
			if (category.equals(MainActivity.HAPPY)){
				// Usamos un servicio u otro dependiendo si es el primer listado de escándalos o ya posteriores
				if (escandalos.size() == 0){
					Log.v("WE","primera vez happy");
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=1&country=" + MyApplication.code_selected_country;
				}
				else{
				    url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/?category__id=1";
				}
			}
			
			// ANGRY
			else if (category.equals(MainActivity.ANGRY)){
				// Usamos un servicio u otro dependiendo si es el primer listado de escándalos o ya posteriores
				if (escandalos.size() == 0){
					Log.v("WE","primera vez angry");
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=2&country=" + MyApplication.code_selected_country;
				}
				else{
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/?category__id=2";
				}
			}
			
			/*
			// BOTH
			else if (category.equals(MainActivity.BOTH)){
				// Usamos un servicio u otro dependiendo si es el primer listado de escándalos o ya posteriores
				if (MyApplication.FIRST_TIME_BOTH){
					url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&country=" + MyApplication.code_selected_country;
				}
				else{
					
					if (adapter.getCount() == 0){
						url = MyApplication.SERVER_ADDRESS + "api/v1/photo/?limit=10&category__id=1&country=" + MyApplication.code_selected_country;

					}
					else{
					
                    url = MyApplication.SERVER_ADDRESS + "api/v1/photo/" + escandalos.get(escandalos.size()-1).getId() + "/" + MyApplication.code_selected_country+ "/previous/";
					//}
				}
			}
			*/
			
	    	HttpResponse response = null;
			
		    try{		
		    	HttpClient httpClient = new DefaultHttpClient();
		    	HttpGet getEscandalos = new HttpGet(url);
		   		getEscandalos.setHeader("content-type", "application/json");        		    

				// Hacemos la petición al servidor
		        response = httpClient.execute(getEscandalos);
		        String respStr = EntityUtils.toString(response.getEntity());
		        Log.i("WE",respStr);
		        
		        JSONArray escandalosObject = null;
		        
		        // Si es la primera vez obtenemos los escandalos a partir de un JSONObject, si no obtenemos directamente el JSONArray	       	        
		        // HAPPY
		        if (category.equals(MainActivity.HAPPY)) {        	
		        	if (escandalos.size() == 0){
			        	// Obtenemos el json
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
		        
		        // ANGRY
		        else if (category.equals(MainActivity.ANGRY)) {
		        	if (escandalos.size() == 0){
			        	// Obtenemos el json
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
		        
		        /*
		        // BOTH
		        else if (category.equals(MainActivity.BOTH)) {
			        if (MyApplication.FIRST_TIME_BOTH){
			        	MyApplication.FIRST_TIME_BOTH = false;
			        	// Obtenemos el json
				        JSONObject respJson = new JSONObject(respStr);	                       
				            
				        escandalosObject = respJson.getJSONArray("objects");
			        }
			        else{
			        	if (adapter.getCount() == 0){
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
		        */
		        	      
		        
		        // Obtenemos los datos de los escándalos
		        for (int i=0 ; i < escandalosObject.length(); i++){
		        	JSONObject escanObject = escandalosObject.getJSONObject(i);
		            	
		            final String category = escanObject.getString("category");
		            final String date = escanObject.getString("date");
		            final String id = escanObject.getString("id");
		            final String img_p = escanObject.getString("img_p"); // Fotos pequeñas sin marca de agua
		            final String img = escanObject.getString("img");
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
			            runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                        	// Añadimos el escandalo al ArrayList
	                        	Escandalo escanAux = new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
	          							R.drawable.loading), Integer.parseInt(comments_count), resource_uri, 
	          							"http://scandaloh.s3.amazonaws.com/" + img_p, "http://scandaloh.s3.amazonaws.com/" + img, 
	          							sound, username, date);
	                        	escandalos.add(escanAux);
	  			                adapter.addFragment(ScandalohFragment.newInstance(escanAux));
	  			                adapter.notifyDataSetChanged();
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
		            /*
					EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build());                                                 // False indicates a fatal exception			                       
		             */
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
			pager.setVisibility(View.VISIBLE);
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666){
				Toast toast = Toast.makeText(mContext, "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			
			/*
			// No hubo ningún error inesperado
			else{
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	// Los mostramos en pantalla
		        	adapter.notifyDataSetChanged();
		        }
			}
			*/
				     		
		    // Habilitamos los botones
		    if (category.equals(HAPPY)){
		    	but_angry.setClickable(true);
		    }
		    else{
		    	but_happy.setClickable(true);
		    }
		    
		    // Si hemos llegado aqui porque no habían escándalos, paramos el loading del menu
		    if (no_hay_escandalos){
				refreshFinished();
				no_hay_escandalos = false;
		    }
			
			// Ya no se están obteniendo escándalos (abrimos la llave)
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
		            final String img_p = escanObject.getString("img_p");
		            final String img = escanObject.getString("img");
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
				        runOnUiThread(new Runnable() {
							@Override
							public void run() {
						        // Añadimos el escandalo al comienzo
								Escandalo escanAux = new Escandalo(id, title, category, BitmapFactory.decodeResource(getResources(),
										R.drawable.loading), Integer.parseInt(comments_count), resource_uri, 
										"http://scandaloh.s3.amazonaws.com/" + img_p, "http://scandaloh.s3.amazonaws.com/" + img,
										sound, username, date);
						        escandalos.add(0,escanAux);		
						        adapter.addFragmentAtStart(ScandalohFragment.newInstance(escanAux));
						        adapter.notifyDataSetChanged();
							}
				        });
		            }		               	
		    	 }
		     }
		     catch(Exception ex){
		            Log.e("ServicioRest","Error!", ex);
		            // Hubo algún error inesperado
		            any_error = true;
		            /*
					// Mandamos la excepcion a Google Analytics
					EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build());  
					*/
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
				Toast toast = Toast.makeText(mContext, "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			
			else{
				// Si es codigo 2xx --> OK
				if (result >= 200 && result <300){
					//pager.setAdapter(null);
					//pager.setAdapter(adapter);
				}      			
			}
			// Abrimos la llave
			getting_escandalos = false;
			
			// Indicamos a la actividad que ha terminado de actualizar
			refreshFinished();
	    }
	}
	
	
	

	
	/**
	 * Actualiza el número de comentarios de cada escándalo
	 *
	 */
	/*
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
			            runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                        //Modificamos el número de comentarios del escándalo
	                         Escandalo escaAux = escandalos.get(posicion);
	                         escaAux.setNumComments(Integer.parseInt(num_comments));
	                         escandalos.set(posicion, escaAux);
	                        // adapter.setFragmentNumComments(posicion, Integer.parseInt(num_comments));
	                        }
			            }); 
		            }          
		    	 }  	 	    	 
		     }
		     
		     catch(Exception ex){
		            Log.e("ServicioRest","Error actualizando número de comentarios", ex);
		            // Hubo algún error inesperado
		            any_error = true;
		            
		            /*
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
			
			try{
				// Si hubo algún error inesperado mostramos un mensaje
				if (result == 666){
					Toast toast = Toast.makeText(mContext, "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
					toast.show();
				}
				
				// No hubo ningún error inesperado
				if (!isCancelled()){
					// Si es codigo 2xx --> OK
			        if (result >= 200 && result <300){
			        	pager.setAdapter(null);
			        	pager.setAdapter(adapter);
			        	pager.setCurrentItem(5);
			        	//adapter.notifyDataSetChanged();
			        }
			        else{
			        }        
				}
			}
			catch(Exception e){
				
			}

			// Abrimos la llave
			getting_escandalos = false;
			
			// Indicamos a la actividad que ha terminado de actualizar
			onRefreshFinished();
	    }
	}
	
	*/

	

	
	
	
	// -----------------------------------------------------------------------------
	// --------------------         MÉTODOS PRIVADOS          ----------------------
	// -----------------------------------------------------------------------------
	
	
	/**
	 * Crea un archivo temporal en una ruta con un formato específico
	 * @param part
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	private File createFileTemporary(String part, String ext) {
	    File scandaloh_dir = Environment.getExternalStorageDirectory();
	    scandaloh_dir = new File(scandaloh_dir.getAbsolutePath()+"/ScándalOh/");
	    if(!scandaloh_dir.exists()){
	    	scandaloh_dir.mkdirs();
	    }
	    try {
			return File.createTempFile(part, ext, scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
	        Log.v("MainActivity", "No se pudo crear archivo temporal para la foto");
			// Mandamos la excepcion a Google Analytics
	        EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
			                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
			                       e),                                                             // The exception.
			                       false).build()); 
			Toast toast = Toast.makeText(mContext, "No se pudo acceder a la cámara", Toast.LENGTH_SHORT);
			toast.show();
		}
	    
	    return null;
	}
	
	
	/**
	 * Comprueba si el dispositivo dispone de cámara
	 * @param context
	 * @return
	 */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        return true;
	    } else {
	        return false;
	    }
	}


	/**
	 * Se llama cuando se ha terminado de refrescar el carrusel
	 */
	public void refreshFinished() {
		// Cambiamos el loading por el botón de actualizar
		progress_refresh.setVisibility(View.GONE);
		img_update_list.setVisibility(View.VISIBLE);
	}
	
	

	/**
	 * Cancela si hubiese alguna hebra obteniendo escándalos
	 */
	private void cancelGetEscandalos(){	
			
		Log.v("WE","CANCELAMOS ESCANDALOS");
		if (getEscandalosAsync != null){
			if (getEscandalosAsync.getStatus() == AsyncTask.Status.PENDING || getEscandalosAsync.getStatus() == AsyncTask.Status.RUNNING){
				Log.v("WE","Entra en cancel happy");
				getEscandalosAsync.cancel(true);
			}
		} 
		
		if (getNewEscandalosAsync != null){
			if (getNewEscandalosAsync.getStatus() == AsyncTask.Status.PENDING || getNewEscandalosAsync.getStatus() == AsyncTask.Status.RUNNING){
				getNewEscandalosAsync.cancel(true);
			}
		} 
		
		/*
		if (updateNumCommentsAsync != null){
			if (updateNumCommentsAsync.getStatus() == AsyncTask.Status.PENDING || updateNumCommentsAsync.getStatus() == AsyncTask.Status.RUNNING){
				updateNumCommentsAsync.cancel(true);
			}
		}
		*/
	}
	
	
	
	/**
	 * Listener para las opciones del menu lateral
	 *
	 */
	/*
	 * TODO V2.0
	 * @author Alejandro
	 *
	 
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
	            case 0: // Perfil
	                Log.v("WE","Seleccionado posicion");
	                break;
	
	            case 1: // Notificaciones
	                Log.v("WE","Seleccionado notificaciones");
	                break;
	                
	            case 2: // País
	                Log.v("WE","Seleccionado país");
	                break;
	                  
	            case 3: // Ajustes
	            	Log.v("WE","Seleccionado ajustes");
	            	Intent i = new Intent(MainActivity.this, SettingsActivity.class);
	            	startActivity(i);
	            	break;
	            	
	            case 4: // Danos tu opinión
	            	Log.v("WE","seleccionado danos tu opinión");
	            	break;
            }
            list_menu_lateral.setItemChecked(position, true);
     
            // Cerramos el menu
            mDrawerLayout.closeDrawer(list_menu_lateral);
        }
    }
	*/
	
	
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {                  
	            case 0: // Ajustes
	            	Log.v("WE","Seleccionado ajustes");
	            	Intent i = new Intent(MainActivity.this, SettingsActivity.class);
	            	startActivity(i);
	            	break;            
            }
            list_menu_lateral.setItemChecked(position, true);
     
            // Cerramos el menu
            mDrawerLayout.closeDrawer(list_menu_lateral);
        }
    }
}
