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
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
			
		if (getIntent() != null){
			route_img = getIntent().getExtras().getString("route_img");
			photo = Cache.getInstance(this).obtenImagenDeCache(route_img);

			mImage = (ImageViewTouch) findViewById(R.id.img_photo_detail);
			mImage.setDisplayType(DisplayType.FIT_IF_BIGGER);
			mImage.setImageBitmap(photo);
		}
	}
	
	
	
	

}
