package com.bizeu.escandaloh;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class DetailCommentsActivity extends Activity {

	private ListView list_comments;
	private EditText edit_new_comment;
	private ImageView img_new_comment;	
	private String resource_uri;
	private String written_comment;	
	private ArrayAdapter<String> commentsAdapter;
	private ArrayList<String> comments;
	private String id;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);
		
		if (getIntent() != null){
			id = getIntent().getExtras().getString("id");
		}
		
		list_comments = (ListView) findViewById(R.id.list_comments);
		edit_new_comment = (EditText) findViewById(R.id.edit_new_comment);
		img_new_comment = (ImageView) findViewById(R.id.img_new_comment);
		
		comments = new ArrayList<String>();
		commentsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, comments);
		list_comments.setAdapter(commentsAdapter);
		
		img_new_comment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				written_comment = edit_new_comment.getText().toString();
				// Si ha escrito algo enviamos el comentario
				if (!written_comment.equals("")){
					new SendComment().execute();
				}		
			}
		});
		
		new GetComments().execute();
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
	             dato.put("photo", "/api/v1/photo/" + id +"/"); // Formato: /api/v1/photo/id/
	             dato.put("text", written_comment);

	             StringEntity entity = new StringEntity(dato.toString());
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             Log.v("WE","response subir comentario: " + response.getStatusLine().getStatusCode());
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
	        	comments.add(written_comment);
	        	commentsAdapter.notifyDataSetChanged();
	        }
	        else{
	        	Log.v("WE","comentario no enviado");
	        }
	    }
	}
	
	
	
	
	/**
	 * Muestra la lista de comentarios
	 * @author Alejandro
	 *
	 */
	private class GetComments extends AsyncTask<Void,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(Void... params) {
			
			boolean result = false;
			
			HttpClient httpClient = new DefaultHttpClient();
			
			HttpGet del = new HttpGet("http://192.168.1.48:8000/api/v1/comment/?photo__id=" + id);
			 
			del.setHeader("content-type", "application/json");
			 
			try{
			        HttpResponse resp = httpClient.execute(del);
		            Log.v("WE","response get comments: " + resp.getStatusLine().getStatusCode());
			        String respStr = EntityUtils.toString(resp.getEntity());
			 
			        JSONObject respJSON = new JSONObject(respStr);
			        
			        if (respStr != null){
		            	result = true;
		            }
			        
			        // Parseamos el json para obtener los escandalos
		            JSONArray escandalosObject = null;
		            
		            escandalosObject = respJSON.getJSONArray("objects");
		            
		            for (int i=0 ; i < escandalosObject.length(); i++){
		            	JSONObject escanObject = escandalosObject.getJSONObject(i);
		            	
		            	 String comment = escanObject.getString("text");
					     
					     comments.add(comment);					 
		            }		            
			}
			catch(Exception ex){
				Log.e("ServicioRest","Error!", ex);
			}
			       
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
	        if (result){
	        	Log.v("WE","comentarios listados");
	        	commentsAdapter.notifyDataSetChanged();
	        }
	        else{
	        	Log.v("WE","comentarios no listados");
	        }
	    }
	}
	
	
	
	
}
