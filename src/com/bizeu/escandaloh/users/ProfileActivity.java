package com.bizeu.escandaloh.users;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
	private Button but_logout;
	
	private Context mContext;
	private Uri mImageUri;
	private ProgressDialog progress;
	private boolean any_error;
	private SharedPreferences prefs;
	
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
		but_logout = (Button) findViewById(R.id.but_profile_logout);
		
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
		
		but_logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert_logout = new AlertDialog.Builder(mContext);
				alert_logout.setTitle(R.string.cerrar_sesion_usuario);
				alert_logout.setMessage(R.string.seguro_que_quieres_cerrar);
				alert_logout.setPositiveButton(R.string.confirmar,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialogo1, int id) {

								// Avisamos al servidor
								new LogOutTask().execute();
							}
						});
				alert_logout.setNegativeButton(R.string.cancelar,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialogo1, int id) {
							}
						});
				alert_logout.show();
			}
		});
		
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		progress = new ProgressDialog(this);
		progress.setMessage(getResources().getString(R.string.cerrando_sesion));
		progress.setCancelable(false);
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
	
	
	
	
	
    
	/**
	 * Desloguea a un usuario
	 * 
	 */
	private class LogOutTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			any_error = false;
			// Mostramos el ProgressDialog
			progress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/logout/";

			HttpResponse response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Session-Token", MyApplication.session_token);
	
				response = client.execute(post);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);

				// Comprobamos si ha habido algún error
				if (response_str != null) {
					Log.i("WE", response_str);
					// Obtenemos el json devuelto
					JSONObject respJSON = new JSONObject(response_str);

					if (respJSON.has("error")) {
						any_error = true;
					}
				}

			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				any_error = true;

				// Mandamos la excepción a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) 
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), // The exception.
						false).build()); // False indicates a fatal exception
			}
			
			// Si hubo algún error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}		
		}
				
		@Override
		protected void onPostExecute(Integer result) {

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
				progress.dismiss();
			}
			
			// Si hubo algún error inesperado
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			else{	
				// Deslogueamos al usuario
				prefs.edit().putString(MyApplication.SESSION_TOKEN, null).commit();
				MyApplication.session_token = null;
	        	prefs.edit().putString(MyApplication.USER_NAME, getResources().getString(R.string.invitado)).commit();
	        	MyApplication.user_name = getResources().getString(R.string.invitado) ;
	        	prefs.edit().putString(MyApplication.AVATAR, null).commit();
	        	MyApplication.avatar = null;
				MyApplication.logged_user = false;
							
				// Reiniciamos los escándalos
				MyApplication.reset_scandals = true;
							
				// Cerramos la pantalla
				finish();
			}
		}
	}


}
