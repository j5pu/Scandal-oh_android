package com.bizeu.escandaloh.users;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.ScandalActivity;
import com.bizeu.escandaloh.adapters.HistoryAdapter;
import com.bizeu.escandaloh.model.History;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.ImageViewRounded;
import com.bizeu.escandaloh.util.Utils;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R.id;

public class ProfileActivity extends SherlockActivity {

	public static final int AVATAR_FROM_CAMERA = 15;
	public static final int AVATAR_FROM_GALLERY = 14;
	public static final int CROP_PICTURE = 16;
	public static final int PROFILE_SETTINGS = 17;
	public static final String USER_ID = "user_id";
	public static final String PICTURE_BYTES = "picture_bytes";
	public static final String LOGGED = "logged";
	public static final String FOLLOW = "follow";
	public static final String UNFOLLOW = "unfollow";
	
	private ImageViewRounded img_picture;
	private TextView txt_username;
	private Button but_follow_unfollow;
	private TextView txt_followers;
	private TextView txt_following;
	private ImageView img_settings;
	private ListView list_history;
	private LinearLayout ll_list_historys;
	private LinearLayout ll_loading;
	private LinearLayout ll_num_seguidores;
	private LinearLayout ll_num_seguidos;
	private LinearLayout ll_userinfo_data;
	private LinearLayout ll_userinfo_loading;
	
