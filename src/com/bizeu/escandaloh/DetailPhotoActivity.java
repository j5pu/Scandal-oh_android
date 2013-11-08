package com.bizeu.escandaloh;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import com.bizeu.escandaloh.model.Cache;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


public class DetailPhotoActivity extends Activity {

	private ImageViewTouch mImage;
	private Bitmap photo;
	private String route_img;
	
	private Audio audio_recorder;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
			
		if (getIntent() != null){
			
			// Obtenemos y mostarmos la foto
			byte[] bytes = getIntent().getByteArrayExtra("bytes");
			photo = ImageUtils.BytesToBitmap(bytes);
			mImage = (ImageViewTouch) findViewById(R.id.img_photo_detail);
			mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
			mImage.setImageBitmap(photo);
			
			// Obtenemos y reproducimos el audio (si tiene)
			String uri_audio = getIntent().getStringExtra("uri_audio");
			if (!uri_audio.equals("null")){
				Audio.getInstance().startPlaying("http://scandaloh.s3.amazonaws.com/" + uri_audio);
			}	
		}
	}
	
	
	
	
	
	/**
	 * onPause Detiene y elimina si había algún audio activo
	 */
	@Override
	protected void onPause(){
		super.onPause();
		Audio.getInstance().closeAudio();
	}
	
	
	

}
