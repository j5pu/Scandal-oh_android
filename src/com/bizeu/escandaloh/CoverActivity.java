package com.bizeu.escandaloh;

import com.bizeu.escandaloh.util.Screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

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
		
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		// Obtenemos el código del pais 
		MyApplication.code_selected_country = prefs.getString(MyApplication.CODE_COUNTRY, null);
		
		// Obtenemos si el usuario está logueado
		String user_uri = prefs.getString(MyApplication.USER_URI, null); 
		if (user_uri != null){
			MyApplication.logged_user = true;
			MyApplication.resource_uri = user_uri;
		}
		else{
			MyApplication.logged_user = false;
		}
		
		// Hay un país ya elegido para este dispositivo?
		if (MyApplication.code_selected_country != null){		
			// Mostramos la pantalla del carrusel
			Intent i = new Intent(CoverActivity.this, MainActivity.class);
			startActivity(i);
		}
		// Si no mostramos la pantalla de elegir país
		else{
			Intent i = new Intent(CoverActivity.this, SelectCountryActivity.class);
			startActivity(i);
		}
		finish();	
	}
}
