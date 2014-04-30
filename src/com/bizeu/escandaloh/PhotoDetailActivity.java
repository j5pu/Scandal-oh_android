package com.bizeu.escandaloh;



import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;


public class PhotoDetailActivity extends SherlockActivity {

	private ImageViewTouch mImage;
	
	private Bitmap photo;
	private String uri_audio;
	private boolean played_already ;
	private boolean orientation_changed ;
	private Context mContext;
	private String url_big;
	private GetBigPictureTask getBigPictureAsync;
	private boolean any_error = false;
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.photo_detail);
		
		played_already = false;
		mContext = this;
		
		// Quitamos el action bar
		getSupportActionBar().hide();
			
		if (getIntent() != null){	
			// Obtenemos y mostramos la foto pequeña	
			byte[] bytes = getIntent().getByteArrayExtra("bytes");
			photo = ImageUtils.bytesToBitmap(bytes);
			mImage = (ImageViewTouch) findViewById(R.id.img_photo_detail);
			mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
			mImage.setImageBitmap(photo);
			
			// Obtenemos y mostramos la foto grande
			url_big = getIntent().getStringExtra("url_big");
			Log.v("WE","Url big: " + url_big);
			getBigPictureAsync = new GetBigPictureTask();
			getBigPictureAsync.execute();

			
			// Obtenemos el audio
			uri_audio = getIntent().getStringExtra("uri_audio");
			orientation_changed = false ;
			
			mImage.setSingleTapListener(new OnImageViewTouchSingleTapListener() {
				
				@Override
				public void onSingleTapConfirmed() {
					finish();	
				}
			});
		}
	}
	
	

	/**
	 * onResume
	 */
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
	 * onStart
	 */
	@Override 
	public void onStart(){
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	
	
	/**
	 * onPause Detiene y elimina si había algún audio activo
	 */
	@Override
	protected void onPause(){
		super.onPause();
		
		if (!orientation_changed){
			Audio.getInstance(mContext).releaseResources();
		}
		
	}
	
	
	/**
	 * onStop
	 */
	@Override
	public void onStop(){
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	
	/**
	 * onDestroy
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		// Volvemos a permitir que se pulse en las fotos del carrusel
		MyApplication.PHOTO_CLICKED = false; 
		
	}
	
	
	/**
	 * onConfigurationChanged
	 */
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	    
	    // Ha cambiado la orientación del dispositivo
        orientation_changed = true;
	  }
	
	/**
	 * onSaveInstanceState
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  savedInstanceState.putBoolean("already_played", played_already);
	}
	
	
	/**
	 * onRestoreInstanceState
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  played_already = savedInstanceState.getBoolean("already_played");
	}
	
	
	
	/**
	 * Reproduce el audio
	 *
	 */
	private class PlayAudio extends AsyncTask<String,Integer,Boolean> {
		 	
		@Override
	    protected Boolean doInBackground(String... params) {
	    	
	    	Audio.getInstance(mContext).startPlaying("http://scandaloh.s3.amazonaws.com/" + uri_audio);							
	        return false;
	    }	
	}
	
	
	

	/**
	 * Muestra la foto grande
	 * 
	 */
	private class GetBigPictureTask extends AsyncTask<Void, Integer, Integer> {

		Bitmap big_bitmap = null;

		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
        	big_bitmap = ImageUtils.getBitmapFromURL(url_big);
        	if (big_bitmap == null){
        		any_error = true;
        	}
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (!isCancelled() && !any_error){
				mImage.setImageBitmap(big_bitmap);
				
				mImage.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Log.v("WE","entra lalala");
						finish();		
					}
				});
			}			
		}
	}

}
