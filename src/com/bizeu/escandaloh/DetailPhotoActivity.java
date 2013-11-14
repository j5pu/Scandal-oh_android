package com.bizeu.escandaloh;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;


public class DetailPhotoActivity extends Activity {

	private ImageViewTouch mImage;
	private Bitmap photo;
	private String route_img;
	private String uri_audio;
	
	private Audio audio_recorder;
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
			
		if (getIntent() != null){
			
			// Obtenemos y mostarmos la foto
			byte[] bytes = getIntent().getByteArrayExtra("bytes");
			photo = ImageUtils.BytesToBitmap(bytes);
			mImage = (ImageViewTouch) findViewById(R.id.img_photo_detail);
			mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
			mImage.setImageBitmap(photo);
			
			// Obtenemos y reproducimos el audio (si tiene)
			uri_audio = getIntent().getStringExtra("uri_audio");
			
			orientation_changed = false ;
		}
	}
	
	

	
	@Override
	protected void onResume(){
		super.onResume();
		// Si no lo ha reproducido ya (sólo lo reproducimos una vez): reproducimos el audio
		if (!uri_audio.equals("null") && !played_already){
			played_already = true;
			new PlayAudio().execute();	
		}
	}
	
	
	
	
	
	/**
	 * onPause Detiene y elimina si había algún audio activo
	 */
	@Override
	protected void onPause(){
		super.onPause();
		if (!orientation_changed){
			Audio.getInstance().closeAudio();
		}
	}
	
	

	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
	    // Ha cambiado la orientación del dispositivo
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
	 * Obtiene la imagen de Amazon y la muestra
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
