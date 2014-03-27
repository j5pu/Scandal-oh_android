package com.bizeu.escandaloh.users;

import java.io.File;
import java.io.IOException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class ProfileActivity extends SherlockActivity {

	public static final int AVATAR_FROM_CAMERA = 15;
	public static final int AVATAR_FROM_GALLERY = 14;
	public static final int CROP_PICTURE = 16;
	public static final String PICTURE_BYTES = "picture_bytes";
	
	private FetchableImageView picture;
	private TextView username;
	
	private Context mContext;
	private Uri mImageUri;
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		mContext = this;
		
		// Activamos el logo del menu para el menu lateral
		ActionBar actBar = getSupportActionBar();
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.logo_blanco);
		actBar.setDisplayShowTitleEnabled(false);
		
		picture = (FetchableImageView) findViewById(R.id.img_profile_picture);
		username = (TextView) findViewById(R.id.txt_profile_username);
		
		if (MyApplication.avatar != null){
			picture.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
		}
		else{
			picture.setImageResource(R.drawable.avatar_mas);
		}
		
		picture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Creamos un popup para elegir entre hacer foto con la cámara o cogerla de la galería
				final CharSequence[] items = {
						getResources()
								.getString(R.string.hacer_foto_con_camara),
						getResources().getString(
								R.string.seleccionar_foto_galeria) };
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				builder.setTitle(R.string.avatar);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						// Cámara
						if (items[item].equals(getResources().getString(
								R.string.hacer_foto_con_camara))) {

							// Si dispone de cámara iniciamos la cámara
							if (Utils.checkCameraHardware(mContext)) {
								Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
								File photo = null;
								photo = createFileTemporary("picture", ".png");
								if (photo != null) {
									mImageUri = Uri.fromFile(photo);
									takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
									startActivityForResult(takePictureIntent,AVATAR_FROM_CAMERA);
									photo.delete();
								}
							}
							// El dispositivo no dispone de cámara
							else {
								Toast toast = Toast
										.makeText(
												mContext,
												R.string.este_dispositivo_no_dispone_camara,
												Toast.LENGTH_LONG);
								toast.show();
							}
						}

						// Galería
						else if (items[item].equals(getResources().getString(
								R.string.seleccionar_foto_galeria))) {

							Intent i = new Intent(
									Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(i, AVATAR_FROM_GALLERY);
						}
					}
				});
				builder.show();		
			}
		});
		
		username.setText(MyApplication.user_name);
	}
	
	
	/**
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
	    	break;
		}
		return true;
	}
	
	

	/**
	 * onActivityResult
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Avatar desde la cámara
		if (requestCode == AVATAR_FROM_CAMERA) {
			if (resultCode == RESULT_OK) {
				if (mImageUri != null) {
					Intent i = new Intent(ProfileActivity.this, CropActivity.class);
					i.putExtra("photo_from", AVATAR_FROM_CAMERA);
					i.putExtra("photoUri", mImageUri.toString());
					startActivityForResult(i,CROP_PICTURE);
				}
			}
		}

		// Avatar desde la galería
		else if (requestCode == AVATAR_FROM_GALLERY) {
			if (data != null) {
				Uri selectedImageUri = data.getData();
				Intent i = new Intent(ProfileActivity.this, CropActivity.class);
				i.putExtra("photo_from", AVATAR_FROM_GALLERY);
				i.putExtra("photoUri", ImageUtils.getRealPathFromURI(mContext,selectedImageUri));
				startActivityForResult(i,CROP_PICTURE);
			}
		}

		// Crop de la foto
		else if (requestCode == CROP_PICTURE) {
			picture.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
		}
	}
	
	
	
	
	
	/**
	 * Crea un archivo temporal en una ruta con un formato específico
	 */
	private File createFileTemporary(String part, String ext) {
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath()
				+ "/ScándalOh/");
		if (!scandaloh_dir.exists()) {
			scandaloh_dir.mkdirs();
		}
		try {
			return File.createTempFile(part, ext, scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(this.getClass().toString(),
					"No se pudo crear el archivo temporal para la foto");
			// Mandamos la excepcion a Google Analytics
			EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			easyTracker.send(MapBuilder.createException(
					new StandardExceptionParser(mContext, null)
							.getDescription(Thread.currentThread().getName(), 
									e), // The exception.
					false).build());
			Toast toast = Toast.makeText(mContext,
					R.string.no_se_puede_acceder_camara, Toast.LENGTH_SHORT);
			toast.show();
		}

		return null;
	}

}
