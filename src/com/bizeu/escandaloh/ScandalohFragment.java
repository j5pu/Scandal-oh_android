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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.CommentAdapter;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
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
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;


public class ScandalohFragment extends SherlockFragment {

	private static final String ID = "id";
    private static final String URL = "url";
    private static final String URL_BIG = "url_big";
    private static final String TITLE = "title";
    private static final String NUM_COMMENTS = "num_comments";
    private static final String HAS_AUDIO = "has_audio";
    private static final String USER_NAME = "user_name";
    private static final String DATE = "date";
    private static final String URI_AUDIO = "uri_audio";
    private static final String AVATAR = "avatar";
    private static final String COMMENTS = "comments";
    private static final String SOCIAL_NETWORK = "social_network";
    private static final String ALREADY_VOTED = "already_voted";
    private static final String LIKE = "like";
    private static final String DISLIKE = "dislike";
    private static final String RESOURCE_URI ="resource_uri";
 
    private TextView num_com ;
	private LinearLayout ll_comments;
    private ListView list_comments;
    private EditText edit_write_comment;
    private ImageView aud;
    private ProgressBar loading_audio;
	private ImageView img_arrow;

    
    private String id;
    private String url;
    private String url_big;
    private String title;
    private int num_comments;
    private boolean has_audio;
    private String user_name;
    private String date;
    private String resource_uri;
    private String avatar;
    private Bitmap bitma;
	private boolean any_error;
	private int chosen_report; // 1:Copyright      2:Ilegalcontent      3:Spam
	private String uri_audio;	
	private String social_network; // 0:Scandaloh        1:Facebook
	private CommentAdapter commentsAdapter;
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	private boolean reproduciendo; // Nos indica si está reproduciendo el audio en un momento dado
	private SharedPreferences prefs;
	private boolean autoplay;
	private int already_voted;  // 0: nada       1: like            2: dislike
	private int likes;
	private int dislikes;

    
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
        bundle.putString(RESOURCE_URI, escan.getResourceUri());
        bundle.putString(URI_AUDIO, escan.getUriAudio());
        bundle.putString(AVATAR, escan.getAvatar());
        bundle.putParcelableArrayList(COMMENTS, escan.getComments());
        bundle.putString(SOCIAL_NETWORK, escan.getSocialNetwork());
        bundle.putInt(ALREADY_VOTED, escan.getAlreadyVoted());
        bundle.putInt(LIKE, escan.getLikes());
        bundle.putInt(DISLIKE, escan.getDislikes());

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
        this.has_audio = (getArguments() != null) ? getArguments().getBoolean(HAS_AUDIO) : null;
        this.user_name = (getArguments() != null) ? getArguments().getString(USER_NAME) : null;
        this.date = (getArguments() != null) ? getArguments().getString(DATE) : null;
        this.resource_uri = (getArguments() != null) ? getArguments().getString(RESOURCE_URI) : null;
        this.uri_audio = (getArguments() != null) ? getArguments().getString(URI_AUDIO) : null;
        this.avatar = (getArguments() != null) ? getArguments().getString(AVATAR) : null;
        this.comments = (getArguments() != null) ? getArguments().<Comment>getParcelableArrayList(COMMENTS) : null;
        this.social_network = (getArguments() != null) ? getArguments().getString(SOCIAL_NETWORK) : null;
        this.already_voted = (getArguments() != null) ? getArguments().getInt(ALREADY_VOTED) : 0;
        this.likes = (getArguments() != null) ? getArguments().getInt(LIKE) : 0;
        this.dislikes = (getArguments() != null) ? getArguments().getInt(DISLIKE) : 0;
        
        
        
