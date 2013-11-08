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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizeu.escandaloh.R;

public class LoginActivity extends Activity {
	
	private EditText edit_nombre_email;
	private EditText edit_password;
	private TextView txt_nombre_email;
	private TextView txt_password;
	private Button boton_aceptar;
	private Button boton_cancelar;
	private ProgressDialog progress;
	
	private String name_error;
	private String password_error;
	private int reason_code;
	private String user_uri;
	private String reason;
	
	private boolean has_name_error;
	private boolean has_password_error;
	
	private String status = null;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		edit_nombre_email = (EditText) findViewById(R.id.edit_login_nombre_email);
		edit_password = (EditText) findViewById(R.id.edit_login_pasword);
		txt_nombre_email = (TextView) findViewById(R.id.txt_login_nombre_email);
		txt_password = (TextView) findViewById(R.id.txt_login_password);
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
				new LogInUser().execute();
			}			
		});
	}
	
	
	
	
	
	/**
	 * Loguea un usuario
	 * @author Alejandro
	 *
	 */
	
	private class LogInUser extends AsyncTask<Void,Integer,Void> {
		 
		@Override
		protected void onPreExecute(){
			has_name_error = false;
			has_password_error = false;
			txt_nombre_email.setVisibility(View.GONE);
			txt_password.setVisibility(View.GONE);
			// Mostramos el ProgressDialog
			progress.show();
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.26:8000/api/v1/user/login/";        
	        
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();	   
	             
	             String username_email = edit_nombre_email.getText().toString();
	             String password = edit_password.getText().toString();
             
	             dato.put("username_email", username_email);
	             dato.put("password", password);

	             // Creamos el StringEntity como UTF-8 (Caracteres �,�, ...)
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
	                 
	                 // Si es OK obtenemos el user_uri
	                 if (status.equals("ok")){
	                	 user_uri = respJSON.getString("user_uri");
	                 }
	                 // Si no es OK obtenemos la raz�n
	                 else if (status.equals("error")){
	                	 reason_code = Integer.parseInt(respJSON.getString("reason_code"));
	                	 reason = respJSON.getString("reason");
	                	 if (reason_code == 1){ // Fallo de identificaci�n: nombre de usuario no existe
	                		 name_error = reason;
	                		 has_name_error = true;
	                	 }
	                	 else if (reason_code == 2){ // Fallo de identificaci�n: password no existe
	                		 password_error = reason;
	                		 has_password_error = true;
	                	 }
	                	 if (reason_code == 3){ // Fallo de autentificaci�n ((caracteres raros, espacio, longitud...)
	                		 JSONObject jsonReason = new JSONObject(respJSON.getString("reason_details"));
	                		 if (jsonReason.has("username_email")){
	                			 name_error = jsonReason.getString("username_email");
	                			 has_name_error = true;
	                		 }
	                		 if (jsonReason.has("password")){
	                			 password_error = jsonReason.getString("password");
	                			 has_password_error = true;
	                		 }	 
	                	 }
	                 }
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
			return null;
	      
	    }
		
		
		@Override
	    protected void onPostExecute(Void result) {
			
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			if (has_name_error){
				txt_nombre_email.setVisibility(View.VISIBLE);
				txt_nombre_email.setText(name_error);
			}
			if (has_password_error){
				txt_password.setVisibility(View.VISIBLE);
				txt_password.setText(password_error);
			}
			// Se ha logueado correctamente
			if (!has_name_error && !has_password_error){
				SharedPreferences prefs = getBaseContext().getSharedPreferences(
	        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
	        	prefs.edit().putString("user_uri", user_uri).commit();
	        	Toast.makeText(getBaseContext(), "Login ok", Toast.LENGTH_SHORT)
				.show();
	        	
	        	// Le indicamos a la anterior actividad que ha habido �xito en el log in
	        	setResult(Activity.RESULT_OK);
	        	finish();
			}				
	    }
	}
}
