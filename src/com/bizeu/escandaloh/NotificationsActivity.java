package com.bizeu.escandaloh;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.adapters.NotificationAdapter;
import com.bizeu.escandaloh.model.Notification;
import com.bizeu.escandaloh.util.Connectivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class NotificationsActivity extends SherlockActivity {

	public static String PHOTO_ID = "photo_id";
	private static int NUM_NOTIFICATIONS_TO_LOAD = 20;
	
	private ListView list_notifications;
	
	private ArrayList<Notification> array_notifications = new ArrayList<Notification>();
	private NotificationAdapter notificationsAdapter;
	private boolean any_error = false;
	private boolean there_are_more_notifs = true;
	private Context mContext;
	private String meta_next_notifs = null;
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications);

		mContext = this;
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setDisplayShowTitleEnabled(false);
		actBar.setIcon(R.drawable.logo_blanco);
		
		list_notifications = (ListView) findViewById(R.id.list_notifications);
		
		// Al seleccionar una notificación mostramos el escándalo al que referencia
		list_notifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  @Override
			  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				  
				  Notification n = (Notification) list_notifications.getItemAtPosition(position);
				  Intent i = new Intent(NotificationsActivity.this, NotificationScandalActivity.class);
				  i.putExtra(PHOTO_ID, n.getPhotoId());
				  startActivity(i);	     
			  }
		});
		
		list_notifications.setOnScrollListener(new OnScrollListener() {
			
	        @Override
	        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	           
	        	if ((firstVisibleItem + visibleItemCount == totalItemCount - 3) && there_are_more_notifs) {
	            	if (Connectivity.isOnline(mContext)){
	            		new GetNotificationsTask().execute(); 
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
		
		// Obtenemos las notificaciones
		notificationsAdapter = new NotificationAdapter(this, R.layout.notification, array_notifications);
		list_notifications.setAdapter(notificationsAdapter);
		new GetNotificationsTask().execute();
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
	 * Muestra la lista de comentarios
	 * 
	 */
	private class GetNotificationsTask extends AsyncTask<Void, Integer, Integer> {

		String n_text;
		String n_photo_img_p;
		String n_photo_id;
		String n_is_read;
		String n_count;

		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;
			
			// No hay notificaciones: obtenemos las primeras
			if (array_notifications.size() == 0){	
				url = MyApplication.SERVER_ADDRESS + "/api/v1/notification/grouped/?limit=" + NUM_NOTIFICATIONS_TO_LOAD ;		
			}
			
			// Obtenemos las siguientes notificaciones
			else{
				// Fin del carrusel: meta nulo indica que no hay más escándalos
				if (meta_next_notifs.equals("null")){
					there_are_more_notifs = false;
					return 5;
				}
				url = MyApplication.SERVER_ADDRESS + meta_next_notifs;
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

				// Parseamos el json para obtener los escandalos
				JSONArray notificationsObject = null;

				notificationsObject = respJSON.getJSONArray("objects");

				for (int i = 0; i < notificationsObject.length(); i++) {
					JSONObject notiObject = notificationsObject.getJSONObject(i);

					n_count = notiObject.getString("count");
					n_is_read = notiObject.getString("is_read");
					n_photo_img_p = notiObject.getString("photo_img_p");
					n_text = new String(notiObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
					n_photo_id = notiObject.getString("photo_id");

					Notification notiAux = new Notification(n_text, n_photo_img_p, n_photo_id, n_is_read);
					array_notifications.add(notiAux);
				}

			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				any_error = true; // Indicamos que hubo un error

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

			// Si hubo algún error
			if (result == 666) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			}

			// No hubo ningún error extraño
			else {
				// Si es codigo 2xx --> OK 
				notificationsAdapter.notifyDataSetChanged();
			}
		}
	}
}
