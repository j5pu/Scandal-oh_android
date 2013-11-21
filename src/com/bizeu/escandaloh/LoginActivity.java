package com.bizeu.escandaloh;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.internal.SessionTracker;
import com.facebook.model.GraphUser;


public class LoginActivity extends Activity {

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
	        }
	        
	        if (currentSession.isOpened()) {
	            Log.v("WE","abierta");

	        } 
	        else if (!currentSession.isOpened()) {
	            Log.v("WE","CERrada");
	            // Ask for username and password
	            OpenRequest op = new Session.OpenRequest((Activity) this);

	            op.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
	            op.setCallback(new Session.StatusCallback() {

			          // callback when session changes state
			          @Override
			          public void call(Session session, SessionState state, Exception exception) {
			        	  Log.v("WE","Entra en callb 1");
			            if (session.isOpened()) {
			            	 Request re = Request.newMeRequest(session, new Request.GraphUserCallback(){
			     	        	
			     				@Override
			     				public void onCompleted(GraphUser user, Response response) {
			     					Log.v("WE","Entra");
			     					String name = user.getName();
			     					String json = user.getInnerJSONObject().toString();
			     					Log.v("WE","json: " + json);
			     					
			     				}
			     	        });
			     	        
			     	        re.executeAsync();
			              }
			            else{
			            	Log.v("WE","SEsion no abierta");
			            	
			            }
			            }
			        });

	            List<String> permissions = new ArrayList<String>();
	            permissions.add("email");
	            op.setPermissions(permissions);

	            Session session = new Session.Builder(LoginActivity.this).build();
	            Session.setActiveSession(session);
	            Log.v("WE","antes de openForRead");
	            session.openForRead(op);
	        }
	        
	        
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
		     					Log.v("WE","Entra");
		     					String name = user.getName();
		     					String json = user.getInnerJSONObject().toString();
		     					Log.v("WE","json: " + json);
		     					
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

                                        TextView welcome = (TextView) findViewById(R.id.welcome);
                                        welcome.setText("Hello "
                                                + user.getName() + "!");
                                        Log.v("WE","Email: " + user.getProperty("email").toString());
                                    }
			     				}
		                	});
		                	re.executeAsync();        
		                }
		            }
		        });
		    }
		}
	  
	 
}
