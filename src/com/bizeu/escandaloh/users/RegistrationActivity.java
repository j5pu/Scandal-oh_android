package com.bizeu.escandaloh.users;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bizeu.escandaloh.R;

public class RegistrationActivity extends Activity {

	private EditText edit_nombre_usuario;
	private EditText edit_password_usuario;
	private EditText edit_email_usuario;
	private Button aceptar;
	private Button cancelar;
	
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
				// Si ha introducido los tres datos hacemos su registro
				if (checkFields()){
					new SignInUser().execute();
				}
				// Si no mostramos un mensaje
				else{
					Toast toast = Toast.makeText(getBaseContext(), "Debe rellenar todos los campos", Toast.LENGTH_SHORT);
					toast.show();
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
	 * Sube un comentario
	 * @author Alejandro
	 *
	 */
	private class SignInUser extends AsyncTask<Void,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(Void... params) {
	    	boolean result = false;
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.48:8000/api/v1/user/";        

	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();	              
	             
	             dato.put("username", edit_nombre_usuario.getText().toString());
	             dato.put("password", edit_password_usuario.getText().toString());
	             dato.put("email", edit_email_usuario.getText().toString());

	             StringEntity entity = new StringEntity(dato.toString());
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	             
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);
	                 result = true;
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
	        
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
	        if (result){
	        	Log.v("WE","usuario registrado");
	        	finish();
	        }
	        else{
	        	Log.v("WE","usuario no registrado");
	        }
	    }
	}
	
}

