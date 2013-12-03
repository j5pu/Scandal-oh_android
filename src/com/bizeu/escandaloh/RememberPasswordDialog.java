package com.bizeu.escandaloh;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Audio.PlayListener;

public class RememberPasswordDialog extends Dialog{

	private TextView txt_mensaje;
	private Button but_cancelar; 
	private Button but_enviar;
	private EditText edit_email;
	private ProgressDialog progress;
	private boolean any_error;
	private Context context;
	private String status = null;
	private String msg = null;
	private String reason = null;
	private int reason_code;
	private boolean result_ok;

	public RememberPasswordDialog(Context con) {
		super(con);
		this.context = con;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recordar_contrasenia);
		
		txt_mensaje = (TextView) findViewById(R.id.txt_recordar_descripcion);
		but_cancelar = (Button) findViewById(R.id.but_recordar_cancelar);
		but_enviar = (Button) findViewById(R.id.but_recordar_enviar);
		edit_email = (EditText) findViewById(R.id.edit_recordar_email);
		
		but_cancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();			
			}
		});
		
		but_enviar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Connectivity.isOnline(context)){
					// Comprobamos si el email tiene al menos un carácter
					if (edit_email.getText().toString().length() > 0){
						new RememberPassUser().execute();
					}	
					else{
						edit_email.setError("Introduzca un email");
					}
				}
				else{
					Toast toast = Toast.makeText(context, "No dispone de conexión a internet", Toast.LENGTH_LONG);
					toast.show();
				}			
			}
		});
		
		progress = new ProgressDialog(context);
		progress.setTitle("Solicitando contraseña ...");
		progress.setMessage("Espere, por favor");
	}


	
	
	

	/**
	 * Solicita el envio de la contraseña a partir de un email de registro
	 * @author Alejandro
	 *
	 */
	private class RememberPassUser extends AsyncTask<Void,Integer,Void> {
		 	
		@Override
		protected void onPreExecute(){
			// Mostramos el ProgressDialog
			progress.setCancelable(false);
			progress.show();	
			
			// Reseteamos
			any_error = false;
		}
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "api/v1/user/generate-new-password/";
	
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();	              
	             
	             dato.put("email", edit_email.getText().toString());

	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	                          
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);
	                 // Obtenemos el json devuelto
	                 JSONObject respJSON = new JSONObject(response_str);
	                 
	                 // Comprobamos el campo status del json
	                 status = respJSON.getString("status"); 
	                 
	                 // Si es OK obtenemos el msg
	                 if (status.equals("ok")){
	                	 result_ok = true;
	                 }
	                 // Si no es OK obtenemos la razón
	                 else if (status.equals("error")){
	                	 result_ok = false;
	                	 reason = respJSON.getString("reason");
	                	 reason_code = respJSON.getInt("reason_code");       	
	                 }
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	             any_error = true;
	        }
	        
	        return null;
	    }

		
		@Override
	    protected void onPostExecute(Void result) {

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			// Si hubo algún error mostramos un mensaje
			if (any_error){
				Toast toast = Toast.makeText(context, "Lo sentimos, se produjo algún error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			// Si no hubo ningún error extraño
			else{
				// Comprobamos si se hizo correctamente la petición
				if (result_ok){
		        	Toast.makeText(context, "Se ha enviado un email con la contraseña", Toast.LENGTH_SHORT).show();
				    dismiss();
				}
				else{
					if (reason_code == 1){ // Email no registrado
						txt_mensaje.setText("Este email no está registrado");
					}
					else if (reason_code == 2){ // Error al enviar email
						txt_mensaje.setText("Hubo algún error en la petición");
					}
				}	
			}				
	    }
	}



  
	
	
}
