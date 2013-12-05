package com.bizeu.escandaloh;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.users.MainLoginActivity;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;

public class MainActivity extends SherlockFragmentActivity implements onAdsReadyListener, OnClickListener {

	public static final String CATEGORY = "Category";
	public static final String ANGRY = "Denuncia";
	public static final String HAPPY = "Humor";
	public static final String BOTH = "Todas";
	private final static String APP_ID = "d83c1504-0e74-4cd6-9a6e-87ca2c509506";
	public static final int SHOW_CAMERA = 10;
    private static final int CREATE_ESCANDALO = 11;
    public static final int FROM_GALLERY = 12;
    
	private File photo;
	public static ArrayList<Escandalo> escandalos;
	EscandaloAdapter escanAdapter;
	private Uri mImageUri;
	AmazonS3Client s3Client;
	private FrameLayout banner;
	private BannerView adM;
	private SharedPreferences prefs;
	public static FragmentTabHost mTabHost;
	private Context context;
	
	private ImageView img_logout;
	private ImageView img_update_list;
	private ImageView img_take_photo;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
			
		context = this;
		
		// Si el usuario no está logueado mostramos la pantalla de registro/login
		if (!MyApplication.logged_user){
	        Intent i = new Intent(MainActivity.this, MainLoginActivity.class);
	        startActivity(i);
		}
	
		// Action Bar
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		View view = getLayoutInflater().inflate(R.layout.action_bar, null);
		getSupportActionBar().setCustomView(view);
			
		// Listeners del action bar
		img_logout = (ImageView) findViewById(R.id.img_actionbar_logout);
		img_logout.setOnClickListener(this);
		img_update_list = (ImageView) findViewById(R.id.img_actionbar_updatelist);
		img_update_list.setOnClickListener(this);
		img_take_photo = (ImageView) findViewById(R.id.img_actionbar_takephoto);
		img_take_photo.setOnClickListener(this);
		

