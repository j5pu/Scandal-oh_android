package com.bizeu.escandaloh;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.notifications.NotificationsActivity;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class ScandalActivity extends SherlockFragmentActivity {
	
	public static String PHOTO_ID = "photo_id";
	
	private boolean any_error;
	private String photo_id;
	private Context mContext;
	private ScandalFragment scandaloh_frag;
	private GetScandalTask getScandalAsync;
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scandal_fragment);
		
		mContext = this;
		
		if (getIntent() != null){
			photo_id = getIntent().getStringExtra(PHOTO_ID);
		}
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setDisplayShowTitleEnabled(false);
		actBar.setIcon(R.drawable.logo_blanco);

	}
	
	
	/**
	 * onStart
	 */
	public void onStart(){
		super.onStart();
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
		
		// Obtenemos el escándalo
		getScandalAsync = new GetScandalTask();
		getScandalAsync.execute();
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
	public void onDestroy(){
		super.onDestroy();
		cancelGetScandal();
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
	 * Obtiene los siguientes 10 escándalos anteriores a partir de uno dado
	 * 
	 */
	private class GetScandalTask extends AsyncTask<Void, Integer, Integer> {

		String c_date;
		String c_id;
		String c_photo;
		String c_resource_uri;
		String c_social_network;
		String c_text;
		String c_user;
		String c_user_id;
		String c_username;
		String c_avatar;

		@Override
		protected void onPreExecute() {
			any_error = false;	
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;
			url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/?id=" + photo_id;

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getEscandalos = new HttpGet(url);
				getEscandalos.setHeader("content-type", "application/json");
				
				// Si es con usuario le añadimos el session_token
				if (MyApplication.logged_user){
					getEscandalos.setHeader("Session-Token", MyApplication.session_token);
				}

				// Hacemos la petición al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", "escandalo solo: " + respStr);

				// Parseamos el escandalo devuelto
				JSONObject respJson = new JSONObject(respStr);
				JSONArray escandalosObject = respJson.getJSONArray("objects");
				
				// Si hay escándalo (existe)
				if (escandalosObject.length() > 0){
					// Obtenemos los datos de los escándalos
					for (int i = 0; i < escandalosObject.length(); i++) {
						final Comment last_comment;
						
						JSONObject escanObject = escandalosObject.getJSONObject(i);

						final String category = escanObject.getString("category");
						final String date = escanObject.getString("date");
						final String id = escanObject.getString("id");
						final String user_id = escanObject.getString("user_id");
						final String img_p = escanObject.getString("img_p"); // Fotos pequeñas sin marca de agua
						final String img = escanObject.getString("img");
						final String comments_count = escanObject.getString("comments_count");
						String latitude = escanObject.getString("latitude");
						String longitude = escanObject.getString("longitude");
						final String resource_uri = escanObject.getString("resource_uri");
						final String title = new String(escanObject.getString("title").getBytes("ISO-8859-1"), HTTP.UTF_8);
						final String user = escanObject.getString("user");
						String visits_count = escanObject.getString("visits_count");
						final String sound = escanObject.getString("sound");
						final String username = escanObject.getString("username");
						final String avatar = escanObject.getString("avatar");
						final String social_network = escanObject.getString("social_network");
						final int already_voted = Integer.parseInt(escanObject.getString("already_voted"));
						final int likes = Integer.parseInt(escanObject.getString("likes"));
						final int dislikes = Integer.parseInt(escanObject.getString("dislikes"));
						final int media_type = Integer.parseInt(escanObject.getString("media_type"));
						final String favicon = escanObject.getString("favicon");
						final String source = escanObject.getString("source");
						final String source_name = escanObject.getString("source_name");

						// Obtenemos el comentario más reciente
						if (!escanObject.isNull("last_comment")){
							JSONObject commentObject = escanObject.getJSONObject("last_comment");
							c_date = commentObject.getString("date");
							c_id = commentObject.getString("id");
							c_photo = commentObject.getString("photo");
							c_resource_uri = commentObject
							.getString("resource_uri");
							c_social_network = commentObject
							.getString("social_network");
							c_text = new String(commentObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
							c_user = commentObject.getString("user");
							c_user_id = commentObject.getString("user_id");
							c_username = commentObject.getString("username");
							c_avatar = commentObject.getString("avatar");

							last_comment = new Comment(c_date, c_id, c_photo,
									c_resource_uri, c_social_network, c_text,
								c_user, c_user_id, c_username, c_avatar);
						}
						else{
							last_comment = null;
						}
						
						runOnUiThread(new Runnable() {
							@Override
							public void run() {							
								Scandaloh scandal = new Scandaloh(id, user_id, title,category,
										Integer.parseInt(comments_count),resource_uri,
										MyApplication.DIRECCION_BUCKET + img_p,
										MyApplication.DIRECCION_BUCKET + img, sound, username, date,
										avatar, last_comment, social_network,
										already_voted, likes, dislikes, media_type, MyApplication.DIRECCION_BUCKET + favicon, source, source_name);
								scandaloh_frag = ScandalFragment.newInstance(scandal);
							}
						});				
					}
				}
				
				// No hay escándalo --> objects:[]
				else{
					// Devolvemos un código para saber que no hay escándalo
					return 555;
				}
				
				
			} catch (Exception ex) {
				Log.e("ServicioRest",
						"Error obteniendo escándalos o comentarios", ex);
				// Hubo algún error inesperado
				any_error = true;
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

			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hay escándalo (se ha borrado)
			else if (result == 555){
				Toast toast = Toast.makeText(mContext,
						R.string.ese_escandalo_ha_sido_borrado, Toast.LENGTH_SHORT);
				toast.show();
				// Terminamos la actividad
				finish();
			}
			else{	
				// Si no se ha destruido la actividad mostramos el fragmento
				if (!isCancelled()){
					getSupportFragmentManager().beginTransaction().replace(R.id.frag__scandal, scandaloh_frag).commitAllowingStateLoss();;
				}
			}
		}
	}
	
	
	/**
	 * Cancela si hubiese alguna hebra obteniendo el escándalo
	 */
	private void cancelGetScandal() {
		if (getScandalAsync != null) {
			if (getScandalAsync.getStatus() == AsyncTask.Status.PENDING|| getScandalAsync.getStatus() == AsyncTask.Status.RUNNING) {
				getScandalAsync.cancel(true);
			}
		}
	}
}
