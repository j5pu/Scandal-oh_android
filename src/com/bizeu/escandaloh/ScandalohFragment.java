package com.bizeu.escandaloh;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ScandalohFragment extends Fragment {

	private static final String ID = "id";
    private static final String URL = "url";
    private static final String URL_BIG = "url_big";
    private static final String TITLE = "title";
    private static final String NUM_COMMENTS = "num_comments";
    private static final String HAS_AUDIO = "has_audio";
    private static final String USER_NAME = "user_name";
    private static final String DATE = "date";
    private static final String URI_AUDIO = "uri_audio";
 
    private TextView num_com ;
	private LinearLayout ll_comments;
    private ListView list_comments;
    private EditText edit_write_comment;
    
    private String id;
    private String url;
    private String url_big;
    private String title;
    private int num_comments;
    private boolean has_audio;
    private String user_name;
    private String date;
    private Bitmap bitma;
	private boolean any_error;
	private int chosen_report; // 1:Copyright      2:Ilegalcontent      3:Spam
	private String uri_audio;	
	private CommentAdapter commentsAdapter;
	private ArrayList<Comment> comments;
	private boolean reproduciendo; // Nos indica si está reproduciendo el audio en un momento dado
	private SharedPreferences prefs;


    
    /**
     * Crea y devuelve una nueva instancia de un fragmento
     * @param escan Escandalo para dicho fragmento
     * @return Fragmento con el escándalo
     */
    public static ScandalohFragment newInstance(Scandaloh escan) {
        // Instanciamos el fragmento
        ScandalohFragment fragment = new ScandalohFragment();
 
        // Guardamos los datos del fragmento (del escándalo)
        Bundle bundle = new Bundle();
        bundle.putString(ID, escan.getId());
        bundle.putString(URL, escan.getRouteImg());
        bundle.putString(URL_BIG, escan.getRouteImgBig());
        bundle.putString(TITLE, escan.getTitle());
        bundle.putInt(NUM_COMMENTS, escan.getNumComments());
        bundle.putBoolean(HAS_AUDIO, escan.hasAudio());
        bundle.putString(USER_NAME, escan.getUser());
        bundle.putString(DATE, escan.getDate());
        bundle.putString(URI_AUDIO, escan.getUriAudio());

        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
 
        // Devolvemos el fragmento
        return fragment;
    }
 
    
    
    /**
     * onCreate
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Obtenemos los valores del fragmento (del escándalo)
        this.id = (getArguments() != null) ? getArguments().getString(ID) : null;
        this.url = (getArguments() != null) ? getArguments().getString(URL) : null;
        this.url_big = (getArguments() != null) ? getArguments().getString(URL_BIG) : null;
        this.title = (getArguments() != null) ? getArguments().getString(TITLE) : null;
        this.num_comments = (getArguments() != null) ? getArguments().getInt(NUM_COMMENTS) : 0;
        this.has_audio = (getArguments() != null) ? getArguments().getBoolean(HAS_AUDIO) : false;
        Log.v("WE","has_audio: " + has_audio);
        this.user_name = (getArguments() != null) ? getArguments().getString(USER_NAME) : null;
        this.date = (getArguments() != null) ? getArguments().getString(DATE) : null;
        this.uri_audio = (getArguments() != null) ? getArguments().getString(URI_AUDIO) : null;
        
        // Preferencias
		prefs = getActivity().getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
    }
 
    
    
    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 
    	Log.v("WE","ENTRA EN ONCREATEVIEW");
    	Log.v("WE","num com: " + num_comments);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.escandalo3, container, false);    
        
        SlidingUpPanelLayout layout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
        layout.setIsTransparent(true);
        
        // FOTO
        FetchableImageView img = (FetchableImageView) rootView.findViewById(R.id.img_escandalo_foto);
        img.setImage(this.url, R.drawable.cargando);      
     
        img.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Evitamos que se pulse dos o más veces en las fotos (para que no se abra más de una vez)
				if (!MyApplication.PHOTO_CLICKED){
					MyApplication.PHOTO_CLICKED = true;
					
					// Paramos si hubiera algún audio reproduciéndose
					Audio.getInstance(getActivity().getBaseContext()).releaseResources();
					
					// Mostramos la pantalla de la foto en detalle
					Intent i = new Intent(getActivity(), DetailPhotoActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ImageView imView = (ImageView) v;
					Bitmap bitm = ((BitmapDrawable)imView.getDrawable()).getBitmap();
					byte[] bytes = ImageUtils.bitmapToBytes(bitm);
					i.putExtra("bytes", bytes);
					Log.v("WE","uri audio antes del imagen: " + uri_audio);
					i.putExtra("uri_audio", uri_audio);				
					getActivity().startActivity(i);				
				}	
			}
		});
        
        
        img.setOnLongClickListener(new View.OnLongClickListener() {
        	
			@Override
			public boolean onLongClick(View v) {			
				
				// Paramos si hubiera algún audio reproduciéndose
				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
				
				// Mostramos la opción de guardar la foto en la galería
				final CharSequence[] items = {getResources().getString(R.string.guardar_foto_galeria)};
				 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			        builder.setItems(items, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int item) {
			            	
			            	// Guardamos en galería
			                if (items[item].equals(R.string.guardar_foto_galeria)) {
			                	new SaveImageTask(getActivity()).execute(url_big);     			   
			                } 			                
			            }
			        });
			        builder.show();
			        
				return true;
			}
		});
        
        // AUDIO
        /*
        ImageView aud = (ImageView) rootView.findViewById(R.id.img_escandalo_audio);
        if(has_audio){
        	aud.setVisibility(View.VISIBLE);
            // Si tiene autoreproducir activado lo iniciamos
            boolean autoplay = prefs.getBoolean(MyApplication.AUTOPLAY_ACTIVATED, false);
            if (autoplay){
            	reproduciendo = true;
    			// Paramos si hubiera algún audio reproduciéndose
    			Audio.getInstance(getActivity().getBaseContext()).releaseResources();
    			Log.v("WE","uri_audio: "+  uri_audio);
            	new PlayAudioTask().execute(uri_audio);
            }
        }
        else{
        	aud.setVisibility(View.INVISIBLE);
        }
        

        aud.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Paramos si hubiera algún audio reproduciéndose
				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
				
				// Lo reproducimos		
				if (uri_audio != null){
					new PlayAudioTask().execute(uri_audio);	
				}
			}
		});
		*/
		
        
        
        
		// COMENTARIOS
        /*
		comments = new ArrayList<Comment>();
		list_comments = (ListView) rootView.findViewById(R.id.lv_comments);
		commentsAdapter = new CommentAdapter(getActivity(),R.layout.comment, comments, user_name);
		list_comments.setAdapter(commentsAdapter);
		
		// Si hay conexión
		if (Connectivity.isOnline(getActivity().getBaseContext())){
			new GetComments(getActivity().getBaseContext(), true).execute();
		}
		*/
		
		/*
		edit_write_comment = (EditText) rootView.findViewById(R.id.edit_write_comment);
		edit_write_comment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		*/
        
       /*
        // Número de comentarios
        num_com = (TextView) rootView.findViewById(R.id.txt_numero_comentarios);
        num_com.setText(Integer.toString(num_comments));
        num_com.setOnClickListener(new View.OnClickListener() {
			
        	@Override
			public void onClick(View v) {			
				// Paramos si hubiera algún audio reproduciéndose
				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
				
				// Mostramos la pantalla de comentarios
				Intent i = new Intent(getActivity().getBaseContext(), DetailCommentsActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("id", id);
				i.putExtra("route_image", url);
				i.putExtra("user", user_name);
				i.putExtra("title", title);
				getActivity().getBaseContext().startActivity(i);	
			}
		});
        
        */
        // Título
        //TextView tit = (TextView) rootView.findViewById(R.id.txt_escandalo_titulo);
        //tit.setText(title);
        
        

        
        /*
        
        // Nombre de usuario
        TextView user_na = (TextView) rootView.findViewById(R.id.txt_escandalo_name_user);
        user_na.setText(user_name);
        
        // Fecha
        TextView dat = (TextView) rootView.findViewById(R.id.txt_escandalo_date);
        dat.setText(changeFormatDate(date));  
        
        // Compartir 
        ImageView share = (ImageView) rootView.findViewById(R.id.img_escandalo_compartir);
        share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				 // Creamos un menu para elegir entre compartir y denunciar foto
				 final CharSequence[] opciones_compartir = {"Compartir scándalOh!", "Reportar scándalOh!"};
				 AlertDialog.Builder dialog_compartir = new AlertDialog.Builder(getActivity());
				 dialog_compartir.setTitle("Selecciona opción");
				 dialog_compartir.setItems(opciones_compartir, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int item) {
			            	
			            	// Compartir scándalOh
			                if (opciones_compartir[item].equals("Compartir scándalOh!")) {
			    				// Paramos si hubiera algún audio reproduciéndose
			    				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
			    				
			    				// Compartimos la foto
			    				Uri screenshotUri = Uri.parse(url_big);	
			    				new ShareImageTask(getActivity().getBaseContext(), title).execute(screenshotUri.toString());	      			   
			                } 
			                
			                // Reportar foto
			                else if (opciones_compartir[item].equals("Reportar scándalOh!")) {
			                	
			                	// Si el usuario está logueado
			                	if (MyApplication.logged_user){
				                	// Creamos un menu para elegir el tipo de report
				                	final CharSequence[] opciones_reportar = {"Material ofensivo", "Spam", "Copyright"};
				                	AlertDialog.Builder dialog_report = new AlertDialog.Builder(getActivity());
				                	dialog_report.setTitle("Reportar esta foto por");
				                	dialog_report.setItems(opciones_reportar, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int item) {

											// Material ofensivo
											if (opciones_reportar[item].equals("Material ofensivo")){
												chosen_report = 2;
											}
											// Spam
											else if (opciones_reportar[item].equals("Spam")){
												chosen_report = 3;
											}
											// Copyright
											else if (opciones_reportar[item].equals("Copyright")){
												chosen_report = 1;
											}	
											new ReportPhoto(getActivity().getBaseContext()).execute();
										}
									});
				                	dialog_report.show();
			                	}
			                	// No está logueado
			                	else{
			    		        	Toast toast = Toast.makeText(getActivity().getBaseContext(), "Debes iniciar sesión para reportar", Toast.LENGTH_LONG);
			    		        	toast.show();        
			                	}

			                } 
			            }
			        });
				 dialog_compartir.show();			
			}
		});
		*/
        
        // Devolvemos la vista
        return rootView;
    }
    
   

    
    
	/**
	 * Comparte un escándalo
	 *
	 */
	private class ShareImageTask extends AsyncTask<String, String, String> {
	    private Context context;
	    private ProgressDialog pDialog;
	    URL myFileUrl;
	    String title;
	    Bitmap bmImg = null;
	    Intent share;
	    File file;

	    public ShareImageTask(Context context, String title) {
	        this.context = context;
	        this.title = title;
	    }

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();

	        pDialog = new ProgressDialog(getActivity());
	        pDialog.setMessage(getResources().getString(R.string.preparando_para_compartir));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();		     
	    }

	    @Override
	    protected String doInBackground(String... args) {
	    	
	    	// Obtenemos la foto desde la url de amazon
	        try {
	            myFileUrl = new URL(args[0]);
	            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
	            conn.setDoInput(true);
	            conn.connect();
	            InputStream is = conn.getInputStream();
	            bmImg = BitmapFactory.decodeStream(is);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            String path = myFileUrl.getPath();
	            String idStr = path.substring(path.lastIndexOf('/') + 1);
	            File filepath = Environment.getExternalStorageDirectory();
	            File dir = new File(filepath.getAbsolutePath()+ "/ScándalOh/");
	            dir.mkdirs();
	            String fileName = idStr;
	            // Guardamos la ruta de la foto para más adelante eliminarla
	            MyApplication.FILES_TO_DELETE.add(filepath.getAbsolutePath() + "/ScándalOh/" + idStr);
	            file = new File(dir, fileName);
	            FileOutputStream fos = new FileOutputStream(file);
	            bmImg.compress(CompressFormat.JPEG, 100, fos);
	            fos.flush();
	            fos.close();

	        } catch (Exception e) {
	            e.printStackTrace();
	            
	             // Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(context);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(context, null) // Context and optional collection of package names to be used in reporting the exception.
				                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
				                       e),                                                             // The exception.
				                       false).build());		                       
	        }

	        return null;
	    }

	    @Override
	    protected void onPostExecute(String args) {
	    	// Quitamos el progress dialog
	        pDialog.dismiss();  
	        
	        // Ejecutamos el intent de compartir
	        share = new Intent(Intent.ACTION_SEND);		        
	        share.putExtra(Intent.EXTRA_SUBJECT, R.string.deberias_ver_esto_que);
	        share.putExtra(Intent.EXTRA_TEXT, title);
	        share.putExtra(Intent.EXTRA_TITLE, title);	        
	        share.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file));
	        share.setType("image/jpeg");
	        getActivity().startActivityForResult(Intent.createChooser(share, getResources().getString(R.string.compartir_scandaloh_con)), MainActivity.SHARING);
	    }
	}	
	
	
	
	/**
	 * Guarda una foto en la galería
	 *
	 */
	private class SaveImageTask extends AsyncTask<String, String, String> {
	    private Context context;
	    private ProgressDialog pDialog;

	    public SaveImageTask(Context context) {
	        this.context = context;
	    }

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();

	        pDialog = new ProgressDialog(context);
	        pDialog.setMessage(getResources().getString(R.string.guardando_dospuntos));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();		     
	    }

	    @Override
	    protected String doInBackground(String... args) {
	        try {
		    	// Obtenemos la foto desde la url
            	bitma = ImageUtils.getBitmapFromURL(args[0]);

	        } catch (Exception e) {
	            e.printStackTrace();
	                     
	             // Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(context);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(context, null) // Context and optional collection of package names to be used in reporting the exception.
				                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
				                       e),                                                             // The exception.
				                       false).build());		 
	        }

	        return null;
	    }

	    @Override
	    protected void onPostExecute(String args) {
	    	// Quitamos el progress dialog
	        pDialog.dismiss();  
	        
	        // Guardamos la foto en la galería
			ImageUtils.saveBitmapIntoGallery(bitma, context);	
	    }
	}	
	
	
	
	
	
	

	/**
	 * Reporta una foto
	 *
	 */
	private class ReportPhoto extends AsyncTask<Void,Integer,Integer> {
		 
	    private ProgressDialog pDialog;
		private Context mContext;
		
	    public ReportPhoto (Context context){
	         mContext = context;
	         any_error = false;
	    }
		
		@Override
		protected void onPreExecute(){
			// Mostramos el ProgressDialog
	        pDialog = new ProgressDialog(getActivity());
	        pDialog.setMessage(getResources().getString(R.string.reportando_scandaloh));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();	
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	    	String urlString = MyApplication.SERVER_ADDRESS + "api/v1/photocomplaint/";        

	        HttpResponse response = null;
	        
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             
	             JSONObject dato = new JSONObject();
	             
	             dato.put("user", MyApplication.resource_uri);
	             dato.put("photo", "/api/v1/photo/" + id +"/");
	             dato.put("category", chosen_report);

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
	                          
				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
					                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
					                       ex),                                                             // The exception.
					                       false).build());  			
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
			if (pDialog.isShowing()) {
		        pDialog.dismiss();
		    }
			
			// Si hubo algún error mostramos un mensaje
			if (any_error){
				Toast toast = Toast.makeText(mContext, R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			else{
				
				// Si es codigo 2xx --> OK
				if (result >= 200 && result <300){
					Toast toast = Toast.makeText(mContext, R.string.report_enviado_correctamente, Toast.LENGTH_SHORT);
					toast.show();      	
		        }
		        else{
		        	Toast toast;
		        	toast = Toast.makeText(mContext, R.string.hubo_algun_error_enviando_comentario, Toast.LENGTH_LONG);
		        	toast.show();        	
		        }	      
			}
	    }
	}
	
	
	/**
	 * Reproduce el audio
	 *
	 */
	private class PlayAudioTask extends AsyncTask<String,Integer,Boolean> {
		 
		@Override
		protected void onPreExecute(){
			
		}

		@Override
	    protected Boolean doInBackground(String... params) {
	    	
	    	Audio.getInstance(getActivity().getBaseContext()).startPlaying("http://scandaloh.s3.amazonaws.com/" + params[0]);							
	        return false;
	    }	
	}
	
	
	
	 
    /**
	 * Muestra la lista de comentarios para esa foto
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
			/*
			// Si estamos obteniendo comntarios sin haber enviado uno mostramos el progress bar
			if (!add_comm){
				progress_list_comments.setVisibility(View.VISIBLE);
				list_comments.setVisibility(View.GONE);		
			}
			*/
			
			any_error = false;
			
		}
		
		@Override
	    protected Integer doInBackground(Void... params) {
			
			comments.clear();
			
			HttpClient httpClient = new DefaultHttpClient();		
			HttpGet del = new HttpGet(MyApplication.SERVER_ADDRESS + "api/v1/comment/?photo__id=" + id);		 
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
		            	 
		        	// Añadimos el comentario en formato UTF-8 (caracteres ñ,á,...)
		        	comments.add(new Comment(comment, username, date, resource_uri));					 
		        }		            
			}
			catch(Exception ex){
				Log.e("ServicioRest","Error!", ex);
				any_error = true; // Indicamos que hubo un error

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(new StandardExceptionParser(mContext, null) // Context and optional collection of package names to be used in reporting the exception.
				                       .getDescription(Thread.currentThread().getName(),                // The name of the thread on which the exception occurred.
				                       ex),                                                             // The exception.
				                       false).build());  
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
		
			/*
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
			*/
			
				
			// Si hubo algún error 
			if (result == 666){
				Toast toast = Toast.makeText(mContext, R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hubo ningún error extraño
			else{
				// Si es codigo 2xx --> OK
		        if (result >= 200 && result <300){
		        	commentsAdapter.notifyDataSetChanged();
		        	
		        	/*
		        	// Actualizamos el indicador de número de comentarios
		        	if (comments.size() == 1){
		        		txt_num_comments.setText(comments.size() + " comentario");
		        	}
		        	else{
			        	txt_num_comments.setText(comments.size() + " comentarios");
		        	}
		        	*/
		        }
		        else{
		        	Toast toast;
		        	toast = Toast.makeText(mContext, R.string.no_se_pudieron_obtener_comentarios, Toast.LENGTH_LONG);
		        	toast.show();
		        } 
			}   
	    }
	}
    
	
	
	

	// ---------------------------------------------------------------------------------------------
	// --------------------    MÉTODOS PRIVADOS       ----------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
    /**
     * Transforma una fecha con formato AAAA-MM-DDTHH:MM:SS a formato DD-MM-AAAA
     * @param date Fecha a transformar
     * @return
     */
    private String changeFormatDate(String date){
        String date_without_time = (date.split("T",2))[0];   
        String year = date_without_time.split("-",3)[0];
        String month = date_without_time.split("-",3)[1];
        String day = date_without_time.split("-",3)[2];
        String final_date = day + "-" + month + "-" + year;     
        return final_date;
    }
    
    

    /**
     * Devuelve el número de comentarios
     */
    public int getNumComments(){
    	return num_comments;
    }
    
    
    
   
}
