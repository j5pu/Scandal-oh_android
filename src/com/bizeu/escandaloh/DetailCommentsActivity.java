package com.bizeu.escandaloh;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class DetailCommentsActivity extends Activity {

	private ListView list_comments;
	private EditText edit_new_comment;
	private ImageView img_new_comment;	
	private String resource_uri;
	private String written_comment;	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);
		
		if (getIntent() != null){
			resource_uri = getIntent().getExtras().getString("resource_uri");
		}
		
		list_comments = (ListView) findViewById(R.id.list_comments);
		edit_new_comment = (EditText) findViewById(R.id.edit_new_comment);
		img_new_comment = (ImageView) findViewById(R.id.img_new_comment);
		
		img_new_comment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("WE","subir comentario");
				new SendComment().execute();
			}
		});
	}
	
	
	
	

	/**
	 * Sube un comentario
	 * @author Alejandro
	 *
	 */
	private class SendComment extends AsyncTask<Void,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(Void... params) {
	    	boolean result = false;
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.48:8000/api/v1/comment/";        

	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();
	             
	             written_comment= edit_new_comment.getText().toString();   
	             
	             dato.put("user", "/api/v1/user/2/");
	             dato.put("photo", "/api/v1/photo/117/");
	             dato.put("text", "prueba asdjfaksdfjk");

	             StringEntity entity = new StringEntity(dato.toString());
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	             
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);
	                 result = true;
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
	        
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
	        if (result){
	        	Log.v("WE","comentario enviado");
	        }
	        else{
	        	Log.v("WE","comentario no enviado");
	        }
	    }
	}
}
