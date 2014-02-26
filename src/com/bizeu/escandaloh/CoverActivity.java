package com.bizeu.escandaloh;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Utils;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CoverActivity extends Activity {

	private SharedPreferences prefs;
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cover);	
	}
	
	
	/**
	 * onStart
	 */
	@Override
	protected void onStart(){
		super.onStart();
		
		Log.v("WE","IP: " + Connectivity.getIPAddress(true));
		Log.v("WE", "Locale: " + Locale.getDefault().getCountry()); 
		
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		// Obtenemos el código del pais 
		MyApplication.code_selected_country = prefs.getString(MyApplication.CODE_COUNTRY, null);
		// Si es la primera vez que usa la app 
		if (MyApplication.code_selected_country == null){
			// Obtenemos el código del país por IP
			String country_code = Locale.getDefault().getCountry();
			MyApplication.code_selected_country = country_code;
			SharedPreferences prefs = getBaseContext().getSharedPreferences(
      		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
			prefs.edit().putString(MyApplication.CODE_COUNTRY, country_code).commit();
			
			// TODO Mostrar pantalla de bienvenida
		}
	
		// Obtenemos el avatar
		MyApplication.avatar = prefs.getString(MyApplication.AVATAR, null);
		
		// Obtenemos si el usuario estaba logueado
		String user_uri = prefs.getString(MyApplication.USER_URI, null); 
		if (user_uri != null){
			MyApplication.logged_user = true;
			MyApplication.resource_uri = user_uri;
			MyApplication.user_name = prefs.getString(MyApplication.USER_NAME,  null);
		}
		else{
			MyApplication.logged_user = false;
		}
				
		// Mostramos la pantalla del carrusel
		Intent i = new Intent(CoverActivity.this, MainActivity.class);
		startActivity(i);
		finish();	
	}
}
