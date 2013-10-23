package com.bizeu.escandaloh;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import com.bizeu.escandaloh.util.Base64;

public class MainActivity extends SherlockActivity {

	private static final int SHOW_CAMERA = 10;
	private File photo;
	public static ArrayList<Escandalo> escandalos_prueba;
	EscandaloAdapter escanAdapter;
	private int first_visible_item_count;
	private ListView list_escandalos;
	
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
		escandalos_prueba = new ArrayList<Escandalo>();
		escandalos_prueba.add(new Escandalo("Prueba 1", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_1), 222));
		escandalos_prueba.add(new Escandalo("Prueba 2", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_2), 12));
		escandalos_prueba.add(new Escandalo("Prueba 3", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_3), 2));
		escandalos_prueba.add(new Escandalo("Prueba 4", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_4), 3));
		escandalos_prueba.add(new Escandalo("Prueba 5", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_5), 32));
		escandalos_prueba.add(new Escandalo("Prueba 6", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_1), 332));
		escandalos_prueba.add(new Escandalo("Prueba 7", Escandalo.CONIA,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_aleman_2), 2));
		escandalos_prueba.add(new Escandalo("Grande", Escandalo.SERIO,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.pastor_grande), 234));

		escanAdapter = new EscandaloAdapter(this, R.layout.escandalo,
				escandalos_prueba);
		

		list_escandalos = (ListView) findViewById(R.id.list_escandalos);
		list_escandalos.setAdapter(escanAdapter);

		list_escandalos.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				// Comprobamos cuando el scroll termina de moverse
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					
					// Si no es el �ltimo (tiene uno detr�s)
					if (list_escandalos.getChildAt(1) != null){

						// Obtenemos la coordenada Y donde empieza el segundo escandalo
						final int[] location = new int[2];
						list_escandalos.getChildAt(1).getLocationOnScreen(location);
						
						// Si el primer escandalo ocupa m�s pantalla que el segundo mostrado, mostramos el primero			
						// Para versi�n menor a 11: no tenemos en cuenta el status bar
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
						// Para versi�n 11+: tenemos en cuenta el status bar
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
				// Guardamos en que posici�n est� el primer escandalo visible (actualmente) en la pantalla
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
			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(takePictureIntent, SHOW_CAMERA);

			// Intent cameraintent = new
			// Intent("android.media.action.IMAGE_CAPTURE");
			// cameraintent.putExtra(MediaStore.EXTRA_OUTPUT,
			// Uri.fromFile(photo));
			// startActivityForResult(cameraintent, SHOW_CAMERA);

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

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SHOW_CAMERA) {
			if (data != null){
				if (data.hasExtra("data")) {
					Uri photoUri = data.getData();
					taken_photo = (Bitmap) data.getParcelableExtra("data");
		
					Intent i = new Intent(MainActivity.this, CreateEscandalo.class);
					i.putExtras(data);
					//i.setData(photoUri);
					startActivity(i);
					
					
					// A�adimos y actualizamos listado
					//escandalos_prueba.add(new Escandalo("Nueva foto",
							//Escandalo.SERIO, taken_photo, 3));
					//escanAdapter.notifyDataSetChanged();
	
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
			}
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
	 * Devuelve el alto de pantalla disponible en p�xeles: screen height - (status bar height + action bar height)
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
	
	
	
	
	
	
	
	

}