        // Preferencias
		prefs = getActivity().getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
    }

    
    /**
     * Se ejecuta cuando el fragmento cambia su visibilidad
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser); 
       
        if (isVisibleToUser) {   
        	// Si tiene audio 
            if(has_audio){
            	 // Si tiene autoreproducir activado reproducimos el audio          	
                autoplay = prefs.getBoolean(MyApplication.AUTOPLAY_ACTIVATED, false);
                if (autoplay){
                	reproduciendo = true;
        			// Paramos si hubiera algún audio reproduciéndose
        			Audio.getInstance(getActivity().getBaseContext()).releaseResources();
                	new PlayAudioTask().execute(uri_audio);
                }
            }
        }    
    }
    
    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 
        if (!getSherlockActivity().getSupportActionBar().isShowing()) {
            getSherlockActivity().getSupportActionBar().show();
        }
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.escandalo, container, false);    
        
        SlidingUpPanelLayout layout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
        layout.setIsTransparent(true);
        layout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
       // layout.setAnchorPoint(0.3f);
        
        final LinearLayout edit_falso_write_comment = (LinearLayout) rootView.findViewById(R.id.ll_escandalo_falso_edit_comentario);
        
        layout.setPanelSlideListener(new PanelSlideListener() {
        	
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i("WE", "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
            	// Mostramos el action bar de escribir comentario
                ((MainActivity) getActivity()).updateActionBar(true, id);
                // Cambiamos la flecha hacia abajo
                changeArrowDirection(false);
                // Ocultamos el falso edit
                edit_falso_write_comment.setVisibility(View.GONE);
            }

            @Override
            public void onPanelCollapsed(View panel) {       
            	// Mostramos el action bar normal
                ((MainActivity) getActivity()).updateActionBar(false, id);
                // Cambiamos la flecha hacia arriba
                changeArrowDirection(true);
                // Mostramos el falso edit
                edit_falso_write_comment.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i("WE", "onPanelAnchored");

            }
        });
        
                
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
        
        Paint mShadow = new Paint(); 
        // radius=10, y-offset=2, color=black 
        mShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000); 
		
        
        // AUDIO    
        aud = (ImageView) rootView.findViewById(R.id.img_escandalo_audio);
        // Si tiene audio mostramos el icono de audio
        if(has_audio){
        	aud.setVisibility(View.VISIBLE);
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
	
        // SOCIAL NETWORK 
        ImageView user_type = (ImageView) rootView.findViewById(R.id.img_escandalo_tipo_usuario);
        // Scandaloh
        if (Integer.parseInt(social_network) == 0){
        	user_type.setImageResource(R.drawable.s_rosa);
        }
        // Facebook
        else if (Integer.parseInt(social_network) == 1){
        	user_type.setImageResource(R.drawable.facebook_rosa);
        }
			
        // AVATAR
        FetchableImageView emoticono = (FetchableImageView) rootView.findViewById(R.id.emoticono);
        emoticono.setImage(MyApplication.DIRECCION_BUCKET + avatar, R.drawable.avatar_defecto);
                      
        // LIKES
        final ImageView iLike = (ImageView) rootView.findViewById(R.id.img_escandalo_like);
        final ImageView iDislike = (ImageView) rootView.findViewById(R.id.img_escandalo_dislike);
        final TextView tLikes = (TextView) rootView.findViewById(R.id.txt_escandalo_num_likes);
        final TextView tDislikes = (TextView) rootView.findViewById(R.id.txt_escandalo_num_dislikes);
        tLikes.setText(Integer.toString(likes));
        tDislikes.setText(Integer.toString(dislikes));
        
        // Si está logueado
        if (MyApplication.logged_user){
        	// Mostramos si ya había marcado likes/dislikes anteriormente
            if (already_voted != 0){ // Si ya ha votado
            	if (already_voted == 1){ // Ha hecho like
            		iLike.setImageResource(R.drawable.like_rosa);
            	}
            	else if (already_voted == 2){ // Ha hecho dislike
            		iDislike.setImageResource(R.drawable.dislike_rosa);
            	}
            }
        }
            
        iLike.setOnClickListener(new View.OnClickListener() {
    			
    		@Override
    		public void onClick(View v) {
    				
    			if (MyApplication.logged_user){
    				int old_likes = Integer.parseInt(tLikes.getText().toString());  
                	int old_dislikes = Integer.parseInt(tDislikes.getText().toString());
                		
                	new SendLikeDislikeTask(LIKE).execute();
        				
        			// Había puesto like: quitamos like
        			if (already_voted == 1){
        				// Indicamos que no hay likes/dislikes marcados
        				iLike.setImageResource(R.drawable.like_azul);
        	        	already_voted = 0;
        	        	// Decrementamos el nº de likes
        	        	tLikes.setText(Integer.toString(old_likes-1));
        	        	// Actualizamos el fragmento
        	        	((MainActivity) getActivity()).updateLikesDislikes(0, old_likes-1, old_dislikes);
        			}
        			// Habia puesto dislike: quitamos dislike y marcamos like
        			else if (already_voted == 2){
        				// Indicamos que está marcado like
        				iDislike.setImageResource(R.drawable.dislike_azul);
        				iLike.setImageResource(R.drawable.like_rosa);
        	        	already_voted = 1;
        	        	// Incrementamos like y decrementamos dislike
        	        	tLikes.setText(Integer.toString(old_likes+1));
        	        	tDislikes.setText(Integer.toString(old_dislikes-1));
        	        	// Actualizamos el fragmento
        	        	((MainActivity) getActivity()).updateLikesDislikes(1, old_likes+1, old_dislikes-1);
        			}
        			// No había puesto nada: marcamos like
        			else{
        				// Indicamos que está marcado like
        				iLike.setImageResource(R.drawable.like_rosa);
        	        	already_voted = 1;
        	        	// Incrementamos like
        	        	tLikes.setText(Integer.toString(old_likes+1));
        	        	// Actualizamos el fragmento
        	        	((MainActivity) getActivity()).updateLikesDislikes(1, old_likes+1, old_dislikes);
        			}
    			}
    			else{
        			Toast toast = Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.registrate_o_inicia_sesion), Toast.LENGTH_SHORT);
        			toast.show();
        		}     					
    		}
    	});

        iDislike.setOnClickListener(new View.OnClickListener() {
    			
    		@Override
    		public void onClick(View v) {
    				
    			if (MyApplication.logged_user){
    				int old_likes = Integer.parseInt(tLikes.getText().toString());  
                	int old_dislikes = Integer.parseInt(tDislikes.getText().toString());
                		
                	new SendLikeDislikeTask(DISLIKE).execute();
                	
        			// Había puesto dislike: quitamos dislike
        			if (already_voted == 2){
        				// Indicamos que está marcado dislike
        				iDislike.setImageResource(R.drawable.dislike_azul);
        	        	already_voted = 0;
        	        	// Decrementamos dislike
        	        	tDislikes.setText(Integer.toString(old_dislikes-1));
        	        	// Actualizamos el fragmento
        	        	((MainActivity) getActivity()).updateLikesDislikes(0, old_likes, old_dislikes-1);
        				}
        			// Había puesto like: quitamos like y ponemos dislike
        			else if (already_voted == 1){
        				// Indicamos que está marcado dislike
        				iLike.setImageResource(R.drawable.like_azul);
        				iDislike.setImageResource(R.drawable.dislike_rosa);
        	        	already_voted = 2;
        	        	// Decrementamos nº likes e incrementamos nº dislikes
        	        	tLikes.setText(Integer.toString(old_likes-1));
        	        	tDislikes.setText(Integer.toString(old_dislikes+1));
        	        	// Actualizamos el fragmento
        	        	((MainActivity) getActivity()).updateLikesDislikes(2, old_likes-1, old_dislikes+1);
        			}
        			// No había puesto nada: marcamos dislike
        			else{
        				// Indicamos que está marcado dislike
        				iDislike.setImageResource(R.drawable.dislike_rosa);
        	        	already_voted = 2;
        	        	// Incrementamos nº dislikes
        	        	tDislikes.setText(Integer.toString(old_dislikes+1));
        	        	// Actualizamos el fragmento
        	        	((MainActivity) getActivity()).updateLikesDislikes(2, old_likes, old_dislikes+1);
        			}
    			}
    			else{
    					Toast toast = Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.registrate_o_inicia_sesion), Toast.LENGTH_SHORT);
    				toast.show();
    			}       					
    		}
    	});
        
             
        // COMPARTIR 
        ImageView share = (ImageView) rootView.findViewById(R.id.img_escandalo_compartir);
        share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				 // Creamos un menu para elegir entre compartir y denunciar foto
				 final CharSequence[] opciones_compartir = {getResources().getString(R.string.compartir_escandalo), getResources().getString(R.string.reportar_escandalo)};
				 AlertDialog.Builder dialog_compartir = new AlertDialog.Builder(getActivity());
				 dialog_compartir.setTitle(R.string.selecciona_opcion);
				 dialog_compartir.setItems(opciones_compartir, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int item) {
			            	
			            	// Compartir scándalOh
			                if (opciones_compartir[item].equals(getResources().getString(R.string.compartir_escandalo))) {
			    				// Paramos si hubiera algún audio reproduciéndose
			    				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
			    				
			    				// Compartimos la foto
			    				Uri screenshotUri = Uri.parse(url_big);	
			    				new ShareImageTask(getActivity().getBaseContext(), title).execute(screenshotUri.toString());	      			   
			                } 
			                
			                // Reportar foto
			                else if (opciones_compartir[item].equals(getResources().getString(R.string.reportar_escandalo))) {
			                	
			                	// Si el usuario está logueado
			                	if (MyApplication.logged_user){
				                	// Creamos un menu para elegir el tipo de report
				                	final CharSequence[] opciones_reportar = {getResources().getString(R.string.material_ofensivo), 
				                			getResources().getString(R.string.spam), getResources().getString(R.string.copyright)};
				                	AlertDialog.Builder dialog_report = new AlertDialog.Builder(getActivity());
				                	dialog_report.setTitle(R.string.reportar_esta_foto_por);
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
			    		        	Toast toast = Toast.makeText(getActivity().getBaseContext(), R.string.debes_iniciar_sesion_para_reportar, Toast.LENGTH_LONG);
			    		        	toast.show();        
			                	}

			                } 
			            }
			        });
				 dialog_compartir.show();			
			}
		});
            
        // TÍTULO
        TextView tit = (TextView) rootView.findViewById(R.id.txt_escandalo_titulo);
        tit.setText(title);
       
        // NOMBRE DE USUARIO
        TextView user_na = (TextView) rootView.findViewById(R.id.txt_escandalo_name_user);
        user_na.setText(Utils.limitaCaracteres(user_name));
        
        // FECHA
        TextView dat = (TextView) rootView.findViewById(R.id.txt_escandalo_date);
        dat.setText(changeFormatDate(date)); 
        
		// COMENTARIOS
		edit_write_comment = (EditText) rootView.findViewById(R.id.edit_write_comment);
		list_comments = (ListView) rootView.findViewById(R.id.lv_comments);
		commentsAdapter = new CommentAdapter(getActivity(),R.layout.comment_izquierda, R.layout.comment_derecha, comments, user_name);
		list_comments.setAdapter(commentsAdapter);	
		
 		// Número de comentarios
		num_com = (TextView) rootView.findViewById(R.id.txt_num_comments);
		if (num_comments == 0){
			num_com.setText(num_comments + " " + getResources().getString(R.string.comentarios));
	        layout.setPanelHeight(convertToDp(65));
		}
		else if (num_comments == 1){
			num_com.setText(num_comments + " " + getResources().getString(R.string.comentario));
			layout.setPanelHeight(convertToDp(135));
		}
		else{
			num_com.setText(num_comments + " " + getResources().getString(R.string.comentarios));
			layout.setPanelHeight(convertToDp(135));
		}

 		img_arrow = (ImageView) rootView.findViewById(R.id.img_flecha);	
           
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
	    	String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/photocomplaint/";        

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
	    protected Boolean doInBackground(String... params) {
	    	
	    	Audio.getInstance(getActivity().getBaseContext()).startPlaying("http://scandaloh.s3.amazonaws.com/" + params[0]);							
	        return false;
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
    
    
    /**
     * Cambia la dirección de la flecha de los comentarios
     * @param top True indica flecha hacia arriba. False hacia abajo
     */
 	public void changeArrowDirection(boolean top){
 		if (top){
 	 		img_arrow.setImageDrawable(getResources().getDrawable(R.drawable.flecha_arriba));
 		}
 		else{
 	 		img_arrow.setImageDrawable(getResources().getDrawable(R.drawable.flecha_abajo));
 		}
 	}
	
 	
 	
    /**
     * Convierte pixel en dp
     * @param input
     * @return
     */
 	private int convertToDp(int input) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (input * scale + 0.5f);
 	}
 	
 	
 	
 	
 	/**
	 * Envía un like o dislike al servidor a partir de un usuario y una foto
	 *
	 */
	private class SendLikeDislikeTask extends AsyncTask<Void,Integer,Void> {
		
		private String like_dislike ;
		
		public SendLikeDislikeTask(String like_dislike){
			this.like_dislike = like_dislike;
		}
		
		
		@Override
	    protected Void doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	        String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/photovote/";
	
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");

	             JSONObject dato = new JSONObject();	                        
	             dato.put("user", MyApplication.resource_uri);
	             dato.put("photo", resource_uri);
	             dato.put("vote", like_dislike);
	             
	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	                          
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);	            
	             }
	        }
	        catch (Exception ex){
	        }
	        
	        return null;
	    }
			
	    }
 	
   
}
