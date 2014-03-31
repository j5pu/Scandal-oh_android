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

public class SettingsActivity extends SherlockPreferenceActivity {

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

		checkP = (CheckBoxPreference) findPreference("autoreproduccion");
		screen = (PreferenceScreen) findPreference("screen");
		
        actBar.setHomeButtonEnabled(true);
        actBar.setDisplayHomeAsUpEnabled(true);

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
    
}