		// Tab Host (FragmentTabHost)
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tabcontent);       

        // Separadores de los tabs
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.prueba_separador); //id of your drawble resource here
        
        // Añadimos los tabs para cada uno de los 3 fragmentos
        Bundle b = new Bundle();
        b.putString(CATEGORY, HAPPY);
        mTabHost.addTab(mTabHost.newTabSpec(HAPPY).setIndicator(HAPPY),ListEscandalosFragment.class, b);  
        
        b = new Bundle();
        b.putString(CATEGORY, ANGRY);
        mTabHost.addTab(mTabHost.newTabSpec(ANGRY).setIndicator(ANGRY),ListEscandalosFragment.class, b);
        
        b = new Bundle();
        b.putString(CATEGORY, BOTH);
        mTabHost.addTab(mTabHost.newTabSpec(BOTH).setIndicator(BOTH),ListEscandalosFragment.class, b);


        // Cambiamos el color del texto y le añadimos el selector (para la barrita de abajo)
        for(int i=0;i<mTabHost.getTabWidget().getChildCount();i++){
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.gris_oscuro));

           mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_selector);           
        } 

        
        // Almacenamos el alto del FragmentTabHost
        Display display = getWindowManager().getDefaultDisplay();
        mTabHost.measure(display.getWidth(), display.getHeight());
        MyApplication.ALTO_TABS = mTabHost.getMeasuredHeight();
	             
		// Ten
		//AdsSessionController.setApplicationId(getApplicationContext(),APP_ID);
       // AdsSessionController.registerAdsReadyListener(this);	
	}
	
	
	@Override
	public void onStart(){
		super.onStart();

		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
	}

	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
	   // AdsSessionController.enableTracking();
		
		// Si está logueado quitamos el botón de logout y añadimos la cámara (con su selector)
		if (MyApplication.logged_user){
			img_logout.setVisibility(View.VISIBLE);
			
			StateListDrawable states = new StateListDrawable();

			states.addState(new int[] {android.R.attr.state_pressed},
			    getResources().getDrawable(R.drawable.camara_pressed));
			states.addState(new int[] {android.R.attr.state_focused},
			    getResources().getDrawable(R.drawable.camara_pressed));
			states.addState(new int[] { },
			    getResources().getDrawable(R.drawable.camara));
			img_take_photo.setImageDrawable(states);
			
		}
		// Si no está logueado mostramos el botón de logout y añadimos el más (con su selector)
		else{			
			img_logout.setVisibility(View.INVISIBLE);
			
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
					Bitmap bitAux = ImageUtils.uriToBitmap(mImageUri, context);
					ImageUtils.saveBitmapIntoGallery(bitAux, context);
					
					Intent i = new Intent(MainActivity.this, CreateEscandaloActivity.class);
					i.putExtra("photo_from", SHOW_CAMERA);
					i.putExtra("photoUri", mImageUri.toString());
					startActivityForResult(i, CREATE_ESCANDALO);
				}
				else{
					Toast toast = Toast.makeText(context, "Hubo algún error con la cámara", Toast.LENGTH_LONG);
					toast.show();
				}			
			}
			else if (resultCode == RESULT_CANCELED) {	
				Log.v("WE","Result canceled");
	        }	
		}
			
		else if (requestCode == FROM_GALLERY) {
			if (data != null){
	            Uri selectedImageUri = data.getData();
	            Intent i = new Intent(MainActivity.this, CreateEscandaloActivity.class);
	            i.putExtra("photo_from", FROM_GALLERY);
	            i.putExtra("photoUri", ImageUtils.getRealPathFromURI(context,selectedImageUri));
	            startActivityForResult(i, CREATE_ESCANDALO);
			}
        }
		
		else if (requestCode == CREATE_ESCANDALO){
		}
	}
	



	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.img_actionbar_takephoto:

			// Si dispone de conexión
			if (Connectivity.isOnline(context)){
				// Si está logueado iniciamos la cámara
				if (MyApplication.logged_user){ 
					
					// Creamos un menu para elegir entre hacer foto con la cámara o cogerla de la galería
					final CharSequence[] items = {"Tomar desde la cámara", "Cogerla de la galería"};
					 AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				        builder.setTitle("Añadir foto");
				        builder.setItems(items, new DialogInterface.OnClickListener() {
				            @Override
				            public void onClick(DialogInterface dialog, int item) {
				                if (items[item].equals("Tomar desde la cámara")) {
				                	if (checkCameraHardware(context)){
										Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
										File photo = null;
										try{
									        photo = createFileTemporary("picture", ".png");
									        photo.delete();
									    }
									    catch(Exception e){
									        Log.v("WE", "Can't create file to take picture!");
									    }
										
									    mImageUri = Uri.fromFile(photo);
									    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
										startActivityForResult(takePictureIntent, SHOW_CAMERA);
									}
									// El dispositivo no dispone de camara
									else{
										Toast toast = Toast.makeText(context, "Este dispositivo no dispone de cámara", Toast.LENGTH_LONG);
										toast.show();
									}
				                } 
				                else if (items[item].equals("Cogerla de la galería")) {
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
					startActivity(i);
				}
			}
			else{
				Toast toast = Toast.makeText(context, "No dispone de conexión a internet", Toast.LENGTH_LONG);
				toast.show();
			}
			
			break;
			
		case R.id.img_actionbar_logout:
			if (MyApplication.logged_user){
				AlertDialog.Builder alert_logout = new AlertDialog.Builder(this);
				alert_logout.setTitle("Cerrar sesión usuario");
				alert_logout.setMessage("¿Seguro que desea cerrar la sesión actual?");
				alert_logout.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {  
			            public void onClick(DialogInterface dialogo1, int id) {  
							// Deslogueamos al usuario
							prefs.edit().putString(MyApplication.USER_URI, null).commit();
							MyApplication.logged_user = false;
							img_logout.setVisibility(View.INVISIBLE);
							// Cabiamos el icono de la cámara al más (con su selector)
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
			            }  
			        });            
			     alert_logout.show(); 
			}
			break;
			
		// Le decimos al fragmento que actualice los escándalos (y suba el carrusel al primero)
		case R.id.img_actionbar_updatelist:
		
			// Obtenemos cuál es el tab activo
			String current_tab = mTabHost.getCurrentTabTag();

			ListEscandalosFragment lef = null;
			if (current_tab.equals(HAPPY)){
				lef = (ListEscandalosFragment) ((SherlockFragmentActivity)context).getSupportFragmentManager().findFragmentByTag(HAPPY);		
			}
			
			else if (current_tab.equals(ANGRY)){
				lef = (ListEscandalosFragment) ((SherlockFragmentActivity)context).getSupportFragmentManager().findFragmentByTag(ANGRY);				
			}
			else if (current_tab.equals(BOTH)){
				lef = (ListEscandalosFragment) ((SherlockFragmentActivity)context).getSupportFragmentManager().findFragmentByTag(BOTH);
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
	    File scandaloh_dir= Environment.getExternalStorageDirectory();
	    scandaloh_dir=new File(scandaloh_dir.getAbsolutePath()+"/Scandaloh/");
	    Log.v("WE","scandaloh_dir: " + scandaloh_dir.toString());
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
}
