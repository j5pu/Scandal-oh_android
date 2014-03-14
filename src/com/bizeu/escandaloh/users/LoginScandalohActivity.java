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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MyApplication;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.bizeu.escandaloh.RememberPasswordDialog;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

public class LoginScandalohActivity extends SherlockActivity {
	
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
		setContentView(R.layout.login_scandaloh);
		
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
					Toast toast = Toast.makeText(mContext, R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
					toast.show();
				}	
			}			
		});
		
		txt_recordar_contrasenia.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Mostramos el dialog de pedir email para reenv�o de la contrase�a
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
	 * Loguea un usuario
	 * @author Alejandro
	 *
	 */
	private class LogInUser extends AsyncTask<Void,Integer,Void> {
		 
		private String avatar;
		private String username;
		private String session_token;
		
		@Override
		protected void onPreExecute(){
			has_name_error = false;
			has_password_error = false;
			login_error = false;
			any_error = false;
			
			// Mostramos el ProgressDialog
			progress.setTitle(R.string.iniciando_sesion);
			progress.setMessage(getResources().getString(R.string.espera_por_favor));
			progress.setCancelable(false);
			progress.show();
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/login/";        
	        
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
	                 Log.i("LOGIN","login: " + response_str);
	                 // Obtenemos el json devuelto
	                 JSONObject respJSON = new JSONObject(response_str);
	                 
	                 // Comprobamos el campo status del json
	                 status = respJSON.getString("status");  
	                 
	                 // Si es OK obtenemos el user_uri
	                 if (status.equals("ok")){
	                	// user_uri = respJSON.getString("user_uri");
	                	 avatar = respJSON.getString("avatar");
	                	 username = respJSON.getString("username");
	                	 session_token = respJSON.getString("session_token");
	                 }
	                 // Si no es OK obtenemos la raz�n
	                 else if (status.equals("error")){
	                	 if (respJSON.has("reason_code")){
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
		                			 JSONArray name_errors = jsonReason.getJSONArray("username_email");
		                			 name_error = name_errors.getString(0);
		                			// name_error = jsonReason.getString("username_email");
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
			
			// Si hubo alg�n error mostramos un mensaje
			if (any_error){
				Toast.makeText(getBaseContext(), R.string.lo_sentimos_se_ha_producido, Toast.LENGTH_SHORT).show();
			}
			else{
				// Si no ha habido alg�n error extra�o 
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
						// Guardamos el session_token
			        	prefs.edit().putString(MyApplication.SESSION_TOKEN, session_token);
			        	MyApplication.session_token = session_token;
			        	// Guardamos el nombre de usuario
			        	prefs.edit().putString(MyApplication.USER_NAME, username);
			        	MyApplication.user_name = username ;
			        	// Guardamos su avatar
			        	prefs.edit().putString(MyApplication.AVATAR, avatar);
			        	prefs.edit().commit();
			        	MyApplication.avatar = avatar;
			        	// Indicamos que est� logueado
			        	MyApplication.logged_user = true;
			        	Toast.makeText(getBaseContext(), R.string.sesion_iniciada_exito, Toast.LENGTH_SHORT).show();
			        	
			        	// Debemos reiniciar los esc�ndalos
			        	MyApplication.reset_scandals = true;
			        	
			        	// Le indicamos a la anterior actividad que ha habido �xito en el log in
			        	setResult(Activity.RESULT_OK);
			        	finish();
					}	
				}
				
				// Ha habido alg�n error extra�o: mostramos el mensaje
				else{
		        	Toast.makeText(getBaseContext(), loginMessageError, Toast.LENGTH_SHORT)
					.show();
				}
			}								
	    }
	}
	
	
	
	// ---------------------------------------------------------------------------------------------------------
	// ----------------------             M�TODOS PRIVADOS             -----------------------------------------
	// ---------------------------------------------------------------------------------------------------------
	
	/**
	 * Comprueba si todos los campos son correctos
	 * @return True si todos los campos son correctos. False si alguno no lo es.
	 */
	private boolean checkFields(){
		boolean all_correct = true ;
		
		edit_nombre_email.setError(null);
		edit_password.setError(null);
		
		// Nombre/Email menos de 4 caracteres
		if (edit_nombre_email.getText().toString().length() < 4){
			edit_nombre_email.setError(getResources().getString(R.string.este_campo_debe_tener_4_caracteres));
			all_correct = false;
		}
		
		// Nombre/Email con espacio en blanco
		Pattern pattern = Pattern.compile("\\s");
		Matcher m = pattern.matcher(edit_nombre_email.getText().toString());
		if (m.find()){
			edit_nombre_email.setError(getResources().getString(R.string.este_campo_no_permite_espacios));
			all_correct = false;
		}
		
		// Nombre/Email vac�o
		if (edit_nombre_email.getText().toString().length() == 0){
			edit_nombre_email.setError(getResources().getString(R.string.este_campo_es_obligatorio));
			all_correct = false;
		}

		// Password vac�o
		if (edit_password.getText().toString().length() == 0){
			edit_password.setError(getResources().getString(R.string.este_campo_es_obligatorio));
			all_correct = false;
		}
		// Password menos de 4 caracteres
		if (edit_password.getText().toString().length() < 6){
			edit_password.setError(getResources().getString(R.string.este_campo_debe_tener_6));
			all_correct = false;
		}

		return all_correct;
	}
}
