package com.bizeu.escandaloh;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CoverActivity extends Activity {

	public static int FROM_SHARING = 934;
	
	private SharedPreferences prefs;
	private Uri shareUri;
	
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
		String session_token = prefs.getString(MyApplication.SESSION_TOKEN, null);
		if (session_token != null){
			MyApplication.logged_user = true;
			MyApplication.session_token = session_token;
			MyApplication.user_name = prefs.getString(MyApplication.USER_NAME,  null);
		}
		else{
			MyApplication.logged_user = false;
		}
		
		// La aplicación se ha iniciado porque se ha compartido desde otra app (galería)
		if (getIntent().getAction().equals(Intent.ACTION_SEND)){
			Intent i = getIntent();
			if (i.getType().equals("image/*")){
				shareUri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);

				// Si está logueado: le mandamos a la pantalla de subir escandalo
				if (MyApplication.logged_user){
					Intent in = new Intent(CoverActivity.this, CreateScandalohActivity.class);
					in.putExtra("photo_from", FROM_SHARING);
					in.putExtra("shareUri", shareUri.toString());
					startActivity(in);
					finish();
				}
				
				// No está logueado, le mandamos a la pantalla de login
				else{
					Intent in = new Intent(CoverActivity.this, LoginSelectActivity.class);
					in.putExtra("shareUri", shareUri.toString());
					in.putExtra("from_sharing", true);
					startActivity(in);
					finish();
				}
			}
		}
		 // La aplicación se ha iniciado por el método normal
		else{
			// Mostramos la pantalla del carrusel
			Intent i = new Intent(CoverActivity.this, MainActivity.class);
			startActivity(i);
			finish();
		}			
	}
}
