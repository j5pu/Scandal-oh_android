package com.bizeu.escandaloh;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bizeu.escandaloh.notifications.PushReceiver;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.parse.ParseInstallation;

public class CoverActivity extends Activity {

	public static int FROM_SHARING_PICTURE = 234;
	public static int FROM_SHARING_TEXT = 2367;
	
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
		
		// Si es la primera vez que usa la app 
		if (MyApplication.code_selected_country == null){
			// Obtenemos el código del país por IP
			String country_code = Locale.getDefault().getCountry();
			MyApplication.code_selected_country = country_code;
			SharedPreferences prefs = getBaseContext().getSharedPreferences(
      		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
			prefs.edit().putString(MyApplication.CODE_COUNTRY, country_code).commit();
			
			MyApplication.logged_user = false;
			// TODO Mostrar pantalla de bienvenida
		}
		else{
			// Obtenemos si el usuario estaba logueado
			String session_token = prefs.getString(MyApplication.SESSION_TOKEN, null);
			if (session_token != null){
				MyApplication.logged_user = true;
				MyApplication.session_token = session_token;
				MyApplication.user_name = prefs.getString(MyApplication.USER_NAME,  null);
				MyApplication.avatar = prefs.getString(MyApplication.AVATAR, null);
				MyApplication.social_network = prefs.getInt(MyApplication.SOCIAL_NETWORK, 1);
			}
			else{
				MyApplication.logged_user = false;
			}
		}
			
		
		String shared = null;
		int sharing_type = 0;
		
		// La aplicación se ha iniciado porque se ha compartido desde otra app 
		if (getIntent().getAction().equals(Intent.ACTION_SEND)){
			Bundle extras = getIntent().getExtras();
			Intent i = getIntent();

			// Es una foto
			if (extras.containsKey(Intent.EXTRA_STREAM)){
				Uri shareUri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
				shared = shareUri.toString();
				sharing_type = FROM_SHARING_PICTURE;
			}

			// Es texto (Url)
			else if (extras.containsKey(Intent.EXTRA_TEXT)){
				shared = (String) extras.getCharSequence(Intent.EXTRA_TEXT);
				sharing_type = FROM_SHARING_TEXT;
			}
			
			Intent in;
			
			// Si está logueado: le mandamos a la pantalla de subir escandalo
			if (MyApplication.logged_user){
				in = new Intent(CoverActivity.this, CreateScandalohActivity.class);
			}
				
			// No está logueado, le mandamos a la pantalla de login
			else{
				in = new Intent(CoverActivity.this, LoginSelectActivity.class);
			}	
			
			in.putExtra("photo_from", sharing_type);
			in.putExtra("shareUri", shared);			
			startActivity(in);
			finish();
		}
		
		else{
			Intent i = new Intent(CoverActivity.this, MainActivity.class);
			// La aplicación se ha iniciado porque viene de una notificación push
			if (getIntent().getAction().equals(PushReceiver.PUSH_NOTIFICATION)){
				i.setAction(PushReceiver.PUSH_NOTIFICATION);
			}
			else{
				i.setAction(Intent.ACTION_DEFAULT);
			}
			startActivity(i);
			finish();
		}			
	}
}
