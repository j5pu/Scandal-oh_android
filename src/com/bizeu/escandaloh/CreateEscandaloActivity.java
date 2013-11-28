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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.RecordAudioDialog.OnMyDialogResult;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;

public class CreateEscandaloActivity extends SherlockActivity {

	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	public static final int REQUESTCODE_RECORDING = 50;

	private ImageView picture;
	private ImageView img_subir_escandalo;
	private TextView txt_subir_escandalo;
	private EditText edit_title;
	private RadioGroup radio_category;
	private TextView txt_contador_titulo;
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
	private int photo_from;

	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_escandalo);
		
		context = this;
		// Quitamos el action bar
		getSupportActionBar().hide();

		if (getIntent() != null) {
			Intent data = getIntent();

			if (data != null) {

				mImageUri = Uri.parse(data.getExtras().getString("photoUri"));
				this.getContentResolver().notifyChange(mImageUri, null);
				ContentResolver cr = this.getContentResolver();
				try {
					taken_photo = ImageUtils.uriToBitmap(mImageUri, this);
				} catch (Exception e) {
					Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
							.show();
					Log.d("WE", "Failed to load", e);
				}
				
				// Obtenemos de donde se ha tomado la foto
				photo_from = data.getExtras().getInt("photo_from");
			}
		}

		progress = new ProgressDialog(this);
		progress.setTitle("Subiendo esc�ndalo ...");
		progress.setMessage("Espere, por favor");
		progress.setCancelable(false);
		
		txt_contador_titulo = (TextView) findViewById(R.id.txt_contador_caracteres_titulo);

		// Cada vez que se modifique el titulo actualizamos el contador: x/75
		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		edit_title.addTextChangedListener(new TextWatcher() {          
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {                                                
            	txt_contador_titulo.setText(s.length() + "/75");
            }

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub		
			} 
		});
		
		radio_category = (RadioGroup) findViewById(R.id.rg_create_category);

		picture = (ImageView) findViewById(R.id.img_new_escandalo_photo);
		picture.setImageBitmap(taken_photo);	
		
		txt_subir_escandalo = (TextView) findViewById(R.id.txt_new_escandalo_subir);
		txt_subir_escandalo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String introducido = edit_title.getText().toString();
				if (introducido.equals("")) {
					Toast toast = Toast.makeText(getBaseContext(),
							"Debe introducir primero un t�tulo",
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					AlertDialog.Builder alert_audio = new AlertDialog.Builder(context);
					alert_audio.setMessage("�Deseas agregar audio?");
					alert_audio.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1,
										int id) {
									RecordAudioDialog record = new RecordAudioDialog(context, Audio.getInstance());
									record.setDialogResult(new OnMyDialogResult(){
									    public void finish(String result){
									       if (result.equals("OK")){
									    	   con_audio = true;									       
									       }	
									       else if (result.equals("CANCELED")){
									    	   con_audio = false;
									       }
									       new SendScandalo().execute();
									    }
									});
									record.setCancelable(false);
									record.show(); 								
								}
							});
					alert_audio.setNegativeButton("Cancelar",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1,
										int id) {
									// Enviamos el escandalo sin audio
									con_audio = false;
									new SendScandalo().execute();
								}
							});
					alert_audio.show();
				}
			}		
		});

		img_subir_escandalo = (ImageView) findViewById(R.id.img_new_escandalo_subir);
		img_subir_escandalo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String introducido = edit_title.getText().toString();
				if (introducido.equals("")) {
					Toast toast = Toast.makeText(getBaseContext(),
							"Debe introducir primero un t�tulo",
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					AlertDialog.Builder alert_audio = new AlertDialog.Builder(context);
					alert_audio.setMessage("�Deseas agregar audio?");
					alert_audio.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1,
										int id) {
									RecordAudioDialog record = new RecordAudioDialog(context, Audio.getInstance());
									record.setDialogResult(new OnMyDialogResult(){
									    public void finish(String result){
									       if (result.equals("OK")){
									    	   con_audio = true;									       
									       }	
									       else if (result.equals("CANCELED")){
									    	   con_audio = false;
									       }
									       new SendScandalo().execute();
									    }
									});
									record.setCancelable(false);
									record.show(); 								
								}
							});
					alert_audio.setNegativeButton("Cancelar",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1,
										int id) {
									// Enviamos el escandalo sin audio
									con_audio = false;
									new SendScandalo().execute();
								}
							});
					alert_audio.show();
				}
			}		
		});

	}


	
	
	
	/**
	 * onPause Detiene y elimina si hab�a alg�n audio activo
	 */
	@Override
	protected void onPause(){
		super.onPause();
		Audio.getInstance().closeAudio();
	}


	
	/**
	 * Sube un escandalo al servidor: foto, categor�a y t�tulo
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

			
			// Se ha tomado desde la camara
			if (photo_from == MainActivity.SHOW_CAMERA){
				photo_file = new File(mImageUri.getPath());
			}
			// Desde la galer�a
			else if (photo_from == MainActivity.FROM_GALLERY){
				photo_file = ImageUtils.bitmapToFile(taken_photo);		
			}

			HttpResponse response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);

				// Obtenemos los datos y comprimimos en Multipart para su env�o
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
				Toast toast = Toast.makeText(context, "Esc�ndalo subido con �xito", Toast.LENGTH_LONG);
				toast.show();
				Log.v("WE", "foto enviada");
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			} else {
				Toast toast = Toast.makeText(context, "Error subiendo el esc�ndalo", Toast.LENGTH_LONG);
				toast.show();
				Log.v("WE", "foto no enviada");
				finish();
			}
			
			// Si la foto se tom� de la galer�a borramos el archivo
			if (photo_from == MainActivity.FROM_GALLERY && photo_file.exists()){
				photo_file.delete();
			}
		}
	}


}
