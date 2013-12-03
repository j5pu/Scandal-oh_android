package com.bizeu.escandaloh.users;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.text.Editable;
import android.text.TextWatcher;
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
					// Si todos los campos son correctos hacemos la petición de registro
					if (checkFields()){
						new SignInUser().execute();
					}
				}
				else{
					Toast toast = Toast.makeText(context, "No dispone de conexión a internet", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});
		
		
		// Cuando escriban algo que desaparezca el mensaje de error (en caso de que exista)
		edit_nombre_usuario.addTextChangedListener(new TextWatcher() {          
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {                                                
				edit_nombre_usuario.setError(null);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// TODO Auto-generated method stub		
			} 	
			});
				
				
		// Cuando escriban algo que desaparezca el mensaje de error (en caso de que exista)	
		edit_email_usuario.addTextChangedListener(new TextWatcher() {          
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {                                                
				edit_email_usuario.setError(null);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub		
			}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,int after) {
					// TODO Auto-generated method stub		
				} 
		});
				
				
		// Cuando escriban algo que desaparezca el mensaje de error (en caso de que exista)
		edit_password_usuario.addTextChangedListener(new TextWatcher() {          
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {                                                
				edit_password_usuario.setError(null);
		    }

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// TODO Auto-generated method stub		
			} 
		});
		
		progress = new ProgressDialog(this);
	}
	
	
	
	
	/**
	 * Comprueba si todos los campos son correctos
	 * @return True si todos los campos son correctos. False si alguno no lo es.
	 */
	private boolean checkFields(){
		boolean all_correct = true ;
		
		edit_nombre_usuario.setError(null);
		edit_password_usuario.setError(null);
		edit_email_usuario.setError(null);
		
		// Nombre menos de 4 caracteres
		if (edit_nombre_usuario.getText().toString().length() < 4){
			edit_nombre_usuario.setError("Este campo debe tener al menos 4 caracteres");
			all_correct = false;
		}
		// Nombre vacío
		if (edit_nombre_usuario.getText().toString().length() == 0){
			edit_nombre_usuario.setError("Este campo es obligatorio");
			all_correct = false;
		}
		// Email incorrecto
		if (!isEmailValid(edit_email_usuario.getText().toString())){
			edit_email_usuario.setError("Introduzca una dirección de email válida");
			all_correct = false;
		}	
		// Email vacío
		if (edit_email_usuario.getText().toString().length() == 0){
			edit_email_usuario.setError("Este campo es obligatorio");
			all_correct = false;
		}
		// Password menos de 4 caracteres
		if (edit_password_usuario.getText().toString().length() < 6){
			edit_password_usuario.setError("Este campo debe tener al menos 6 caracteres");
			all_correct = false;
		}
		// Password vacío
		if (edit_password_usuario.getText().toString().length() == 0){
			edit_password_usuario.setError("Este campo es obligatorio");
			all_correct = false;
		}
		return all_correct;
	}
	
	
	/**
	 * Comprueba si un string tiene formato de email
	 * @param email
	 * @return
	 */
	public static boolean isEmailValid(String email) {
	    boolean isValid = false;

	    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;

	    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    if (matcher.matches()) {
	        isValid = true;
	    }
	    return isValid;
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
		        	Toast.makeText(getBaseContext(), "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
		        	
		        	// Le indicamos a la anterior actividad que ha habido éxito en el registro
		        	setResult(Activity.RESULT_OK);
		        	finish();

					Log.e("WE","Registro ok");
				}	
			}				
	    }
	}
	
}

