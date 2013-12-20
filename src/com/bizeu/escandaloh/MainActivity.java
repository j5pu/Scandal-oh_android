package com.bizeu.escandaloh;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.users.MainLoginActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;

public class MainActivity extends SherlockFragmentActivity implements onAdsReadyListener, 
															OnClickListener, ListEscandalosFragment.Callbacks {

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
	
	private LinearLayout ll_logout;
	private LinearLayout ll_refresh;
	private LinearLayout ll_take_photo;
	private ImageView img_logout;
	private ImageView img_update_list;
	private ImageView img_take_photo;
	private ProgressBar progress_refresh;
  
	EscandaloAdapter escanAdapter;
	private Uri mImageUri;
	AmazonS3Client s3Client;
	private FrameLayout banner;
	private BannerView adM;
	private SharedPreferences prefs;
	private Context mContext;
	
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup)findViewById(R.id.lay_pantalla_main));
			
		mContext = this;
		
		// Si el usuario no está logueado mostramos la pantalla de registro/login
		if (!MyApplication.logged_user){
	        Intent i = new Intent(MainActivity.this, MainLoginActivity.class);
	        i.putExtra(FIRST_TIME, true);
	        startActivity(i);
		}

		// Action bar
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
	    
		// Si es 4.2+ deshabilitamos el botón home
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
	    	actBar.setDisplayShowHomeEnabled(false);
	    }
	    // Si no, le ponemos una imagen invisible (fallan los tabs si quitamos el home en estas versiones)
	    else{
	    	actBar.setIcon(R.drawable.noimage);
	    }
	    
		View view = getLayoutInflater().inflate(R.layout.action_bar, null);
		getSupportActionBar().setCustomView(view);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
		ll_logout = (LinearLayout) findViewById(R.id.ll_main_logout);
		ll_refresh = (LinearLayout) findViewById(R.id.ll_main_refresh);
		ll_take_photo = (LinearLayout) findViewById(R.id.ll_main_take_photo);
		
		// Action Bar Tabs
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {

		        Bundle b = new Bundle();
				ListEscandalosFragment lef = new ListEscandalosFragment();
					
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				
				switch(tab.getPosition()){
				
					case 0:
						b.putString(CATEGORY, HAPPY);
						lef.setArguments(b);
						ft.replace(R.id.frag_list_escandalos, lef, HAPPY);
						
						 // Mandamos el evento a Google Analytics
						 easyTracker.send(MapBuilder
						      .createEvent("Acción UI",     // Event category (required)
						                   "Tab seleccionado",  // Event action (required)
						                   "Escandalos categoría Humor",   // Event label
						                   null)            // Event value
						      .build()
						  );
						 
						break;
						
					case 1:
						b.putString(CATEGORY, ANGRY);
						lef.setArguments(b);
						ft.replace(R.id.frag_list_escandalos, lef, ANGRY);
						
						  // Mandamos el evento a Google Analytics
						  easyTracker.send(MapBuilder
						      .createEvent("Acción UI",     // Event category (required)
						                   "Tab seleccionado",  // Event action (required)
						                   "Escandalos categoría Denuncia",   // Event label
						                   null)            // Event value
						      .build()
						  );
						  
						break;
						
					case 2:
						b.putString(CATEGORY, BOTH);
						lef.setArguments(b);
						ft.replace(R.id.frag_list_escandalos, lef, BOTH);
						
						  // Mandamos el evento a Google Analytics
						  easyTracker.send(MapBuilder
						      .createEvent("Acción UI",     // Event category (required)
						                   "Tab seleccionado",  // Event action (required)
						                   "Escandalos categoría Todas",   // Event label
						                   null)            // Event value
						      .build()
						  );
						  
						break;
				}
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
	    };
	    
	    getSupportActionBar().addTab(getSupportActionBar().newTab().setText(HAPPY).setTabListener(tabListener), 0, true);
	    getSupportActionBar().addTab(getSupportActionBar().newTab().setText(ANGRY).setTabListener(tabListener), 1, false);
	    getSupportActionBar().addTab(getSupportActionBar().newTab().setText(BOTH).setTabListener(tabListener), 2, false);
	    
		// Listeners del action bar
		img_logout = (ImageView) findViewById(R.id.img_actionbar_logout);
		ll_logout.setOnClickListener(this);
		img_update_list = (ImageView) findViewById(R.id.img_actionbar_updatelist);
		ll_refresh.setOnClickListener(this);
		img_take_photo = (ImageView) findViewById(R.id.img_actionbar_takephoto);
		ll_take_photo.setOnClickListener(this);
		progress_refresh = (ProgressBar) findViewById(R.id.prog_refresh_action_bar);
		
		// Ten
		//AdsSessionController.setApplicationId(getApplicationContext(),APP_ID);
       // AdsSessionController.registerAdsReadyListener(this);	
	}

	
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart(){
		super.onStart();

		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		// Actualizamos el nº de comentarios de los escándalos
		String current_tab = getSupportActionBar().getSelectedTab().getText().toString();
		
		ListEscandalosFragment lef = null;
		if (current_tab.equals(HAPPY)){
			lef = (ListEscandalosFragment) ((SherlockFragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag(HAPPY);		
		}
		
		else if (current_tab.equals(ANGRY)){
			lef = (ListEscandalosFragment) ((SherlockFragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag(ANGRY);				
		}
		
		else if (current_tab.equals(BOTH)){
			lef = (ListEscandalosFragment) ((SherlockFragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag(BOTH);
		}
		
		lef.updateComments();
		
		// Activamos google analytics
		EasyTracker.getInstance(this).activityStart(this);  
	}

	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
		// Abrimos la llave para el caso de error del tiemout al obtener fotos
		MyApplication.TIMEOUT_PHOTO_SHOWN = false;
		
	   // AdsSessionController.enableTracking();
		
		// Si está logueado quitamos el botón de logout y añadimos la cámara (con su selector)
		if (MyApplication.logged_user){
			ll_logout.setVisibility(View.VISIBLE);
			
			StateListDrawable states = new StateListDrawable();

			states.addState(new int[] {android.R.attr.state_pressed},
			    getResources().getDrawable(R.drawable.camara_pressed));
			states.addState(new int[] {android.R.attr.state_focused},
			    getResources().getDrawable(R.drawable.camara_pressed));
			states.addState(new int[] { },
			    getResources().getDrawable(R.drawable.camara));
			img_take_photo.setImageDrawable(states);
			
		}
		// Si no está logueado mostramos el botón de logout y añadimos el "+" (con su selector)
		else{			
			ll_logout.setVisibility(View.INVISIBLE);
			
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
	   // AdsSessionController.pauseTracking();
	}
	
	
	/**
	 * onStop
	 */
	@Override
	public void onStop(){
		super.onStop();
		// Paramos google analytics
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	
	
	/**
	 * It will be called when the ads are ready to be shown
	 */
	@Override
	public void onAdsReady(){ 
		
	    //The banner will be show inside this view.
        banner = (FrameLayout) findViewById(R.id.banner);
     
        //BannerView initialization
        adM = new BannerView( this, getApplicationContext());
 
        banner.removeAllViews();
 
        //Add the bannerView to the container view
        banner.addView( adM );
 
        //Set the visibility to VISIBLE.
        banner.setVisibility( FrameLayout.VISIBLE );	
        	
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
	       // AdsSessionController.stopTracking();
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
	    //AdsSessionController.detectHomeButtonEvent();
	}
	*/
	

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
		
		else if (requestCode == CREATE_ESCANDALO){}
		
		else if (requestCode == SHARING){	
			// Eliminamos la foto que se compartió
			try{
				File photo_to_delete = new File(MyApplication.FILE_TO_DELETE);
				photo_to_delete.delete();
			}catch(Exception e){
	            e.printStackTrace();
	             // Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
				                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
				                       e),                                                             // The exception.
				                       false).build());
			}
       
		}
	}
	



	@Override
	public void onClick(View v) {
		
		EasyTracker easyTracker = EasyTracker.getInstance(mContext);
		  
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
										try{
									        photo = createFileTemporary("picture", ".png");
									        photo.delete();
									    }
									    catch(Exception e){
									        Log.v("WE", "Can't create file to take picture!");
											// Mandamos la excepcion a Google Analytics
											easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
											                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
											                       e),                                                             // The exception.
											                       false).build()); 
									    }
										
									    mImageUri = Uri.fromFile(photo);
									    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
										startActivityForResult(takePictureIntent, SHOW_CAMERA);
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
			
			
		// Logout
		case R.id.ll_main_logout:
			
			// Paramos si hubiera algún audio reproduciéndose
			Audio.getInstance(mContext).releaseResources();
			
			if (MyApplication.logged_user){
				AlertDialog.Builder alert_logout = new AlertDialog.Builder(this);
				alert_logout.setTitle("Cerrar sesión usuario");
				alert_logout.setMessage("¿Seguro que desea cerrar la sesión actual?");
				alert_logout.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {  
			            public void onClick(DialogInterface dialogo1, int id) {  
			            	
			  			  // Mandamos el evento a Google Analytics
			        	  EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			  			  easyTracker.send(MapBuilder.createEvent("Acción UI",     // Event category (required)
			  			                   "Boton clickeado",  // Event action (required)
			  			                   "Acepta log out",   // Event label
			  			                   null)            // Event value
			  			      .build()
			  			  );
			            	
							// Deslogueamos al usuario
							prefs.edit().putString(MyApplication.USER_URI, null).commit();
							MyApplication.logged_user = false;
							ll_logout.setVisibility(View.INVISIBLE);
							// Cambiamos el icono de la cámara al más (con su selector)
							StateListDrawable states = new StateListDrawable();
							states.addState(new int[] {android.R.attr.state_pressed},
								    getResources().getDrawable(R.drawable.mas_pressed));
								states.addState(new int[] {android.R.attr.state_focused},
								    getResources().getDrawable(R.drawable.mas_pressed));
								states.addState(new int[] { },
								    getResources().getDrawable(R.drawable.mas));
								img_take_photo.setImageDrawable(states);
			            }  
			        });  
				alert_logout.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {  
			        	public void onClick(DialogInterface dialogo1, int id) { 
			        		
			  			  // Mandamos el evento a Google Analytics
			        	  EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			  			  easyTracker.send(MapBuilder.createEvent("Acción UI",     // Event category (required)
			  			                   "Boton clickeado",  // Event action (required)
			  			                   "Rechaza Log out",   // Event label
			  			                   null)            // Event value
			  			      .build()
			  			  );
			            }  
			        });            
			     alert_logout.show(); 
			}
			break;
			
			
		// Actualizar carrusel: Le decimos al fragmento que actualice los escándalos (y suba el carrusel al primero)
		case R.id.ll_main_refresh:
			
			// Mandamos el evento a Google Analytics
			easyTracker.send(MapBuilder.createEvent("Acción UI",     // Event category (required)
			                   "Boton clickeado",  // Event action (required)
			                   "Actualizar lista escándalos",   // Event label
			                   null)            // Event value
			      .build()
			);
			
			// Mostramos el progress bar (loading) y ocultamos el botón de refrescar
			progress_refresh.setVisibility(View.VISIBLE);
			img_update_list.setVisibility(View.GONE);
										
			// Obtenemos cuál es el tab activo
			String current_tab = getSupportActionBar().getSelectedTab().getText().toString();
			
			ListEscandalosFragment lef = null;
			if (current_tab.equals(HAPPY)){
				lef = (ListEscandalosFragment) ((SherlockFragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag(HAPPY);		
			}
			
			else if (current_tab.equals(ANGRY)){
				lef = (ListEscandalosFragment) ((SherlockFragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag(ANGRY);				
			}
			
			else if (current_tab.equals(BOTH)){
				lef = (ListEscandalosFragment) ((SherlockFragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag(BOTH);
			}
			
			lef.updateList();
	        break;		
		}		
	}
	
	
	
	
	/**
	 * Crea un archivo temporal en una ruta con un formato específico
	 * @param part
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	private File createFileTemporary(String part, String ext) throws Exception{
	    File scandaloh_dir = Environment.getExternalStorageDirectory();
	    scandaloh_dir = new File(scandaloh_dir.getAbsolutePath()+"/ScándalOh/");
	    if(!scandaloh_dir.exists()){
	    	scandaloh_dir.mkdir();
	    }
	    return File.createTempFile(part, ext, scandaloh_dir);
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
	@Override
	public void onRefreshFinished() {
		// Ocultamos el progress bar (loading) y mostramos el botón de refrescar
		progress_refresh.setVisibility(View.GONE);
		img_update_list.setVisibility(View.VISIBLE);
	}
	
	
	

}
