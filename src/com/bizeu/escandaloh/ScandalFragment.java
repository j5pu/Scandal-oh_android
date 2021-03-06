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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.applidium.shutterbug.FetchableImageView;
import com.applidium.shutterbug.FetchableImageView.FetchableImageViewListener;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.users.ProfileActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Audio.PlayListener;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.ImageViewRounded;
import com.bizeu.escandaloh.util.Utils;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class ScandalFragment extends SherlockFragment {

	public static final String ID = "id";
	public static final String USER_ID = "user_id";
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
    public static final int FROM_SCANDAL_FRAGMENT = 235;

    private ImageView img_aud;
	private TextView comment_text;
	private TextView txt_user_name; 
    private ImageViewRounded img_avatar;
    private ImageView social_net;
	private TextView txt_date;
	private TextView txt_num_comm;
	private LinearLayout ll_last_comment ;
    private ImageView iLike;
    private ImageView iDislike;
    private TextView txt_likes;
    private TextView txt_dislikes;
    private FetchableImageView img;
    private FetchableImageView img_favicon;
    private ImageView user_type;
    private ImageViewRounded emoticono;
    private ImageView share;
    private TextView tit;
    private TextView user_na;
    private ProgressBar prog_loading_audio;
    private View v_linea_arriba;
    private View v_linea_abajo_izq;
    private ImageView img_bocadillo;
 
    private String id;
    private String user_id;
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
	private boolean reproduciendo; // Nos indica si est� reproduciendo el audio en un momento dado
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
	private boolean play_when_viewcreated = false;

    
    /**
     * Crea y devuelve una nueva instancia de un fragmento
     * @param escan Escandalo para dicho fragmento
     * @return Fragmento con el esc�ndalo
     */
    public static ScandalFragment newInstance(Scandaloh escan) {
        // Instanciamos el fragmento
        ScandalFragment fragment = new ScandalFragment();
 
        // Guardamos los datos del fragmento (del esc�ndalo)
        Bundle bundle = new Bundle();
        bundle.putString(ID, escan.getId());
        bundle.putString(USER_ID, escan.getUserId());
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
        
        // Obtenemos los valores del fragmento (del esc�ndalo)
        this.id = (getArguments() != null) ? getArguments().getString(ID) : null;
        this.user_id = (getArguments() != null) ? getArguments().getString(USER_ID) : null;
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
	    		
		    	// ACTUALIZAMOS EL �LTIMO COMENTARIO
	 	        Comment last_comm = (Comment) data.getExtras().getParcelable(CommentsActivity.LST_COMMENT);
	 	        // Actualizamos el adaptador
	        	MainActivity.updateLastComment(last_comm);
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
	            	social_net.setImageResource(R.drawable.s_circular_gris);
	            }
	            else if (social_ne == 1){
	            	social_net.setImageResource(R.drawable.f_circular_gris);
	            }
	            
	            // ACTUALIZAMOS EL N� DE COMENTARIOS
	            int num_comments = data.getExtras().getInt(CommentsActivity.NUM_COMMENTS);
	            // Actualizamos el adaptador
	            MainActivity.updateNumComments(num_comments);
	            // Actualizamos la vista
	            txt_num_comm.setText(Integer.toString(num_comments));
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
        	
			// Paramos si hubiera alg�n audio reproduci�ndose
			Audio.getInstance(getActivity().getBaseContext()).releaseResources();
			
        	// Si tiene audio 
            if(has_audio){
            	 // Si tiene autoreproducir activado reproducimos el audio          	
                autoplay = prefs.getBoolean(MyApplication.AUTOPLAY_ACTIVATED, true);
                if (autoplay){
                	// Si ya se ha cargado la vista reproducimos el audio, sino lo reproducimos cuando est� cargada
                	if (prog_loading_audio != null){
                		play_when_viewcreated = false;
                		reproduciendo = true;
            			// Reproducimos
            			new PlayAudioTask().execute(uri_audio);
                	}
                	// Indicamos que se debe reproducir al cargarse la vista
                	else{
                		play_when_viewcreated = true;
                	}
                }
            }
        } 
    }
   

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.scandal, container, false);  
    	
        iLike = (ImageView) rootView.findViewById(R.id.img_escandalo_like);
        iDislike = (ImageView) rootView.findViewById(R.id.img_escandalo_dislike);
        txt_likes = (TextView) rootView.findViewById(R.id.txt_escandalo_num_likes);
        txt_dislikes = (TextView) rootView.findViewById(R.id.txt_escandalo_num_dislikes);
        img = (FetchableImageView) rootView.findViewById(R.id.img_escandalo_foto);
        img_favicon = (FetchableImageView) rootView.findViewById(R.id.img_escandalo_favicon);
        img_aud = (ImageView) rootView.findViewById(R.id.img_escandalo_audio);
        user_type = (ImageView) rootView.findViewById(R.id.img_escandalo_tipo_usuario);
        emoticono = (ImageViewRounded) rootView.findViewById(R.id.emoticono);
        share = (ImageView) rootView.findViewById(R.id.img_escandalo_compartir);
        tit = (TextView) rootView.findViewById(R.id.txt_escandalo_titulo);
        user_na = (TextView) rootView.findViewById(R.id.txt_escandalo_name_user);
        comment_text = (TextView) rootView.findViewById(R.id.txt_comment_text);
		txt_user_name = (TextView) rootView.findViewById(R.id.txt_comment_username);
        img_avatar = (ImageViewRounded) rootView.findViewById(R.id.img_comment_avatar);
        social_net = (ImageView) rootView.findViewById(R.id.img_lastcomment_socialnetwork);
		txt_date = (TextView) rootView.findViewById(R.id.txt_comment_date);
        ll_last_comment = (LinearLayout) rootView.findViewById(R.id.ll_escandalo_lastcomment);
        txt_num_comm = (TextView) rootView.findViewById(R.id.txt_scandal_numcomments);
    	TextView txt_fuente = (TextView) rootView.findViewById(R.id.img_escandalo_fuente);
    	prog_loading_audio = (ProgressBar) rootView.findViewById(R.id.prog_escandalo_loading_audio);
    	v_linea_arriba = rootView.findViewById(R.id.view_scandal_linea_arriba);
    	v_linea_abajo_izq = rootView.findViewById(R.id.view_scandal_abajo_izquierda);
    	img_bocadillo = (ImageView) rootView.findViewById(R.id.img_scandal_bocadillo);
    	
        if (!getSherlockActivity().getSupportActionBar().isShowing()) {
            getSherlockActivity().getSupportActionBar().show();
        }     
        
        // FOTO
        img.setTag(109);
        img.setImage(this.url, R.drawable.cargando);    
        Paint mShadow = new Paint(); 
        mShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000); // radius=10, y-offset=2, color=black 
           
        // TIPO DE ESC�NDALO	
    	// Esc�ndalo normal
        if (media_type == 0){ 
        	
        	txt_fuente.setVisibility(View.INVISIBLE);
        	
        	// Listeners del esc�ndalo
            img.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				
    				// Evitamos que se pulse dos o m�s veces en las fotos (para que no se abra m�s de una vez)
    				if (!MyApplication.PHOTO_CLICKED){
    					MyApplication.PHOTO_CLICKED = true;
    					
    					// Paramos si hubiera alg�n audio reproduci�ndose
    					Audio.getInstance(getActivity().getBaseContext()).releaseResources();
    					
    					// Mostramos la pantalla de la foto en detalle
    					Intent i = new Intent(getActivity(), PhotoDetailActivity.class);
    					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    					ImageView imView = (ImageView) v;
    					Bitmap bitm = ((BitmapDrawable)imView.getDrawable()).getBitmap();
    					byte[] bytes = ImageUtils.bitmapToBytes(bitm);
    					i.putExtra("bytes", bytes);
    					i.putExtra("uri_audio", uri_audio);	
    					i.putExtra("url_big", url_big);
    					getActivity().startActivity(i);				
    				}	
    			}
    		});
        }
        
        // Noticia 
        else if (media_type == 1){ 
        	
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
				}
			});
        	
        	img.setOnClickListener(new View.OnClickListener() {
				
        		@Override
				public void onClick(View v) {
					// Cargamos nuestro navegador con la noticia
        			Intent i = new Intent(getActivity(), BrowserActivity.class);
        			i.putExtra("source", source);
        			startActivity(i);	
				}
			});
        }
        
        // Esc�ndalo de mentira ("se el primero en subir un esc�ndalo")
        else if (media_type == -1){
        	
        	// Ocultamos la fuente, los likes, comentarios y �ltimo comentario
        	txt_fuente.setVisibility(View.INVISIBLE); 	
            iLike.setVisibility(View.INVISIBLE);
            iDislike.setVisibility(View.INVISIBLE);
            txt_likes.setVisibility(View.INVISIBLE);
            txt_dislikes.setVisibility(View.INVISIBLE);
            share.setVisibility(View.INVISIBLE);
            ll_last_comment.setVisibility(View.INVISIBLE);
            img_bocadillo.setVisibility(View.INVISIBLE);
            txt_num_comm.setVisibility(View.INVISIBLE);
        	
        	// Listeners del esc�ndalo
            img.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {			
    				((MainActivity) getActivity()).uploadScandal();	  				
    			}
    		});
        }
        
        // Audio
        else if (media_type == 2){
        	img.setClickable(false);
        }
		
                       
        // AUDIO    
        // Si tiene audio mostramos el icono de audio
        if(has_audio){
        	img_aud.setVisibility(View.VISIBLE);
        }
        
        if (play_when_viewcreated){
    		reproduciendo = true;
    		play_when_viewcreated = false;
			// Paramos si hubiera alg�n audio reproduci�ndose
			Audio.getInstance(getActivity().getBaseContext()).releaseResources();
			// Reproducimos
        	new PlayAudioTask().execute(uri_audio);
        }
        
        img_aud.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				// Paramos si hubiera alg�n audio reproduci�ndose
				Audio.getInstance(getActivity().getBaseContext()).releaseResources();			
				// Lo reproducimos		
				if (uri_audio != null){
					new PlayAudioTask().execute(uri_audio);	
				}
			}
		});
	
        // SOCIAL NETWORK 
        // Scandaloh
        if (Integer.parseInt(social_network) == 0){
        	user_type.setImageResource(R.drawable.s_circular_blanca);
        }
        // Facebook
        else if (Integer.parseInt(social_network) == 1){
        	user_type.setImageResource(R.drawable.f_circular_blanca);
        }
        user_type.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ProfileActivity.class);
				i.putExtra(ProfileActivity.USER_ID, user_id);
				startActivity(i);		
			}
		});
			
        // AVATAR
        // Si el usuario tiene avatar     
        if (!avatar.equals("")){
            emoticono.setImage(MyApplication.DIRECCION_BUCKET + avatar, R.drawable.avatar_defecto);	
        }
        emoticono.setOnClickListener(new View.OnClickListener() {
				
        	@Override
        	public void onClick(View v) {
        		Intent i = new Intent(getActivity(), ProfileActivity.class);
        		i.putExtra(ProfileActivity.USER_ID, user_id);
        		startActivity(i);
        	}
        });
      	
                    
        // LIKES
        txt_likes.setText(Integer.toString(likes));
        txt_dislikes.setText(Integer.toString(dislikes));
      
        // Si est� logueado
        if (MyApplication.logged_user){
        	// Mostramos si ya hab�a marcado likes/dislikes anteriormente
            if (already_voted != 0){ // Si ya ha votado
            	if (already_voted == 1){ // Ha hecho like
            		iLike.setImageResource(R.drawable.mas_con_circulo_verde);
            		txt_likes.setTextColor(getResources().getColor(R.color.verde));
            	}
            	else if (already_voted == 2){ // Ha hecho dislike
            		iDislike.setImageResource(R.drawable.menos_con_circulo_rojo);
            		txt_dislikes.setTextColor(getResources().getColor(R.color.rojo));
            	}
            }
        }
            
        iLike.setOnClickListener(new View.OnClickListener() {
    			
    		@Override
    		public void onClick(View v) {
    				
    			if (MyApplication.logged_user){
    				int old_likes = Integer.parseInt(txt_likes.getText().toString());  
                	int old_dislikes = Integer.parseInt(txt_dislikes.getText().toString());
                		
                	new SendLikeDislikeTask(LIKE).execute();
        				
        			// Hab�a puesto like: quitamos like
        			if (already_voted == 1){
        				// Indicamos que no hay likes/dislikes marcados
        				iLike.setImageResource(R.drawable.mas_con_circulo_blanco);
                		txt_likes.setTextColor(getResources().getColor(R.color.blanco));
        	        	already_voted = 0;
        	        	// Decrementamos el n� de likes
        	        	txt_likes.setText(Integer.toString(old_likes-1));
        	        	// Actualizamos el fragmento
        	        	MainActivity.updateLikesDislikes(0, old_likes-1, old_dislikes);
        			}
        			// Habia puesto dislike: quitamos dislike y marcamos like
        			else if (already_voted == 2){
        				// Indicamos que est� marcado like
        				iDislike.setImageResource(R.drawable.menos_con_circulo_blanco);
                		txt_dislikes.setTextColor(getResources().getColor(R.color.blanco));
        				iLike.setImageResource(R.drawable.mas_con_circulo_verde);
                		txt_likes.setTextColor(getResources().getColor(R.color.verde));
        	        	already_voted = 1;
        	        	// Incrementamos like y decrementamos dislike
        	        	txt_likes.setText(Integer.toString(old_likes+1));
        	        	txt_dislikes.setText(Integer.toString(old_dislikes-1));
        	        	// Actualizamos el fragmento
        	        	MainActivity.updateLikesDislikes(1, old_likes+1, old_dislikes-1);
        			}
        			// No hab�a puesto nada: marcamos like
        			else{
        				// Indicamos que est� marcado like
        				iLike.setImageResource(R.drawable.mas_con_circulo_verde);
                		txt_likes.setTextColor(getResources().getColor(R.color.verde));
        	        	already_voted = 1;
        	        	// Incrementamos like
        	        	txt_likes.setText(Integer.toString(old_likes+1));
        	        	// Actualizamos el fragmento
        	        	MainActivity.updateLikesDislikes(1, old_likes+1, old_dislikes);
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
    				int old_likes = Integer.parseInt(txt_likes.getText().toString());  
                	int old_dislikes = Integer.parseInt(txt_dislikes.getText().toString());
                		
                	new SendLikeDislikeTask(DISLIKE).execute();
                	
        			// Hab�a puesto dislike: quitamos dislike
        			if (already_voted == 2){
        				// Indicamos que est� marcado dislike
        				iDislike.setImageResource(R.drawable.menos_con_circulo_blanco);
                		txt_dislikes.setTextColor(getResources().getColor(R.color.blanco));
        	        	already_voted = 0;
        	        	// Decrementamos dislike
        	        	txt_dislikes.setText(Integer.toString(old_dislikes-1));
        	        	// Actualizamos el fragmento
        	        	MainActivity.updateLikesDislikes(0, old_likes, old_dislikes-1);
        				}
        			// Hab�a puesto like: quitamos like y ponemos dislike
        			else if (already_voted == 1){
        				// Indicamos que est� marcado dislike
        				iLike.setImageResource(R.drawable.mas_con_circulo_blanco);
                		txt_likes.setTextColor(getResources().getColor(R.color.blanco));
        				iDislike.setImageResource(R.drawable.menos_con_circulo_rojo);
                		txt_dislikes.setTextColor(getResources().getColor(R.color.rojo));
        	        	already_voted = 2;
        	        	// Decrementamos n� likes e incrementamos n� dislikes
        	        	txt_likes.setText(Integer.toString(old_likes-1));
        	        	txt_dislikes.setText(Integer.toString(old_dislikes+1));
        	        	// Actualizamos el fragmento
        	        	MainActivity.updateLikesDislikes(2, old_likes-1, old_dislikes+1);
        			}
        			// No hab�a puesto nada: marcamos dislike
        			else{
        				// Indicamos que est� marcado dislike
        				iDislike.setImageResource(R.drawable.menos_con_circulo_rojo);
                		txt_dislikes.setTextColor(getResources().getColor(R.color.rojo));
        	        	already_voted = 2;
        	        	// Incrementamos n� dislikes
        	        	txt_dislikes.setText(Integer.toString(old_dislikes+1));
        	        	// Actualizamos el fragmento
        	        	MainActivity.updateLikesDislikes(2, old_likes, old_dislikes+1);
        			}
    			}
    			else{
    					Toast toast = Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.registrate_o_inicia_sesion), Toast.LENGTH_SHORT);
    				toast.show();
    			}       					
    		}
    	});
        
             
        // COMPARTIR 
        share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
    				// Creamos un menu para elegir entre compartir y denunciar foto
    				AlertDialog.Builder dialog_compartir = new AlertDialog.Builder(getActivity());
    				dialog_compartir.setTitle(R.string.selecciona_opcion);
    				List<String> options_share = new ArrayList<String>();

    				// Si est� logueado mostramos las opciones de compartir, reportar y guardar en galeria
    				if (MyApplication.logged_user){
    					options_share.add(new String(getResources().getString(R.string.compartir_escandalo)));
    					options_share.add(new String(getResources().getString(R.string.reportar_escandalo)));
    					options_share.add(new String(getResources().getString(R.string.guardar_foto_galeria)));
    				}
    				
    				// Si no, no mostramos la opci�n de reportar
    				else{
    					options_share.add(new String(getResources().getString(R.string.compartir_escandalo)));
    					options_share.add(new String(getResources().getString(R.string.guardar_foto_galeria)));
    				}
    				
    				opciones_compartir = options_share.toArray(new CharSequence[options_share.size()]);
    				dialog_compartir.setItems(opciones_compartir, new DialogInterface.OnClickListener() {
   			            @Override
   			            public void onClick(DialogInterface dialog, int item) {
   			            	
   			            	// Compartir sc�ndalOh
   			                if (opciones_compartir[item].equals(getResources().getString(R.string.compartir_escandalo))) {
   			    				// Paramos si hubiera alg�n audio reproduci�ndose
   			    				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
   			    				
   			    				// Compartimos la foto
   			    				Uri screenshotUri = Uri.parse(url_big);	
   			    				new ShareImageTask(getActivity().getBaseContext(), title).execute(screenshotUri.toString());	      			   
   			                } 
   			                
   			                // Reportar foto
   			                else if (opciones_compartir[item].equals(getResources().getString(R.string.reportar_escandalo))) {
   			                	
   			                	// Si el usuario est� logueado
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
   											Toast toast = Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.reportando_scandaloh) , Toast.LENGTH_SHORT);
   											toast.show();
   											new ReportPhoto(getActivity().getBaseContext()).execute();
   										}
   									});
   				                	dialog_report.show();
   			                	}
   			                } 
   			                
   			                // Guardar foto en la galer�a
   			                else if (opciones_compartir[item].equals(getResources().getString(R.string.guardar_foto_galeria))) {
   			                	new SaveImageTask(getActivity()).execute(url_big);
   			                }
   			            }
   			        });
   				 dialog_compartir.show();			 		
			}
		});
        
        // T�TULO
        tit.setText(title);
       
        // NOMBRE DE USUARIO
        user_na.setText(user_name);  
        user_na.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ProfileActivity.class);
				i.putExtra(ProfileActivity.USER_ID, user_id);
				startActivity(i);		
			}
		});
        
        // COMENTARIOS   
		// Si se pulsa accedemos a los comentarios
        txt_num_comm.setOnClickListener(new View.OnClickListener() {
			
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
        
		// Si se pulsa accedemos a los comentarios
        img_bocadillo.setOnClickListener(new View.OnClickListener() {
			
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
        
        
        // �ltimo comentario               
        updateLastComment();
 		// N�mero de comentarios  
        txt_num_comm.setText(Integer.toString(num_comments));
        
        // Color de la categoria
		if (MainActivity.current_category.equals(MainActivity.HAPPY)){
			v_linea_arriba.setBackgroundColor(getResources().getColor(R.color.morado));
			v_linea_abajo_izq.setBackgroundColor(getResources().getColor(R.color.morado));
		}
		else if (MainActivity.current_category.equals(MainActivity.ANGRY)){
			v_linea_arriba.setBackgroundColor(getResources().getColor(R.color.azul));
			v_linea_abajo_izq.setBackgroundColor(getResources().getColor(R.color.azul));
		}
   
        // Devolvemos la vista
        return rootView;
    }
    

    
	/**
	 * Comparte un esc�ndalo
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
	            File dir = new File(filepath.getAbsolutePath()+ "/Sc�ndalOh/");
	            dir.mkdirs();
	            String fileName = idStr;
	            // Guardamos la ruta de la foto para m�s adelante eliminarla
	            MyApplication.FILES_TO_DELETE.add(filepath.getAbsolutePath() + "/Sc�ndalOh/" + idStr);
	            file = new File(dir, fileName);
	            FileOutputStream fos = new FileOutputStream(file);
	            bmImg.compress(CompressFormat.JPEG, 100, fos);
	            fos.flush();
	            fos.close();

	        } catch (Exception e) {
	            e.printStackTrace();	                       
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
	 * Guarda una foto en la galer�a
	 *
	 */
	private class SaveImageTask extends AsyncTask<String, String, String> {
	    private Context context;
	    private ProgressDialog pDialog;
	    private boolean any_error;
	    private File file = null;

	    public SaveImageTask(Context context) {
	        this.context = context;
	    }

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        
	        any_error = false;

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
            	file = Utils.createPhotoScandalOh(context);
            	if (file != null){        	
                	FileOutputStream fOut = new FileOutputStream(file);
                	bitma.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                	fOut.flush();
                	fOut.close();
            	}

	        } catch (Exception e) {
	            e.printStackTrace();
	            any_error = true;
	        }

	        return null;
	    }

	    @Override
	    protected void onPostExecute(String args) {
	    	
	    	// Quitamos el progress dialog
	        pDialog.dismiss();  
	        
	        if (!any_error){    
	        	
		        // Guardamos la foto en la galer�a  
	        	Utils.galleryAddPic(file.getAbsolutePath(), context);
				
				// Mostramos un mensaje
				Toast toast = Toast.makeText(context, R.string.foto_guardada_en_este_dispositivo, Toast.LENGTH_SHORT);
				toast.show();
	        }
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
	    protected Integer doInBackground(Void... params) {
	 
	    	HttpEntity resEntity;
	    	String urlString = MyApplication.SERVER_ADDRESS + "/api/v1/photocomplaint/";        

	        HttpResponse response = null;
	        
	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             post.setHeader("Content-Type", "application/json");
	             post.setHeader("Session-Token", MyApplication.session_token);
	             
	             JSONObject dato = new JSONObject();
	             
	             dato.put("photo", "/api/v1/photo/" + id +"/");
	             dato.put("category", chosen_report);

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
	             any_error = true; // Indicamos que hubo alg�n error 			
	        }
	        
	        if (any_error){
	        	return 666;
	        }
	        else{
		        // Devolvemos el resultado 
		        return (response.getStatusLine().getStatusCode());
	        }
	    }
	}
	
	
	/**
	 * Reproduce el audio
	 *
	 */
	private class PlayAudioTask extends AsyncTask<String,Integer,Integer> {
		
		private Audio audio;
		
		@Override
		protected void onPreExecute() {
			// Mostramos el loading y quitamos la imagen del audio
			prog_loading_audio.setVisibility(View.VISIBLE);
			img_aud.setVisibility(View.GONE);
			
			audio = Audio.getInstance(getActivity().getBaseContext());
		}
		
		@Override
	    protected Integer doInBackground(String... params) {
	    			
			audio.setOnPlayListener(new PlayListener() {

				@Override
				public void onPlayPrepared() {
					// Mostramos el audio y quitamos el loading
					prog_loading_audio.setVisibility(View.GONE);
					img_aud.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPlayFinished() {			
				}
			});
			
			try{
				audio.startPlaying(MyApplication.DIRECCION_BUCKET + params[0]);
			}
			catch(Exception e){
				
			}

	    	return null;
	    }	
		
		@Override
		protected void onPostExecute(Integer result) {
			
		}

	}
	
	
	

	

	// ---------------------------------------------------------------------------------------------
	// --------------------    M�TODOS      ----------------------------------------------
	// ---------------------------------------------------------------------------------------------
	

    /**
     * Devuelve el n�mero de comentarios
     */
    public int getNumComments(){
    	return num_comments;
    }
    
 
 	
 	
 	/**
	 * Env�a un like o dislike al servidor a partir de un usuario y una foto
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

	             // Hacemos la petici�n al servidor
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
 	
	
	/**
	 * Actualiza en pantalla el �ltimo comentario del esc�ndalo
	 */
	private void updateLastComment(){
		
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
        
        // Si hay alg�n comentario
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
            	social_net.setImageResource(R.drawable.s_circular_gris);
            }
            else if (social_ne == 1){
            	social_net.setImageResource(R.drawable.f_circular_gris);
            }
            
            // Avatar
            if (!last_comment.getAvatar().equals("")){
                img_avatar.setImage(MyApplication.DIRECCION_BUCKET + last_comment.getAvatar(), getActivity().getResources().getDrawable(R.drawable.avatar_defecto));
            }
        }
        
        // No hay comentarios
        else{
        	// Si el usuario est� logueado
        	if (MyApplication.logged_user){
        		
        		comment_text.setText(getResources().getString(R.string.se_el_primero_en_comentar));
        		txt_user_name.setText(MyApplication.user_name);
        		txt_date.setText(Utils.getCurrentDate());
        		if (MyApplication.social_network == 0){
        			social_net.setImageResource(R.drawable.s_circular_gris);
        		}
        		else{
        			social_net.setImageResource(R.drawable.f_circular_gris);
        		}
        		
                // Avatar
                img_avatar.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, getActivity().getResources().getDrawable(R.drawable.avatar_defecto));
                
        	}
        	
        	// Si no est� logueado
        	else{
        		comment_text.setText(getResources().getString(R.string.inicia_sesion_para_comentar_este_escandalo));
        		txt_user_name.setText(getResources().getString(R.string.invitado));
        		txt_date.setText(Utils.getCurrentDate());
        		social_net.setImageResource(R.drawable.s_gris);
        		
        		// Le mandamos a la pantalla de login si pulsa
                ll_last_comment.setOnClickListener(new View.OnClickListener() {
        			
        			@Override
        			public void onClick(View v) {
        				Intent i = new Intent(getActivity(), LoginSelectActivity.class);
        				startActivityForResult(i,FROM_SCANDAL_FRAGMENT);		
        			}
        		});
        	}
        }
	}
   
}
