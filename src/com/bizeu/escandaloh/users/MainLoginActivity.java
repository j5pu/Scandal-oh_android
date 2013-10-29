package com.bizeu.escandaloh.users;

import com.bizeu.escandaloh.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainLoginActivity extends Activity {

	private TextView txt_pasar;
	private Button but_registro;
	private Button but_login;
	
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
		
		but_registro.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), RegistrationActivity.class);
				startActivity(i);		
			}
		});
	}
}
