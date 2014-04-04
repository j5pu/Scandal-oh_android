package com.bizeu.escandaloh.dialogs;


import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.bizeu.escandaloh.util.Fuente;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button; 

public class LoginSocialNetworksDialog extends Dialog {

	private Context mContext;
	private Activity acti;
	private Button but_login_facebook;
	private Button but_login_google;
	private Button but_login_twitter;

	OnMyDialogResult mDialogResult; 
	
	/**
	 * Constructor
	 * @param con Contexto
	 * @param act Actividad
	 */
	public LoginSocialNetworksDialog(Context con, Activity act) {
		super(con);
		mContext = con;
		acti = act;
	}

	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_dialog);

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.lay_pantalla_login_dialog));

		// Botón login con facebook
		but_login_facebook = (Button) findViewById(R.id.but_login_facebook);

		// Ponemos el icono de facebook en el botón
		Drawable drawable = mContext.getResources().getDrawable(
				R.drawable.facebook_rosa);
		drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.5),
				(int) (drawable.getIntrinsicHeight() * 0.5));
		ScaleDrawable sd = new ScaleDrawable(drawable, 0, 30, 30);
		but_login_facebook.setCompoundDrawables(sd.getDrawable(), null, null,
				null);

		but_login_facebook.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Indicamos a la actividad que ha seleccionado "Log in con facebook"
				mDialogResult.finish("FACEBOOK");
				
				// Cerramos el dialog
				LoginSocialNetworksDialog.this.dismiss();		
			}
		});

		// Botón login con google
		//but_login_google = (Button) findViewById(R.id.but_login_google);

		// Botón login con twitter
		but_login_twitter = (Button) findViewById(R.id.but_login_twitter);
		but_login_twitter.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Indicamos a la actividad que ha seleccionado "Log in con twitter"
				mDialogResult.finish("TWITTER");
				
				//Cerramos el dialog
				LoginSocialNetworksDialog.this.dismiss();		
			}
		});
	}
	
	// Callbacks
    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }
	
    public interface OnMyDialogResult{
        void finish(String result);
     }
	
}
