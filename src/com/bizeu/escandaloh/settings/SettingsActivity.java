package com.bizeu.escandaloh.settings;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.MyApplication;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.bizeu.escandaloh.users.MainLoginActivity;
import com.bizeu.escandaloh.users.RegistrationActivity;
import com.bizeu.escandaloh.util.Audio;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceClickListener {

	private PreferenceCategory perfilCategory;
	private SharedPreferences prefs;
	private CheckBoxPreference checkP;
	private Preference cerrar_sesion;
	private Preference iniciar_sesion;
	private Preference registro;
	private Context mContext;

	/**
	 * onCreate
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings2);

		// Título action bar
		ActionBar actBar = getSupportActionBar();
		actBar.setTitle("Ajustes");

		perfilCategory = (PreferenceCategory) findPreference("perfilCategory");
		// TODO V2.0 checkP = (CheckBoxPreference) findPreference("autoreproduccion");
		cerrar_sesion = (Preference) findPreference("cerrarSesion");
		iniciar_sesion = (Preference) findPreference("iniciarSesion");
		registro = (Preference) findPreference("registro");
		
        actBar.setHomeButtonEnabled(true);
        actBar.setDisplayHomeAsUpEnabled(true);
		
		// Si el usuario está logueado mostramos la opción de cerrar sesión
		if (MyApplication.logged_user){
			perfilCategory.removePreference(iniciar_sesion);
			perfilCategory.removePreference(registro);
		}
		// Si no mostramos las opciones de iniciar sesión y registro 
		else{
			perfilCategory.removePreference(cerrar_sesion);
		}

		cerrar_sesion.setOnPreferenceClickListener(this);
		iniciar_sesion.setOnPreferenceClickListener(this);
		registro.setOnPreferenceClickListener(this);

		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);

		/* V2.0
		checkP.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (newValue.toString().equals("true")) {
					Log.v("WE", "Autoreproducir activado");
				} else {
					Log.v("WE", "Autoreprroducir quitado");
				}
				return true;
			}
		});
		*/
	}

	
	@Override
	public boolean onPreferenceClick(Preference preference) {

		// Cerrar sesión
		if (preference.getKey().equals("cerrarSesion")) {

			// Paramos si hubiera algún audio reproduciéndose
			Audio.getInstance(mContext).releaseResources();

			AlertDialog.Builder alert_logout = new AlertDialog.Builder(this);
			alert_logout.setTitle("Cerrar sesión usuario");
			alert_logout.setMessage("¿Seguro que desea cerrar la sesión actual?");
			alert_logout.setPositiveButton("Confirmar",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {

							// Deslogueamos al usuario
							prefs.edit().putString(MyApplication.USER_URI, null).commit();
							MyApplication.logged_user = false;
							
							// Cerramos la pantalla
							finish();
						}
					});
			alert_logout.setNegativeButton("Cancelar",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
						}
					});
			alert_logout.show();
		}
		
		// Iniciar sesión
		else if (preference.getKey().equals("iniciarSesion")) {
			Intent i = new Intent(SettingsActivity.this, MainLoginActivity.class);
			startActivity(i);
			finish();
		}
		
		// Registrar usuario
		else if (preference.getKey().equals("registro")){
			Intent i = new Intent(SettingsActivity.this, RegistrationActivity.class);
			startActivity(i);
			finish();
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
}
