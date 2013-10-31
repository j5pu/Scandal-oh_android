package com.bizeu.escandaloh.users;

import com.bizeu.escandaloh.CreateEscandaloActivity;
import com.bizeu.escandaloh.MainActivity;
import com.bizeu.escandaloh.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainLoginActivity extends Activity {

	public static int LOG_IN = 1;
	
	private TextView txt_pasar;
	private Button but_registro;
	private Button but_login;
	
	private boolean esta_logeado;
	private SharedPreferences prefs;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_login);
		
		txt_pasar = (TextView) findViewById(R.id.txt_pasar_registro);
		but_registro = (Button) findViewById(R.id.but_registro_usuario);
		but_login = (Button) findViewById(R.id.but_log_in);
		
		// Comprobamos si el usuario esta logeado
		prefs = this.getSharedPreferences("com.bizeu.escandaloh", this.MODE_PRIVATE);
		
		String user_uri = prefs.getString("user_uri", null); 
		
		// Mostramos "Log in" o "Log out" según el usuario esté logeado o no
		if (user_uri != null){
			esta_logeado = true;
			but_login.setText("Log out");
		}
		else{
			esta_logeado = false;
			but_login.setText("Log in");
		}	
		
		but_registro.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), RegistrationActivity.class);
				startActivity(i);		
			}
		});
		
		but_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (esta_logeado){
					// Deslogueamos al usuario
					prefs.edit().putString("user_uri", null).commit();
					but_login.setText("Log in");
					esta_logeado = false;
				}
				else{
					// Mostramos la pantalla de log in
					Intent i = new Intent(getBaseContext(), LoginActivity.class);
					startActivityForResult(i, LOG_IN);
				}	
			}
		});
	}
	
	
	
	
	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		// Si viene de hacer log in 
		if (requestCode == LOG_IN) {
			// Y lo ha hecho exitosamente
			if (resultCode == RESULT_OK) {
				// Cerramos directamente la pantalla
				finish();					
			}		 
		}	
	}
}
