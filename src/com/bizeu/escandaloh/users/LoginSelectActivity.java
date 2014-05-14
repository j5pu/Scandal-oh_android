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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
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
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.parse.ParseInstallation;

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
	private String shared;
	private int sharing_type;
	private String device_token;

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_main);
		
		// Si tiene datos, obtenemos si viene de haber compartido desde la galería
		if (getIntent().getExtras() != null){
			shared = getIntent().getExtras().getString("shareUri");
			sharing_type = getIntent().getExtras().getInt("photo_from");
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
				startActivityForResult(i,REGISTRATION);	
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
				progress.show();
				// Sesión abierta
				if (session.isOpened()) {
					access_token = session.getAccessToken();
					Request.newMeRequest(session,new Request.GraphUserCallback() {
								@Override
								public void onCompleted(GraphUser user,Response response) {
									
									if (user != null) {									
										username = user.getUsername();
										new LogInSocialNetwork().execute(LOGGING_FACEBOOK);
									}
									else{
										if (progress.isShowing()) {
											progress.dismiss();
										}			
										Toast.makeText(mContext, R.string.lo_sentimos_se_ha_producido,Toast.LENGTH_SHORT).show();
									}
								}
							}).executeAsync();				
				}
				
				// Sesión cerrada
				else {
					// Si existe device_token
					if (device_token != null){
						// Si el usuario está logueado 
						if (MyApplication.logged_user || login_error){
							finish();
						}
					}
					
					// Quitamos el loading
					if (progress.isShowing()) {
						progress.dismiss();
					}

				}

			}
		});

		but_login_scandaloh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

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
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Si viene de hacer log in o registro
		if (requestCode == LOG_IN || requestCode == REGISTRATION) {
			// Y lo ha hecho exitosamente
			if (resultCode == RESULT_OK){ 
				// Si venía de compartir y ha hecho login le mandamos a la pantalla de subir escándalo
				if (sharing_type == CoverActivity.FROM_SHARING_PICTURE || sharing_type == CoverActivity.FROM_SHARING_TEXT){
					Intent in = new Intent(LoginSelectActivity.this, CreateScandalohActivity.class);
					in.putExtra("photo_from", sharing_type);
					in.putExtra("shareUri", shared);
					startActivity(in);
				}
				// Cerramos directamente la pantalla
				finish();
			}
			
		} 
		else {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
				}		

				// Obtenemos el device token de parse
	            device_token = ParseInstallation.getCurrentInstallation().getString("deviceToken");
	            dato.put("device_token", device_token);
	            dato.put("device_type", 2);
				dato.put("access_token", access_token);
				dato.put("social_network", social_network);

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

	                 if (device_token == null){
	                	 login_error = true;
                 }				        
					
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
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
						
			// Si no ha habido algún error extraño
			if (!login_error) {
				// Logueamos al usuario en la aplicación
				SharedPreferences prefs = mContext.getSharedPreferences(
						"com.bizeu.escandaloh", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				// Guardamos el session token 
				editor.putString(MyApplication.SESSION_TOKEN, session_token);
				MyApplication.session_token = session_token;
				// Guardamos el nombre de usuario
				editor.putString(MyApplication.USER_NAME, username);
				MyApplication.user_name = username;
				// Guardamos el avatar
				editor.putString(MyApplication.AVATAR, avatar);
				MyApplication.avatar = avatar;
				// Indicamos que es usuario de facebook
				editor.putInt(MyApplication.SOCIAL_NETWORK, 1);
				MyApplication.social_network = 1;	
				// Indicamos que está logueado
				MyApplication.logged_user = true;
				Toast.makeText(mContext, R.string.sesion_iniciada_exito,
						Toast.LENGTH_SHORT).show();
				
				editor.commit();
				
				// Reiniciamos los escándalos
				MyApplication.reset_scandals = true;
				
				// Si venía de compartir y ha hecho login le mandamos a la pantalla de subir escándalo
				if (sharing_type == CoverActivity.FROM_SHARING_PICTURE || sharing_type == CoverActivity.FROM_SHARING_TEXT){
					Intent in = new Intent(LoginSelectActivity.this, CreateScandalohActivity.class);
					in.putExtra("photo_from", sharing_type);
					in.putExtra("shareUri", shared);
					startActivity(in);
				}
				else{
					// Le indicamos a la anterior actividad que ha habido éxito en el login
					setResult(Activity.RESULT_OK);
				}

			}

			// Ha habido algún error extraño: mostramos el mensaje
			else {		
				
				if (device_token  == null){
					// Si el device token es nulo es porque no tiene cuenta de Google: mostramos un dialog
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
				
				else{
					Toast.makeText(getBaseContext(), R.string.lo_sentimos_se_ha_producido, Toast.LENGTH_SHORT).show();
				}
			}
			
			// Cerramos sesión facebook
			Session.getActiveSession().closeAndClearTokenInformation();
		}
	}
}
