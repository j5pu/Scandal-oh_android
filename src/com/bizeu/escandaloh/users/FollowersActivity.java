package com.bizeu.escandaloh.users;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.adapters.FollowAdapter;
import com.bizeu.escandaloh.model.Follow;
import com.bizeu.escandaloh.util.Connectivity;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class FollowersActivity extends SherlockActivity {

	public static final String USER_ID = "user_id";
	public static final String FOLLOWERS = "followers";
	
	private ListView list_followers_follows;
	private LinearLayout ll_loading;
	private LinearLayout ll_list_follows;
	
	private ArrayList<Follow> array_follows = new ArrayList<Follow>();
	private boolean seguidores = true; // Nos indica si se quiere obtener los seguidores (y no los seguidos)
	private FollowAdapter followAdapter;
	private GetFollowsTask getFollowsAsync;
	private boolean any_error = false;
	private String user_id;
	private boolean there_are_more_follows = true;
	private boolean getting_follows = false;
	private Context mContext;
	private String meta_next_follows = null;
	
	
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.followers);
		
		if (getIntent() != null){
			seguidores = getIntent().getBooleanExtra(FOLLOWERS, true);
			user_id = getIntent().getStringExtra(USER_ID);
		}
		
		mContext = this;
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setDisplayShowTitleEnabled(true);
		actBar.setIcon(R.drawable.logo_blanco);
			
		if (seguidores){
			actBar.setTitle(getResources().getString(R.string.seguidores));
		}
		
		else{
			actBar.setTitle(getResources().getString(R.string.siguiendo));
		}
		
		list_followers_follows = (ListView) findViewById(R.id.list_followers_list);
		ll_loading = (LinearLayout) findViewById(R.id.ll_follows_loading);
		ll_list_follows = (LinearLayout) findViewById(R.id.ll_follows_list);
		
		followAdapter = new FollowAdapter(this, R.layout.follow, array_follows);
		list_followers_follows.setAdapter(followAdapter);
		
		list_followers_follows.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Follow followAux =  ((Follow) list_followers_follows.getItemAtPosition(position));
				
				Intent i = new Intent(FollowersActivity.this, ProfileActivity.class);
				i.putExtra(ProfileActivity.USER_ID, followAux.getUserId());
				startActivity(i);	
			}
		});
		
		list_followers_follows.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
				if ((firstVisibleItem + visibleItemCount == followAdapter.getCount()) && there_are_more_follows) {
	            	if (Connectivity.isOnline(mContext)){
	            		if (!getting_follows){
		            		// Obtenemos los siguientes follows
		            		getFollowsAsync = new GetFollowsTask();
		            		getFollowsAsync.execute();
	            		}
	            	}
	            	else{
	            		Toast toast = Toast.makeText(mContext, R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
	            		toast.show();
	            	}	     			    		
	            } 	
			}
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) { 	
			}
		});
		
		
		// Mostramos el loading
		showLoading();	
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
		cancelGetFollows();
	}
	
	

	/**
	 * Muestra la lista de follows
	 * 
	 */
	private class GetFollowsTask extends AsyncTask<Void, Integer, Integer> {

		String f_avatar;
		String f_id;
		String f_username;

		@Override
		protected void onPreExecute() {
			any_error = false;
			getting_follows = true;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;
			
			// No hay follows: obtenemos los primeros
			if (array_follows.size() == 0){	
				if (seguidores){
					url = MyApplication.SERVER_ADDRESS + "/api/v1/user/" + user_id + "/followers/" ;
				}
				else{
					url = MyApplication.SERVER_ADDRESS + "/api/v1/user/" + user_id + "/follows/" ;
				}
			}
			
			// Obtenemos las siguientes notificaciones
			else{
				// Fin del carrusel: meta nulo indica que no hay más escándalos
				if (meta_next_follows.equals("null")){
					there_are_more_follows = false;
					return 5;
				}
				url = MyApplication.SERVER_ADDRESS + meta_next_follows;
			}
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet del = new HttpGet(url);
			del.setHeader("Content-Type", "application/json");
			del.setHeader("Session-Token", MyApplication.session_token);
			HttpResponse response = null;

			try {
				response = httpClient.execute(del);
				String respStr = EntityUtils.toString(response.getEntity());

				Log.i("WE", "com: " + respStr.toString());
			
				JSONObject respJSON = new JSONObject(respStr);

				// Parseamos el json para obtener los follows
				JSONArray followsObject = null;
				
				// Obtenemos el meta
				JSONObject respMetaJson = respJSON.getJSONObject("meta");
				meta_next_follows = respMetaJson.getString("next");

				followsObject = respJSON.getJSONArray("objects");

				for (int i = 0; i < followsObject.length(); i++) {
					
					JSONObject followObject = followsObject.getJSONObject(i);

					if (followObject.has("avatar")){
						f_avatar = followObject.getString("avatar");
					}
					
					if (followObject.has("username")){
						f_username = new String(followObject.getString("username").getBytes("ISO-8859-1"), HTTP.UTF_8);
					}
					if (followObject.has("id")){
						f_id = followObject.getString("id");
					}	

					Follow followAux = new Follow(f_id, f_username, f_avatar);
					array_follows.add(followAux);
				}

			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				any_error = true; // Indicamos que hubo un error
			}

			// Si hubo algún error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el código de respuesta
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Mostramos el listado de notificaciones
			showListFollows();
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			// No hubo ningún error extraño
			else {
				// Si es codigo 2xx --> OK 
				followAdapter.notifyDataSetChanged();
			}
			
			// Ya no se está obteniendo follows: abrimos la llave
			getting_follows = false;
		}
	}
	
	
	
	/**
	 * Muestra el loading en pantalla
	 */
	private void showLoading(){
		ll_list_follows.setVisibility(View.GONE);
		ll_loading.setVisibility(View.VISIBLE);
	}
	
	
	
	/**
	 * Oculta el loading y muestra el listado de follows
	 */
	private void showListFollows(){
		ll_list_follows.setVisibility(View.VISIBLE);
		ll_loading.setVisibility(View.GONE);
	}
	
	
	/**
	 * Cancela si hubiese alguna hebra obteniendo follows
	 */
	private void cancelGetFollows() {
		if (getFollowsAsync != null) {
			if (getFollowsAsync.getStatus() == AsyncTask.Status.PENDING|| getFollowsAsync.getStatus() == AsyncTask.Status.RUNNING) {
				getFollowsAsync.cancel(true);
			}
		}
	}
	
}
