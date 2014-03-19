package com.bizeu.escandaloh.users;

import java.util.Arrays;

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
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import com.bizeu.escandaloh.CoverActivity;
import com.bizeu.escandaloh.CreateScandalohActivity;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.util.Fuente;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class LoginSelectActivity extends SherlockActivity {

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
	private String TAG_FACEBOOK = "Facebook Login";
	
	private Button but_login_scandaloh;
	private LoginButton but_login_facebook;
	private TextView txt_crea_tu_cuenta;
	
	private ProgressDialog progress;
	private Activity acti;
	private Context mContext;
	private String username;
	private String status = null;
	private boolean login_error = false;
	private String access_token;
	private Uri shareUri;
	private boolean viene_de_compartir = false;

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_main);
		
		// Si tiene datos, obtenemos si viene de haber compartido desde la galería
		if (getIntent().getExtras() != null){
			viene_de_compartir = getIntent().getExtras().getBoolean("from_sharing");	
			shareUri = Uri.parse(getIntent().getExtras().getString("shareUri"));
		}
		
		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.lay_pantalla_main_login));

		acti = this;
		mContext = this;

		progress = new ProgressDialog(mContext);
		progress.setTitle(R.string.iniciando_sesion);
		progress.setMessage(getResources().getString(R.string.espera_por_favor));
		progress.setCancelable(false);

		// Ocultamos el action bar
		getSupportActionBar().hide();

		but_login_scandaloh = (Button) findViewById(R.id.but_log_in_scandaloh);
		but_login_facebook = (LoginButton) findViewById(R.id.but_log_in_facebook);
		txt_crea_tu_cuenta = (TextView) findViewById(R.id.txt_loginmain_register);
		
		// Subrayamos el TextView
		txt_crea_tu_cuenta.setPaintFlags(txt_crea_tu_cuenta.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		txt_crea_tu_cuenta.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginSelectActivity.this, RegistrationActivity.class);
				startActivity(i);	
			}
		});

		but_login_facebook.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(FacebookException error) {
				Log.i(TAG_FACEBOOK, "Error " + error.getMessage());
			}
		});

		but_login_facebook.setReadPermissions(Arrays.asList("basic_info", "email"));
		// Callback de cuando cambia el estado de la sesión
		but_login_facebook.setSessionStatusCallback(new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state,
					Exception exception) {

				if (session.isOpened()) {
					Log.i(TAG_FACEBOOK, "Access Token" + session.getAccessToken());
					access_token = session.getAccessToken();
					Request.newMeRequest(session,new Request.GraphUserCallback() {
								@Override
								public void onCompleted(GraphUser user,Response response) {
									if (user != null) {
										
										username = user.getUsername();
										// Cerramos sesión facebook: sólo queremos el nombre e email
										Session.getActiveSession().closeAndClearTokenInformation();
										new LogInSocialNetwork().execute(LOGGING_FACEBOOK);
									}
								}
							}).executeAsync();
					
				} else {
					Log.i("WE", "Sesion no abierta");
				}

			}
		});

		but_login_scandaloh.setOnClickListener(new View.OnClickListener() {

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
				Intent i = new Intent(mContext, LoginScandalohActivity.class);
				acti.startActivityForResult(i, LoginSelectActivity.LOG_IN);
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
		// Si viene de hacer log in o registro
		if (requestCode == LOG_IN || requestCode == REGISTRATION) {
			// Y lo ha hecho exitosamente
			if (resultCode == RESULT_OK){ 
				// Si venía de compartir y ha hecho login le mandamos a la pantalla de subir escándalo
				if (viene_de_compartir){
					Intent in = new Intent(LoginSelectActivity.this, CreateScandalohActivity.class);
					in.putExtra("photo_from", CoverActivity.FROM_SHARING);
					in.putExtra("shareUri", shareUri.toString());
					startActivity(in);
				}
				// Cerramos directamente la pantalla
				finish();
			}
		} else {
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}
	}




	/**
	 * Loguea un usuario a partir de un nombre de usuario (obtenido de facebook,
	 * twitter o google+)
	 * 
	 */
	private class LogInSocialNetwork extends AsyncTask<Integer, Integer, Void> {

		private String avatar;
		private int social_network;
		private String session_token;

		@Override
		protected void onPreExecute() {
			progress.show();
			login_error = false;
		}

		@Override
		protected Void doInBackground(Integer... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS
					+ "/api/v1/user/login/";

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Content-Type", "application/json");

				JSONObject dato = new JSONObject();

				// Indicamos en qué red social hacer login
				switch (params[0]) {
					case LOGGING_FACEBOOK:
						social_network = 1;
						break;
					/*
					case LOGGING_GOOGLE:
						social_network = 2;
						email = "google@email.com";
						break;
					case LOGGING_TWITTER:
						social_network = 3;
						email = "twitter@email.com";
						break;
					*/
				}		
				
				Log.v("WE","acceso token: " + access_token);
				dato.put("access_token", access_token);
				dato.put("social_network", social_network);
				dato.put("device_token", "");

				// Creamos el StringEntity como UTF-8 (Caracteres ñ,á, ...)
				StringEntity entity = new StringEntity(dato.toString(),
						HTTP.UTF_8);
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
						//user_uri = respJSON.getString("user_uri");
						avatar = respJSON.getString("avatar");
						session_token = respJSON.getString("session_token");
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
										.getName(), // The name of the thread on
													// which the exception
													// occurred
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
				SharedPreferences.Editor editor = prefs.edit();
				// Guardamos el session_token, nombre de usuario y avatar
				editor.putString(MyApplication.SESSION_TOKEN, session_token);
				Log.v("WE","primer session token: " + session_token);
				editor.putString(MyApplication.USER_NAME, username);
				editor.putString(MyApplication.AVATAR, avatar);
				editor.commit();
				Log.v("WE", "Avatar de facebook: " + avatar);
				MyApplication.user_name = username;
				MyApplication.session_token = session_token;
				MyApplication.avatar = avatar;
				MyApplication.logged_user = true;
				Toast.makeText(mContext, R.string.sesion_iniciada_exito,
						Toast.LENGTH_SHORT).show();
				
				// Reiniciamos los escándalos
				MyApplication.reset_scandals = true;
				
				// Si venía de compartir y ha hecho login le mandamos a la pantalla de subir escándalo
				if (viene_de_compartir){
					Intent in = new Intent(LoginSelectActivity.this, CreateScandalohActivity.class);
					in.putExtra("photo_from", CoverActivity.FROM_SHARING);
					in.putExtra("shareUri", shareUri.toString());
					startActivity(in);
				}
				else{
					// Le indicamos a la anterior actividad que ha habido éxito en el login
					setResult(Activity.RESULT_OK);
				}

				finish();
			}

			// Ha habido algún error extraño: mostramos el mensaje
			else {
				Toast.makeText(mContext, R.string.lo_sentimos_se_ha_producido,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
