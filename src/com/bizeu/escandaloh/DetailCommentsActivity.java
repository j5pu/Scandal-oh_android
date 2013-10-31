package com.bizeu.escandaloh;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class DetailCommentsActivity extends Activity {

	private ListView list_comments;
	private EditText edit_new_comment;
	private ImageView img_new_comment;	
	private LinearLayout layout_write_comment;
	
	private String resource_uri;
	private String written_comment;	
	private ArrayAdapter<Comment> commentsAdapter;
	private ArrayList<Comment> comments;
	private String id;
	private boolean user_login = false;
	private String user_uri;
	
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
		layout_write_comment = (LinearLayout) findViewById(R.id.ll_write_comment);
		
		comments = new ArrayList<Comment>();
		commentsAdapter = new CommentAdapter(this,R.layout.comment, comments);
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
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		// Comprobamos si el usuario esta logueado
		SharedPreferences prefs = this.getSharedPreferences(
			      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
		
		user_uri = prefs.getString("user_uri", null); 
		if (user_uri != null){
			user_login = true;
		}
		else{
			user_login = false;
			// Ocultamos la vista de enviar cometario
			layout_write_comment.setVisibility(View.GONE);
		}
	}
	
	
	

	/**
	 * Sube un comentario
	 * @author Alejandro
	 *
	 */
	private class SendComment extends AsyncTask<Void,Integer,Integer> {
		 
		@Override
	    protected Integer doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.48:8000/api/v1/comment/";        

	        HttpResponse response = null;
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();
	             
	             // Obtenemos el comentario en formato UTF-8
	             written_comment = edit_new_comment.getText().toString();
	             
	             dato.put("user", user_uri);
	             dato.put("photo", "/api/v1/photo/" + id +"/"); // Formato: /api/v1/photo/id/
	             dato.put("text", written_comment);

	             // Formato UTF-8 (ñ,á,ä,...)
	             StringEntity entity = new StringEntity(dato.toString(),  HTTP.UTF_8);
	             post.setEntity(entity);

	             response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	        }
	        
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
	        
	        // Devolvemos el resultado 
	        return (response.getStatusLine().getStatusCode());
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			// Si es codigo 2xx --> OK
			if (result >= 200 && result <300){
	        	Log.v("WE","comentario enviado");
	        	//comments.add(written_comment, username);
	        	//commentsAdapter.notifyDataSetChanged();
	        }
	        else{
	        	Log.v("WE","comentario no enviado");
	        }
	    }
	}
	
	
	
	
	/**
	 * Muestra la lista de comentarios para esa foto
	 * @author Alejandro
	 *
	 */
	private class GetComments extends AsyncTask<Void,Integer,Integer> {
		 
		@Override
	    protected Integer doInBackground(Void... params) {
			
			HttpClient httpClient = new DefaultHttpClient();
			
			HttpGet del = new HttpGet("http://192.168.1.48:8000/api/v1/comment/?photo__id=" + id);
			 
			del.setHeader("content-type", "application/json");
			
			HttpResponse response = null ;
			try{
					response = httpClient.execute(del);
			        String respStr = EntityUtils.toString(response.getEntity());
			        
			        Log.i("WE",respStr);
			  
			        JSONObject respJSON = new JSONObject(respStr);
			        
			        // Parseamos el json para obtener los escandalos
		            JSONArray escandalosObject = null;
		            		   
		            escandalosObject = respJSON.getJSONArray("objects");
		            
		            for (int i=0 ; i < escandalosObject.length(); i++){
		            	JSONObject escanObject = escandalosObject.getJSONObject(i);
		            	
		            	 String comment = new String(escanObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
		            	 String username = escanObject.getString("username");
		            	 String date = escanObject.getString("date");
		            	// SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		            	// Date date = formatter.parse(date_string);
		            	 
					     // Añadimos el comentario en formato UTF-8 (caracteres ñ,á,...)
					     comments.add(new Comment(comment, username, date));					 
		            }		            
			}
			catch(Exception ex){
				Log.e("ServicioRest","Error!", ex);
			}
			
			// Devolvemos el código de respuesta
	        return (response.getStatusLine().getStatusCode());
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			// Si es codigo 2xx --> OK
	        if (result >= 200 && result <300){
	        	Log.v("WE","comentarios listados");
	        	commentsAdapter.notifyDataSetChanged();
	        }
	        else{
	        	Log.v("WE","comentarios no listados");
	        }
	    }
	}
	
	
	
	
}
