package com.bizeu.escandaloh.settings;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.util.Connectivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;


public class SettingsActivity extends SherlockPreferenceActivity {

	private SharedPreferences prefs;
	private CheckBoxPreference checkA;
	private CheckBoxPreference checkN;
	private PreferenceCategory prefN;

	private Context mContext;

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
		actBar.setIcon(R.drawable.logo_blanco);

		checkA = (CheckBoxPreference) findPreference("autoreproduccion");
		checkN = (CheckBoxPreference) findPreference("notifications");
		prefN = (PreferenceCategory) findPreference("notificationsCategory");
		
        actBar.setHomeButtonEnabled(true);
        actBar.setDisplayHomeAsUpEnabled(true);
        
        mContext = this;

        if (!MyApplication.logged_user){
        	PreferenceScreen preferenceScreen = getPreferenceScreen();
        	preferenceScreen.removePreference(prefN);
        }
        
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		// Obtenemos si el usuario tiene activado o no las notificaciones
		if (prefs.getBoolean(MyApplication.NOTIFICATIONS_ACTIVATED, true)){
			checkN.setChecked(true);
		}
		else{
			checkN.setChecked(false);
		}
		

	
		checkA.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
	
				// Guardamos la configuración nueva de autoplay
				if (newValue.toString().equals("true")) {
					prefs.edit().putBoolean(MyApplication.AUTOPLAY_ACTIVATED, true).commit();
				} 
				else {
					prefs.edit().putBoolean(MyApplication.AUTOPLAY_ACTIVATED, false).commit();
				}
				return true;
			}
		});
		
		
		checkN.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				// Si hay conexión: cambiamos la configuración de las notis
				if (Connectivity.isOnline(mContext)){
					boolean to_activate;
					
					// Avisamos al servidor
					if (newValue.toString().equals("true")){
						to_activate = true;
						prefs.edit().putBoolean(MyApplication.NOTIFICATIONS_ACTIVATED, true).commit();
					}
					else{
						to_activate = false;
						prefs.edit().putBoolean(MyApplication.NOTIFICATIONS_ACTIVATED, false).commit();
					}
					
					new ActivateNotificationsTask(to_activate).execute();
				}
				
				// Si no, deshabilitamosla opción
				else{
					checkN.setEnabled(false);			
					
					Toast toast = Toast.makeText(mContext, R.string.no_dispones_de_conexion, Toast.LENGTH_SHORT);
					toast.show();
				}
				
				return true;
			}
		});
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
	 * Activa/Desactiva las notificaciones
	 * 
	 */
	private class ActivateNotificationsTask extends AsyncTask<Void, Integer, Integer> {

		private boolean activate;

		public ActivateNotificationsTask(boolean activate) {
			this.activate = activate;
		}


		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/user/logged/";
			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPut put = new HttpPut(urlString);
				put.setHeader("Session-Token", MyApplication.session_token);
				MultipartEntity reqEntity = new MultipartEntity();
				
				StringBody activate_string;
				if (activate){
					activate_string = new StringBody("false");
				}
				else{
					activate_string = new StringBody("true");
				}
				
				reqEntity.addPart("has_notifications_disabled", activate_string);
				put.setEntity(reqEntity);
				response = client.execute(put);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);
				
				Log.i("WE","act/desct notificaciones " + response_str);
				
				JSONObject respJSON = new JSONObject(response_str);
			}

			catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);

			}

			// Devolvemos el resultado
			return null;	
		}

	}
    
}
