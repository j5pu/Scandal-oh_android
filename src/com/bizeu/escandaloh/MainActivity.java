package com.bizeu.escandaloh;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
import com.bizeu.escandaloh.adapters.EscandaloAdapter.EscandaloHolder;
import com.bizeu.escandaloh.model.Cache;
import com.bizeu.escandaloh.model.Escandalo;

public class MainActivity extends SherlockActivity {

	private static final int SHOW_CAMERA = 10;
    private static final int CREATE_ESCANDALO = 11;
	private File photo;
	public static ArrayList<Escandalo> escandalos;
	EscandaloAdapter escanAdapter;
	private int first_visible_item_count;
	private ListView list_escandalos;
	private Uri fileUri;
	private Uri mImageUri;
	public static AmazonClientManager clientManager = null;	
	Bitmap taken_photo;
	AmazonS3Client s3Client;
	private int escandalo_loading = 0 ;
	private Bitmap bitm;
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Action bar
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setLogo(R.drawable.corte_manga);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
	

		escandalos = new ArrayList<Escandalo>();
		
		// Datos de prueba	
		/*
		escandalos.add(new Escandalo("Prueba 1", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_1), 222, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Prueba 2", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_2), 12, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Prueba 3", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_3), 2, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Prueba 4", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_4), 3, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Prueba 5", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_5), 32, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Prueba 6", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_1), 332, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Prueba 7", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_2), 2, "/api/v1/photo/1/"));
		escandalos.add(new Escandalo("Grande", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_grande), 234, "/api/v1/photo/1/"));
		*/

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
	 * onCreateOptionsMenu
	 */
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.take_photo:
				Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
				File photo;
				try{
			        photo = this.createTemporaryFile("picture", ".png");
			        photo.delete();
			    }
			    catch(Exception e){
			        Log.v("WE", "Can't create file to take picture!");
			        return false;
			    }
				
			    mImageUri = Uri.fromFile(photo);
			    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				startActivityForResult(takePictureIntent, SHOW_CAMERA);
				break;

			case R.id.update_list:
				Log.v("WE", "update list");
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
	 * Crea un archivo (File) temporal
	 * @param part
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	private File createTemporaryFile(String part, String ext) throws Exception
	{
	    File tempDir= Environment.getExternalStorageDirectory();
	    tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
	    if(!tempDir.exists())
	    {
	        tempDir.mkdir();
	    }
	    return File.createTempFile(part, ext, tempDir);
	}
	
	

	
	
	
	/**
	 * Obtiene los escandalos del servidor y los muestra en pantalla
	 * @author Alejandro
	 *
	 */
	private class GetEscandalos extends AsyncTask<Void,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(Void... params) {
	    	boolean result = false;
	
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	        String url = "http://192.168.1.48:8000/api/v1/photo/";
	        	    	        
	        HttpGet getEscandalos = new HttpGet(url);
	        getEscandalos.setHeader("content-type", "application/json");        
	        
	        
	        try{
				// Hacemos la petición al servidor
	        	HttpResponse resp = httpClient.execute(getEscandalos);
	        	String respStr = EntityUtils.toString(resp.getEntity());
	         
	        	// Obtenemos el json
	            JSONObject respJson = new JSONObject(respStr);
	            
	            if (respJson != null){
	            	result = true;
	            }
	            
	            // Parseamos el json para obtener los escandalos
	            JSONArray escandalosObject = null;
	            
	            escandalosObject = respJson.getJSONArray("objects");
	            
	            for (int i=0 ; i < escandalosObject.length(); i++){
	            	JSONObject escanObject = escandalosObject.getJSONObject(i);
	            	
	            	final String category = escanObject.getString("category");
	            	String date = escanObject.getString("date");
	            	String id = escanObject.getString("id");
	            	final String img = escanObject.getString("img");
	            	String comments_count = escanObject.getString("comments_count");
	            	String latitude = escanObject.getString("latitude");
	            	String longitude = escanObject.getString("longitude");
	            	final String resource_uri = escanObject.getString("resource_uri");	        
	            	final String title = escanObject.getString("title");
	            	String user = escanObject.getString("user");
	            	final String visits_count = escanObject.getString("visits_count");		            		           
	    	    	
		        	runOnUiThread(new Runnable() {
						@Override
						public void run() {
				            // Añadimos el escandalo al ArrayList
				        	escandalos.add(new Escandalo(title, category, BitmapFactory.decodeResource(getResources(),
									R.drawable.loading), Integer.parseInt(visits_count), resource_uri, img));
							escanAdapter.notifyDataSetChanged();	
							
				        	new GetPictureFromAmazon().execute(img);
						}
		        	});		        	
	    	    }             
	        }
	        catch(Exception ex){
	                Log.e("ServicioRest","Error!", ex);
	        }
	        	   							
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
			
	        if (result){
	        	Log.v("WE","escandalos recibidos");
	        	// Indicamos que descague su foto y la muestre (cuando la tenga)

	        }
	        else{
	        	Log.v("WE","escandalos NO recibidos");
	        }
	        
	    }
	}
	
	
	
	
	
	
	private class GetPictureFromAmazon extends AsyncTask<String,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(String... params) {
	    	boolean result = false;
		            	
	        //Obtenemos la imagen de cache
	    	 bitm = Cache.getInstance(getBaseContext()).obtenImagenDeCache(params[0]);
	    	    	
	    	  if (bitm == null){
	    	    	s3Client = new AmazonS3Client( new BasicAWSCredentials( "AKIAJ6GJKNGVTOB3AREA", "RSNSbgY+HJJTufi4Dq6yM/r4tWBdTzEos+lUmDQU") );
	    	    	// Hacemos la petición a Amazon y obtenemos la imagen
				    S3ObjectInputStream content = s3Client.getObject("scandaloh", params[0]).getObjectContent();
				    byte[] bytes;
						
					try {
						bytes = IOUtils.toByteArray(content);
						content.close();
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 5;
						bitm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
					} catch (IOException e) {
						e.printStackTrace();
					}

				    // Añadimos la foto a caché
				    Cache.getInstance(getBaseContext()).aniadeImagenAcache(params[0], bitm);  // La almacenamos en cache		   
	    	    }
	    	    else{
	    	    }
	 							
	    	result = true;
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
		    final Escandalo esc = escandalos.get(escandalo_loading);
		    esc.setPicture(bitm);	
		    escandalos.set(escandalo_loading, esc);
		    escandalo_loading++;    
			escanAdapter.notifyDataSetChanged();
			Log.v("WE","Imagen añadida");
			

	    }
	}
	
	
	
	
	
	
	

	
	
	

}
