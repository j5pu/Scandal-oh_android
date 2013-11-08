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

public class RegistrationActivity extends Activity {

	private EditText edit_nombre_usuario;
	private EditText edit_password_usuario;
	private EditText edit_email_usuario;
	private TextView txt_nombre_usuario;
	private TextView txt_password_usuario;
	private TextView txt_email_usuario;
	private Button aceptar;
	private Button cancelar;
	
	private boolean has_name_error;
	private boolean has_password_error;
	private boolean has_email_error;
	
	private String name_error;
	private String password_error;
	private String email_error; 
	
	private String status = null;
	private String reason;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		edit_nombre_usuario = (EditText) findViewById(R.id.edit_registro_nombre);
		edit_password_usuario = (EditText) findViewById(R.id.edit_registro_password);
		edit_email_usuario = (EditText) findViewById(R.id.edit_registro_email);
		txt_nombre_usuario = (TextView) findViewById(R.id.txt_registro_nombre);
		txt_password_usuario = (TextView) findViewById(R.id.txt_registro_password);
		txt_email_usuario = (TextView) findViewById(R.id.txt_registro_email);
		aceptar = (Button) findViewById(R.id.but_confirmar_registro);
		cancelar = (Button) findViewById(R.id.but_cancelar_registro);
		
		cancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();	
			}
		});
		
		aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new SignInUser().execute();
			}
		});
	}
	
	
	/**
	 * Comprueba si los tres campos de datos han sido rellenados
	 * @return
	 */
	private boolean checkFields(){
		boolean result;
		
		if (edit_nombre_usuario.getText().toString().equals("") ||  
				edit_password_usuario.getText().toString().equals("")|| 
				edit_email_usuario.getText().toString().equals("")){
			result = false;
		}
		else{
			result = true;
		}
		
		return result;
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
			txt_nombre_usuario.setVisibility(View.GONE);
			txt_password_usuario.setVisibility(View.GONE);
			txt_email_usuario.setVisibility(View.GONE);
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	    	boolean result = false;
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.26:8000/api/v1/user/";
	        
	        

	        HttpResponse response = null;
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

	             response = client.execute(post);
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
	                	 //user_uri = respJSON.getString("user_uri");
	                 }
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 JSONObject jsonReason = new JSONObject(respJSON.getString("reason"));
	                	 JSONObject jsonUser = new JSONObject(jsonReason.getString("user"));
	                	 if (jsonUser.has("username")){
	                		 name_error = jsonUser.getString("username");
	                		 has_name_error = true;
	                	 }
	                	 if (jsonUser.has("password")){
	                		 password_error = jsonUser.getString("password");
	                		 has_password_error = true;
	                	 }
	                	 
	                	 if (jsonUser.has("email")){
	                		 email_error = jsonUser.getString("email");
	                		 has_email_error = true;
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

			if (has_name_error){
				txt_nombre_usuario.setVisibility(View.VISIBLE);
				txt_nombre_usuario.setText(name_error);
			}
			if (has_password_error){
				txt_password_usuario.setVisibility(View.VISIBLE);
				txt_password_usuario.setText(password_error);
			}
			if (has_email_error){
				txt_email_usuario.setVisibility(View.VISIBLE);
				txt_email_usuario.setText(email_error);
			}
			// Se ha logueado correctamente
			if (!has_name_error && !has_password_error && !has_email_error){
				/*
				SharedPreferences prefs = getBaseContext().getSharedPreferences(
	        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
	        	prefs.edit().putString("user_uri", user_uri).commit();
	        	Toast.makeText(getBaseContext(), "Login ok", Toast.LENGTH_SHORT)
				.show();
	        	
	        	// Le indicamos a la anterior actividad que ha habido éxito en el log in
	        	setResult(Activity.RESULT_OK);
	        	finish();
	        	*/
				Log.e("WE","Login ok");
			}	
	    }
	}
	
}

