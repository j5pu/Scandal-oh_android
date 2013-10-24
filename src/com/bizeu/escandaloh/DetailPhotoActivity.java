package com.bizeu.escandaloh;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class DetailPhotoActivity extends Activity {

	private ImageView img_photo;
	
	private Bitmap photo;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
		
		if (getIntent() != null){
			//photo = getIntent().getParcelableExtra("Image");
		}
		
		//img_photo = (ImageView) findViewById(R.id.img_photo_detail);
		//img_photo.setImageBitmap(photo);
		
		

	}
}
