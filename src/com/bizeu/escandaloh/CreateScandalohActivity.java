package com.bizeu.escandaloh;

import java.io.File;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.RecordAudioDialog.OnMyDialogResult;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CreateScandalohActivity extends SherlockActivity {

	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	public static final int REQUESTCODE_RECORDING = 50;
	public static final int SHARING_NOT_LOGGED = 1;

	private FetchableImageView img_picture;
	private ImageView img_subir_escandalo;
	private EditText edit_title;
	private RadioGroup radio_category;
	private TextView txt_contador_titulo;
	private String selected_category;
	private String written_title;
	private Bitmap taken_photo;
	private Uri mImageUri;
	private ProgressDialog share_progress;
	private Context mContext;
	private Activity acti;
	private File audio_file;
	private boolean con_audio = false;
	private int photo_from;
	private boolean any_error;
	private String photo_string;
	private Uri shareUri;
	private String shared_url;
	private String preview_source;
	private String preview_img;
	private String preview_favicon;
	private String preview_title;

	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_escandalo);

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.lay_pantalla_create_escandalo));

		mContext = this;
		acti = this;

		// Quitamos el action bar
		getSupportActionBar().hide();

		img_picture = (FetchableImageView) findViewById(R.id.img_new_escandalo_photo);

		// Mostramos la foto
		if (getIntent() != null) {
			Intent data = getIntent();
			photo_from = data.getExtras().getInt("photo_from");

			if (data != null) {
				photo_string = data.getExtras().getString("photoUri");

				// Si se ha tomado de la cámara
				if (photo_from == MainActivity.SHOW_CAMERA) {
					mImageUri = Uri.parse(data.getExtras().getString("photoUri"));
					this.getContentResolver().notifyChange(mImageUri, null);
					taken_photo = ImageUtils.uriToBitmap(mImageUri, this);

					// Mostramos la foto
					img_picture.setImageBitmap(taken_photo);
				}

				// Se ha cogido de la galería
				else if (photo_from == MainActivity.FROM_GALLERY) {
					img_picture.setImageBitmap(BitmapFactory.decodeFile(photo_string));
					taken_photo = BitmapFactory.decodeFile(photo_string);
				}

				// Se ha compartido una imagen (galería)
				else if (photo_from == CoverActivity.FROM_SHARING_PICTURE) {
					// Mostramos la foto
					shareUri = Uri.parse(data.getExtras().getString("shareUri"));
					taken_photo = ImageUtils.uriToBitmap(shareUri, this);
					img_picture.setImageBitmap(taken_photo);
				}
				
				// Se ha compartido un texto (url)
				else if (photo_from == CoverActivity.FROM_SHARING_TEXT){
					shared_url = data.getExtras().getString("shareUri");
					new GetPreviewScandalFromUrlTask().execute();
					Log.v("WE","share uri: " + shared_url);
				}
			}
		}

		radio_category = (RadioGroup) findViewById(R.id.rg_create_category);
		txt_contador_titulo = (TextView) findViewById(R.id.txt_contador_caracteres_titulo);
		// Cada vez que se modifique el titulo actualizamos el contador: x/75
		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		edit_title.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
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

		img_subir_escandalo = (ImageView) findViewById(R.id.img_new_escandalo_subir);
		img_subir_escandalo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Si hay conexión
				if (Connectivity.isOnline(mContext)) {
					uploadScandaloh();
				} else {
					Toast toast;
					toast = Toast.makeText(
							mContext,
							getResources().getString(
									R.string.no_dispones_de_conexion),
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});
	}

	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(mContext).activityStart(this);
	}

	/**
	 * onStop. Liberamos los recursos del audio
	 */
	@Override
	protected void onStop() {
		super.onStop();
		Audio.getInstance(mContext).releaseResources();
	}


	
	
	/**
	 * Sube un escándalo
	 */
	private void uploadScandaloh() {

		// Inicializamos el alert dialog
		AlertDialog.Builder dialog_audio = new AlertDialog.Builder(mContext);
		dialog_audio.setMessage(getResources().getString(
				R.string.quieres_aniadir_un_audio));
		dialog_audio.setPositiveButton(R.string.si,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogo1, int id) {

						// Enviamos el evento a Google Analytics
						EasyTracker easyTracker = EasyTracker
								.getInstance(mContext);
						easyTracker.send(MapBuilder.createEvent("Acción UI", // Event
																				// category
																				// (required)
								"Botón clickeado", // Event action (required)
								"Acepta agregar audio", // Event label
								null) // Event value
								.build());

						// Mostramos el dialog del audio
						RecordAudioDialog record_audio = new RecordAudioDialog(
								mContext, Audio.getInstance(mContext));
						record_audio.setDialogResult(new OnMyDialogResult() {
							public void finish(String result) {
								if (result.equals("OK")) {
									con_audio = true;
								} else if (result.equals("CANCELED")) {
									con_audio = false;
								}
								// Mostramos un mensaje
								Toast toast = Toast.makeText(mContext, getResources().getString(R.string.subiendo_scandaloh) , Toast.LENGTH_SHORT);
								toast.show();
								// Enviamos el escándalo en un hilo aparte
								new SendScandalTask().execute();
								// Cerramos la pantalla
								acti.finish();
							}
						});
						record_audio.setCancelable(false);
						record_audio.show();
					}
				});
		dialog_audio.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogo1, int id) {

						// Enviamos el evento a Google Analytics
						EasyTracker easyTracker = EasyTracker
								.getInstance(mContext);
						easyTracker.send(MapBuilder.createEvent("Acción UI", // Event
																				// category
																				// (required)
								"Botón clickeado", // Event action (required)
								"Rechaza agregar audio", // Event label
								null) // Event value
								.build());

						// Enviamos el escandalo sin audio
						con_audio = false;
						Toast toast = Toast.makeText(mContext, getResources().getString(R.string.subiendo_scandaloh) , Toast.LENGTH_SHORT);
						toast.show();
						new SendScandalTask().execute();
						finish();
					}
				});

		// Mostramos el dialog del audio
		dialog_audio.show();

	}

	/**
	 * Sube un escandalo al servidor
	 * 
	 */
	private class SendScandalTask extends AsyncTask<Void, Integer, Integer> {

		File f;
		
		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/photo/";		
		
			HttpResponse response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Session-Token", MyApplication.session_token);

				// Obtenemos los datos y comprimimos en Multipart para su envío
				written_title = edit_title.getText().toString();
				if (written_title.equals("")) {
					written_title = getResources().getString(R.string.foto_sin_titulo);
				}
				
				int id_category_selected = radio_category.getCheckedRadioButtonId();
				switch (id_category_selected) {
					case R.id.rb_create_category_happy:
						selected_category = HAPPY_CATEGORY;
						break;
					case R.id.rb_create_category_angry:
						selected_category = ANGRY_CATEGORY;
						break;
				}
				
				MultipartEntity reqEntity = new MultipartEntity();				
				
				if (con_audio) {
					audio_file = new File(Audio.getInstance(mContext).getPath());
					FileBody audioBody = new FileBody(audio_file);
					reqEntity.addPart("sound", audioBody);
				}

				StringBody categoryBody = new StringBody(selected_category);
				StringBody titleBody = new StringBody(written_title);
				StringBody codeCountryBody = new StringBody(MyApplication.code_selected_country);
				
				reqEntity.addPart("title", titleBody);
				reqEntity.addPart("category", categoryBody);
				reqEntity.addPart("country", codeCountryBody);
				
				// Si se ha compartido un enlace añadimos el source(url), favicon, foto(url) y media_type=1
				if (photo_from == CoverActivity.FROM_SHARING_TEXT){
					StringBody imgBody = new StringBody(preview_img);
					StringBody faviconBody = new StringBody(preview_favicon);
					StringBody sourceBody = new StringBody(preview_source);
					StringBody mediaBody = new StringBody("1");
					reqEntity.addPart("img", imgBody);
					reqEntity.addPart("favicon", faviconBody);
					reqEntity.addPart("source", sourceBody);
					reqEntity.addPart("media_type", mediaBody);				
				}
				// Si no añadimos la foto
				else{
					f = ImageUtils.reduceSizeBitmap(taken_photo, 200, mContext);
					FileBody bin1 = new FileBody(f);
					reqEntity.addPart("img", bin1);
				}

				post.setEntity(reqEntity);
				response = client.execute(post);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);

				// Comprobamos si ha habido algún error
				if (response_str != null) {
					Log.i("WE", "response createEscandalo: " + response_str);
					// Obtenemos el json devuelto
					JSONObject respJSON = new JSONObject(response_str);

					if (respJSON.has("error")) {
						any_error = true;
					}
				}

			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				// Hubo algún error
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
			
			if (any_error) {
				return 666;
			} else {
				Log.v("WE","response final: " + response.getStatusLine().getStatusCode());
				return (response.getStatusLine().getStatusCode());
			}
		}	
	}
	
	
	/**
	 * Obtiene la preview de una url
	 * 
	 */
	private class GetPreviewScandalFromUrlTask extends AsyncTask<Void, Integer, Integer> {
		
		@Override
		protected void onPreExecute() {
			any_error = false;
			// Mostramos el ProgressDialog
			share_progress = new ProgressDialog(mContext);
			//share_progress.setTitle(getResources().getString(R.string.subiendo_scandaloh));
			share_progress.setMessage(getResources().getString(R.string.preparando_para_compartir));
			share_progress.setCancelable(false);
			share_progress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/photo/preview-url/";

			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Content-Type", "application/json");

				JSONObject dato = new JSONObject();
				dato.put("url", shared_url);

				// Formato UTF-8 (ñ,á,ä,...)
				StringEntity entity = new StringEntity(dato.toString(),HTTP.UTF_8);
				post.setEntity(entity);

				response = client.execute(post);
				resEntity = response.getEntity();
				String response_str = EntityUtils.toString(resEntity);
				
				// Parseamos la preview obtenida
				JSONObject respJson = new JSONObject(response_str);
				
				if (respJson.has("img")){
					preview_img = respJson.getString("img");
				}
				
				if (respJson.has("favicon")){
					preview_favicon = respJson.getString("favicon");
				}

				if (respJson.has("title")){
					preview_title = respJson.getString("title");
				}

				if (respJson.has("source")){
					preview_source = respJson.getString("source");		
				}
				
				if (respJson.has("status")){
					String status = respJson.getString("status");
					if (status.equals("error")){
						any_error = true;
					}
				}
				
				// Comprobamos si ha habido algún error
				if (response_str != null) {
					Log.i("WE", "response createEscandalo: " + response_str);
					// Obtenemos el json devuelto
					JSONObject respJSON = new JSONObject(response_str);

					if (respJSON.has("error")) {
						any_error = true;
					}
				}

				Log.i("WE", "preview: " + response_str);
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
			if (share_progress.isShowing()) {
				share_progress.dismiss();
			}
			
			// Si hubo algún error mostramos un mensaje
			if (any_error) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_dicho_contenido_no_se_puede_compartir),
						Toast.LENGTH_LONG);
				toast.show();
				// Quitamos el ProgressDialog
				if (share_progress.isShowing()) {
					share_progress.dismiss();
				}
				// Cerramos la pantalla
				finish();

			} else {
				// Si es codigo 2xx --> OK
				if (result >= 200 && result < 300) {
					// Mostramos la foto y el titulo
					img_picture.setImage(preview_img,R.drawable.cargando);
					edit_title.setText(preview_title);
				} else {
					Toast toast;
					toast = Toast
							.makeText(
									mContext,
									getResources()
											.getString(
													R.string.hubo_algun_error_enviando_comentario),
									Toast.LENGTH_LONG);
					toast.show();
				}
			}
		}
	}
	

}
