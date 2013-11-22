package com.bizeu.escandaloh.users;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.R;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;


public class LoginFacebook extends Activity {

	private ProgressDialog progress;
	private String status = null;
	private boolean login_error = false;
	private int reason_code;
	private String user_uri;
	private String reason;
	private String loginMessageError;
	
	private String username;
	private String email;
	
	/**
	 * OnCreate
	 */
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.acti_main);
	        
	        Session currentSession = Session.getActiveSession();
	        if (currentSession == null || currentSession.getState().isClosed()) {
	        	Log.v("WE","nula os isClosed");
	            Session session = new Session.Builder(this).build();
	            Session.setActiveSession(session);
	            currentSession = session;
	            
	            Session.OpenRequest or = new Session.OpenRequest(this);
		        or.setPermissions("email");
		        
		        Session se = new Session(this);
		        se.addCallback(new Session.StatusCallback() {

			          // callback when session changes state
			          @Override
			          public void call(Session session, SessionState state, Exception exception) {
			        	  Log.v("WE","Entra en call");
			            if (session.isOpened()) {
			            	 Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
			     	        	
			     				@Override
			     				public void onCompleted(GraphUser user, Response response) {
			     					if (user != null){
				     					Log.v("WE","Entra 1");
                                        username = limitaCaracteres(user.getUsername());
				     					email = user.getProperty("email").toString();
				     					
				     					 new LogInUserFacebook().execute();
			     					}
			     					else{
			     				        Toast.makeText(getBaseContext(), "Hubo algún problema. Compruebe la conexión", Toast.LENGTH_SHORT).show();
			     					}

			     					
			     				}
			     	        });
			     	        
			     	        re.executeAsync();
			              }
			            else{
			            	Log.v("WE","SEsion no abierta");
			            	
			            }
			            }
			        });
		        Session.setActiveSession(se);
		        Log.v("WE","antes del segundo openforread");
		        se.openForRead(or);
	            
	        }
	        
	        if (currentSession.isOpened()) {
	            Log.v("WE","abierta");
	            Session.openActiveSession(this, true, new Session.StatusCallback() {

		            @Override
		            public void call(final Session session, SessionState state,
		                    Exception exception) {

		                if (session.isOpened()) {
		                	
		                	Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
			     	        	
			     				@Override
			     				public void onCompleted(GraphUser user, Response response) {
			     					if (user != null) {
                                        email = user.getProperty("email").toString();
                                        username = limitaCaracteres(user.getUsername());
                                       
                                        new LogInUserFacebook().execute();
                                    }
			     					else{
			     				        Toast.makeText(getBaseContext(), "Hubo algún problema. Compruebe la conexión", Toast.LENGTH_SHORT).show();
			     					}
			     				}
		                	});
		                	re.executeAsync();        
		                }
		            }
		        });

	        } 
	        
			progress = new ProgressDialog(this);
   
	   }
	        

		public void call(Session session, SessionState state, Exception exception) {
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
		    super.onActivityResult(requestCode, resultCode, data);
		    if (Session.getActiveSession() != null){
		    	Log.v("WE","result != null");
		        Session.getActiveSession().onActivityResult(this, requestCode,
		                resultCode, data);
		    }

		    Session currentSession = Session.getActiveSession();
		    if (currentSession == null || currentSession.getState().isClosed()) {
		    	Log.v("WE","result ==null o isclosed");
		        Session session = new Session.Builder(this).build();
		        Session.setActiveSession(session);
		        currentSession = session;
		    }

		    if (currentSession.isOpened()) {
		    	Log.v("WE","result isopened");
		        Session.openActiveSession(this, true, new Session.StatusCallback() {

		            @Override
		            public void call(final Session session, SessionState state,
		                    Exception exception) {

		                if (session.isOpened()) {
		                	
		                	Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
			     	        	
			     				@Override
			     				public void onCompleted(GraphUser user, Response response) {
			     					if (user != null) {
                                        username = limitaCaracteres(user.getUsername());
                                        email = user.getProperty("email").toString();
                                        
                                        new LogInUserFacebook().execute();
                                    }
			     					else{
			     				        Toast.makeText(getBaseContext(), "Hubo algún problema. Compruebe la conexión", Toast.LENGTH_SHORT).show();
			     					}
			     				}
		                	});
		                	re.executeAsync();        
		                }
		            }
		        });
		    }
		}
		
		
		
		
		
		

		/**
		 * Loguea un usuario (a partir de datos de facebook)
		 *
		 */
		
		
		private class LogInUserFacebook extends AsyncTask<Void,Integer,Void> {
			 
			@Override
			protected void onPreExecute(){
				login_error = false;
				
				// Mostramos el ProgressDialog
				progress.setTitle("Logueando ...");
				progress.setMessage("Espere, por favor");
				progress.setCancelable(false);
				progress.show();
			}
			
			@Override
		    protected Void doInBackground(Void... params) {
		 
		    	HttpEntity resEntity;
		        String urlString = MyApplication.SERVER_ADDRESS + "api/v1/user/login/";        
		        
		        try{
		             HttpClient client = new DefaultHttpClient();
		             HttpPost post = new HttpPost(urlString);
		             post.setHeader("Content-Type", "application/json");
		             
		             JSONObject dato = new JSONObject();	   
	             
		             Log.v("WE","user name es: " + username);
		             dato.put("username", username);
		             dato.put("email", email);
		             dato.put("social_network", 1);

		             // Creamos el StringEntity como UTF-8 (Caracteres ñ,á, ...)
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
		                 
		                 // Si es OK obtenemos el user_uri
		                 if (status.equals("ok")){
		                	 user_uri = respJSON.getString("user_uri");
		                	 login_error = false;
		                 }
		                 else{
		                	 login_error = true;
		                 }
		                 /*
		                 // Si no es OK obtenemos la razón
		                 else if (status.equals("error")){
		                	 if (respJSON.has("reason_code")){
		                		 reason_code = Integer.parseInt(respJSON.getString("reason_code"));
			                	 reason = respJSON.getString("reason");
			                	 if (reason_code == 1){ // Fallo de identificación: nombre de usuario no existe
			                		 name_error = reason;
			                		 has_name_error = true;
			                	 }
			                	 else if (reason_code == 2){ // Fallo de identificación: password no existe
			                		 password_error = reason;
			                		 has_password_error = true;
			                	 }
			                	 if (reason_code == 3){ // Fallo de autentificación ((caracteres raros, espacio, longitud...)
			                		 JSONObject jsonReason = new JSONObject(respJSON.getString("reason_details"));
			                		 if (jsonReason.has("username_email")){
			                			 name_error = jsonReason.getString("username_email");
			                			 has_name_error = true;
			                		 }
			                		 if (jsonReason.has("password")){
			                			 password_error = jsonReason.getString("password");
			                			 has_password_error = true;
			                		 }	 
			                	 }
		                	 }
		                	 // Ha dado error pero no tiene el reason code: mostramos el mensaje de error
		                	 else{
		                		 JSONObject jsonMessageError = new JSONObject(respJSON.getString("reason"));
		                		 loginMessageError = jsonMessageError.getString("error_message");
		                		 login_error = true;
		                	 }    	
		                 }
		                 */
		             }
		        }
		        catch (Exception ex){
		             Log.e("Debug", "error: " + ex.getMessage(), ex);
		        }
				return null;
		      
		    }
			
			
			@Override
		    protected void onPostExecute(Void result) {
				
				// Quitamos el ProgressDialog
				if (progress.isShowing()) {
			        progress.dismiss();
			    }
				
				// Si no ha habido algún error extraño 
				if (!login_error){
					Log.v("WE","El user uri es: " + user_uri);
					SharedPreferences prefs = getBaseContext().getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
			        prefs.edit().putString(MyApplication.USER_URI, user_uri).commit();
			        MyApplication.logged_user = true;
			        Toast.makeText(getBaseContext(), "Login ok", Toast.LENGTH_SHORT).show();
			        	
			        // Le indicamos a la anterior actividad que ha habido éxito en el log in
			        setResult(Activity.RESULT_OK);
			        finish();					
				}
				
				// Ha habido algún error extraño: mostramos el mensaje
				else{
		        	Toast.makeText(getBaseContext(), loginMessageError, Toast.LENGTH_SHORT)
					.show();
				}
							
		    }
		}
		
	  
		/**
		 * Limita un string a 25 caracteres como máximo
		 * @param completo String oritinal
		 * @return String con un tamaño máximo de 25 caracteres
		 */
		private String limitaCaracteres(String completo){
			String acortado = null;
			if (completo.length() > 25){
				acortado = completo.substring(0,25);
			}
			else{
				acortado = completo;
			}
			
			return acortado;
		}
	 
}
