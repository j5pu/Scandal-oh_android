package com.bizeu.escandaloh;

import java.io.File;
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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.widget.IcsAdapterView.AdapterContextMenuInfo;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.users.MainLoginActivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.zed.adserver.BannerView;
import com.zed.adserver.onAdsReadyListener;

public class MainActivity extends SherlockFragmentActivity implements onAdsReadyListener {

	public static final String ANGRY = "Enfadado";
	public static final String HAPPY = "Feliz";
	public static final String BOTH = "Ambos";
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
	private FragmentTabHost mTabHost;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);	
		
		// Tab Host (FragmentTabHost)
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tabcontent);

        // Añadimos los tabs para cada uno de los 3 fragmentos
        mTabHost.addTab(mTabHost.newTabSpec(HAPPY).setIndicator(HAPPY),ListEscandalosFragmentHappy.class, null);  
        mTabHost.addTab(mTabHost.newTabSpec(ANGRY).setIndicator(ANGRY),ListEscandalosFragmentAngry.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(BOTH).setIndicator(BOTH),ListEscandalosFragmentBoth.class, null);
 
        // Almacenamos el alto del FragmentTabHost
        Display display = getWindowManager().getDefaultDisplay();
        mTabHost.measure(display.getWidth(), display.getHeight());
        MyApplication.ALTO_TABS = mTabHost.getMeasuredHeight();
	             
		// Ten
		//AdsSessionController.setApplicationId(getApplicationContext(),APP_ID);
       // AdsSessionController.registerAdsReadyListener(this);

		// Action bar
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setLogo(R.drawable.corte_manga);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);		
		
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
			
	}

	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
	   // AdsSessionController.enableTracking();
		
		Log.v("WE","onresume");
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
	   // AdsSessionController.pauseTracking();
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
	       // AdsSessionController.stopTracking();
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
	    //AdsSessionController.detectHomeButtonEvent();
	}
	
	
	
	
	/**
	 * onCreateOptionsMenu
	 */
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_action_bar, menu);
		
		com.actionbarsherlock.view.MenuItem mi_photo = menu.findItem(R.id.menu_action_bar_take_photo);
		com.actionbarsherlock.view.MenuItem mi_logout = menu.findItem(R.id.menu_action_bar_logout);
		
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

		com.actionbarsherlock.view.MenuItem mi_photo = menu.findItem(R.id.menu_action_bar_take_photo);
		com.actionbarsherlock.view.MenuItem mi_logout = menu.findItem(R.id.menu_action_bar_logout);
		
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

			case R.id.menu_action_bar_take_photo:	
				
			    // Si está logueado iniciamos la cámara
				if (logged){ 				
					
					if (checkCameraHardware(this)){
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
					// El dispositivo no dispone de camara
					else{
						Toast toast = Toast.makeText(this, "Este dispositivo no dispone de cámara", Toast.LENGTH_LONG);
						toast.show();
					}
				}
				// Si no, iniciamos la pantalla de login
				else{
					Intent i = new Intent(this, MainLoginActivity.class);
					startActivity(i);
				}
				
				break;
				
			case R.id.menu_action_bar_logout:
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
				
				break;
				
			case R.id.menu_action_bar_update_list:
				
				Bundle arguments = new Bundle();
				
				// Obtenemos cuál es el tab activo
				String current_tab = mTabHost.getCurrentTabTag();
				
				// Volvemos a mostrar los escandalos según el tab en el que estemos
				if (current_tab.equals(HAPPY)){
					ListEscandalosFragmentHappy fragment = new ListEscandalosFragmentHappy();
					fragment.setArguments(arguments);
		            getSupportFragmentManager().beginTransaction()
		                    .replace(R.id.tabcontent, fragment)
		                    .commit(); 
				}
				else if (current_tab.equals(ANGRY)){
					ListEscandalosFragmentAngry fragment = new ListEscandalosFragmentAngry();
					fragment.setArguments(arguments);
		            getSupportFragmentManager().beginTransaction()
		                    .replace(R.id.tabcontent, fragment)
		                    .commit(); 
				}
				else if (current_tab.equals(BOTH)){
					ListEscandalosFragmentBoth fragment = new ListEscandalosFragmentBoth();
					fragment.setArguments(arguments);
		            getSupportFragmentManager().beginTransaction()
		                    .replace(R.id.tabcontent, fragment)
		                    .commit(); 
				}
		        Log.v("WE","es: " + mTabHost.getCurrentTabTag());
		        break;
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
