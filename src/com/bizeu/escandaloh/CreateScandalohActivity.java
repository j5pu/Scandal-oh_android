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
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.applidium.shutterbug.FetchableImageView;
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
	private ProgressDialog share_progress;
	private LinearLayout ll_audio;
	private LinearLayout ll_photo;
	private Button but_play;
	private LinearLayout ll_first_comment;
	private EditText edit_first_comment;

	private String selected_category;
	private String written_title;
	private Bitmap taken_photo;
	private Uri mImageUri;
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
	private String first_comment;

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
		ll_audio = (LinearLayout) findViewById(R.id.ll_create_audio);
		ll_photo = (LinearLayout) findViewById(R.id.ll_create_foto);
		but_play = (Button) findViewById(R.id.but_create_play_audio);
		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		ll_first_comment = (LinearLayout) findViewById(R.id.ll_create_firstcomment);
		edit_first_comment = (EditText) findViewById(R.id.edit_create_firstcomment);
		radio_category = (RadioGroup) findViewById(R.id.rg_create_category);
		txt_contador_titulo = (TextView) findViewById(R.id.txt_create_contadortitulo);

		// Mostramos la foto
		if (getIntent() != null) {
			Intent data = getIntent();
			photo_from = data.getExtras().getInt("photo_from");

			if (data != null) {
				photo_string = data.getExtras().getString("photoUri");

				// Si se ha tomado de la cámara
				if (photo_from == MainActivity.FROM_CAMERA) {
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

				// Subir audio
				else if (photo_from == MainActivity.FROM_AUDIO){
					// Ocultamos la foto y mostramos el audio
					ll_audio.setVisibility(View.VISIBLE);
					ll_photo.setVisibility(View.GONE);
					con_audio = true;
				}

				// Desde url
				else if (photo_from == MainActivity.FROM_URL){
					shared_url = data.getExtras().getString("shareUri");
					// Hacemos que el título no parezca un edittext
					edit_title.setKeyListener(null);
					edit_title.setBackgroundColor(getResources().getColor(R.color.gris_claro));
					txt_contador_titulo.setVisibility(View.INVISIBLE);			
					// Mostramos el edit del primer comentario
					ll_first_comment.setVisibility(View.VISIBLE);
					new GetPreviewScandalFromUrlTask().execute();
				}

				// Se ha compartido una imagen (galería)
				else if (photo_from == CoverActivity.FROM_SHARING_PICTURE) {
					// Mostramos la foto
					shareUri = Uri.parse(data.getExtras().getString("shareUri"));
					taken_photo = ImageUtils.uriToBitmap(shareUri, this);
					img_picture.setImageBitmap(taken_photo);
				}

				// Se ha compartido una url
				else if (photo_from == CoverActivity.FROM_SHARING_TEXT){
					shared_url = data.getExtras().getString("shareUri");
					// Hacemos que el título no parezca un edittext
					edit_title.setKeyListener(null);
					edit_title.setBackgroundColor(getResources().getColor(R.color.gris_claro));
					txt_contador_titulo.setVisibility(View.INVISIBLE);
					// Mostramos el edit del primer comentario
					ll_first_comment.setVisibility(View.VISIBLE);
					new GetPreviewScandalFromUrlTask().execute();
				}				

			}
		}

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
					// Mostramos el mensaje de subiendo escándalo
					Toast toast = Toast.makeText(mContext, getResources().getString(R.string.subiendo_scandaloh) , Toast.LENGTH_SHORT);
					toast.show();
					// Enviamos el escándalo en un hilo aparte
					new SendScandalTask().execute();
					// Cerramos la pantalla
					acti.finish();
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

		but_play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Audio.getInstance(mContext).startPlaying();
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
				first_comment = edit_first_comment.getText().toString();

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
					if (audio_file == null){
						any_error = true;
					}
					FileBody audioBody = new FileBody(audio_file);
					reqEntity.addPart("sound", audioBody);
				}

				StringBody categoryBody = new StringBody(selected_category);
				StringBody titleBody = new StringBody(written_title);
				StringBody codeCountryBody = new StringBody(MyApplication.code_selected_country);

				reqEntity.addPart("title", titleBody);
				reqEntity.addPart("category", categoryBody);
				reqEntity.addPart("country", codeCountryBody);

				// Si es a partir de una url añadimos el source(url), favicon, foto(url) y media_type=1
				if (photo_from == CoverActivity.FROM_SHARING_TEXT || photo_from == MainActivity.FROM_URL){
					StringBody imgBody = new StringBody(preview_img);
					StringBody faviconBody = new StringBody(preview_favicon);
					StringBody sourceBody = new StringBody(preview_source);
					StringBody mediaBody = new StringBody("1");
					reqEntity.addPart("img", imgBody);
					reqEntity.addPart("favicon", faviconBody);
					reqEntity.addPart("source", sourceBody);
					reqEntity.addPart("media_type", mediaBody);	

					// Si hay un primer comentario lo añadimos
					if (first_comment.length() > 0){
						StringBody firstCommentBody = new StringBody(first_comment);
						reqEntity.addPart("comment", firstCommentBody);
					}
				}

				// Si viene de la cámara o la galería añadimos la foto
				else if (photo_from == MainActivity.FROM_CAMERA | photo_from == MainActivity.FROM_GALLERY){
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
				return (response.getStatusLine().getStatusCode());
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			
			// Si hubo algún error mostramos un mensaje
			if (any_error) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();

			} else {
				// Si es codigo 2xx --> OK
				if (result >= 200 && result < 300) {
					Toast toast;
					toast = Toast.makeText(mContext,getResources().getString(R.string.escandalo_enviado_con_exito),
								Toast.LENGTH_LONG);
					toast.show();
				} 
				
				else {
					Toast toast;
					toast = Toast.makeText(mContext,getResources().getString(R.string.lo_sentimos_hubo),
								Toast.LENGTH_SHORT);
					toast.show();
				}
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