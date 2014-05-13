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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MyApplication;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.parse.ParseInstallation;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.flurry.android.FlurryAgent;

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
	private ProgressDialog progress;
	private Context mContext;
	private boolean any_error;

	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup)findViewById(R.id.lay_pantalla_registration));
		
		mContext = this;
		
		// Ocultamos el action bar
		getSupportActionBar().hide();
		
		edit_nombre_usuario = (EditText) findViewById(R.id.edit_registro_nombre);
		edit_password_usuario = (EditText) findViewById(R.id.edit_registro_password);
		edit_email_usuario = (EditText) findViewById(R.id.edit_registro_email);
		aceptar = (Button) findViewById(R.id.but_confirmar_registro);
		
		aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (Connectivity.isOnline(mContext)){
					
					// Si todos los campos son correctos hacemos la petición de registro
					if (checkFields()){
						
						// Pedimos registro si tenemos device token
						if (ParseInstallation.getCurrentInstallation().getString("deviceToken") != null){
							new SignInUser().execute();	
						}
						
						// Si no mostramos un dialog pidiendo añadir una cuenta de Google
						else{
							AlertDialog.Builder alert_accounts = new AlertDialog.Builder(mContext);
							alert_accounts.setTitle(R.string.debes_tener_una_cuenta_de_google);
							alert_accounts.setMessage(R.string.quieres_aniadir_una);
							alert_accounts.setPositiveButton(R.string.confirmar,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialogo1, int id) {
											
											// Intent hacia pantalla de cuentas
											Intent i_accounts = new Intent(Settings.ACTION_ADD_ACCOUNT);
											// Si es API 18+ le llevamos directamente a la pantalla de cuenta de Google
											if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
												i_accounts.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[] {"com.google"});
											}
											PackageManager pm = mContext.getPackageManager();
											
											// Si es nulo es que no hay actividad para dicho intent (el dispositivo no lo acepta)
											if (pm.resolveActivity(i_accounts, PackageManager.MATCH_DEFAULT_ONLY) != null){
												// Usamos ACTION_ADD_ACCOUNT
												startActivity(i_accounts);
											}
											else{
												// Usamos ACTION_SETTINGS
												startActivity(new Intent(Settings.ACTION_SETTINGS));

											}	
										}
									});
							alert_accounts.setNegativeButton(R.string.cancelar,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialogo1, int id) {
										}
									});
							alert_accounts.show();
						}
					}
				}
				else{
					Toast toast = Toast.makeText(mContext, R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
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
	 * onStart
	 */
	@Override
	public void onStart() {
	    super.onStart();
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
	}

	
	
	/**
	 * onStop
	 */
	@Override
	public void onStop() {
	    super.onStop();
		// Paramos Flurry
		FlurryAgent.onEndSession(mContext);
	}
	
	
	
	
	
	/**
	 * Registra un usuario
	 * @author Alejandro
	 *
	 */
	private class SignInUser extends AsyncTask<Void,Integer,Void> {
		 	
		private String session_token;
		private String device_token;
		
		@Override
		protected void onPreExecute(){
			has_name_error = false;
			has_password_error = false;
			has_email_error = false;
			edit_nombre_usuario.setError(null);
			edit_password_usuario.setError(null);
			edit_email_usuario.setError(null);
			
			// Mostramos el ProgressDialog
			progress.setTitle(R.string.registrando_usuario);
			progress.setMessage(getResources().getString(R.string.espera_por_favor));
			progress.setCancelable(false);
			progress.show();	
			
			// Reseteamos
			any_error = false;
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/";
	
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();	              
	             
	             String username = edit_nombre_usuario.getText().toString();
	             String password = edit_password_usuario.getText().toString();
	             String email = edit_email_usuario.getText().toString();
	             
	             // Obtenemos el device token de parse
	             device_token = ParseInstallation.getCurrentInstallation().getString("deviceToken");
	             dato.put("device_token", device_token);
	             dato.put("device_type", 2); // Soy dispositivo android
	             dato.put("username", username);
	             dato.put("password", password);
	             dato.put("email", email);

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
	                	 session_token = respJSON.getString("session_token");
	                 }
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 
	                	 JSONObject jsonReason = new JSONObject(respJSON.getString("reason"));
	                	 if (jsonReason.has("username")){
	                		 JSONArray jsonUserNameErrors = new JSONArray(jsonReason.getString("username"));
	                		 name_error = (String) jsonUserNameErrors.get(0);
	                		 has_name_error = true;
	                	 }
	                	 
	                	 if (jsonReason.has("password")){
	                		 JSONArray jsonPasswordErrors = (JSONArray) jsonReason.get("password");
	                		 password_error = (String) jsonPasswordErrors.get(0);
	                		 has_password_error = true;	                		 
	                	 }
	                	 
	                	 if (jsonReason.has("email")){
	                		 JSONArray jsonEmailErrors = new JSONArray(jsonReason.getString("email"));
	                		 email_error = (String) jsonEmailErrors.get(0);
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
				Toast.makeText(getBaseContext(), R.string.lo_sentimos_se_ha_producido, Toast.LENGTH_SHORT).show();			
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
					SharedPreferences.Editor editor = prefs.edit();
					// Guardamos el session token 
					editor.putString(MyApplication.SESSION_TOKEN, session_token);
		        	MyApplication.session_token = session_token;
		        	// Guardamos el nombre de usuario
		        	editor.putString(MyApplication.USER_NAME, edit_nombre_usuario.getText().toString());
		        	MyApplication.user_name = edit_nombre_usuario.getText().toString();
		        	// Indicamos que es usuario de scandaloh
		        	editor.putInt(MyApplication.SOCIAL_NETWORK, 0);
		        	MyApplication.social_network = 0;
		        	// Indicamos que está logueado
		        	MyApplication.logged_user = true;
		        	Toast.makeText(getBaseContext(), R.string.usuario_registrado_correctamente, Toast.LENGTH_SHORT).show();
		        	
		        	editor.commit();
		        	
		        	// Debemos reiniciar los escándalos
		        	MyApplication.reset_scandals = true;
		        	
		        	// Le indicamos a la anterior actividad que ha habido éxito en el registro
		        	setResult(Activity.RESULT_OK);
		        	finish();
				}	
			}				
	    }
	}
	
	
	
	// ----------------------------------------------------------------------------------------------------------
	// ---------------------------        MÉTODOS PRIVADOS       ------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------
	

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
			edit_nombre_usuario.setError(getResources().getString(R.string.este_campo_debe_tener_4_caracteres));
			all_correct = false;
		}
		
		// Nombre/Email con espacio en blanco
		Pattern pattern = Pattern.compile("\\s");
		Matcher m = pattern.matcher(edit_nombre_usuario.getText().toString());
		if (m.find()){
			edit_nombre_usuario.setError(getResources().getString(R.string.este_campo_no_permite_espacios));
			all_correct = false;
		}
			
		// Nombre vacío
		if (edit_nombre_usuario.getText().toString().length() == 0){
			edit_nombre_usuario.setError(getResources().getString(R.string.este_campo_es_obligatorio));
			all_correct = false;
		}
		// Email incorrecto
		if (!isEmailValid(edit_email_usuario.getText().toString())){
			edit_email_usuario.setError(getResources().getString(R.string.introduce_una_direccion_email_valida));
			all_correct = false;
		}	
		// Email vacío
		if (edit_email_usuario.getText().toString().length() == 0){
			edit_email_usuario.setError(getResources().getString(R.string.este_campo_es_obligatorio));
			all_correct = false;
		}
		// Password menos de 4 caracteres
		if (edit_password_usuario.getText().toString().length() < 6){
			edit_password_usuario.setError(getResources().getString(R.string.este_campo_debe_tener_6));
			all_correct = false;
		}
		// Password vacío
		if (edit_password_usuario.getText().toString().length() == 0){
			edit_password_usuario.setError(getResources().getString(R.string.este_campo_es_obligatorio));
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
	
}

