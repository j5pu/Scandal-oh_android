package com.bizeu.escandaloh.users;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bizeu.escandaloh.R;

public class LoginActivity extends Activity {
	
	private EditText edit_nombre_email;
	private EditText edit_password;
	private Button boton_aceptar;
	private Button boton_cancelar;
	private ProgressDialog progress;
	
	private String reason;
	private String user_uri;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		edit_nombre_email = (EditText) findViewById(R.id.edit_login_nombre_email);
		edit_password = (EditText) findViewById(R.id.edit_login_pasword);
		boton_aceptar = (Button) findViewById(R.id.but_confirmar_login);
		boton_cancelar = (Button) findViewById(R.id.but_cancelar_login);	
		progress = new ProgressDialog(this);
		progress.setTitle("Logueando ...");
		progress.setCancelable(false);
		
		boton_cancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();			
			}
		});
		
		boton_aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (checkFields()){
					new LogInUser().execute();
				}			
			}
		});
	}
	
	
	
	/**
	 * Comprueba si los tres campos de datos han sido rellenados
	 * @return
	 */
	private boolean checkFields(){
		boolean result;
		
		if (edit_nombre_email.getText().toString().equals("") ||  
				edit_password.getText().toString().equals("")){
			result = false;
		}
		else{
			result = true;
		}
		
		return result;
	}
	
	
	
	/**
	 * Loguea un usuario
	 * @author Alejandro
	 *
	 */
	
	private class LogInUser extends AsyncTask<Void,Integer,String> {
		 
		@Override
		protected void onPreExecute(){
			// Mostramos el ProgressDialog
			progress.show();
		}
		
		@Override
	    protected String doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.48:8000/api/v1/user/login/";        

	        String status = null;
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();	   
	             
	             String username_email = edit_nombre_email.getText().toString();
	             String password = edit_password.getText().toString();

	             
	             dato.put("username_email", username_email);
	             dato.put("password", password);

	             // Creamos el StringEntity como UTF-8 (Caracteres ñ,á, ...)
	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	             
	             // Obtenemos el json devuelto
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);
	                 JSONObject respJSON = new JSONObject(response_str);
	                 
	                 // Comprobamos el campo status del json
	                 status = respJSON.getString("status");  
	                 
	                 // Si es OK obtenemos el user_uri
	                 if (status.equals("ok")){
	                	 user_uri = respJSON.getString("user_uri");
	                 }
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 reason = respJSON.getString("reason");
	                 }
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
	        
	        return status;
	    }
		
		
		@Override
	    protected void onPostExecute(String result) {
			
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			// Si se ha logeado correctamente guardamos el user_uri como un sharedPreference
	        if (result.equals("ok")){
	        	getBaseContext();
				SharedPreferences prefs = getBaseContext().getSharedPreferences(
	        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
	        	prefs.edit().putString("user_uri", user_uri).commit();
	        	Toast.makeText(getBaseContext(), "Login ok", Toast.LENGTH_SHORT)
				.show();
	        	
	        	// Le indicamos a la anterior actividad que ha habido éxito en el log in
	        	setResult(Activity.RESULT_OK);
	        	finish();
	        }
	        else if (result.equals("error")){
	        	 if (reason.equals("invalid password")){
            		 Toast.makeText(getBaseContext(), "Password incorrecto", Toast.LENGTH_SHORT)
						.show();
            	 }
            	 else if(reason.equals("invalid username/email")){
            		 Toast.makeText(getBaseContext(), "Nombre de usuario o email incorrecto", Toast.LENGTH_SHORT)
						.show();
            	 }
	        }
	    }
	}
}
