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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MainActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.util.Fuente;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

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
	private Context mContext;
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_login);
		
		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup)findViewById(R.id.lay_pantalla_main_login));
		
		login_facebook_pulsado = false;
		acti = this;
		mContext = this;
		
		boolean first_time = false;
		
		if (getIntent() != null) {
			first_time = getIntent().getBooleanExtra(MainActivity.FIRST_TIME, false);
		}
		
		// Si estamos en esta pantalla porque el usuario puls� "+": mostramos un mensaje
		if (!first_time){ 
			Toast toast = Toast.makeText(acti, "Reg�strate o inicia sesi�n para agregar contenidos", 2600);
			toast.show();
		}
		
		
		progress = new ProgressDialog(this);
		progress.setTitle("Iniciando sesi�n ...");
		progress.setMessage("Espere, por favor");
		progress.setCancelable(false);
		
			
		// Ocultamos el action bar
		getSupportActionBar().hide();
		
		txt_pasar = (TextView) findViewById(R.id.txt_pasar_registro);
		but_registro = (Button) findViewById(R.id.but_registro_usuario);
		but_login = (Button) findViewById(R.id.but_log_in);
	
		but_entra_facebook = (Button) findViewById(R.id.but_enra_facebook);
		
		// Ponemos el icono de facebook en el bot�n
		Drawable drawable = getResources().getDrawable(R.drawable.logo_facebook);
		drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*0.8), 
		                         (int)(drawable.getIntrinsicHeight()*0.8));
		ScaleDrawable sd = new ScaleDrawable(drawable, 0, 30, 30);
		but_entra_facebook.setCompoundDrawables(sd.getDrawable(), null, null, null); //set drawableLeft for example
		
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
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Iniciar sesi�n con la aplicaci�n",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				// Mostramos la pantalla de log in
				Intent i = new Intent(getBaseContext(), LoginActivity.class);
				startActivityForResult(i, LOG_IN);	
			}
		});
		
	
		but_entra_facebook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Iniciar sesi�n con Facebook",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				if (!login_facebook_pulsado){
					// Mostramos el progress bar (s�lo desaparecer� en alg�n caso de error o al tener �xito con el login)
					progress.show();
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
						     						progress.dismiss();
						     				        Toast.makeText(getBaseContext(), "Lo sentimos, se ha producido un error", Toast.LENGTH_SHORT).show();
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
						     						progress.dismiss();
						     				        Toast.makeText(getBaseContext(), "Lo sentimos, se ha producido un error", Toast.LENGTH_SHORT).show();
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
		});
		
		txt_pasar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acci�n UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Saltar desde pantalla de login",   // Event label
				                   null)            // Event value
				      .build()
				  );
				
				// Cerramos esta pantalla
				finish();	
			}
		});
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
			     						progress.dismiss();
			     				        Toast.makeText(getBaseContext(), "Lo sentimos, se ha producido un error", Toast.LENGTH_SHORT).show();
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
			
			// Si no ha habido alg�n error extra�o 
			if (!login_error){
				SharedPreferences prefs = getBaseContext().getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		        prefs.edit().putString(MyApplication.USER_URI, user_uri).commit();
		        MyApplication.resource_uri = user_uri;
		        MyApplication.logged_user = true;
		        Toast.makeText(getBaseContext(), "Logueado correctamente", Toast.LENGTH_SHORT).show();
		        	
		        // Le indicamos a la anterior actividad que ha habido �xito en el log in
		        setResult(Activity.RESULT_OK);
		        finish();					
			}
			
			// Ha habido alg�n error extra�o: mostramos el mensaje
			else{
	        	Toast.makeText(getBaseContext(), "Lo sentimos, se ha producido un error", Toast.LENGTH_SHORT)
				.show();
			}
						
	    }
	}
	
  
	/**
	 * Limita un string a 22 caracteres + tres puntos suspensivos
	 * @param completo String oritinal
	 * @return String con un tama�o m�ximo de 25 caracteres
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
