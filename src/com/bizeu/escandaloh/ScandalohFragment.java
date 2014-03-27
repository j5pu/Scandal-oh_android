package com.bizeu.escandaloh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.applidium.shutterbug.FetchableImageView;
import com.applidium.shutterbug.FetchableImageView.FetchableImageViewListener;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class ScandalohFragment extends SherlockFragment {

	public static final String ID = "id";
    public static final String URL = "url";
    private static final String URL_BIG = "url_big";
    public static final String TITLE = "title";
    private static final String NUM_COMMENTS = "num_comments";
    private static final String HAS_AUDIO = "has_audio";
    private static final String USER_NAME = "user_name";
    private static final String DATE = "date";
    private static final String URI_AUDIO = "uri_audio";
    private static final String AVATAR = "avatar";
    private static final String LAST_COMMENT = "last_comment";
    private static final String SOCIAL_NETWORK = "social_network";
    private static final String ALREADY_VOTED = "already_voted";
    private static final String LIKE = "like";
    private static final String DISLIKE = "dislike";
    private static final String RESOURCE_URI ="resource_uri";
    private static final String FAVICON = "favicon";
    private static final String SOURCE = "Source";
    public static final String SOURCE_NAME = "Source_name";
    private static final String MEDIA_TYPE = "Media_type";
    public static final int SHOW_COMMENTS = 343;

    private ImageView aud;
	private ImageView img_arrow;
	private TextView comment_text;
	private TextView txt_user_name; 
    private FetchableImageView img_avatar;
    private ImageView social_net;
	private TextView txt_date;
	private TextView num_com;
 
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
	private Comment last_comment;
	private boolean reproduciendo; // Nos indica si está reproduciendo el audio en un momento dado
	private SharedPreferences prefs;
	private boolean autoplay;
	private int already_voted;  // 0: nada       1: like            2: dislike
	private int likes;
	private int dislikes;
	private String favicon;
	private int media_type;
	private String source;
	private String source_name;
	private CharSequence[] opciones_compartir;

    
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
        bundle.putParcelable(LAST_COMMENT, escan.getLastComment());
        bundle.putString(SOCIAL_NETWORK, escan.getSocialNetwork());
        bundle.putInt(ALREADY_VOTED, escan.getAlreadyVoted());
        bundle.putInt(LIKE, escan.getLikes());
        bundle.putInt(DISLIKE, escan.getDislikes());
        bundle.putInt(MEDIA_TYPE, escan.getMediaType());
        bundle.putString(FAVICON, escan.getFavicon());
        bundle.putString(SOURCE, escan.getSource());
        bundle.putString(SOURCE_NAME, escan.getSourceName());

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
        this.last_comment = (getArguments() != null) ? getArguments().<Comment>getParcelable(LAST_COMMENT) : null;
        this.social_network = (getArguments() != null) ? getArguments().getString(SOCIAL_NETWORK) : null;
        this.already_voted = (getArguments() != null) ? getArguments().getInt(ALREADY_VOTED) : 0;
        this.likes = (getArguments() != null) ? getArguments().getInt(LIKE) : 0;
        this.dislikes = (getArguments() != null) ? getArguments().getInt(DISLIKE) : 0;
        this.media_type = (getArguments() != null) ? getArguments().getInt(MEDIA_TYPE) : 0;
        this.favicon = (getArguments() != null) ? getArguments().getString(FAVICON) : null;
        this.source = (getArguments() != null) ? getArguments().getString(SOURCE) : null;
        this.source_name = (getArguments() != null) ? getArguments().getString(SOURCE_NAME) : null;
           
        // Preferencias
		prefs = getActivity().getSharedPreferences("com.bizeu.escandaloh", Context.MODE_PRIVATE);
    }
    
    
    
    /**
     * onActivityResult
     */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == SHOW_COMMENTS && resultCode == Activity.RESULT_OK) {

	    	if (data.getExtras() != null){   
		    	// Actualizamos el último comentario
	 	        Comment last_comm = (Comment) data.getExtras().getParcelable(CommentsActivity.LST_COMMENT);
	 	        // Actualizamos el adaptador
	        	((MainActivity) getActivity()).updateLastComment(last_comm);
	        	// Actualizamos las vistas
	        	comment_text.setText(last_comm.getText());
	        	// Nombre de usuario
	    		txt_user_name.setText(last_comm.getUsername());   		
	    		// Fecha (formato dd-mm-aaaa)
	            String date_without_time = (last_comm.getDate().split("T",2))[0];   
	            String year = date_without_time.split("-",3)[0];
	            String month = date_without_time.split("-",3)[1];
	            String day = date_without_time.split("-",3)[2];
	            String final_date = day + "-" + month + "-" + year;
	            txt_date.setText(final_date); 	            
	            // Avatar
	            img_avatar.setImage(MyApplication.DIRECCION_BUCKET + last_comm.getAvatar(), getActivity().getResources().getDrawable(R.drawable.avatar_defecto));       
	            // Red social
	            int social_ne = Integer.parseInt(last_comm.getSocialNetwork());
	            if (social_ne == 0){
	            	social_net.setImageResource(R.drawable.s_gris);
	            }
	            else if (social_ne == 1){
	            	social_net.setImageResource(R.drawable.facebook_gris);
	            }
	            
	            // Actualizamos el nº de comentarios
	            int num_comments = data.getExtras().getInt(CommentsActivity.NUM_COMMENTS);
	            // Actualizamos el adaptador
	            ((MainActivity) getActivity()).updateNumComments(num_comments);
	            // Actualizamos la vista
	            num_com.setText(Integer.toString(num_comments));
	    	} 
	    }
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
        			// Reproducimos
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
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.scandal, container, false);    
        
        // FOTO
        FetchableImageView img = (FetchableImageView) rootView.findViewById(R.id.img_escandalo_foto);
        img.setTag(109);
        img.setImage(this.url, R.drawable.cargando);  
        
        Paint mShadow = new Paint(); 
        // radius=10, y-offset=2, color=black 
        mShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000); 
           
        // NOTICIA 
        final FetchableImageView img_favicon = (FetchableImageView) rootView.findViewById(R.id.img_escandalo_favicon);
    	TextView txt_fuente = (TextView) rootView.findViewById(R.id.img_escandalo_fuente);
    	
    	// No es noticia
        if (media_type == 0){ 
        	txt_fuente.setVisibility(View.INVISIBLE);
        	
        	// Listeners del escándalo
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
        }
        
        // Si es noticia
        else{ 
        	// Fuente
        	txt_fuente.setText(source_name);
        	// Favicon
        	img_favicon.setImage(favicon);
        	if (img_favicon.getDrawable() != null){
        		img_favicon.setVisibility(View.VISIBLE);
        	}
        	img_favicon.setListener(new FetchableImageViewListener() {
				
				@Override
				public void onImageFetched(Bitmap bitmap, String url) {
					img_favicon.setVisibility(View.VISIBLE);
					
				}
				
				@Override
				public void onImageFailure(String url) {
					// TODO Auto-generated method stub
					
				}
			});
        	
        	img.setOnClickListener(new View.OnClickListener() {
				
        		@Override
				public void onClick(View v) {
					// Cargamos nuestro navegador con la noticia
        			Intent i = new Intent(getActivity(), BrowserNewsActivity.class);
        			i.putExtra("source", source);
        			startActivity(i);	
				}
			});
        }
		
        
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
        Log.v("WE","avatar del escandalo en fragment: " + avatar);
        // Si el usuario tiene avatar
        if (!avatar.equals("")){
            emoticono.setImage(MyApplication.DIRECCION_BUCKET + avatar, R.drawable.avatar_defecto);	
        }

                      
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
        final ImageView share = (ImageView) rootView.findViewById(R.id.img_escandalo_compartir);
        share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
    				// Creamos un menu para elegir entre compartir y denunciar foto
    				AlertDialog.Builder dialog_compartir = new AlertDialog.Builder(getActivity());
    				dialog_compartir.setTitle(R.string.selecciona_opcion);
    				List<String> options_share = new ArrayList<String>();

    				// Si está logueado mostramos las opciones de compartir, reportar y guardar en galeria
    				if (MyApplication.logged_user){
    					options_share.add(new String(getResources().getString(R.string.compartir_escandalo)));
    					options_share.add(new String(getResources().getString(R.string.reportar_escandalo)));
    					options_share.add(new String(getResources().getString(R.string.guardar_foto_galeria)));
    				}
    				
    				// Si no, no mostramos la opción de reportar
    				else{
    					options_share.add(new String(getResources().getString(R.string.compartir_escandalo)));
    					options_share.add(new String(getResources().getString(R.string.guardar_foto_galeria)));
    				}
    				
    				opciones_compartir = options_share.toArray(new CharSequence[options_share.size()]);
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
   			                } 
   			                
   			                // Guardar foto en la galería
   			                else if (opciones_compartir[item].equals(getResources().getString(R.string.guardar_foto_galeria))) {
   			                	new SaveImageTask(getActivity()).execute(url_big);
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
        user_na.setText(Utils.limitaCaracteres(user_name, 25));      
        
        // COMENTARIOS
        // Último comentario    
        comment_text = (TextView) rootView.findViewById(R.id.txt_comment_text);
		txt_user_name = (TextView) rootView.findViewById(R.id.txt_comment_username);
        img_avatar = (FetchableImageView) rootView.findViewById(R.id.img_comment_avatar);
        social_net = (ImageView) rootView.findViewById(R.id.img_lastcomment_socialnetwork);
		txt_date = (TextView) rootView.findViewById(R.id.txt_comment_date);
        LinearLayout ll_last_comment = (LinearLayout) rootView.findViewById(R.id.ll_escandalo_lastcomment);
        
        // Si se pulsa accedemos a los comentarios
        ll_last_comment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), CommentsActivity.class);
				i.putExtra(SOURCE_NAME, source_name);
				i.putExtra(ID, id);
				i.putExtra(TITLE, title);
				i.putExtra(URL, url);			
				startActivityForResult(i,SHOW_COMMENTS);		
			}
		});       

        // Si hay algún comentario
        if (last_comment != null){
        	// Texto
    		comment_text.setText(last_comment.getText());
    		
    		// Nombre de usuario
    		txt_user_name.setText(last_comment.getUsername());
    		
    		// Fecha (formato dd-mm-aaaa)
            String date_without_time = (last_comment.getDate().split("T",2))[0];   
            String year = date_without_time.split("-",3)[0];
            String month = date_without_time.split("-",3)[1];
            String day = date_without_time.split("-",3)[2];
            String final_date = day + "-" + month + "-" + year;
            txt_date.setText(final_date); 
            
            // Red social
            int social_ne = Integer.parseInt(last_comment.getSocialNetwork());
            if (social_ne == 0){
            	social_net.setImageResource(R.drawable.s_gris);
            }
            else if (social_ne == 1){
            	social_net.setImageResource(R.drawable.facebook_gris);
            }
            
            // Avatar
            Log.v("WE","Avatar del ultimo comentario en fragment: " + last_comment.getAvatar());
            if (!last_comment.getAvatar().equals("")){
                img_avatar.setImage(MyApplication.DIRECCION_BUCKET + last_comment.getAvatar(), getActivity().getResources().getDrawable(R.drawable.avatar_defecto));
            }
        }
        
        // No hay comentarios
        else{
        	// Si el usuario está logueado
        	if (MyApplication.logged_user){
        		
        		comment_text.setText(getResources().getString(R.string.se_el_primero_en_comentar));
        		txt_user_name.setText(MyApplication.user_name);
        		txt_date.setText(Utils.getCurrentDate());
        		
                // Avatar
                img_avatar.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, getActivity().getResources().getDrawable(R.drawable.avatar_defecto));
                
        	}
        	// Si no está logueado
        	else{
        		comment_text.setText(getResources().getString(R.string.inicia_sesion_para_comentar_este_escandalo));
        		txt_user_name.setText(getResources().getString(R.string.invitado));
        		txt_date.setText(Utils.getCurrentDate());
        		// Le mandamos a la pantalla de login si pulsa
                ll_last_comment.setOnClickListener(new View.OnClickListener() {
        			
        			@Override
        			public void onClick(View v) {
        				Intent i = new Intent(getActivity(), LoginSelectActivity.class);
        				startActivity(i);		
        			}
        		});
        	}
        }
   
        
 		// Número de comentarios        
		num_com = (TextView) rootView.findViewById(R.id.txt_lastcomment_num_comments);
		num_com.setText(Integer.toString(num_comments));
   
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
	    	
	    	Audio.getInstance(getActivity().getBaseContext()).startPlaying(MyApplication.DIRECCION_BUCKET + params[0]);							
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
	             post.setHeader("Session-Token", MyApplication.session_token);

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
