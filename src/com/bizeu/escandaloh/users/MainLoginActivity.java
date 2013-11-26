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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class MainLoginActivity extends SherlockActivity{
	
	public static int LOG_IN = 1;
	public static int REGISTRATION = 2;
	public static int LOG_FACEBOOK = 3;
	
	private TextView txt_pasar;
	private Button but_registro;
	private Button but_login;
	private Button but_entra_facebook;	
	private ProgressDialog progress;
	private String status = null;
	private boolean login_error = false;
	private String user_uri;
	private String username;
	private String email;
	private boolean login_facebook_pulsado ;
	
	private Activity acti;
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_login);
		
		login_facebook_pulsado = false;
		acti = this;
		
		
		// Ocultamos el action bar
		getSupportActionBar().hide();
		
		txt_pasar = (TextView) findViewById(R.id.txt_pasar_registro);
		but_registro = (Button) findViewById(R.id.but_registro_usuario);
		but_login = (Button) findViewById(R.id.but_log_in);
		but_entra_facebook = (Button) findViewById(R.id.but_enra_facebook);
		
		but_registro.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), RegistrationActivity.class);
				startActivityForResult(i, REGISTRATION);		
			}
		});
		
		but_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Mostramos la pantalla de log in
				Intent i = new Intent(getBaseContext(), LoginActivity.class);
				startActivityForResult(i, LOG_IN);	
			}
		});
		
	
		but_entra_facebook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!login_facebook_pulsado){
					login_facebook_pulsado = true;
					 Session currentSession = Session.getActiveSession();
				        if (currentSession == null || currentSession.getState().isClosed()) {
				        	Log.v("WE","nula os isClosed");
				            Session session = new Session.Builder(acti).build();
				            Session.setActiveSession(session);
				            currentSession = session;
				            
				            Session.OpenRequest or = new Session.OpenRequest(acti);
					        or.setPermissions("email");
					        
					        Session se = new Session(acti);
					        se.addCallback(new Session.StatusCallback() {

						          // Callback when session changes state
						          @Override
						          public void call(Session session, SessionState state, Exception exception) {
						            if (session.isOpened()) {
						            	 Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
						     	        	
						     				@Override
						     				public void onCompleted(GraphUser user, Response response) {
						     					if (user != null){
			                                        username = limitaCaracteres(user.getUsername());
							     					email = user.getProperty("email").toString();
							     					
							     					 new LogInUserFacebook().execute();
						     					}
						     					else{
						     				        Toast.makeText(getBaseContext(), "Hubo algún problema. Compruebe la conexión", Toast.LENGTH_SHORT).show();
						     					}

						     					
						     				}
						     	        });
						     	        
						     	        re.executeAsync();
						              }
						            }
						        });
					        Session.setActiveSession(se);
					        se.openForRead(or);
				            
				        }
				        
				        if (currentSession.isOpened()) {
				            Session.openActiveSession(acti, true, new Session.StatusCallback() {

					            @Override
					            public void call(final Session session, SessionState state,
					                    Exception exception) {

					                if (session.isOpened()) {
					                	
					                	Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
						     	        	
						     				@Override
						     				public void onCompleted(GraphUser user, Response response) {
						     					if (user != null) {
			                                        email = user.getProperty("email").toString();
			                                        username = limitaCaracteres(user.getUsername());
			                                       
			                                        new LogInUserFacebook().execute();
			                                    }
						     					else{
						     				        Toast.makeText(getBaseContext(), "Hubo algún problema. Compruebe la conexión", Toast.LENGTH_SHORT).show();
						     					}
						     				}
					                	});
					                	re.executeAsync();        
					                }
					            }
					        });

				        } 
				        
						progress = new ProgressDialog(acti);
				}
				 		
			}
		});
		
		txt_pasar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Cerramos esta pantalla
				finish();	
			}
		});
	
	}
	
	
	
	 

	
	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		// Si viene de hacer log in o registro
		if (requestCode == LOG_IN || requestCode == REGISTRATION) {
			// Y lo ha hecho exitosamente
			if (resultCode == RESULT_OK) {
				// Cerramos directamente la pantalla
				finish();					
			}		 
		}	
		// Login con facebook
		else{
			if (Session.getActiveSession() != null){
		        Session.getActiveSession().onActivityResult(this, requestCode,
		                resultCode, data);
		    }

		    Session currentSession = Session.getActiveSession();
		    if (currentSession == null || currentSession.getState().isClosed()) {
		        Session session = new Session.Builder(this).build();
		        Session.setActiveSession(session);
		        currentSession = session;
		    }

		    if (currentSession.isOpened()) {
		        Session.openActiveSession(this, true, new Session.StatusCallback() {

		            @Override
		            public void call(final Session session, SessionState state,
		                    Exception exception) {

		                if (session.isOpened()) {
		                	
		                	Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
			     	        	
			     				@Override
			     				public void onCompleted(GraphUser user, Response response) {
			     					if (user != null) {
                                        username = limitaCaracteres(user.getUsername());
                                        email = user.getProperty("email").toString();
                                        
                                        new LogInUserFacebook().execute();
                                    }
			     					else{
			     				        Toast.makeText(getBaseContext(), "Hubo algún problema. Compruebe la conexión", Toast.LENGTH_SHORT).show();
			     					}
			     				}
		                	});
		                	re.executeAsync();        
		                }
		            }
		        });
		    }
		}
	}
	
	
	

	/**
	 * Loguea un usuario (a partir de datos de facebook)
	 *
	 */
	
	
	private class LogInUserFacebook extends AsyncTask<Void,Integer,Void> {
		 
		@Override
		protected void onPreExecute(){
			login_error = false;
			
			// Mostramos el ProgressDialog
			progress.setTitle("Logueando ...");
			progress.setMessage("Espere, por favor");
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
             
	             Log.v("WE","user name es: " + username);
	             dato.put("username", username);
	             dato.put("email", email);
	             dato.put("social_network", 1);

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
	                	 login_error = false;
	                 }
	                 else{
	                	 login_error = true;
	                 }	             
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	             login_error = true;
	        }
			return null;
	      
	    }
		
		
		@Override
	    protected void onPostExecute(Void result) {
			
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			// Si no ha habido algún error extraño 
			if (!login_error){
				Log.v("WE","El user uri es: " + user_uri);
				SharedPreferences prefs = getBaseContext().getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		        prefs.edit().putString(MyApplication.USER_URI, user_uri).commit();
		        MyApplication.resource_uri = user_uri;
		        MyApplication.logged_user = true;
		        Toast.makeText(getBaseContext(), "Login ok", Toast.LENGTH_SHORT).show();
		        	
		        // Le indicamos a la anterior actividad que ha habido éxito en el log in
		        setResult(Activity.RESULT_OK);
		        finish();					
			}
			
			// Ha habido algún error extraño: mostramos el mensaje
			else{
	        	Toast.makeText(getBaseContext(), "Hubo algún error conectando con facebook. Inténtelo más tarde", Toast.LENGTH_SHORT)
				.show();
			}
						
	    }
	}
	
  
	/**
	 * Limita un string a 22 caracteres + tres puntos suspensivos
	 * @param completo String oritinal
	 * @return String con un tamaño máximo de 25 caracteres
	 */
	private String limitaCaracteres(String completo){
		String acortado = null;
		if (completo.length() > 25){
			acortado = completo.substring(0,22) + "...";		
		}
		else{
			acortado = completo;
		}

		return acortado;
	}
	
	
	

	public void call(Session session, SessionState state, Exception exception) {}
	
	
	
}
