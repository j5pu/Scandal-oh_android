package com.bizeu.escandaloh.users;

import java.io.File;
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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.util.ImageUtils;
import com.edmodo.cropper.CropImageView;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CropActivity extends Activity {

	
	private CropImageView img_crop_picture;
	private Button but_crop;
	private Bitmap cropped_picture;
	private ProgressDialog progress;
	private boolean any_error;
	private Context mContext;
	private int photo_from;
	private String photo_string;
	private Uri mImageUri;
	private Bitmap taken_photo;
	
	
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
			Intent data = getIntent();
			photo_from = data.getExtras().getInt("photo_from");
			
			if (data != null) {
				photo_string = data.getExtras().getString("photoUri");

				// Se ha tomado de la cámara
				if (photo_from == ProfileActivity.AVATAR_FROM_CAMERA) {
					mImageUri = Uri.parse(data.getExtras().getString("photoUri"));
					this.getContentResolver().notifyChange(mImageUri, null);
					taken_photo = ImageUtils.uriToBitmap(mImageUri, this);
					img_crop_picture.setImageBitmap(taken_photo);
				}

				// Se ha cogido de la galería
				else if (photo_from == ProfileActivity.AVATAR_FROM_GALLERY) {
					img_crop_picture.setImageBitmap(BitmapFactory.decodeFile(photo_string));
				}
			}

	        img_crop_picture.setAspectRatio(180, 180);   
	        img_crop_picture.setFixedAspectRatio(true);
	        
	        but_crop.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Obtenemos la foto después del crop
					cropped_picture = img_crop_picture.getCroppedImage();
					// Escalamos la foto a 180 px x 180 px
					cropped_picture = ImageUtils.scaleBitmap(cropped_picture, 180, 180);
					//cropped_picture = ImageUtils.getResizedBitmap(cropped_picture, 180, 180);
					//cropped_picture = ImageUtils.compressBitmapToJpg(cropped_picture, 80);
					// Actualizamos el avatar
					new UpdateAvatarTask(mContext, cropped_picture).execute();
				}
			});
		}		
	}
	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
	}
	
	
	
	
	/**
	 * onStop
	 */
	@Override
	public void onStop() {
		super.onStop();
		// Paramos Flurry
		FlurryAgent.onEndSession(mContext);
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
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/logged/";
			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPut put = new HttpPut(urlString);
				put.setHeader("Session-Token", MyApplication.session_token);
				MultipartEntity reqEntity = new MultipartEntity();
				
				File f = ImageUtils.bitmapToFileTemp(photo_avatar, mContext, "avatar.jpg");
				
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
