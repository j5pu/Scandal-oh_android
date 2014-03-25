package com.bizeu.escandaloh.users;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.util.ImageUtils;
import com.edmodo.cropper.CropImageView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CropActivity extends Activity {

    private static final int DEFAULT_ASPECT_RATIO_VALUES = 200;
    private static final int ROTATE_NINETY_DEGREES = 200;
	
	private CropImageView img_crop_picture;
	private Button but_crop;
	
	private Bitmap picture_to_crop;
	private Bitmap cropped_picture;
	private ProgressDialog progress;
	private boolean any_error;
	private Context mContext;
	
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.crop);
		
		mContext = this;
		
		img_crop_picture = (CropImageView) findViewById(R.id.img_crop_picture);
		but_crop = (Button) findViewById(R.id.but_crop_recortar);
		
		if (getIntent().getExtras() != null){
			Intent i = getIntent();
			Bundle data = i.getExtras();
			picture_to_crop = ImageUtils.bytesToBitmap(data.getByteArray(ProfileActivity.PICTURE_BYTES));
			img_crop_picture.setImageBitmap(picture_to_crop);
			
	        // Sets initial aspect ratio to 10/10, for demonstration purposes
	        img_crop_picture.setAspectRatio(400, 400);   
	        img_crop_picture.setFixedAspectRatio(true);
	        
	        but_crop.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					cropped_picture = img_crop_picture.getCroppedImage();
					new UpdateAvatarTask(mContext, cropped_picture).execute();
				}
			});

		}
	}
	
	
	
	/**
	 * Envía un avatar nuevo al servidor
	 * 
	 */
	private class UpdateAvatarTask extends AsyncTask<Void, Integer, Integer> {

		private Context mContext;
		private Bitmap photo_avatar;
		private String url_avatar;

		public UpdateAvatarTask(Context context, Bitmap avatar) {
			photo_avatar = avatar;
			mContext = context;
			progress = new ProgressDialog(mContext);
			progress.setTitle(R.string.actualizando_avatar);
			progress.setMessage(getResources().getString(
					R.string.espera_por_favor));
			progress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			// Mostramos el ProgressDialog
			progress.show();
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/logged/"
					+ MyApplication.resource_uri;
			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPut put = new HttpPut(urlString);
				put.setHeader("Session-Token", MyApplication.session_token);
				MultipartEntity reqEntity = new MultipartEntity();
				
				File f = ImageUtils.bitmapToFileTemp(photo_avatar, mContext, "avatar.jpg");
				
				/*
				// Creamos un file a partir del bitmap
				File f = new File(mContext.getCacheDir(), "avatar.jpg");
				f.createNewFile();

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				photo_avatar.compress(CompressFormat.JPEG, 100, bos);
				byte[] bitmapdata = bos.toByteArray();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(bitmapdata);
				
				*/
				
				FileBody fb_avatar = new FileBody(f);
				reqEntity.addPart("avatar", fb_avatar);
				put.setEntity(reqEntity);
				response = client.execute(put);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);
				
				Log.i("WE",response_str);
				
				JSONObject respJSON = new JSONObject(response_str);
				url_avatar = respJSON.getString("avatar");
			}

			catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				any_error = true; // Indicamos que hubo algún error

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) 
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), // The exception.
						false).build());
			}

			if (any_error) {
				return 666;
			} else {
				// Devolvemos el resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
				progress.dismiss();
			}

			// Si hubo algún error mostramos un mensaje
			if (any_error) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				// Si es codigo 2xx --> OK
				if (result >= 200 && result < 300) {
		        	// Guardamos su avatar
					SharedPreferences prefs = getBaseContext().getSharedPreferences(
		        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
		        	prefs.edit().putString(MyApplication.AVATAR, url_avatar).commit();
		        	MyApplication.avatar = url_avatar;
		        	setResult(Activity.RESULT_OK);
		        	finish();
				} else {
					Toast toast;
					toast = Toast
							.makeText(mContext,getResources().getString(
													R.string.hubo_algun_problema_actualizando_avatar),
									Toast.LENGTH_LONG);
					toast.show();
					// Quitamos el ProgressDialog
					if (progress.isShowing()) {
						progress.dismiss();
					}
				}
			}
		}
	}
	
}
