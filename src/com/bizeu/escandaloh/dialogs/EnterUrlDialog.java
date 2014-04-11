package com.bizeu.escandaloh.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.bizeu.escandaloh.CreateScandalohActivity;
import com.bizeu.escandaloh.MainActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class EnterUrlDialog extends Dialog {

	private Context mContext;
	private EditText edit_url;
	private Button but_aceptar;
	private Button but_cancelar;
	
	public EnterUrlDialog(Context con) {
		super(con);
		this.mContext = con;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enter_url);
		
		edit_url = (EditText) findViewById(R.id.edit_enterurl_url);
		but_aceptar = (Button) findViewById(R.id.but_enterurl_aceptar);
		but_cancelar = (Button) findViewById(R.id.but_enterurl_cancelar);
		
		but_cancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();			
			}
		});
		
		but_aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String entered_url = edit_url.getText().toString();
				if (entered_url.length() > 0){
					Intent i = new Intent(mContext, CreateScandalohActivity.class);
					i.putExtra("photo_from", MainActivity.FROM_URL);
					i.putExtra("shareUri", entered_url);
					mContext.startActivity(i);
					dismiss();
				}				
			}
		});
	}
}
