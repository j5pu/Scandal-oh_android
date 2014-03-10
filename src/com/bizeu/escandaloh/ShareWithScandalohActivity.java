package com.bizeu.escandaloh;

import java.io.FileNotFoundException;

import com.actionbarsherlock.app.SherlockActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ShareWithScandalohActivity extends SherlockActivity {

	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.share_with_scandaloh);
		
		if (getIntent().getAction().equals(Intent.ACTION_SEND)){
			Intent i = getIntent();
			// Es una foto
			if (i.getType().equals("image/*")){
				 Uri imageUri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
				 try { 
					 Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri)); 
					 ImageView img = (ImageView) findViewById(R.id.img_share);
					 img.setImageBitmap(bitmap);
					 // targetImage.setImageBitmap(bitmap); 
				  } catch (FileNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); } 
				 }
				 Log.v("WE","Uri de imagen: " + imageUri);
				
			}
			// Es texto
			else if (i.getType().equals("text/plain")){
				Log.v("WE","Es EXTRA_TEXT");
			}
		}
	}
}
