package com.bizeu.escandaloh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.bizeu.escandaloh.RecordAudioDialog.OnMyDialogResult;
import com.bizeu.escandaloh.util.Audio;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateEscandaloActivity extends Activity {

	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	public static final int REQUESTCODE_RECORDING = 50;

	private ImageView picture;
	private Button but_accept;
	private Button but_cancel;
	private EditText edit_title;
	private RadioGroup radio_category;

	private String selected_category;
	private String written_title;
	private Bitmap taken_photo;
	private Uri photoUri;
	private Bitmap bitmap;
	private File photo_file;
	private Uri mImageUri;
	private Uri audioUri = null;
	private ProgressDialog progress;
	private Context context;
	private File audio_file;
	private boolean con_audio = false;
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_escandalo);
		
		context = this;

		if (getIntent() != null) {
			Intent data = getIntent();

			if (data != null) {
				mImageUri = Uri.parse(data.getExtras().getString("photoUri"));
				this.getContentResolver().notifyChange(mImageUri, null);
				ContentResolver cr = this.getContentResolver();
				try {
					taken_photo = readBitmap(mImageUri);
				} catch (Exception e) {
					Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
							.show();
					Log.d("WE", "Failed to load", e);
				}
			}
		}

		progress = new ProgressDialog(this);
		progress.setTitle("Subiendo escándalo ...");
		progress.setMessage("Espere, por favor");
		progress.setCancelable(false);

		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		radio_category = (RadioGroup) findViewById(R.id.rg_create_category);

		picture = (ImageView) findViewById(R.id.img_new_escandalo_photo);
		picture.setImageBitmap(taken_photo);

		but_accept = (Button) findViewById(R.id.but_new_escandalo_accept);
		but_accept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String introducido = edit_title.getText().toString();
				if (introducido.equals("")) {
					Toast toast = Toast.makeText(getBaseContext(),
							"Debe introducir primero un título",
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					AlertDialog.Builder alert_audio = new AlertDialog.Builder(context);
					alert_audio.setTitle("Añadir audio");
					alert_audio
							.setMessage("¿Desea añadir una grabación de audio?");
					alert_audio.setPositiveButton("Si",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1,
										int id) {
									RecordAudioDialog record = new RecordAudioDialog(context, Audio.getInstance());
									record.setDialogResult(new OnMyDialogResult(){
									    public void finish(String result){
									       if (result.equals("OK")){
									    	   con_audio = true;
									    	   new SendScandalo().execute();
									       }						       
									    }
									});
									record.setCancelable(false);
									record.show(); 								
								}
							});
					alert_audio.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1,
										int id) {
									// Enviamos el escandalo sin audio
									new SendScandalo().execute();
								}
							});
					alert_audio.show();
				}
			}		
		});

		but_cancel = (Button) findViewById(R.id.but_new_escandalo_cancel);
		but_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	
	
	
	/**
	 * onPause Detiene y elimina si había algún audio activo
	 */
	@Override
	protected void onPause(){
		super.onPause();
		Audio.getInstance().closeAudio();
	}


	
	/**
	 * Sube un escandalo al servidor: foto, categoría y título
	 * 
	 * @author Alejandro
	 * 
	 */
	private class SendScandalo extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			// Mostramos el ProgressDialog
			progress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "api/v1/photo/";
			photo_file = new File(mImageUri.getPath());
			

			HttpResponse response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);

				// Obtenemos los datos y comprimimos en Multipart para su envío
				written_title = edit_title.getText().toString();
				int id_category_selected = radio_category
						.getCheckedRadioButtonId();
				switch (id_category_selected) {
				case R.id.rb_create_category_happy:
					selected_category = HAPPY_CATEGORY;
					break;
				case R.id.rb_create_category_angry:
					selected_category = ANGRY_CATEGORY;
					break;
				}

				/*
				// Obtenemos el user_uri del usuario
				SharedPreferences prefs = getBaseContext()
						.getSharedPreferences("com.bizeu.escandaloh",
								Context.MODE_PRIVATE);

				String user_uri = prefs.getString("user_uri", null);
				*/

				MultipartEntity reqEntity = new MultipartEntity();
				
				if (con_audio){
					audio_file = new File(Audio.getInstance().getPath());	
					FileBody audioBody = new FileBody(audio_file);
					reqEntity.addPart("sound", audioBody);
				}				
				
				StringBody categoryBody = new StringBody(selected_category);
				FileBody bin1 = new FileBody(photo_file);
				StringBody titleBody = new StringBody(written_title);
				StringBody userBody = new StringBody(MyApplication.resource_uri);
				StringBody codeCountryBody = new StringBody(MyApplication.code_selected_country);
				
				reqEntity.addPart("img", bin1);
				reqEntity.addPart("title", titleBody);
				reqEntity.addPart("category", categoryBody);
				reqEntity.addPart("user", userBody);
				reqEntity.addPart("country", codeCountryBody);

				post.setEntity(reqEntity);
				response = client.execute(post);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);
				Log.i("WE",response_str);
				
			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
			}

			return (response.getStatusLine().getStatusCode());
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
				progress.dismiss();
			}

			// Si es codigo 2xx --> OK
			if (result >= 200 && result < 300) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("title", written_title);
				resultIntent.putExtra("category", selected_category);
				Toast toast = Toast.makeText(context, "Escándalo subido con éxito", Toast.LENGTH_LONG);
				toast.show();
				Log.v("WE", "foto enviada");
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			} else {
				Toast toast = Toast.makeText(context, "Error subiendo el escándalo", Toast.LENGTH_LONG);
				toast.show();
				Log.v("WE", "foto no enviada");
				finish();
			}
		}
	}

	// Read bitmap
	private Bitmap readBitmap(Uri selectedImage) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = this.getContentResolver().openAssetFileDescriptor(
					selectedImage, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bm = BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}

}
