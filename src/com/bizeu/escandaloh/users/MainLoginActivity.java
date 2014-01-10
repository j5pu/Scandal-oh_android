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

import twitter4j.AccountSettings;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.bizeu.escandaloh.MainActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.bizeu.escandaloh.users.LoginDialog.OnMyDialogResult;
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
	public final static int LOGGING_FACEBOOK = 101;
	public final static int LOGGING_GOOGLE = 102;
	public final static int LOGGING_TWITTER = 103;
	static String TWITTER_CONSUMER_KEY = "MJb4bXehocnroOE871Y6g";
	static String TWITTER_CONSUMER_SECRET = "ENQygTJn0zldtPTdjVl15jXAQbuBvjsPwoP7a7bg";
	static final String TWITTER_CALLBACK_URL = "twitter://scandaloh";
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	
	private static Twitter twitter;
	private static RequestToken requestToken;
	 
	
	private TextView txt_pasar;
	private Button but_registro;
	private Button but_login;
	private Button but_login_redes_sociales;
	private Activity acti;
	private Context mContext;
	private ProgressDialog progress;
	
	private String username;
	private String email;
	private String status = null;
	private boolean login_error = false;
	private String user_uri;
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_login);
		
		
		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup)findViewById(R.id.lay_pantalla_main_login));
		
		acti = this;
		mContext = this;
		
		progress = new ProgressDialog(mContext);
		progress.setTitle("Iniciando sesión ...");
		progress.setMessage("Espera, por favor");
		progress.setCancelable(false);
	
		// Si estamos en esta pantalla porque el usuario pulsó "+": mostramos un mensaje
		boolean first_time = false;
		
		if (getIntent() != null) {
			first_time = getIntent().getBooleanExtra(MainActivity.FIRST_TIME, false);
		}
	
		if (!first_time){ 
			Toast toast = Toast.makeText(acti, "Regístrate o inicia sesión para agregar contenidos", 2600);
			toast.show();
		}
				
		// Ocultamos el action bar
		getSupportActionBar().hide();
		
		txt_pasar = (TextView) findViewById(R.id.txt_pasar_registro);
		but_registro = (Button) findViewById(R.id.but_registro_usuario);
		but_login = (Button) findViewById(R.id.but_log_in);
		but_login_redes_sociales = (Button) findViewById(R.id.but_log_in_redes);
		
	
		but_registro.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent i = new Intent(getBaseContext(), RegistrationActivity.class);
				startActivityForResult(i, REGISTRATION);		
			}
		});
		
		but_login_redes_sociales.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Mostramos el dialog para seleccionar la red social
				LoginDialog login_dialog = new LoginDialog(mContext, acti);
				login_dialog.setDialogResult(new OnMyDialogResult(){
					
				    public void finish(String result){    	
				       // FACEBOOK
				       if (result.equals("FACEBOOK")){			       
				    	   // Hacemos loguin con facebook
				    	   Session.openActiveSession(acti, true, new Session.StatusCallback() {
				    	        		
				    		   // Callback de cuando cambia el estado de la sesión
				    		   @Override
				    		   public void call(Session session, SessionState state, Exception exception) {

				    			   if (session.isOpened()) {
				    				   // Obtenemos el nombre de usuario
				    				   Request.newMeRequest(session, new Request.GraphUserCallback() {
				    					   @Override
				    					   public void onCompleted(GraphUser user, Response response) {
				    						   if (user != null) {
				    							   username = limitaCaracteres( user.getUsername());
				    							   new LogInSocialNetwork().execute(LOGGING_FACEBOOK);
				    						   }
				    					   }
				    				   }).executeAsync();
				    			   }
				    		   }
				    	   }); 
				       }
				       
				       // TWITTER
				       else if (result.equals("TWITTER")){	  

				    	   new InitiateWebViewTwitter().execute();
	    	 
				       }
				    }
				});
				login_dialog.show();		
			}
		});
		
		but_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Mandamos el evento a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createEvent("Acción UI", 
						"Boton clickeado", // Event action (required)
						"Iniciar sesión con scandaloh", // Event label
						null) // Event value
						.build());

				// Mostramos la pantalla de log in
				Intent i = new Intent(mContext, LoginActivity.class);
				acti.startActivityForResult(i, MainLoginActivity.LOG_IN);		
			}
		});
		
		txt_pasar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acción UI",     // Event category (required)
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
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		// Si viene de hacer log in o registro
		
		if (requestCode == LOG_IN || requestCode == REGISTRATION) {
			// Y lo ha hecho exitosamente
			if (resultCode == RESULT_OK) {
				// Cerramos directamente la pantalla
				finish();					
			}		 
		}	
	}
	
	
	
    /**
     * Este método es lanzado cuando se recibe un nuevo Intent. 
     * En nuestro caso cuando el usuario se haya logueado con twitter en el webview
     */
    @Override
    protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            
            // Obtenemos la uri devuelta en el intent
            Uri uri = intent.getData();
            
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {     
                // Logueamos con twitter a partir del token devuelto
                new LogInTwitter().execute(uri);
            }          
    }
	
	
	
	
	
	/**
	 * Abre un webView para pedir el email y password en twitter de un usuario
	 * 
	 */
	private class InitiateWebViewTwitter extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			// Mostramos el progress
			progress.show();
			
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			   ConfigurationBuilder builder = new ConfigurationBuilder();
			   builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			   builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			   Configuration configuration = builder.build();
			     
			   TwitterFactory factory = new TwitterFactory(configuration);
			   twitter = factory.getInstance();
			   
			   try {
				   // Obtenemos el requestToken
			       requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
			       // Lanzamos el webView para iniciar sesión con twitter
			       startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
			   } catch (TwitterException e) {
					// Mandamos la excepcion a Google Analytics
					EasyTracker easyTracker = EasyTracker.getInstance(mContext);
					easyTracker.send(MapBuilder.createException(
							new StandardExceptionParser(mContext, null) 
									.getDescription(Thread.currentThread()
											.getName(), // The name of the thread on which the exception occurred
											e), // The exception.
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
		}
	}

	
	
	
	/**
	 * Obtiene el nombre de usuario de Twitter y loguea en la aplicación
	 * 
	 */
	private class LogInTwitter extends AsyncTask<Uri, Integer, Void> {

		@Override
		protected void onPreExecute() {
			login_error = false;
			// Mostramos el progress
			progress.show();
		}
		
		@Override
		protected Void doInBackground(Uri... params) {

			try {
				// Obtenemos el oAuth verifier
			    String verifier = params[0].getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
			    
			    // Obtenemos el access token a partir del oAuth verifier
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                // Obtenemos el nombre de usuario a partir del access token
                long userID = accessToken.getUserId();
                User user = twitter.showUser(userID);
                username = limitaCaracteres(user.getScreenName());
			  
			} catch (TwitterException ex) {
				login_error = true;
				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) 
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on which the exception occurred
										ex), // The exception.
						false).build());
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			if (!login_error){
				// Hacemos finalmente login en la aplicación
				new LogInSocialNetwork().execute(LOGGING_TWITTER);
			}
			else{
				// Quitamos el ProgressDialog
				if (progress.isShowing()) {
					progress.dismiss();
				}
				Toast.makeText(mContext,"Lo sentimos, se ha producido un error",Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	
	
	/**
	 * Loguea un usuario a partir de un nombre de usuario (obtenido de facebook, twitter o google+)
	 * 
	 */
	private class LogInSocialNetwork extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected void onPreExecute() {
			progress.show();
			login_error = false;
		}

		@Override
		protected Void doInBackground(Integer... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS+ "api/v1/user/login/";

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Content-Type", "application/json");

				JSONObject dato = new JSONObject();

				Log.v("WE", "user name es: " + username);
				dato.put("username", username);
				dato.put("email", "redsocial@ejemplo.com");
				
				// Indicamos en qué red social hacer login
				switch(params[0]){
				case LOGGING_FACEBOOK:
					dato.put("social_network", 1);
					break;
				case LOGGING_GOOGLE:
					dato.put("social_network", 2);
					break;
				case LOGGING_TWITTER:
					dato.put("social_network", 3);
					break;
				}

				// Creamos el StringEntity como UTF-8 (Caracteres ñ,á, ...)
				StringEntity entity = new StringEntity(dato.toString(),HTTP.UTF_8);
				post.setEntity(entity);

				HttpResponse response = client.execute(post);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);

				if (resEntity != null) {
					Log.i("RESPONSE", response_str);
					// Obtenemos el json devuelto
					JSONObject respJSON = new JSONObject(response_str);

					// Comprobamos el campo status del json
					status = respJSON.getString("status");

					// Si es OK obtenemos el user_uri
					if (status.equals("ok")) {
						user_uri = respJSON.getString("user_uri");
						login_error = false;
					} else {
						login_error = true;
					}
				}
			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				login_error = true;
				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) 
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on which the exception occurred
										ex), // The exception.
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

			// Si no ha habido algún error extraño
			if (!login_error) {
				// Logueamos al usuario en la aplicación
				SharedPreferences prefs = mContext.getSharedPreferences(
						"com.bizeu.escandaloh", Context.MODE_PRIVATE);
				prefs.edit().putString(MyApplication.USER_URI, user_uri)
						.commit();
				MyApplication.resource_uri = user_uri;
				MyApplication.logged_user = true;
				Toast.makeText(mContext, "Sesión iniciada con éxito",
						Toast.LENGTH_SHORT).show();

				// Le indicamos a la anterior actividad que ha habido éxito en el login
				 setResult(Activity.RESULT_OK);
				 finish();
			}

			// Ha habido algún error extraño: mostramos el mensaje
			else {
				Toast.makeText(mContext,"Lo sentimos, se ha producido un error",Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/**
	 * Limita un string a 22 caracteres + tres puntos suspensivos
	 * 
	 * @param completo
	 *            String oritinal
	 * @return String con un tamaño máximo de 25 caracteres
	 */
	private String limitaCaracteres(String completo) {
		String acortado = null;
		if (completo.length() > 25) {
			acortado = completo.substring(0, 22) + "...";
		} else {
			acortado = completo;
		}

		return acortado;
	}
	
}