	private boolean is_me = false; // Nos indica si soy el mismo que el del perfil
	private Context mContext;
	private String user_id;
	private Uri mImageUri;
	private boolean any_error_user_info;
	private boolean any_error_follow;
	private boolean any_error_history;
	private ArrayList<History> array_history = new ArrayList<History>();
	private HistoryAdapter historyAdapter;
	private boolean there_are_more_historys = true;
	private String meta_next_history = null;
	private GetHistoryTask getHistoryAsync;
	
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
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_profile, null);
		actBar.setCustomView(view);
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.logo_blanco);
		actBar.setDisplayShowTitleEnabled(true);
		actBar.setTitle(getResources().getString(R.string.perfil));
	
		img_picture = (ImageViewRounded) findViewById(R.id.img_profile_picture);
		txt_username = (TextView) findViewById(R.id.txt_profile_username);
		but_follow_unfollow = (Button) findViewById(R.id.but_profile_follow_unfollow);
		txt_followers = (TextView) findViewById(R.id.txt_profile_num_seguidores);
		txt_following = (TextView) findViewById(R.id.txt_profile_num_siguiendo);
		img_settings = (ImageView) findViewById(R.id.img_profile_settings);
		list_history = (ListView) findViewById(R.id.list_profile_history);
		ll_list_historys = (LinearLayout) findViewById(R.id.ll_profile_listhistory);
		ll_loading = (LinearLayout) findViewById(R.id.ll_profile_loading);
		ll_num_seguidores = (LinearLayout) findViewById(R.id.ll_profile_numseguidores);
		ll_num_seguidos = (LinearLayout) findViewById(R.id.ll_profile_numsiguiendo);
		ll_userinfo_data = (LinearLayout) findViewById(R.id.ll_profile_userinfo_data);
		ll_userinfo_loading = (LinearLayout) findViewById(R.id.ll_profile_userinfo_loading);
		
		historyAdapter = new HistoryAdapter(mContext, R.layout.history, array_history);
		list_history.setAdapter(historyAdapter);
		
		img_settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ProfileActivity.this, ProfileSettingsActivity.class);
				startActivityForResult(i, PROFILE_SETTINGS);	
			}
		});
		
		// Mostramos la información del usuario
		if (getIntent() != null){
			user_id = getIntent().getStringExtra(USER_ID);
		}
		
		// Cambiar avatar
		img_picture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (is_me){
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
			}
		});	
		
		// Al seleccionar una history mostramos el escándalo al que referencia
		list_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  @Override
			  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					
				  History historyAux = ((History) list_history.getItemAtPosition(position));
				  Intent i = new Intent(ProfileActivity.this, ScandalActivity.class);
				  i.putExtra(ScandalActivity.PHOTO_ID, historyAux.getId());	
				  startActivity(i);
			  }
		});
		
		// Obtener siguientes historys
		list_history.setOnScrollListener(new OnScrollListener() {
					
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			           
				if ((firstVisibleItem + visibleItemCount == historyAdapter.getCount() -1) && there_are_more_historys) {
					
					if (Connectivity.isOnline(mContext)){		         		
						getHistoryAsync = new GetHistoryTask();
						getHistoryAsync.execute();
					}
					else{
						Toast toast = Toast.makeText(mContext, R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
						toast.show();
					}	     			    		
				}   	
			}
			        
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
			}
		});
		
		// Mostramos la lista de seguidores
		ll_num_seguidores.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String followers_c = txt_followers.getText().toString().substring(0,1);
				if (!followers_c.equals("0")){
					Intent i = new Intent(ProfileActivity.this, FollowersActivity.class);
					i.putExtra(FollowersActivity.USER_ID, user_id);
					i.putExtra(FollowersActivity.FOLLOWERS, true);
					startActivity(i);
				}
			}
		});
		
		// Mostramos la lista de seguidos
		ll_num_seguidos.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String following_c = txt_following.getText().toString().substring(0,1);
				if (!following_c.equals("0")){
					Intent i = new Intent(ProfileActivity.this, FollowersActivity.class);
					i.putExtra(FollowersActivity.USER_ID, user_id);
					i.putExtra(FollowersActivity.FOLLOWERS, false);
					startActivity(i);	
				}
			}
		});
		
		// Obtenemos el historial (Mi Actividad)
		getHistoryAsync = new GetHistoryTask();
		getHistoryAsync.execute();		
	}
	
	
	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart(){
		super.onStart();
		
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
		
		if (user_id != null){
			new ShowUserInformation().execute();
		}
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
	 * onStop
	 */
	@Override
	public void onStop() {
		super.onStop();
		// Paramos Flurry
		FlurryAgent.onEndSession(mContext);
	}
	
	
	/**
	 * onDestroy
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		cancelGetHistorys();
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
			img_picture.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
		}
		
		// Settings del usuario
		else if (requestCode == PROFILE_SETTINGS){
			// Si ha cerrado sesión cerramos esta pantalla
			if (!MyApplication.logged_user){
				finish();
			}
		}
	}
	
	
	
	
	
	/**
	 * Crea un archivo temporal en una ruta con un formato específico
	 */
	private File createFileTemporary(String part, String ext) {
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath()
				+ "/ScandalOh/");
		if (!scandaloh_dir.exists()) {
			scandaloh_dir.mkdirs();
		}
		try {
			return File.createTempFile(part, ext, scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(this.getClass().toString(),
					"No se pudo crear el archivo temporal para la foto");
			Toast toast = Toast.makeText(mContext,
					R.string.no_se_puede_acceder_camara, Toast.LENGTH_SHORT);
			toast.show();
		}

		return null;
	}
	

	
	/**
	 * Muestra la información del usuario
	 * 
	 */
	private class ShowUserInformation extends AsyncTask<Void, Integer, Integer> {

		String username;
		String avatar;
		String followers_count;
		String follows_count;
		boolean is_following;

		@Override
		protected void onPreExecute() {
			any_error_user_info= false;	
			txt_username.setText("");
		}

		
		@Override
		protected Integer doInBackground(Void... params) {

			String url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/" + user_id + "/profile/" ;			

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				get.setHeader("content-type", "application/json");
				
				if (MyApplication.logged_user){
					get.setHeader("session-token", MyApplication.session_token);
				}

				// Hacemos la petición al servidor
				response = httpClient.execute(get);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				// Parseamos el json devuelto
				JSONObject respJson = new JSONObject(respStr);
				
				username = respJson.getString("username");
				avatar = respJson.getString("avatar");
				followers_count = respJson.getString("followers_count");
				follows_count = respJson.getString("follows_count");
				is_following = respJson.getBoolean("is_following");
				is_me = respJson.getBoolean("is_me");
				
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error obteniendo información del usuario", ex);
				// Hubo algún error inesperado
				any_error_user_info = true;
			}

			// Si hubo algún error devolvemos 666
			if (any_error_user_info) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			showUserInfo();
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hubo error: mostramos el avatar, nombre de usuario, seguidores y si le está siguiendo
			else{
				if (is_me){
					img_picture.setImage(MyApplication.DIRECCION_BUCKET + avatar, R.drawable.avatar_mas);
				}
				else{
					img_picture.setImage(MyApplication.DIRECCION_BUCKET + avatar, R.drawable.avatar_defecto);
				}
				txt_username.setText(username);
				if (followers_count.equals("1")){
					txt_followers.setText(followers_count + " " + getResources().getString(R.string.seguidor));
				}
				else{
					txt_followers.setText(followers_count + " " + getResources().getString(R.string.seguidores));

				}
				txt_following.setText(follows_count + " " + getResources().getString(R.string.siguiendo));
				
				if (is_following){
					but_follow_unfollow.setText(getResources().getString(R.string.dejar_de_seguir));
					but_follow_unfollow.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							new FollowUnfollowUserTask(UNFOLLOW).execute();	
						}
					});
				}
				
				else{
					but_follow_unfollow.setText(getResources().getString(R.string.seguir));
					but_follow_unfollow.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							new FollowUnfollowUserTask(FOLLOW).execute();		
						}
					});
				}
				
				// Si soy el del perfil o no soy usuario logueado ocultamos el botón de seguir
				if (is_me || !MyApplication.logged_user){
					but_follow_unfollow.setVisibility(View.GONE);
				}
				
				// Si no soy el usuario del perfil, ocultamos todas las opciones de feedback y ajustes de cuenta
				if (!is_me && !username.equals(MyApplication.user_name)){
					img_settings.setVisibility(View.GONE);
				}
			}
		}
	}
	
	
	/**
	 * Sigue/Deja de seguir a un usuario
	 * 
	 */
	private class FollowUnfollowUserTask extends AsyncTask<Void, Integer, Integer> {

		String status;
		private String follow_unfollow;

		public FollowUnfollowUserTask(String follow_unfollow){
			this.follow_unfollow = follow_unfollow;
		}
		
		@Override
		protected void onPreExecute() {
			any_error_follow= false;
		}

		
		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;

			if (follow_unfollow.equals(FOLLOW)){
				url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/follow/" ;		
			}
			
			else{
				url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/unfollow/" ;
			}	
		
			HttpResponse response = null;

			try {
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpPost post = new HttpPost(url);
	            post.setHeader("Content-Type", "application/json");
	            post.setHeader("Session-Token", MyApplication.session_token);
	            
	             JSONObject dato = new JSONObject();	                        
	             dato.put("user_id", user_id);
	             
	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

				// Hacemos la petición al servidor
				response = httpClient.execute(post);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				// Parseamos el json devuelto
				JSONObject respJson = new JSONObject(respStr);
				
				status = respJson.getString("status");
				
				if (status.equals("error")){
					any_error_follow = true;
				}
							
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error siguiendo/dejando de seguir a un usuario", ex);
				// Hubo algún error inesperado
				any_error_follow = true;
			}

			// Si hubo algún error devolvemos 666
			if (any_error_follow) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hubo error: mostramos el avatar, nombre de usuario, seguidores y si le está siguiendo
			else{
				new ShowUserInformation().execute();
			}
		}
	}
	
	
	
	/**
	 * Obtiene y muestra la actividad de un usuario
	 * 
	 */
	private class GetHistoryTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			any_error_history = false;		
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			String url = null;

			// No hay historys: obtenemos los primeros
			if (array_history.size() == 0){			
				url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/" + user_id + "/activity/" ;
			}
			
			// Obtenemos los siguientes historys
			else{
				// Fin del carrusel: meta nulo indica que no hay más historys
				if (meta_next_history.equals("null")){
					there_are_more_historys = false;
					return 5;
				}
				url = MyApplication.SERVER_ADDRESS + meta_next_history;
			}

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getHistorys = new HttpGet(url);
				getHistorys.setHeader("content-type", "application/json");
				getHistorys.setHeader("Session-Token", MyApplication.session_token);
				
				// Hacemos la petición al servidor
				response = httpClient.execute(getHistorys);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", "History: " + respStr);
				
				// Parseamos los historys devueltos
				JSONObject respJson = new JSONObject(respStr);

				// Obtenemos el meta
				JSONObject respMetaJson = respJson.getJSONObject("meta");
				meta_next_history = respMetaJson.getString("next");

				JSONArray historysObject = respJson.getJSONArray("objects");
				
				// Obtenemos los datos de los historys
				for (int i = 0; i < historysObject.length(); i++) {

					JSONObject historyObject = historysObject.getJSONObject(i);

					final String action = historyObject.getString("action");
					final String date = historyObject.getString("date");
					final String photo_id = historyObject.getString("photo_id");
					final String photo_img = historyObject.getString("photo_img");
					final String text = new String(historyObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {							
							// Añadimos el hostiry al ArrayList
							History history_aux = new History(photo_id, photo_img, action, date, text);
							array_history.add(history_aux);
						}
					});		
				}
				
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error obteniendo history", ex);
				// Hubo algún error inesperado
				any_error_history = true;
			}

			// Si hubo algún error devolvemos 666
			if (any_error_history) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Mostramos la lista de historys
			showListHistorys();
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			// No hubo ningún error extraño
			else {
				// Si es codigo 2xx --> OK 
				historyAdapter.notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * Oculta el loading y muestra el listado de history
	 */
	private void showListHistorys(){
		ll_list_historys.setVisibility(View.VISIBLE);
		ll_loading.setVisibility(View.GONE);
	}
	
	
	/**
	 * Oculta el loading y muestra la información del usuario (nombre, nº seguidores y siguiendo y botón de seguir)
	 */
	private void showUserInfo(){
		ll_userinfo_loading.setVisibility(View.GONE);
		ll_userinfo_data.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * Cancela si hubiese alguna hebra obteniendo historys
	 */
	private void cancelGetHistorys() {
		if (getHistoryAsync != null) {
			if (getHistoryAsync.getStatus() == AsyncTask.Status.PENDING|| getHistoryAsync.getStatus() == AsyncTask.Status.RUNNING) {
				getHistoryAsync.cancel(true);
			}
		}
	}
	
}
