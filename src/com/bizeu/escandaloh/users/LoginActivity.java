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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.RecordAudioDialog;
import com.bizeu.escandaloh.RecordAudioDialog.OnMyDialogResult;
import com.bizeu.escandaloh.RememberPasswordDialog;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

public class LoginActivity extends SherlockActivity {
	
	private EditText edit_nombre_email;
	private EditText edit_password;
	private Button boton_aceptar;
	private TextView txt_recordar_contrasenia;
	private ProgressDialog progress;
	private ViewGroup pantalla;
	
	private String name_error;
	private String password_error;
	private int reason_code;
	private String user_uri;
	private String reason;
	private boolean has_name_error;
	private boolean has_password_error;
	private String status = null;
	private boolean login_error = false;
	private String loginMessageError;
	private Context mContext;
	private boolean any_error;
	
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		// Cambiamos la fuente de la pantalla
		pantalla = (ViewGroup)findViewById(R.id.lay_pantalla_login);
		Fuente.cambiaFuente(pantalla);
		
		mContext = this;
		// Ocultamos el action bar
		getSupportActionBar().hide();
		
		edit_nombre_email = (EditText) findViewById(R.id.edit_login_nombre_email);
		edit_password = (EditText) findViewById(R.id.edit_login_pasword);
		boton_aceptar = (Button) findViewById(R.id.but_confirmar_login);
		txt_recordar_contrasenia = (TextView) findViewById(R.id.txt_recordar_contrasenia);
		
		boton_aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Connectivity.isOnline(mContext)){
					if (checkFields()){
						new LogInUser().execute();	
					}		
				}
				else{
					Toast toast = Toast.makeText(mContext, "No dispone de una conexión a internet", Toast.LENGTH_LONG);
					toast.show();
				}	
			}			
		});
		
		txt_recordar_contrasenia.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Mostramos el dialog de pedir email para reenvío de la contraseña
				RememberPasswordDialog rememberPass = new RememberPasswordDialog(mContext);
				rememberPass.setCancelable(false);
				rememberPass.show(); 		
			}
		});
		
		// Cuando escriban algo que desaparezca el mensaje de error (en caso de que exista)
		edit_nombre_email.addTextChangedListener(new TextWatcher() {          
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {                                                
            	edit_nombre_email.setError(null);
            }

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {	
			} 
		});
		
		// Cuando escriban algo que desaparezca el mensaje de error (en caso de que exista)
		edit_password.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				edit_password.setError(null);				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {	
			}
			
			@Override
			public void afterTextChanged(Editable s) {			
			}
		});
		
		progress = new ProgressDialog(this);
	}
	
	
	/**
	 * onDestroy
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	

	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  
	}

	
	/**
	 * onStop
	 */
	@Override
	public void onStop() {
		super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);
	}
	  
	
	/**
	 * Comprueba si todos los campos son correctos
	 * @return True si todos los campos son correctos. False si alguno no lo es.
	 */
	private boolean checkFields(){
		boolean all_correct = true ;
		
		edit_nombre_email.setError(null);
		edit_password.setError(null);
		
		// Nombre/Email vacío
		if (edit_nombre_email.getText().toString().length() == 0){
			edit_nombre_email.setError("Este campo es obligatorio");
			all_correct = false;
		}
		// Nombre/Email menos de 4 caracteres
		if (edit_nombre_email.getText().toString().length() < 4){
			edit_nombre_email.setError("Este campo debe tener al menos 4 caracteres");
			all_correct = false;
		}
		// Password vacío
		if (edit_password.getText().toString().length() == 0){
			edit_password.setError("Este campo es obligatorio");
			all_correct = false;
		}
		// Password menos de 4 caracteres
		if (edit_password.getText().toString().length() < 6){
			edit_password.setError("Este campo debe tener al menos 6 caracteres");
			all_correct = false;
		}

		return all_correct;
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
			login_error = false;
			any_error = false;
			
			// Mostramos el ProgressDialog
			progress.setTitle("Iniciando sesión ...");
			progress.setMessage("Espera, por favor");
			progress.setCancelable(false);
			progress.show();
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "api/v1/user/login/";        
	        
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
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 if (respJSON.has("reason_code")){
	                		 reason_code = Integer.parseInt(respJSON.getString("reason_code"));
		                	 reason = respJSON.getString("reason");
		                	 if (reason_code == 1){ // Fallo de identificación: nombre de usuario no existe
		                		 name_error = reason;
		                		 has_name_error = true;
		                	 }
		                	 else if (reason_code == 2){ // Fallo de identificación: password no existe
		                		 password_error = reason;
		                		 has_password_error = true;
		                	 }
		                	 if (reason_code == 3){ // Fallo de autentificación ((caracteres raros, espacio, longitud...)
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
	                	 // Ha dado error pero no tiene el reason code: mostramos el mensaje de error
	                	 else{
	                		 JSONObject jsonMessageError = new JSONObject(respJSON.getString("reason"));
	                		 loginMessageError = jsonMessageError.getString("error_message");
	                		 login_error = true;
	                	 }    	
	                 }
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	             any_error = true;
				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build()); 
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
				Toast.makeText(getBaseContext(), "Lo sentimos, se ha producido un error", Toast.LENGTH_SHORT).show();
			}
			else{
				// Si no ha habido algún error extraño 
				 if (!login_error){
					if (has_name_error){
						edit_nombre_email.setError(name_error);
					}
					if (has_password_error){
						edit_password.setError(password_error);
					}
					// Se ha logueado correctamente
					if (!has_name_error && !has_password_error){
						SharedPreferences prefs = getBaseContext().getSharedPreferences(
			        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
			        	prefs.edit().putString(MyApplication.USER_URI, user_uri).commit();
			        	MyApplication.resource_uri = user_uri;
			        	MyApplication.logged_user = true;
			        	Toast.makeText(getBaseContext(), "Logueado correctamente", Toast.LENGTH_SHORT).show();
			        	
			        	// Le indicamos a la anterior actividad que ha habido éxito en el log in
			        	setResult(Activity.RESULT_OK);
			        	finish();
					}	
				}
				
				// Ha habido algún error extraño: mostramos el mensaje
				else{
		        	Toast.makeText(getBaseContext(), loginMessageError, Toast.LENGTH_SHORT)
					.show();
				}
			}								
	    }
	}
	
}
