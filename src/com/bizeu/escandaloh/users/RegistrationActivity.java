package com.bizeu.escandaloh.users;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.util.Connectivity;

public class RegistrationActivity extends SherlockActivity {

	private EditText edit_nombre_usuario;
	private EditText edit_password_usuario;
	private EditText edit_email_usuario;
	private Button aceptar;
	
	private boolean has_name_error;
	private boolean has_password_error;
	private boolean has_email_error;
	
	private String name_error;
	private String password_error;
	private String email_error; 
	
	private String status = null;
	private String user_uri ;
	private ProgressDialog progress;
	private Context context;
	private boolean any_error;

	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		context = this;
		// Ocultamos el action bar
		getSupportActionBar().hide();
		
		edit_nombre_usuario = (EditText) findViewById(R.id.edit_registro_nombre);
		edit_password_usuario = (EditText) findViewById(R.id.edit_registro_password);
		edit_email_usuario = (EditText) findViewById(R.id.edit_registro_email);
		aceptar = (Button) findViewById(R.id.but_confirmar_registro);
		
		aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (Connectivity.isOnline(context)){
					// Comprobamos si el nombre de usuario tiene como máximo 10 caracteres
					if (edit_nombre_usuario.getText().toString().length() < 26){
						new SignInUser().execute();
					}	
					else{
						edit_nombre_usuario.setError("El nombre de usuario debe contener como máximo 10 caracteres");
					}
				}
				else{
					Toast toast = Toast.makeText(context, "No dispone de conexión a internet", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});
		
		progress = new ProgressDialog(this);
	}
	
	
	/**
	 * Registra un usuario
	 * @author Alejandro
	 *
	 */
	private class SignInUser extends AsyncTask<Void,Integer,Void> {
		 	
		@Override
		protected void onPreExecute(){
			has_name_error = false;
			has_password_error = false;
			has_email_error = false;
			edit_nombre_usuario.setError(null);
			edit_password_usuario.setError(null);
			edit_email_usuario.setError(null);
			
			// Mostramos el ProgressDialog
			progress.setTitle("Registrando usuario ...");
			progress.setMessage("Espere, por favor");
			progress.setCancelable(false);
			progress.show();	
			
			// Reseteamos
			any_error = false;
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "api/v1/user/";
	
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();	              
	             
	             dato.put("username", edit_nombre_usuario.getText().toString());
	             dato.put("password", edit_password_usuario.getText().toString());
	             dato.put("email", edit_email_usuario.getText().toString());

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
	                	 user_uri = respJSON.getString("resource_uri");
	                 }
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 JSONObject jsonReason = new JSONObject(respJSON.getString("reason"));
	                	 if (jsonReason.has("username")){
	                		 name_error = jsonReason.getString("username");
	                		 has_name_error = true;
	                	 }
	                	 if (jsonReason.has("password")){
	                		 JSONArray array = (JSONArray) jsonReason.get("password");
	                		 password_error = (String) array.get(0);
	                		
	                		 //Log.v("WE",array.toString());
	                		 //password_error = (ArrayList<String>) jsonReason.get("password");
	                		// password_error = jsonReason.getString("password");
	                		 has_password_error = true;
	                		 
	                	 }
	                	 
	                	 if (jsonReason.has("email")){
	                		 email_error = jsonReason.getString("email");
	                		 has_email_error = true;
	                	 }
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

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			// Si hubo algún error mostramos un mensaje
			if (any_error){
				Toast toast = Toast.makeText(context, "Lo sentimos, se produjo algún error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			// Si no hubo ningún error
			else{
				if (has_name_error){
					edit_nombre_usuario.setError(name_error);
				}
				
				if (has_password_error){
					edit_password_usuario.setError(password_error);
				}
				
				if (has_email_error){
					edit_email_usuario.setError(email_error);
				}
				
				// Se ha registrado correctamente
				if (!has_name_error && !has_password_error && !has_email_error){
					SharedPreferences prefs = getBaseContext().getSharedPreferences(
		        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
		        	prefs.edit().putString(MyApplication.USER_URI, user_uri).commit();
		        	MyApplication.logged_user = true;
		        	Toast.makeText(getBaseContext(), "Registro realizado correctamente", Toast.LENGTH_SHORT).show();
		        	
		        	// Le indicamos a la anterior actividad que ha habido éxito en el registro
		        	setResult(Activity.RESULT_OK);
		        	finish();

					Log.e("WE","Registro ok");
				}	
			}				
	    }
	}
	
}

