package com.bizeu.escandaloh;

import java.util.ArrayList;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;

public class DetailCommentsActivity extends SherlockActivity {

	private ListView list_comments;
	private EditText edit_new_comment;
	private TextView txt_title;
	private TextView txt_user;
	private TextView txt_send;
	private TextView txt_count_characteres;
	private FetchableImageView img_photo;
	private LinearLayout layout_write_comment;
	private String written_comment;	
	private ArrayAdapter<Comment> commentsAdapter;
	private ArrayList<Comment> comments;
	private String photo_id;
	private ProgressDialog progress;
	private String route_image;
	private String user;
	private String title;

	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);
		
		if (getIntent() != null){
			photo_id = getIntent().getExtras().getString("id");
			route_image = getIntent().getExtras().getString("route_image");	
			user = getIntent().getExtras().getString("user");
			title = getIntent().getExtras().getString("title");
		}
		
		// Quitamos el action bar
		getSupportActionBar().hide();
		
		final Context context = this.getApplicationContext();
		
		list_comments = (ListView) findViewById(R.id.list_comments);
		img_photo = (FetchableImageView) findViewById(R.id.img_photo_list_comments);
		img_photo.setImage(route_image, R.drawable.previsualizacion_foto);
		layout_write_comment = (LinearLayout) findViewById(R.id.ll_comments_write);
		
		comments = new ArrayList<Comment>();
		commentsAdapter = new CommentAdapter(this,R.layout.comment, comments);
		list_comments.setAdapter(commentsAdapter);
		
		edit_new_comment = (EditText) findViewById(R.id.edit_new_comment);
		// Cada vez que se modifique el titulo actualizamos el contador: x/75
		edit_new_comment.addTextChangedListener(new TextWatcher() {          
		            @Override
		            public void onTextChanged(CharSequence s, int start, int before, int count) {                                                
		            	txt_count_characteres.setText(s.length() + "/500");
		            }

					@Override
					public void afterTextChanged(Editable arg0) {
						// TODO Auto-generated method stub		
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub		
					} 
				});
		
		
		txt_send = (TextView) findViewById(R.id.txt_send_new_comment);
		txt_send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				written_comment = edit_new_comment.getText().toString();
				// Si ha escrito algo y la longitud es menor de 500 caracteres lo intentamos enviar
				if (!written_comment.equals("") && written_comment.length() < 501){
					Log.v("WE","Longitud comentario: " + written_comment.length());
					new SendComment(context).execute();
				}	
			}
		});
		
	
		
		// Mostramos el usuario
		txt_user = (TextView) findViewById(R.id.txt_comments_user);
		txt_user.setText(user);
		
		// Mostramos el titulo entre comillas dobles (quotation mark)
		txt_title = (TextView) findViewById(R.id.txt_comments_title);
		String title_quotation_mark = "\"" + title + "\"";
		Log.v("WE","title quoation mark: " + title_quotation_mark);
		txt_title.setText(title_quotation_mark);
		
		progress = new ProgressDialog(this);
		
		new GetComments(context).execute();
	}
	
	
	
	
	
	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
		// Si el usuario no est� logueado ocultamos el campo para escribir comentarios
		if (!MyApplication.logged_user){
			layout_write_comment.setVisibility(View.GONE);
		}
	}
	
	
	

	/**
	 * Sube un comentario
	 * @author Alejandro
	 *
	 */
	private class SendComment extends AsyncTask<Void,Integer,Integer> {
		 
		private Context mContext;
		
	    public SendComment (Context context){
	         mContext = context;
	    }
		
		@Override
		protected void onPreExecute(){
		
			// Mostramos el ProgressDialog
			progress.setTitle("Enviando comentario ...");
			progress.setMessage("Espere, por favor");
			progress.setCancelable(false);
			progress.show();
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	    	String urlString = MyApplication.SERVER_ADDRESS + "api/v1/comment/";        

	        HttpResponse response = null;
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();
	             
	             // Obtenemos el comentario en formato UTF-8
	             written_comment = edit_new_comment.getText().toString();
	             
	             dato.put("user", MyApplication.resource_uri);
	             dato.put("photo", "/api/v1/photo/" + photo_id +"/"); // Formato: /api/v1/photo/id/
	             dato.put("text", written_comment);

	             // Formato UTF-8 (�,�,�,...)
	             StringEntity entity = new StringEntity(dato.toString(),  HTTP.UTF_8);
	             post.setEntity(entity);

	             response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	             
	             Log.i("WE",response_str);
	        }
	        
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
	        
	        // Devolvemos el resultado 
	        return (response.getStatusLine().getStatusCode());
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			Toast toast; 
			
			// Si es codigo 2xx --> OK
			if (result >= 200 && result <300){
	        	Log.v("WE","comentario enviado");
	        	toast = Toast.makeText(mContext, "Comentario enviado", Toast.LENGTH_LONG);
	        	
	        	// Vaciamos el editext
	        	edit_new_comment.setText("");
	        	
	        	// Mostramos de nuevo los comentarios
	        	new GetComments(mContext).execute();
	        }
	        else{
	        	Log.v("WE","comentario no enviado");
	        	toast = Toast.makeText(mContext, "Hubo alg�n error enviando el comentario", Toast.LENGTH_LONG);
	        	
	        }
			toast.show();
	    }
	}
	
	
	
	
	/**
	 * Muestra la lista de comentarios para esa foto
	 * @author Alejandro
	 *
	 */
	private class GetComments extends AsyncTask<Void,Integer,Integer> {
		 	
		private Context mContext;
	    public GetComments(Context context){
	         mContext = context;
	    }
		
		@Override
		protected void onPreExecute(){

			// Mostramos el ProgressDialog
			progress.setTitle("Obteniendo comentarios ...");
			progress.setMessage("Espere, por favor");
			progress.setCancelable(false);
			progress.show();
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			comments.clear();
			
			HttpClient httpClient = new DefaultHttpClient();
			
			HttpGet del = new HttpGet(MyApplication.SERVER_ADDRESS + "api/v1/comment/?photo__id=" + photo_id);
			 
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
		            	 
					     // A�adimos el comentario en formato UTF-8 (caracteres �,�,...)
					     comments.add(new Comment(comment, username, date));					 
		            }		            
			}
			catch(Exception ex){
				Log.e("ServicioRest","Error!", ex);
			}
			
			// Devolvemos el c�digo de respuesta
	        return (response.getStatusLine().getStatusCode());
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			
			
			// Si es codigo 2xx --> OK
	        if (result >= 200 && result <300){
	        	Log.v("WE","comentarios listados");
	        	//toast = Toast.makeText(mContext, "Comentarios listados", Toast.LENGTH_LONG);
	        	commentsAdapter.notifyDataSetChanged();
	        }
	        else{
	        	Log.v("WE","comentarios no listados");
	        	Toast toast;
	        	toast = Toast.makeText(mContext, "Hubo alg�n error obteniendo los comentarios", Toast.LENGTH_LONG);
	        	toast.show();
	        }        
	    }
	}
	
	
	

	
}
