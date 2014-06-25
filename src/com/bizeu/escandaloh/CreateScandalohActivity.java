package com.bizeu.escandaloh;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CreateScandalohActivity extends SherlockActivity {

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    VARIABLES                                                      |
	// -----------------------------------------------------------------------------------------------------
	
	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	public static final int REQUESTCODE_RECORDING = 50;
	public static final int SHARING_NOT_LOGGED = 1;

	private FetchableImageView img_picture;
	private FetchableImageView img_avatar;
	private FetchableImageView img_avatar_first_comment;
	private ImageView img_tipo_usuario;
	private ImageView img_tipo_usuario_first_comment;
	private TextView txt_user_name;
	private TextView txt_user_name_first_comment;
	private EditText edit_title;
	private RadioGroup radio_category;
	private ProgressDialog share_progress;
	private TextView txt_date;
	private ImageView img_audio;
	private EditText edit_first_comment;
	private ImageView img_aceptar;

	private String selected_category;
	private String written_title;
	private Bitmap taken_bitmap;
	private Uri mImageUri;
	private Context mContext;
	private Activity acti;
	private File audio_file;
	private boolean con_audio = false;
	private int photo_from;
	private boolean any_error;
	private String photo_string;
	private String shared_url;
	private String preview_source;
	private String preview_img;
	private String preview_favicon;
	private String preview_title;
	private String first_comment;
	private String url_without_img;

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS  ACTIVITY                                              |
	// -----------------------------------------------------------------------------------------------------
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_scandal);

		mContext = this;
		acti = this;

		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_create_scandal, null);
		actBar.setCustomView(view);
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.s_mezcla);
		actBar.setDisplayShowTitleEnabled(true);

		img_picture = (FetchableImageView) findViewById(R.id.img_new_escandalo_photo);
		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		edit_first_comment = (EditText) findViewById(R.id.edit_create_firstcomment);
		radio_category = (RadioGroup) findViewById(R.id.rg_create_category);
		img_aceptar = (ImageView) findViewById(R.id.img_create_aceptar);
		img_avatar = (FetchableImageView) findViewById(R.id.img_create_emoticono);
		img_avatar_first_comment = (FetchableImageView) findViewById(R.id.img_create_avatar_last_comment);
		txt_user_name = (TextView) findViewById(R.id.txt_create_name_user);
		txt_user_name_first_comment = (TextView) findViewById(R.id.txt_create_lastcomment_username);
		img_tipo_usuario = (ImageView) findViewById(R.id.img_create_tipo_usuario);
		img_tipo_usuario_first_comment = (ImageView) findViewById(R.id.img_create_lastcomment_socialnetwork);
		txt_date = (TextView) findViewById(R.id.txt_create_lastcomment_date);
		img_audio = (ImageView) findViewById(R.id.img_create_audio);
		
		// Mostramos los avatares del usuario
		img_avatar.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
		img_avatar_first_comment.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
		
		// Mostramos el nombre de usuario
		txt_user_name.setText(MyApplication.user_name);
		txt_user_name_first_comment.setText(MyApplication.user_name);
		
		// Mostramos el tipo de usuario
        if (MyApplication.social_network == 0){
        	img_tipo_usuario.setImageResource(R.drawable.s_circular_blanca);
        	img_tipo_usuario_first_comment.setImageResource(R.drawable.s_circular_gris);
        }
        else if (MyApplication.social_network == 1){
        	img_tipo_usuario.setImageResource(R.drawable.f_circular_blanca);
        	img_tipo_usuario_first_comment.setImageResource(R.drawable.f_circular_gris);
        }
        
        // Mostramos la fecha actual
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDateandTime = sdf.format(new Date());
        txt_date.setText(currentDateandTime);
		
		// Mostramos la foto
		if (getIntent() != null) {
			Intent data = getIntent();
			photo_from = data.getExtras().getInt("photo_from");

			if (data != null) {
				photo_string = data.getExtras().getString("photoUri");

				// Si se ha tomado de la cámara o compartido foto (Uri)
				if (photo_from == MainActivity.FROM_CAMERA || photo_from == CoverActivity.FROM_SHARING_PICTURE) {
					mImageUri = Uri.parse(photo_string);
					this.getContentResolver().notifyChange(mImageUri, null);
					taken_bitmap = ImageUtils.decodeSampledBitmapFromUri(mContext, mImageUri, Utils.dpToPx(300, mContext), Utils.dpToPx(120, mContext));
					img_picture.setImageBitmap(taken_bitmap);
				}

				// Se ha cogido de la galería (Path)
				else if (photo_from == MainActivity.FROM_GALLERY) {
					taken_bitmap = ImageUtils.decodeSampledBitmapFromString(photo_string, 300, 120);
					img_picture.setImageBitmap(taken_bitmap);
				}

				// Subir audio
				else if (photo_from == MainActivity.FROM_AUDIO){
					// Mostramos el boton del audio
					img_audio.setVisibility(View.VISIBLE);
					con_audio = true;
				}

				// Compartido url
				else if (photo_from == MainActivity.FROM_URL || photo_from == CoverActivity.FROM_SHARING_TEXT){
					shared_url = data.getExtras().getString("photoUri");
					// Hacemos que el título no parezca un edittext
					edit_title.setKeyListener(null);		
					new GetPreviewScandalFromUrlTask().execute();
				}
			}
		}

		img_aceptar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Si hay conexión
				if (Connectivity.isOnline(mContext)) {
					// Mostramos el mensaje de subiendo escándalo
					Toast toast = Toast.makeText(mContext, getResources().getString(R.string.subiendo_scandaloh) , Toast.LENGTH_SHORT);
					toast.show();
					// Enviamos el escándalo en un hilo a parte
					new SendScandalTask().execute();
					// Cerramos la pantalla
					acti.finish();
				} else {
					Toast toast;
					toast = Toast.makeText(mContext,getResources().getString(R.string.no_dispones_de_conexion),Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		
		img_audio.setOnClickListener(new View.OnClickListener() {

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
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
	}



	
	/**
	 * onStop
	 */
	@Override
	protected void onStop() {
		super.onStop();
		// Paramos Flurry
		FlurryAgent.onEndSession(mContext);
		Audio.getInstance(mContext).releaseResources();
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



	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                CLASES                                                             |
	// -----------------------------------------------------------------------------------------------------
	
	
	
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

				// Si lleva audio se lo añadimos
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
				
				// Si hay un primer comentario lo añadimos
				if (first_comment.length() > 0){
					StringBody firstCommentBody = new StringBody(first_comment);
					reqEntity.addPart("comment", firstCommentBody);
				}

				// A PARTIR DE UNA URL: añadimos el source(url), favicon, foto(url) y media_type=1
				if (photo_from == CoverActivity.FROM_SHARING_TEXT || photo_from == MainActivity.FROM_URL){
					StringBody imgBody = new StringBody(preview_img);
					StringBody faviconBody = new StringBody(preview_favicon);
					StringBody sourceBody = new StringBody(preview_source);
					StringBody mediaBody = new StringBody("1");
					StringBody urlWithoutImgBody = new StringBody(url_without_img);
					reqEntity.addPart("img", imgBody);
					reqEntity.addPart("favicon", faviconBody);
					reqEntity.addPart("source", sourceBody);
					reqEntity.addPart("media_type", mediaBody);	


				}

				// CÁMARA O GALERIA 
				else if (photo_from == MainActivity.FROM_CAMERA | photo_from == MainActivity.FROM_GALLERY){
					f = ImageUtils.reduceBitmapSize(taken_bitmap, 200, mContext);
					FileBody bin1 = new FileBody(f);
					reqEntity.addPart("img", bin1);
				}
				
				// FOTO COMPARTIDA
				else if (photo_from == CoverActivity.FROM_SHARING_PICTURE){
					f = ImageUtils.reduceBitmapSize(mImageUri, 200, mContext);
					FileBody bin1 = new FileBody(f);
					reqEntity.addPart("img", bin1);
				}
				
				
				post.setEntity(reqEntity);
				response = client.execute(post);
				resEntity = response.getEntity();				
				final String response_str = EntityUtils.toString(resEntity);

				// Comprobamos si ha habido algún error
				if (response_str != null) {
					Log.i(mContext.getClass().getSimpleName(), "response createEscandalo: " + response_str);
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
					// Mostramos un mensaje de éxito
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
				
				if (respJson.has("url_without_img")){
					url_without_img = respJson.getString("url_without_img");		
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
					toast = Toast.makeText(mContext,getResources().getString(R.string.hubo_algun_error_enviando_comentario),Toast.LENGTH_LONG);
					toast.show();
				}
			}
		}
	}


}