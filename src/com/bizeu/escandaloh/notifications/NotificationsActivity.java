package com.bizeu.escandaloh.notifications;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.ScandalActivity;
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
	private LinearLayout ll_loading;
	private LinearLayout ll_list_notis;
	private TextView txt_no_tienes_notis;
	
	private ArrayList<Notification> array_notifications = new ArrayList<Notification>();
	private NotificationAdapter notificationsAdapter;
	private boolean any_error = false;
	private boolean there_are_more_notifs = true;
	private Context mContext;
	private String meta_next_notifs = null;
	private GetNotificationsTask getNotisAsync;
	
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
		actBar.setDisplayShowTitleEnabled(true);
		actBar.setTitle(getResources().getString(R.string.notificaciones));
		actBar.setIcon(R.drawable.logo_blanco);
		
		list_notifications = (ListView) findViewById(R.id.list_notifications);
		ll_loading = (LinearLayout) findViewById(R.id.ll_notifications_loading);
		ll_list_notis = (LinearLayout) findViewById(R.id.ll_notifications_listnotis);
		notificationsAdapter = new NotificationAdapter(this, R.layout.notification, array_notifications);
		list_notifications.setAdapter(notificationsAdapter);
		txt_no_tienes_notis = (TextView) findViewById(R.id.txt_notifications_notienesnotis);
		
		// Al seleccionar una notificación mostramos el escándalo al que referencia
		list_notifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  @Override
			  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				  Notification n = (Notification) list_notifications.getItemAtPosition(position);
							  
				  // Marcamos la notificación como leída
				  ((Notification) list_notifications.getItemAtPosition(position)).setIsRead(true);
				  notificationsAdapter.notifyDataSetChanged();
				  new MarkNotificationAsReadTask(n.getPhotoId()).execute();
				  
				  // Abrimos el escandalo
				  Intent i = new Intent(NotificationsActivity.this, ScandalActivity.class);
				  i.putExtra(PHOTO_ID, n.getPhotoId());
				  startActivity(i);	     
			  }
		});
		
		// Obtener siguientes notificaciones
		list_notifications.setOnScrollListener(new OnScrollListener() {
			
	        @Override
	        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	           
	        	if ((firstVisibleItem + visibleItemCount == notificationsAdapter.getCount() - 3) && there_are_more_notifs) {
	            	if (Connectivity.isOnline(mContext)){
	            		
						getNotisAsync = new GetNotificationsTask();
						getNotisAsync.execute();
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
		
		// Mostramos el loading
		showLoading();
		
		// Obtenemos las notificaciones
		getNotisAsync = new GetNotificationsTask();
		getNotisAsync.execute();
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
	 * onDestroy
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		cancelGetNotifications();
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
				
				// Obtenemos el meta
				JSONObject respMetaJson = respJSON.getJSONObject("meta");
				meta_next_notifs = respMetaJson.getString("next");

				notificationsObject = respJSON.getJSONArray("objects");

				for (int i = 0; i < notificationsObject.length(); i++) {
					JSONObject notiObject = notificationsObject.getJSONObject(i);

					n_count = notiObject.getString("count");
					n_is_read = notiObject.getString("is_read");
					n_photo_img_p = notiObject.getString("photo_img_small");
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

			// Mostramos el listado de notificaciones
			showListNotifications();
			
			// Si hubo algún error
			if (result == 666) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			}

			// No hubo ningún error extraño
			else {
				if (array_notifications.size() == 0){
					txt_no_tienes_notis.setVisibility(View.VISIBLE);
				}
				// Si es codigo 2xx --> OK 
				notificationsAdapter.notifyDataSetChanged();
			}
		}
	}
	
	
	
	
	
 	/**
	 * Marca una notificación como leída
	 *
	 */
	private class MarkNotificationAsReadTask extends AsyncTask<Void,Integer,Void> {
	
		private String photo_id;
		
		public MarkNotificationAsReadTask(String photo_id){
			this.photo_id = photo_id;
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/notification/mark-as-read/";
	
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             post.setHeader("Session-Token", MyApplication.session_token);

	             JSONObject dato = new JSONObject();	                        
	             dato.put("photo_id", photo_id);

	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	                          
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);	            
	             }
	        }
	        catch (Exception ex){
	        }
	        
	        return null;
	    }	
	}
	
	
	/**
	 * Muestra el loading en pantalla
	 */
	private void showLoading(){
		ll_list_notis.setVisibility(View.GONE);
		ll_loading.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * Oculta el loading y muestra el listado de notificaciones
	 */
	private void showListNotifications(){
		ll_list_notis.setVisibility(View.VISIBLE);
		ll_loading.setVisibility(View.GONE);
	}
	
	
	/**
	 * Cancela si hubiese alguna hebra obteniendo notificaciones
	 */
	private void cancelGetNotifications() {
		if (getNotisAsync != null) {
			if (getNotisAsync.getStatus() == AsyncTask.Status.PENDING|| getNotisAsync.getStatus() == AsyncTask.Status.RUNNING) {
				Log.v("WE","Cancelamos");
				getNotisAsync.cancel(true);
			}
		}
	}
}
