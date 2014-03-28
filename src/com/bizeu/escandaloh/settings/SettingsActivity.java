package com.bizeu.escandaloh.settings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.MainActivity;
import com.bizeu.escandaloh.MyApplication;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.users.RegistrationActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceClickListener {

	private PreferenceCategory perfilCategory;
	private PreferenceScreen screen;
	private SharedPreferences prefs;
	private CheckBoxPreference checkP;
	private Preference cerrar_sesion;
	private Context mContext;
	private boolean any_error;
	private ProgressDialog progress;

	/**
	 * onCreate
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// Título action bar
		ActionBar actBar = getSupportActionBar();
		actBar.setTitle(R.string.ajustes);

		perfilCategory = (PreferenceCategory) findPreference("perfilCategory");
		checkP = (CheckBoxPreference) findPreference("autoreproduccion");
		cerrar_sesion = (Preference) findPreference("cerrarSesion");
		screen = (PreferenceScreen) findPreference("screen");
		
        actBar.setHomeButtonEnabled(true);
        actBar.setDisplayHomeAsUpEnabled(true);
		
		// Si no está logueado ocultamos la opción de cerrar sesión
		if (!MyApplication.logged_user){
			screen.removePreference(perfilCategory);
		}

		cerrar_sesion.setOnPreferenceClickListener(this);

		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		checkP.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				// Guardamos la configuración nueva de autoplay
				if (newValue.toString().equals("true")) {
					prefs.edit().putBoolean(MyApplication.AUTOPLAY_ACTIVATED, true).commit();
				} else {
					prefs.edit().putBoolean(MyApplication.AUTOPLAY_ACTIVATED, false).commit();
				}
				return true;
			}
		});
		
		progress = new ProgressDialog(this);
		progress.setMessage(getResources().getString(R.string.cerrando_sesion));
		progress.setCancelable(false);

	}

	
	@Override
	public boolean onPreferenceClick(Preference preference) {

		// Cerrar sesión
		if (preference.getKey().equals("cerrarSesion")) {

			// Paramos si hubiera algún audio reproduciéndose
			Audio.getInstance(mContext).releaseResources();

			AlertDialog.Builder alert_logout = new AlertDialog.Builder(this);
			alert_logout.setTitle(R.string.cerrar_sesion_usuario);
			alert_logout.setMessage(R.string.seguro_que_quieres_cerrar);
			alert_logout.setPositiveButton(R.string.confirmar,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {

							// Avisamos al servidor
							new LogOutTask().execute();
						}
					});
			alert_logout.setNegativeButton(R.string.cancelar,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
						}
					});
			alert_logout.show();
		}

		return false;
	}
	
	
	
	/**
	 * onOptionsItemSelected
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
 
    	// Si se pulsa el botón home volvemos a la pantalla principal del carrusel
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    
	/**
	 * Desloguea a un usuario
	 * 
	 */
	private class LogOutTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			any_error = false;
			// Mostramos el ProgressDialog
			progress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/logout/";

			HttpResponse response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Session-Token", MyApplication.session_token);
	
				response = client.execute(post);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);

				// Comprobamos si ha habido algún error
				if (response_str != null) {
					Log.i("WE", response_str);
					// Obtenemos el json devuelto
					JSONObject respJSON = new JSONObject(response_str);

					if (respJSON.has("error")) {
						any_error = true;
					}
				}

			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				any_error = true;

				// Mandamos la excepción a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) 
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), // The exception.
						false).build()); // False indicates a fatal exception
			}
			
			// Si hubo algún error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}		
		}
				
		@Override
		protected void onPostExecute(Integer result) {

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
				progress.dismiss();
			}
			
			// Si hubo algún error inesperado
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			else{	
				// Deslogueamos al usuario
				prefs.edit().putString(MyApplication.SESSION_TOKEN, null).commit();
				MyApplication.session_token = null;
	        	prefs.edit().putString(MyApplication.USER_NAME, getResources().getString(R.string.invitado)).commit();
	        	MyApplication.user_name = getResources().getString(R.string.invitado) ;
	        	prefs.edit().putString(MyApplication.AVATAR, null).commit();
	        	MyApplication.avatar = null;
				MyApplication.logged_user = false;
							
				// Reiniciamos los escándalos
				MyApplication.reset_scandals = true;
							
				// Cerramos la pantalla
				finish();
			}
		}

		
	}
}
