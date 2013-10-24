package com.bizeu.escandaloh;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.app.Activity;
import android.os.Bundle;


public class DetailPhotoActivity extends Activity {

	ImageViewTouch mImage;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
		
		/*
		if (getIntent() != null){
			photo = getIntent().getParcelableExtra("Image");
		}
		*/
		
		mImage = (ImageViewTouch) findViewById(R.id.img_photo_detail);
		mImage.setDisplayType( DisplayType.FIT_IF_BIGGER );
		mImage.setImageResource(R.drawable.pastor_aleman_1);
	}
}
