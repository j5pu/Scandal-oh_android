package com.bizeu.escandaloh;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;


public class DetailPhotoActivity extends SherlockActivity {

	private ImageViewTouch mImage;
	
	private Bitmap photo;
	private String uri_audio;
	private boolean played_already ;
	private boolean orientation_changed ;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
		
		played_already = false;

		
		// Quitamos el action bar
		getSupportActionBar().hide();
			
		if (getIntent() != null){
			
			// Obtenemos y mostarmos la foto
			byte[] bytes = getIntent().getByteArrayExtra("bytes");
			photo = ImageUtils.bytesToBitmap(bytes);
			mImage = (ImageViewTouch) findViewById(R.id.img_photo_detail);
			mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
			mImage.setImageBitmap(photo);
			
			// Obtenemos el audio
			uri_audio = getIntent().getStringExtra("uri_audio");
			
			orientation_changed = false ;
		}
	}
	
	

	/**
	 * onResume
	 */
	@Override
	protected void onResume(){
		super.onResume();
		// Si no lo ha reproducido ya (s�lo lo reproducimos una vez): reproducimos el audio
		if (!uri_audio.equals("null") && !played_already){
			played_already = true;	
			new PlayAudio().execute();	
		}
	}
	
	
	
	
	
	/**
	 * onPause Detiene y elimina si hab�a alg�n audio activo
	 */
	@Override
	protected void onPause(){
		super.onPause();
		if (!orientation_changed){
			Audio.getInstance().releaseResources();
		}
	}
	
	
	/**
	 * onDestroy
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		// Volvemos a permitir que se pulse en las fotos del carruse
		MyApplication.PHOTO_CLICKED = false; 
		
	}
	
	

	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	    
	    // Ha cambiado la orientaci�n del dispositivo
        orientation_changed = true;
	  }
	
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  savedInstanceState.putBoolean("already_played", played_already);
	}
	
	
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  played_already = savedInstanceState.getBoolean("already_played");
	}
	
	
	
	/**
	 * Reproduce el audio
	 * @author Alejandro
	 *
	 */
	private class PlayAudio extends AsyncTask<String,Integer,Boolean> {
		 
		
		@Override
	    protected Boolean doInBackground(String... params) {
	    	
	    	Audio.getInstance().startPlaying("http://scandaloh.s3.amazonaws.com/" + uri_audio);							
	        return false;
	    }
		
	}


}
