package com.bizeu.escandaloh;


import com.applidium.shutterbug.FetchableImageView;
import com.applidium.shutterbug.FetchableImageView.FetchableImageViewListener;
import com.bizeu.escandaloh.model.Escandalo;
import com.bizeu.escandaloh.util.Audio;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ScandalohFragment extends Fragment {

	private static final String ID = "id";
    private static final String URL = "url";
    private static final String TITLE = "title";
    private static final String NUM_COMMENTS = "num_comments";
    private static final String HAS_AUDIO = "has_audio";
    private static final String USER_NAME = "user_name";
    private static final String DATE = "date";

    private String id;
    private String url;
    private String title;
    private int num_comments;
    private boolean has_audio;
    private String user_name;
    private String date;
    
    TextView num_com ;
 
    
    /**
     * Crea y devuelve una nueva instancia de un fragmento
     * @param escan Escandalo para dicho fragmento
     * @return Fragmento con el escándalo
     */
    public static ScandalohFragment newInstance(Escandalo escan) {
        // Instanciamos el fragmento
        ScandalohFragment fragment = new ScandalohFragment();
 
        // Guardamos los datos del fragmento (del escándalo)
        Bundle bundle = new Bundle();
        bundle.putString(ID, escan.getId());
        bundle.putString(URL, escan.getRouteImg());
        bundle.putString(TITLE, escan.getTitle());
        bundle.putInt(NUM_COMMENTS, escan.getNumComments());
        bundle.putBoolean(HAS_AUDIO, escan.hasAudio());
        bundle.putString(USER_NAME, escan.getUser());
        bundle.putString(DATE, escan.getDate());
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
        this.title = (getArguments() != null) ? getArguments().getString(TITLE) : null;
        this.num_comments = (getArguments() != null) ? getArguments().getInt(NUM_COMMENTS) : 0;
        this.has_audio = (getArguments() != null) ? getArguments().getBoolean(HAS_AUDIO) : false;
        this.user_name = (getArguments() != null) ? getArguments().getString(USER_NAME) : null;
        this.date = (getArguments() != null) ? getArguments().getString(DATE) : null;
    }
 
    
    
    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.escandalo, container, false);
             
        // Mostramos los datos del escándalo
        // Foto
        FetchableImageView img = (FetchableImageView) rootView.findViewById(R.id.img_foto);
        img.setImage(this.url, R.drawable.cargando);      
        img.setListener(new FetchableImageViewListener() {
			
				@Override
				public void onImageFetched(Bitmap bitmap, String url) {			
				}
				
				@Override
				public void onImageFailure(String url) {
					Log.v("WE","Error obteniendo foto: " + url);
				}	
			});
        
        // Título
        TextView tit = (TextView) rootView.findViewById(R.id.txt_titulo);
        tit.setText(title);
        
        // Número de comentarios
        num_com = (TextView) rootView.findViewById(R.id.txt_numero_comentarios);
        num_com.setText(Integer.toString(num_comments));
        num_com.setOnClickListener(new View.OnClickListener() {
			
        	@Override
			public void onClick(View v) {
				
        		/*
				 // Mandamos el evento a Google Analytics
				 EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				 easyTracker.send(MapBuilder
				      .createEvent("Acción UI",     // Event category (required)
				                   "Boton clickeado",  // Event action (required)
				                   "Ver comentarios desde carrusel",   // Event label
				                   null)            // Event value
				      .build()
				  );
				  */
				
				// Paramos si hubiera algún audio reproduciéndose
				Audio.getInstance(getActivity().getBaseContext()).releaseResources();
				
				Intent i = new Intent(getActivity().getBaseContext(), DetailCommentsActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("id", id);
				//i.putExtra("id", v.getTag(R.string.id).toString());
				i.putExtra("route_image", url);
				//i.putExtra("route_image", (String) v.getTag(R.string.url_foto));
				i.putExtra("user", user_name);
				//i.putExtra("user", (String) v.getTag(R.string.user));
				i.putExtra("title", title);
				//i.putExtra("title", (String) v.getTag(R.string.title));
				getActivity().getBaseContext().startActivity(i);	
			}
		});
        
        
        // Micrófono
        ImageView aud = (ImageView) rootView.findViewById(R.id.img_escandalo_microfono);
        if(has_audio){
        	aud.setVisibility(View.VISIBLE);
        }
        else{
        	aud.setVisibility(View.INVISIBLE);
        }
        
        // Nombre de usuario
        TextView user_na = (TextView) rootView.findViewById(R.id.txt_escandalo_name_user);
        user_na.setText(user_name);
        
        // Fecha
        TextView dat = (TextView) rootView.findViewById(R.id.txt_escandalo_date);
        dat.setText(changeFormatDate(date));     
        
        // Devolvemos la vista
        return rootView;
    }
    
   
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
    
    

    
    public int getNumComments(){
    	return num_comments;
    }
}
