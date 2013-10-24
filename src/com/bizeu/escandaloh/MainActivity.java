package com.bizeu.escandaloh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.adapters.EscandaloAdapter;
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
	
	Bitmap taken_photo;
	
	
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
		
		// Datos de prueba
		escandalos = new ArrayList<Escandalo>();
		escandalos.add(new Escandalo("Prueba 1", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_1), 222));
		escandalos.add(new Escandalo("Prueba 2", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_2), 12));
		escandalos.add(new Escandalo("Prueba 3", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_3), 2));
		escandalos.add(new Escandalo("Prueba 4", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_4), 3));
		escandalos.add(new Escandalo("Prueba 5", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_5), 32));
		escandalos.add(new Escandalo("Prueba 6", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_1), 332));
		escandalos.add(new Escandalo("Prueba 7", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_2), 2));
		escandalos.add(new Escandalo("Grande", Escandalo.ANGRY,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_grande), 234));
		

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

			// --------------- VERSION PARA LISTVIEW ------------------------
			Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
			File photo;
			try{
		        // place where to store camera taken picture
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


			// --------------- VERSION PARA EXIF ------------------------------
			/*
			 * SimpleDateFormat dateFormat = new
			 * SimpleDateFormat("yyyyMMdd-HHmmss"); String fileName =
			 * dateFormat.format(new Date()) + ".jpg";
			 * 
			 * // or use timestamp e.g String fileName =
			 * System.currentTimeMillis()+".jpg";
			 * 
			 * photo = new File(Environment.getExternalStorageDirectory(),
			 * fileName);
			 * 
			 * Intent takePictureIntent = new
			 * Intent(MediaStore.ACTION_IMAGE_CAPTURE); File dir=
			 * Environment.getExternalStoragePublicDirectory
			 * (Environment.DIRECTORY_DCIM);
			 * 
			 * File output=new File(dir, "CameraContentDemo.jpeg");
			 * takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
			 * Uri.fromFile(output)); startActivityForResult(takePictureIntent,
			 * SHOW_CAMERA);
			 */
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
				Intent i = new Intent(MainActivity.this, CreateEscandaloActivity.class);
				i.putExtra("photoUri", mImageUri.toString());
				startActivityForResult(i, CREATE_ESCANDALO);
					
					/*
					 * Metadata metadata = null; try { metadata =
					 * ImageMetadataReader.readMetadata(photo); } catch
					 * (ImageProcessingException e) { Log.v("WE","Entr aen catch");
					 * e.printStackTrace(); } catch (IOException e) { // TODO
					 * Auto-generated catch block e.printStackTrace(); }
					 * 
					 * for (Directory directory : metadata.getDirectories()) { for
					 * (Tag tag : directory.getTags()) { Log.v("WE","Tag es: " +
					 * tag); System.out.println(tag); } }
					 */
		}
		
		else if (requestCode == CREATE_ESCANDALO){
			taken_photo = (Bitmap) data.getParcelableExtra("data");
			String written_title = data.getExtras().getString("title");
			String selected_category = data.getExtras().getString("category");
			
			// Añadimos y actualizamos listado					
			if (selected_category.equals(CreateEscandaloActivity.HAPPY_CATEGORY)){
				escandalos.add(new Escandalo(written_title,
						Escandalo.HAPPY, taken_photo, 3));
			}
			else{
				escandalos.add(new Escandalo(written_title,
						Escandalo.ANGRY, taken_photo, 3));
			}
			escanAdapter.notifyDataSetChanged();


		}	
	}


	
	
	
	public InputStream bitmapToInput(Bitmap bit) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bit.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
		return (InputStream) bs;
	}

	
	public File bitmapToFile(Bitmap bit) {

		File file = null;
		// Bitmap bitmap = Utils.decodeBase64(base64);
		try {
			file = new File(this.getCacheDir(), "prueba.jpg");
			FileOutputStream fOut = new FileOutputStream(file);
			bit.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(null, "Save file error!");
		}
		return file;
	}

	
	
	public String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(contentUri, proj, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Exception e) {
			return contentUri.getPath();
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
	 * Crea una Uri para guardar una foto
	 */
	private static Uri getOutputMediaFileUri(){
	  return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	/**
	 * Crea un File para guardar una foto
	 * @return
	 */
	private static File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	          Environment.DIRECTORY_PICTURES), "Scandal-oh!");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (!mediaStorageDir.exists()){
	        if (!mediaStorageDir.mkdirs()){
	            Log.d("Scandal-oh!", "failed to create directory Scandal-oh!");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");

	    return mediaFile;
	}
	
	
	
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
	
	

	
	
	
	

}
