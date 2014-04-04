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
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.util.Connectivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class CommentsActivity extends SherlockActivity {

	public static String LST_COMMENT = "last_comment";
	public static String NUM_COMMENTS = "num_comments";
	
	private ListView list_comments;
	private LinearLayout img_send;
	private LinearLayout ll_loading;
	private LinearLayout ll_icon;
	private EditText edit_comment;
	private LinearLayout screen;
	private FetchableImageView photo;
	private TextView txt_title;

	private ArrayList<Comment> array_comments = new ArrayList<Comment>();
	private CommentAdapter commentsAdapter;
	private boolean any_error;
	private String id;
	private ProgressDialog send_progress;
	private Context mContext;
	private String title;
	private String url_photo;

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.comments);

		mContext = this;

		if (getIntent() != null) {
			id = getIntent().getExtras().getString(ScandalohFragment.ID);
			title = getIntent().getExtras().getString(ScandalohFragment.TITLE);
			url_photo = getIntent().getExtras().getString(ScandalohFragment.URL);
		}

		list_comments = (ListView) findViewById(R.id.list_comments_listcomments);
		edit_comment = (EditText) findViewById(R.id.edit_comments_comment);
		img_send = (LinearLayout) findViewById(R.id.ll_comments_send);
		screen = (LinearLayout) findViewById(R.id.ll_comments_screen);
		ll_loading = (LinearLayout) findViewById(R.id.ll_comments_loading);
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_comments, null);
		actBar.setCustomView(view);
		actBar.setDisplayShowHomeEnabled(false);
		actBar.setDisplayShowTitleEnabled(false);
		photo = (FetchableImageView) findViewById(R.id.img_actionbar_comments_photo);
		photo.setImage(url_photo, R.drawable.loading);
		txt_title = (TextView) findViewById(R.id.txt_actionbar_comments_title);
		txt_title.setText(title);
		
		// Volvemos al carrusel
		ll_icon = (LinearLayout) findViewById(R.id.ll_actionbar_comments_icon);
		ll_icon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				if (array_comments.size() > 0){
					// Devolvemos el último comentario
					returnIntent.putExtra(LST_COMMENT, array_comments.get(array_comments.size()-1));
				 	// Devolvemos el nº de comentarios
				 	returnIntent.putExtra(NUM_COMMENTS, array_comments.size());
				}
				setResult(RESULT_OK, returnIntent);
				finish();			
			}
		});
		

		// Obtenemos los comentarios
		commentsAdapter = new CommentAdapter(this, R.layout.comment_izquierda,
				R.layout.comment_derecha, array_comments);
		list_comments.setAdapter(commentsAdapter);
		new GetCommentsTask().execute();

		send_progress = new ProgressDialog(this);
		send_progress.setTitle(R.string.enviando_comentario);
		send_progress.setMessage(getResources().getString(R.string.espera_por_favor));
	}

	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Si está logueado puede escribir comentarios
		if (MyApplication.logged_user) {
			edit_comment.setHint(getResources().getString(
					R.string.escribe_un_comentario));
			edit_comment.setOnClickListener(null);
			img_send.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Hay conexión
					if (Connectivity.isOnline(mContext)) {
						// Si ha escrito algo
						if (edit_comment.getText().toString().length() > 0) {
							new SendCommentTask().execute();
						}
					}
					// No hay conexión
					else {
						Toast toast = Toast.makeText(mContext,
								R.string.no_dispones_de_conexion,
								Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			});
		}

		// Si no está logueado le mandamos a la pantalla de login
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
	 * onPause
	 */
	@Override
	public void onPause() {
		super.onPause();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(screen.getWindowToken(), 0);
	}
	
	
	/**
	 * onBackPressed
	 */
	@Override
	public void onBackPressed() {
	   Intent returnIntent = new Intent();
	   if (array_comments.size() > 0){
		   // Devolvemos el último comentario
		   returnIntent.putExtra(LST_COMMENT, array_comments.get(array_comments.size()-1));
		   // Devolvemos el nº de comentarios
		   returnIntent.putExtra(NUM_COMMENTS, array_comments.size());
	   }
	   setResult(RESULT_OK, returnIntent);
	   finish();
	}

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
					//c_photo = escanObject.getString("photo");
					//c_resource_uri = escanObject.getString("user");
					c_social_network = escanObject.getString("social_network");
					c_text = new String(escanObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
					//c_user = escanObject.getString("user");
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

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null)
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), // The exception.
						false).build());
			}

			// Si hubo algún error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el código de respuesta
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			if (send_progress.isShowing()) {
				send_progress.dismiss();
			}
			
			// Ocultamos el loading y mostramos la lista de comentarios
			ll_loading.setVisibility(View.GONE);
			list_comments.setVisibility(View.VISIBLE);

			// Si hubo algún error
			if (result == 666) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			}

			// No hubo ningún error extraño
			else {
				// Si es codigo 2xx --> OK if (result >= 200 && result <300){
				commentsAdapter.notifyDataSetChanged();
				// Nos posicionamos en el último comentario
				list_comments.setSelection(list_comments.getAdapter().getCount() - 1);
			}

			if (array_comments.size() == 0) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
				dato.put("photo", "/api/v1/photo/" + id + "/"); // Formato:
																// /api/v1/photo/id/
				dato.put("text", written_comment);

				// Formato UTF-8 (ñ,á,ä,...)
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
				any_error = true; // Indicamos que hubo algún error

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null)
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), // The exception.
						false).build());
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

			// Si hubo algún error mostramos un mensaje
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
