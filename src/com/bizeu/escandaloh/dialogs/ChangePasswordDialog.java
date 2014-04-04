package com.bizeu.escandaloh.dialogs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.util.Connectivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class ChangePasswordDialog extends Dialog {

	private Context mContext;
	private Button but_enviar;
	private Button but_cancelar;
	private EditText edit_contrasenia;
	private EditText edit_repite;
	
	private boolean any_error = false;
	
	public ChangePasswordDialog(Context con) {
		super(con);
		this.mContext = con;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.change_password);
		
		but_enviar = (Button) findViewById(R.id.but_changepass_enviar);
		but_cancelar = (Button) findViewById(R.id.but_changepass_cancelar);
		edit_contrasenia = (EditText) findViewById(R.id.edit_changepass_nuevacontrasenia);
		edit_repite = (EditText) findViewById(R.id.edit_changepass_repitenuevacontrasenia);
		
		but_cancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();			
			}
		});
		
		but_enviar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Connectivity.isOnline(mContext)){
					new ChangePassUser().execute();					
				}
				else{
					Toast toast = Toast.makeText(mContext, R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
					toast.show();
				}
				
			}
		});
	}
	
	
	

	/**
	 * Cambia la contraseña de un usuario
	 *
	 */
	private class ChangePassUser extends AsyncTask<Void,Integer,Void> {
		 	
		private String status = null;
		private String reason = null;
		private boolean result_ok;
		private String mensaje_respuesta = null;
		
		@Override
		protected void onPreExecute(){
			// Mostramos el ProgressDialog
			//progress.setCancelable(false);
			//progress.show();	
			
			// Reseteamos
			any_error = false;
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/change-password/";
	
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             post.setHeader("Session-Token", MyApplication.session_token);
	             
	             JSONObject dato = new JSONObject();	                        
	             dato.put("new_password", edit_contrasenia.getText().toString());
	             dato.put("new_password_repeated", edit_repite.getText().toString());


	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	                          
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);
	                 // Obtenemos el json devuelto
	                 JSONObject respJSON = new JSONObject(response_str);
	                 
	                 // Comprobamos el campo status del json
	                 status = respJSON.getString("status"); 
	                 
	                 // Si es OK obtenemos el msg
	                 if (status.equals("ok")){
	                	 result_ok = true;
	                	 mensaje_respuesta = respJSON.getString("msg");
	                 }
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 result_ok = false;
	                	 reason = respJSON.getString("reason");      	
	                 }
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	             any_error = true;
	        }
	        
	        return null;
	    }

		
		@Override
	    protected void onPostExecute(Void result) {

			/*
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
		    */
			
			// Si hubo algún error mostramos un mensaje
			if (any_error){
				Toast toast = Toast.makeText(mContext, R.string.lo_sentimos_se_ha_producido, Toast.LENGTH_SHORT);
				toast.show();
			}
			// Si no hubo ningún error extraño
			else{
				// Comprobamos si se hizo correctamente la petición
				if (result_ok){
		        	Toast.makeText(mContext, mensaje_respuesta, Toast.LENGTH_SHORT).show();
				    dismiss();
				}
				else{
		        	Toast.makeText(mContext, reason, Toast.LENGTH_SHORT).show();
				}	
			}				
	    }
	}

}
