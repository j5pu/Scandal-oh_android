package com.bizeu.escandaloh.users;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.ScandalActivity;
import com.bizeu.escandaloh.adapters.HistoryAdapter;
import com.bizeu.escandaloh.model.History;
import com.bizeu.escandaloh.util.Connectivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryFragment extends Fragment {

	public static final String ESCANDALOS = "subidos";
	public static final String COMENTARIOS = "comentados";
	public static final String LIKES = "valorados";
	
	private ListView list_historys;
	private static View mView;
	private LinearLayout ll_list_historys;
	private LinearLayout ll_loading;
	
	private HistoryAdapter historyAdapter;
	private ArrayList<History> array_history = new ArrayList<History>();
	private GetHistoryTask getHistoryAsync;
	private boolean any_error_history;
	private boolean there_are_more_historys = true;
	private String meta_next_history = null;
	private String user_id;
	private String history_type;
	private Activity acti;
	private boolean any_error_deleting;
	private ProgressDialog delete_progress;

    public static final HistoryFragment newInstance(String user_id, String history_type) {
        HistoryFragment f = new HistoryFragment();

        Bundle b = new Bundle();
        b.putString("user_id", user_id);
        b.putString("history_type", history_type);
        f.setArguments(b);

        return f;
    }
    

	/**
	 * OnCreate
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		user_id = getArguments().getString("user_id");
		history_type = getArguments().getString("history_type");
		acti = getActivity();
	}

	
	/**
	 * onCreateView
	 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.history_list, container, false);

        list_historys = (ListView) mView.findViewById(R.id.list_historyfrag_historys);
        ll_list_historys = (LinearLayout) mView.findViewById(R.id.ll_listhistorys_list);
        ll_loading = (LinearLayout) mView.findViewById(R.id.ll_listhistorys_loading);
        
		historyAdapter = new HistoryAdapter(acti, R.layout.history, array_history);
		list_historys.setAdapter(historyAdapter);
			
		// Obtenemos el historial (Mi Actividad)
		getHistoryAsync = new GetHistoryTask();
		getHistoryAsync.execute();	
		
		
		// Al seleccionar una history mostramos el escándalo al que referencia
		list_historys.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  @Override
			  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					
				  History historyAux = ((History) list_historys.getItemAtPosition(position));
				  Intent i = new Intent(acti, ScandalActivity.class);
				  i.putExtra(ScandalActivity.PHOTO_ID, historyAux.getId());	
				  startActivity(i);
			  }
		});
		
		list_historys.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
            	
				if (user_id.equals(ProfileActivity.LOGGED) && ((ProfileActivity) getActivity()).getCurrentTab() == 0){
					AlertDialog.Builder alert_delete_scand = new AlertDialog.Builder(acti);
					alert_delete_scand.setTitle(R.string.quieres_borrar_el_escandalo_selec);
					alert_delete_scand.setPositiveButton(R.string.borrar,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1, int id) {
									History historyAux = ((History) list_historys.getItemAtPosition(pos));
									new DeleteScandalTask(historyAux.getId()).execute();
								}
							});
					alert_delete_scand.setNegativeButton(R.string.cancelar,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogo1, int id) {
								}
							});
					alert_delete_scand.show();
					return true;
				}
                else{
                    return false;
                }
            }
        }); 
		
		// Obtener siguientes historys
		list_historys.setOnScrollListener(new OnScrollListener() {
					
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			           
				if ((firstVisibleItem + visibleItemCount == historyAdapter.getCount() -1) && there_are_more_historys) {
					
					if (Connectivity.isOnline(acti)){		         		
						getHistoryAsync = new GetHistoryTask();
						getHistoryAsync.execute();
					}
					else{
						Toast toast = Toast.makeText(acti, R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
						toast.show();
					}	     			    		
				}   	
			}
			        
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});
		

        return mView;
    }
    
	
	
	/**
	 * Oculta el loading y muestra el listado de history
	 */
	private void showListHistorys(){
		ll_list_historys.setVisibility(View.VISIBLE);
		ll_loading.setVisibility(View.GONE);
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
				if (history_type.equals(ESCANDALOS)){
					url += "?action=upload";
				}
				else if (history_type.equals(COMENTARIOS)){
					url += "?action=comment";
				}
				else if (history_type.equals(LIKES)){
					url += "?action=vote";
				}
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
					final String num_comments = historyObject.getString("num_comments");
					final String likes = historyObject.getString("likes");
					final String dislikes = historyObject.getString("dislikes");
					final String username = historyObject.getString("username");
					final String category = historyObject.getString("category");
					
					acti.runOnUiThread(new Runnable() {
						@Override
						public void run() {							
							// Añadimos el history al ArrayList
							History history_aux = new History(photo_id, photo_img, action, date, text, num_comments, likes, dislikes, username, category);
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
				Toast toast = Toast.makeText(acti,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
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
	 * Borra un escándalo
	 * 
	 */
	private class DeleteScandalTask extends AsyncTask<Void, Integer, Integer> {

		private String id_to_delete;
		
		public DeleteScandalTask(String id_photo_to_delete){
			id_to_delete = id_photo_to_delete;
		}
		
		@Override
		protected void onPreExecute() {
			any_error_deleting = false;
			// Mostramos el ProgressDialog
			delete_progress = new ProgressDialog(acti);
			delete_progress.setMessage(getResources().getString(R.string.borrando_escandalo));
			delete_progress.setCancelable(false);
			delete_progress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/photo/" + id_to_delete + "/";

			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpDelete delete = new HttpDelete(urlString);
				delete.setHeader("Session-Token", MyApplication.session_token);
				
				response = client.execute(delete);
			}

			catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				any_error_deleting = true; // Indicamos que hubo algún error
			}

			if (any_error_deleting) {
				return 666;
			} else {
				// Devolvemos el resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			// Quitamos el ProgressDialog
			if (delete_progress.isShowing()) {
				delete_progress.dismiss();
			}
			
			// Si hubo algún error mostramos un mensaje
			if (any_error_deleting || result != 204) {
				Toast toast = Toast.makeText(acti, getResources().getString(R.string.lo_sentimos_hubo),Toast.LENGTH_SHORT);
				toast.show();
			} 
			
			else {
				// Volvemos a cargar el historial
				array_history.clear();
				historyAdapter.notifyDataSetChanged();
				getHistoryAsync = new GetHistoryTask();
				getHistoryAsync.execute();	
				
				// Indicamos que debe reiniciarse el carrusel
				MyApplication.reset_scandals = true;
				
				Toast toast = Toast.makeText(acti, getResources().getString(R.string.escandalo_borrado),Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}
}
