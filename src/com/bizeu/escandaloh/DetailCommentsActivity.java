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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;

public class DetailCommentsActivity extends SherlockActivity {

	private ListView list_comments;
	private EditText edit_new_comment;
	private TextView txt_title;
	private TextView txt_user;
	private ImageView img_send;
	private TextView txt_count_characteres;
	private TextView txt_num_comments;
	private FetchableImageView img_photo;
	private LinearLayout layout_write_comment;
	private LinearLayout ll_list_comments;
	private ProgressBar progress_list_comments;
	private String written_comment;	
	private ArrayAdapter<Comment> commentsAdapter;
	private ArrayList<Comment> comments;
	private String photo_id;
	private ProgressDialog progress;
	private String route_image;
	private String user;
	private String title;
	private boolean add_comment; // Este booleano nos indicará si estamos obteniendo comentarios por haber añadido uno nuevo o no
	private boolean any_error;
	private Activity acti;
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);
		

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup)findViewById(R.id.lay_pantalla_comentarios));
		
		if (getIntent() != null){
			photo_id = getIntent().getExtras().getString("id");
			route_image = getIntent().getExtras().getString("route_image");	
			user = getIntent().getExtras().getString("user");
			title = getIntent().getExtras().getString("title");
		}
		
		acti = this;
		
		// Quitamos el action bar
		//getSupportActionBar().hide();
		
		// Quitamos el texto del action bar
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		final Context context = this.getApplicationContext();
		
		list_comments = (ListView) findViewById(R.id.list_comments);
		img_photo = (FetchableImageView) findViewById(R.id.img_photo_list_comments);
		img_photo.setImage(route_image, R.drawable.previsualizacion_foto);
		layout_write_comment = (LinearLayout) findViewById(R.id.ll_comments_write);
		ll_list_comments = (LinearLayout) findViewById(R.id.ll_comments_comments);
		progress_list_comments = (ProgressBar) findViewById(R.id.prog_list_comments);
		
		comments = new ArrayList<Comment>();
		commentsAdapter = new CommentAdapter(this,R.layout.comment, comments, user);
		list_comments.setAdapter(commentsAdapter);
		
		txt_num_comments = (TextView) findViewById(R.id.txt_comments_num_comments);
		
		txt_count_characteres = (TextView) findViewById(R.id.txt_count_characteres);
		
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
		
		
		img_send = (ImageView) findViewById(R.id.img_send_new_comment);
		img_send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Si hay conexión
				if (Connectivity.isOnline(acti)){
					written_comment = edit_new_comment.getText().toString();
					// Si ha escrito algo y la longitud es menor de 500 caracteres lo intentamos enviar
					if (!written_comment.equals("") && written_comment.length() < 501){
						Log.v("WE","Longitud comentario: " + written_comment.length());
						new SendComment(context).execute();
					}	
				}
				else{
		        	Toast toast;
		        	toast = Toast.makeText(acti, "No dispone de conexión a internet", Toast.LENGTH_SHORT);
		        	toast.show();
				}
			}
		});
			
		// Mostramos el usuario
		txt_user = (TextView) findViewById(R.id.txt_comments_user);
		txt_user.setText(user);
		
		// Mostramos el titulo entre comillas dobles (quotation mark)
		txt_title = (TextView) findViewById(R.id.txt_comments_title);
		txt_title.setText(title);

		progress = new ProgressDialog(this);
		
		add_comment = false; // No hemos añadido un nuevo comentario
		
		// Si hay conexión
		if (Connectivity.isOnline(this)){
			new GetComments(context, add_comment).execute();
		}
		else{
        	Toast toast;
        	toast = Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_LONG);
        	toast.show();
		}
	}
	
	
	
	
	
	
	/**
	 * onResume
	 */
	@Override
	public void onResume(){
		super.onResume();
		
		// Si el usuario no está logueado ocultamos el campo para escribir comentarios
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
	         any_error = false;
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

	             // Formato UTF-8 (ñ,á,ä,...)
	             StringEntity entity = new StringEntity(dato.toString(),  HTTP.UTF_8);
	             post.setEntity(entity);

	             response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	             
	             Log.i("WE",response_str);
	        }
	        
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	             any_error = true; // Indicamos que hubo algún error
	        }
	        
	        if (any_error){
	        	return 666;
	        }
	        else{
		        // Devolvemos el resultado 
		        return (response.getStatusLine().getStatusCode());
	        }
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {
			
			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
		        progress.dismiss();
		    }
			
			// Si hubo algún error mostramos un mensaje
			if (any_error){
				Toast toast = Toast.makeText(acti, "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			else{
				// Si es codigo 2xx --> OK
				if (result >= 200 && result <300){
		        	Log.v("WE","comentario enviado");
		        	
		        	// Vaciamos el editext
		        	edit_new_comment.setText("");
		            	
		        	// Mostramos de nuevo los comentarios (indicamos que si hemos enviado un comentario)
		        	add_comment = true;
		        	new GetComments(mContext, add_comment).execute();
		        	
		        }
		        else{
		        	Log.v("WE","comentario no enviado");
		        	Toast toast;
		        	toast = Toast.makeText(mContext, "Hubo algún error enviando el comentario", Toast.LENGTH_LONG);
		        	toast.show();        	
		        }	
			}
	    }
	}
	
	
	
	
	/**
	 * Muestra la lista de comentarios para esa foto
	 * @author Alejandro
	 *
	 */
	private class GetComments extends AsyncTask<Void,Integer,Integer> {
		 	
		private Context mContext;
		private boolean add_comm;
		
	    public GetComments(Context context, boolean add_comment){
	         mContext = context;
	         add_comm = add_comment;
	    }
		
		@Override
		protected void onPreExecute(){

			// Si estamos obteniendo comntarios sin haber enviado uno mostramos el progress bar
			if (!add_comm){
				progress_list_comments.setVisibility(View.VISIBLE);
				list_comments.setVisibility(View.GONE);		
			}
			
			any_error = false;
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
		            	 String resource_uri = escanObject.getString("user");
		            	// SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		            	// Date date = formatter.parse(date_string);
		            	 
					     // Añadimos el comentario en formato UTF-8 (caracteres ñ,á,...)
					     comments.add(new Comment(comment, username, date, resource_uri));					 
		            }		            
			}
			catch(Exception ex){
				Log.e("ServicioRest","Error!", ex);
				any_error = true; // Indicamos que hubo un error
			}
			
			// Si hubo algún error devolvemos 666
			if (any_error){
				return 666;
			}
			else{			
				// Devolvemos el código de respuesta
		        return (response.getStatusLine().getStatusCode());
			}
	    }

		
		@Override
	    protected void onPostExecute(Integer result) {

			
			// Si estamos obteniéndolos porque hemos enviado uno
			if (add_comm){
				// Quitamos el ProgressDialog
				if (progress.isShowing()) {
			        progress.dismiss();
			    }
			}
			// Si no, mostramos el listview y quitamos el progress bar
			else{			
				progress_list_comments.setVisibility(View.GONE);
				list_comments.setVisibility(View.VISIBLE);
				ll_list_comments.setGravity(Gravity.TOP);
			}
			
				
			// Si hubo algún error 
			if (result == 666){
				Toast toast = Toast.makeText(acti, "Lo sentimos, hubo un error inesperado", Toast.LENGTH_SHORT);
				toast.show();
			}
			// No hubo ningún error extraño
			else{
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	Log.v("WE","comentarios listados");
		        	commentsAdapter.notifyDataSetChanged();
		        	
		        	// Actualizamos el indicador de número de comentarios
		        	if (comments.size() == 1){
		        		txt_num_comments.setText(comments.size() + " comentario");
		        	}
		        	else{
			        	txt_num_comments.setText(comments.size() + " comentarios");
		        	}
		        }
		        else{
		        	Log.v("WE","comentarios no listados");
		        	Toast toast;
		        	toast = Toast.makeText(mContext, "Hubo algún error obteniendo los comentarios", Toast.LENGTH_LONG);
		        	toast.show();
		        } 
			}
       
	    }
	}
	
	
	

	
}
