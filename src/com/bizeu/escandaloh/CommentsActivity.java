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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.util.Connectivity;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CommentsActivity extends SherlockActivity {

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    VARIABLES                                                      |
	// -----------------------------------------------------------------------------------------------------
	
	public static String LST_COMMENT = "last_comment";
	public static String NUM_COMMENTS = "num_comments";
	
	private ListView list_comments;
	private LinearLayout img_send;
	private EditText edit_comment;
	private LinearLayout ll_screen;
	private FetchableImageView img_fondo;
	private ProgressBar prog_loading;

	private ArrayList<Comment> array_comments = new ArrayList<Comment>();
	private CommentAdapter commentsAdapter;
	private boolean any_error;
	private String id;
	private ProgressDialog send_progress;
	private Context mContext;
	private String url_photo;
	private GetCommentsTask getCommentsAsync;

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS  ACTIVITY                                              |
	// -----------------------------------------------------------------------------------------------------
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.comments);

		mContext = this;

		if (getIntent() != null) {
			id = getIntent().getExtras().getString(ScandalFragment.ID);
			url_photo = getIntent().getExtras().getString(ScandalFragment.URL);
		}

		list_comments = (ListView) findViewById(R.id.list_comments_listcomments);
		edit_comment = (EditText) findViewById(R.id.edit_comments_comment);
		img_send = (LinearLayout) findViewById(R.id.ll_comments_send);
		ll_screen = (LinearLayout) findViewById(R.id.ll_comments_screen);
		img_fondo = (FetchableImageView) findViewById(R.id.img_comments_background);
		prog_loading = (ProgressBar) findViewById(R.id.prog_comments_loading);	
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_comments, null);
		actBar.setCustomView(view);
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.s_mezcla);
		
		// Fondo de la pantalla
		img_fondo.setImage(url_photo, R.drawable.cargando);		

		// Obtenemos los comentarios
		commentsAdapter = new CommentAdapter(this, R.layout.comment_izquierda,
				R.layout.comment_derecha, array_comments);
		list_comments.setAdapter(commentsAdapter);
		getCommentsAsync =  new GetCommentsTask();
		getCommentsAsync.execute();

		send_progress = new ProgressDialog(this);
		send_progress.setTitle(R.string.enviando_comentario);
		send_progress.setMessage(getResources().getString(R.string.espera_por_favor));
		send_progress.setCancelable(false);
	}

	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();
		
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);

		// Si est� logueado puede escribir comentarios
		if (MyApplication.logged_user) {
			edit_comment.setHint(getResources().getString(
					R.string.que_opinas));
			edit_comment.setOnClickListener(null);
			img_send.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Hay conexi�n
					if (Connectivity.isOnline(mContext)) {
						// Si ha escrito algo
						if (edit_comment.getText().toString().length() > 0) {
							new SendCommentTask().execute();
						}
					}
					// No hay conexi�n
					else {
						Toast toast = Toast.makeText(mContext,
								R.string.no_dispones_de_conexion,
								Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			});
		}

		// Si no est� logueado le mandamos a la pantalla de login
		else {
			edit_comment.setHint(getResources().getString(
					R.string.inicia_sesion_para_comentar));
			edit_comment.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(CommentsActivity.this,
							LoginSelectActivity.class);
					startActivity(i);
				}
			});
		}
	}
	
	
	
	/**
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent returnIntent = new Intent();
				   // Cancelamos si se estuviesen obteniendo comentarios
				   cancelGetComments();
				if (array_comments.size() > 0){
					// Devolvemos el �ltimo comentario
					returnIntent.putExtra(LST_COMMENT, array_comments.get(array_comments.size()-1));
				 	// Devolvemos el n� de comentarios
				 	returnIntent.putExtra(NUM_COMMENTS, array_comments.size());
				}
				setResult(RESULT_OK, returnIntent);
				finish();	
	    	break;
		}
		return true;
	}

	
	
	/**
	 * onPause
	 */
	@Override
	public void onPause() {
		super.onPause();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ll_screen.getWindowToken(), 0);
	}
	
	
	
	/**
	 * onStop
	 */
	@Override
	public void onStop() {
		super.onStop();
		// Paramos Flurry
		FlurryAgent.onEndSession(mContext);
	}
	
	
	/**
	 * onDestroy
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		// Cancelamos los comentarios que se estuvieran obteniendo
		cancelGetComments();
	}
	
	
	
	/**
	 * onBackPressed
	 */
	@Override
	public void onBackPressed() {
	   Intent returnIntent = new Intent();
	   // Cancelamos si se estuviesen obteniendo comentarios
	   cancelGetComments();
	   if (array_comments.size() > 0){
		   // Devolvemos el �ltimo comentario
		   returnIntent.putExtra(LST_COMMENT, array_comments.get(array_comments.size()-1));
		   // Devolvemos el n� de comentarios
		   returnIntent.putExtra(NUM_COMMENTS, array_comments.size());
	   }
	   setResult(RESULT_OK, returnIntent);
	   finish();   
	}
	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS                                                        |
	// -----------------------------------------------------------------------------------------------------
	

	/**
	 * Cancela si hubiese alguna hebra obteniendo comentarios
	 */
	private void cancelGetComments() {
		if (getCommentsAsync != null) {
			if (getCommentsAsync.getStatus() == AsyncTask.Status.PENDING
					|| getCommentsAsync.getStatus() == AsyncTask.Status.RUNNING) {
				getCommentsAsync.cancel(true);
			}
		}
	}
	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                CLASES                                                             |
	// -----------------------------------------------------------------------------------------------------
	
	

	/**
	 * Muestra la lista de comentarios
	 * 
	 */
	private class GetCommentsTask extends AsyncTask<Void, Integer, Integer> {

		String c_date;
		String c_id;
		String c_photo;
		String c_resource_uri;
		String c_social_network;
		String c_text;
		String c_user;
		String c_user_id;
		String c_username;
		String c_avatar;

		@Override
		protected void onPreExecute() {
			any_error = false;
			array_comments.clear();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet del = new HttpGet(MyApplication.SERVER_ADDRESS
					+ "/api/v1/comment/?photo__id=" + id + "&limit=0");
			del.setHeader("content-type", "application/json");
			HttpResponse response = null;

			try {
				response = httpClient.execute(del);
				String respStr = EntityUtils.toString(response.getEntity());

				Log.i("WE", "com: " + respStr.toString());

				JSONObject respJSON = new JSONObject(respStr);

				// Parseamos el json para obtener los escandalos
				JSONArray escandalosObject = null;

				escandalosObject = respJSON.getJSONArray("objects");

				for (int i = 0; i < escandalosObject.length(); i++) {
					JSONObject escanObject = escandalosObject.getJSONObject(i);

					c_date = escanObject.getString("date");
					c_id = escanObject.getString("id");
					c_photo = null;
					c_social_network = escanObject.getString("social_network");
					c_text = new String(escanObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
					c_user_id = escanObject.getString("user_id");
					c_username = escanObject.getString("username");
					c_avatar = escanObject.getString("avatar");

					Comment commentAux = new Comment(c_date, c_id, c_photo,
							c_resource_uri, c_social_network, c_text, c_user,
							c_user_id, c_username, c_avatar);
					array_comments.add(commentAux);
				}

			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				any_error = true; // Indicamos que hubo un error
			}

			// Si hubo alg�n error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el c�digo de respuesta
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			if (!isCancelled()){
				if (send_progress.isShowing()) {
					send_progress.dismiss();
				}
				
				// Ocultamos el loading y mostramos la lista de comentarios
				prog_loading.setVisibility(View.GONE);

				// Si hubo alg�n error
				if (result == 666) {
					Toast toast = Toast.makeText(mContext, getResources()
							.getString(R.string.lo_sentimos_hubo),
							Toast.LENGTH_SHORT);
					toast.show();
				}

				// No hubo ning�n error extra�o
				else {
					// Si es codigo 2xx --> OK if (result >= 200 && result <300){
					commentsAdapter.notifyDataSetChanged();
					// Nos posicionamos en el �ltimo comentario
					list_comments.setSelection(list_comments.getAdapter().getCount() - 1);
				}

				// Si no hay comentarios y a�n seguimos en la pantalla: abrimos el teclado
				if (array_comments.size() == 0 && !isCancelled()) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}
			}			
		}
	}

	
	/**
	 * Envia un comentario
	 * 
	 */
	private class SendCommentTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			any_error = false;
			// Mostramos el ProgressDialog
			send_progress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS
					+ "/api/v1/comment/";

			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				post.setHeader("Content-Type", "application/json");
				post.setHeader("Session-Token", MyApplication.session_token);

				JSONObject dato = new JSONObject();

				// Obtenemos el comentario en formato UTF-8
				String written_comment = edit_comment.getText().toString();

				dato.put("user", MyApplication.resource_uri);
				dato.put("photo", "/api/v1/photo/" + id + "/"); // Formato: /api/v1/photo/id/
				dato.put("text", written_comment);

				// Formato UTF-8 (�,�,�,...)
				StringEntity entity = new StringEntity(dato.toString(),
						HTTP.UTF_8);
				post.setEntity(entity);

				response = client.execute(post);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);

				Log.i("WE", response_str);
			}

			catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
				any_error = true; // Indicamos que hubo alg�n error
			}

			if (any_error) {
				return 666;
			} else {
				// Devolvemos el resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Si hubo alg�n error mostramos un mensaje
			if (any_error) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
				// Quitamos el ProgressDialog
				if (send_progress.isShowing()) {
					send_progress.dismiss();
				}

			} else {
				// Si es codigo 2xx --> OK
				if (result >= 200 && result < 300) {
					// Vaciamos el editext
					edit_comment.setText("");

					// Mostramos de nuevo los comentarios (indicamos que hemos enviado un comentario)
					new GetCommentsTask().execute();
				} else {
					Toast toast;
					toast = Toast
							.makeText(
									mContext,
									getResources()
											.getString(
													R.string.hubo_algun_error_enviando_comentario),
									Toast.LENGTH_LONG);
					toast.show();
					// Quitamos el ProgressDialog
					if (send_progress.isShowing()) {
						send_progress.dismiss();
					}
				}
			}
		}
	}

}
