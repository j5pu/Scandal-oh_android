package com.bizeu.escandaloh.users;

import com.actionbarsherlock.app.SherlockActivity;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

import android.app.Activity;
import android.os.Bundle;

public class ProfilePhotoActivity extends SherlockActivity {

	public static final String AVATAR = "avatar";
	
	private FetchableImageView photo;
	
	private String avatar;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.profile_photo);
		
		// Quitamos el action bar
		getSupportActionBar().hide();
		
		photo = (FetchableImageView) findViewById(R.id.img_profilephoto_photo);
		
		if (getIntent() != null){
			avatar = getIntent().getStringExtra(AVATAR);
			photo.setImage(MyApplication.DIRECCION_BUCKET + avatar, R.drawable.avatar_defecto);
		}
	}
}
