package com.bizeu.escandaloh.users;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.MyApplication;
import com.bizeu.escandaloh.adapters.HistoryPageAdapter;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class ProfileActivity extends SherlockFragmentActivity implements OnTabChangeListener, OnPageChangeListener{

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    VARIABLES                                                      |
	// -----------------------------------------------------------------------------------------------------
	
	public static final int AVATAR_FROM_CAMERA = 15;
	public static final int AVATAR_FROM_GALLERY = 14;
	public static final int CROP_PICTURE = 16;
	public static final int PROFILE_SETTINGS = 17;
	public static final String USER_ID = "user_id";
	public static final String PICTURE_BYTES = "picture_bytes";
	public static final String LOGGED = "logged";
	public static final String FOLLOW = "follow";
	public static final String UNFOLLOW = "unfollow";
	
	private FetchableImageView img_picture;
	private TextView txt_username;
	private TextView txt_followers;
	private TextView txt_following;
	private TextView txt_siguiendo;
	private ProgressBar prog_userinfo;
	private LinearLayout ll_seguir_siguiendo;
	private LinearLayout ll_seguidores;
	private LinearLayout ll_siguiendo;
	private TextView txt_seguir_siguiendo;
	private ImageView img_seguir_siguiendo;
	private TextView txt_seguidores;
	private TextView txt_settings;
	private ImageView img_editar_avatar;
	private View view_pantalla_negra;

	private boolean is_me = false; // Nos indica si soy el usuario del perfil
	private Context mContext;
	private String user_id;
	private Uri mImageUri;
	private boolean any_error_user_info;
	private boolean any_error_follow;
	private String avatar;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration = 500;
    private ViewPager mViewPager;
    HistoryPageAdapter pageAdapter;
    private TabHost mTabHost;
	private boolean is_following;
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS  ACTIVITY                                              |
	// -----------------------------------------------------------------------------------------------------
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		mContext = this;
		
		// Activamos el logo dell menu para el menu lateral
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_profile, null);
		actBar.setCustomView(view);
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.s_mezcla);
		actBar.setDisplayShowTitleEnabled(true);
		actBar.setTitle(getResources().getString(R.string.perfil));
	
		img_picture = (FetchableImageView) findViewById(R.id.img_profile_picture);
		txt_username = (TextView) findViewById(R.id.txt_profile_username);
		txt_followers = (TextView) findViewById(R.id.txt_profile_num_seguidores);
		txt_following = (TextView) findViewById(R.id.txt_profile_num_siguiendo);
		txt_settings = (TextView) findViewById(R.id.txt_profile_settings);
		ll_seguir_siguiendo = (LinearLayout) findViewById(R.id.ll_profile_seguir_siguiendo);
		prog_userinfo = (ProgressBar) findViewById(R.id.prog_profile_userinfo);
		txt_seguir_siguiendo = (TextView) findViewById(R.id.txt_profile_seguir_siguiendo);
		txt_seguidores = (TextView) findViewById(R.id.txt_profile_seguidores);
		ll_seguidores = (LinearLayout) findViewById(R.id.ll_profile_seguidores);
		ll_siguiendo = (LinearLayout) findViewById(R.id.ll_profile_siguiendo);
		img_seguir_siguiendo = (ImageView) findViewById(R.id.img_profile_seguir_siguiendo);
		view_pantalla_negra = (View) findViewById(R.id.view_profile_pantalla_negra);
		img_editar_avatar = (ImageView) findViewById(R.id.img_profile_editar);
		txt_siguiendo = (TextView) findViewById(R.id.txt_profile_siguiendo);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		
        // Configuración del usuario
		txt_settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ProfileActivity.this, ProfileSettingsActivity.class);
				startActivityForResult(i, PROFILE_SETTINGS);	
			}
		});
		
		// Seguir/dejar de seguir usuario
		ll_seguir_siguiendo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Obtenemos el nº de seguidores
				int num_seguidores = Integer.parseInt(txt_followers.getText().toString());
			
				// Si estoy siguiendo a ese usuario lo dejo de seguir
				if (is_following){
					is_following = false;
					showFollowMenu();
					new FollowUnfollowUserTask(UNFOLLOW).execute();	
					num_seguidores--;
				}
				
				// Si no, lo empiezo a seguir
				else{
					is_following = true;
					showFollowingMenu();
					new FollowUnfollowUserTask(FOLLOW).execute();	
					num_seguidores++;
				}	
				
				// Actualizamos el nº de seguidores
				txt_followers.setText(Integer.toString(num_seguidores));
				if (num_seguidores == 1){
					txt_seguidores.setText(getResources().getString(R.string.seguidor));
				}
				
				else{
					txt_seguidores.setText(getResources().getString(R.string.seguidores));
				}
			}
		});
		
		// Mostramos la información del usuario
		if (getIntent() != null){
			user_id = getIntent().getStringExtra(USER_ID);
		}
		
		// Inicializamos "Mi actividad" del usuario
        initialiseTabHost();
        List<Fragment> fragments = getFragments();
        pageAdapter = new HistoryPageAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(this);
		
		// Mostrar avatar en pantalla completa
		img_picture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				
				// Si es versión 14+ mostramos animación
				if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
					zoomImageFromThumb(img_picture);		
				} 
				
			    // Si no, lo mostramos en otra pantalla
				else{
				    Intent i = new Intent(ProfileActivity.this, ProfilePhotoActivity.class);
				    i.putExtra(ProfilePhotoActivity.AVATAR, avatar);
				    startActivity(i);
				}		
			}
		});	
		
		// Editar avatar
		img_editar_avatar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Creamos un popup para elegir entre hacer foto con la cámara o cogerla de la galería
				final CharSequence[] items = {
					getResources().getString(R.string.hacer_foto_con_camara),
					getResources().getString(R.string.seleccionar_foto_galeria) };
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				builder.setTitle(R.string.avatar);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						// Cámara
						if (items[item].equals(getResources().getString(R.string.hacer_foto_con_camara))) {

							// Comprobamos disponibilidad de la cámara
							if (Utils.checkCameraHardware(mContext)) {
								
								// Comprobamos disponibilidad del almacenamiento externo
								if (Utils.isExternalStorageWritable(mContext)){
									
									Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
									
									// Nos aseguramos que hay una actividad para la cámara
								    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
								    	
								        File photoFile = null;
								        photoFile = Utils.createProfilePhotoScandalOh(mContext);
		        						       
								        if (photoFile != null) {
								        	mImageUri = Uri.fromFile(photoFile);
								            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
								            startActivityForResult(takePictureIntent, AVATAR_FROM_CAMERA);
								        }
								    }
								}
								
								// Almacenamiento externo no disponible
								else{
									Toast toast = Toast.makeText(mContext,R.string.no_se_puede_acceder_al_sistema_de_archivos,Toast.LENGTH_LONG);
									toast.show();
								}											
							}

							// Cámara no disponible
							else {
								Toast toast = Toast.makeText(mContext,R.string.este_dispositivo_no_dispone_camara,Toast.LENGTH_LONG);
								toast.show();
							}															
						}

						// Galería
						else if (items[item].equals(getResources().getString(
								R.string.seleccionar_foto_galeria))) {

							Intent i = new Intent(
									Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(i, AVATAR_FROM_GALLERY);
						}
					}
				});
				builder.show();			
			}
		});
		
		// Guardar avatar
		img_picture.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// Creamos un popup para poder guardar la foto en galería
				final CharSequence[] items = {
					getResources().getString(R.string.guardar_foto_galeria) };
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						// Guardar foto en galería
						if (items[item].equals(getResources().getString(R.string.guardar_foto_galeria))) {	
							new SaveImageTask(mContext).execute(MyApplication.DIRECCION_BUCKET + avatar);			
						}
					}
				});
				builder.show();	
				return false;
			}
		});
		
		
		// Mostramos la lista de seguidores
		ll_seguidores.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String followers_c = txt_followers.getText().toString().substring(0,1);
				if (!followers_c.equals("0")){
					Intent i = new Intent(ProfileActivity.this, FollowersActivity.class);
					i.putExtra(FollowersActivity.USER_ID, user_id);
					i.putExtra(FollowersActivity.FOLLOWERS, true);
					startActivity(i);
				}
			}
		});
		
		// Mostramos la lista de siguiendo
		ll_siguiendo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String following_c = txt_following.getText().toString().substring(0,1);
				if (!following_c.equals("0")){
					Intent i = new Intent(ProfileActivity.this, FollowersActivity.class);
					i.putExtra(FollowersActivity.USER_ID, user_id);
					i.putExtra(FollowersActivity.FOLLOWERS, false);
					startActivity(i);	
				}
			}
		});	
		
		
		// Cambiamos el color de los tabs
		TabHost host = (TabHost) findViewById(R.id.tabhost_profile);
		TabWidget widget = host.getTabWidget();
		for(int i = 0; i < widget.getChildCount(); i++) {
		    View v = widget.getChildAt(i);
		    TextView tv = (TextView)v.findViewById(android.R.id.title);
		    if(tv == null) {
		        continue;
		    }
		    v.setBackgroundResource(R.drawable.apptheme_tab_indicator_holo);
		}
	}
	
	
	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart(){
		super.onStart();
		
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
		
		if (user_id != null){
			new ShowUserInformation().execute();
		}
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
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
	    	break;
		}
		return true;
	}
	
	
	
	/**
	 * onActivityResult
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Avatar desde la cámara
		if (requestCode == AVATAR_FROM_CAMERA) {
			
			if (resultCode == RESULT_OK) {
				
				if (mImageUri != null) {
					
					Intent i = new Intent(ProfileActivity.this, CropActivity.class);
					i.putExtra("photo_from", AVATAR_FROM_CAMERA);
					i.putExtra("photoUri", mImageUri.toString());
					startActivityForResult(i,CROP_PICTURE);
				}
			}
		}

		// Avatar desde la galería
		else if (requestCode == AVATAR_FROM_GALLERY) {
			if (data != null) {
				Uri selectedImageUri = data.getData();
				Intent i = new Intent(ProfileActivity.this, CropActivity.class);
				i.putExtra("photo_from", AVATAR_FROM_GALLERY);
				i.putExtra("photoUri", ImageUtils.getRealPathFromURI(mContext,selectedImageUri));
				startActivityForResult(i,CROP_PICTURE);
			}
		}

		// Crop de la foto
		else if (requestCode == CROP_PICTURE) {
			img_picture.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
		}
		
		// Settings del usuario
		else if (requestCode == PROFILE_SETTINGS){
			// Si ha cerrado sesión cerramos esta pantalla
			if (!MyApplication.logged_user){
				finish();
			}
		}
	}
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS                                                        |
	// -----------------------------------------------------------------------------------------------------
	
	
	/**
	 * Muestra en el menú que se está siguiendo al usuario
	 */
	private void showFollowingMenu(){
		txt_seguir_siguiendo.setText(getResources().getString(R.string.siguiendo_may));
		txt_seguir_siguiendo.setTextColor(getResources().getColor(R.color.verde));
		img_seguir_siguiendo.setImageResource(R.drawable.tick_verde);
	}
	
	
	/**
	 * Muestra en el menú la opción de seguir
	 */
	private void showFollowMenu(){
		txt_seguir_siguiendo.setText(getResources().getString(R.string.seguir));
		txt_seguir_siguiendo.setTextColor(getResources().getColor(R.color.negro));
		img_seguir_siguiendo.setImageResource(R.drawable.mas_negro);
	}
	
	
	/**
	 * Oculta el loading y muestra la información del usuario (nombre, nº seguidores y siguiendo y botón de seguir)
	 */
	private void showUserInfo(){
		prog_userinfo.setVisibility(View.GONE);
		//ll_userinfo_data.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * Devuelve el tab seleccionado actualmente
	 * @return Posición del tab actual
	 */
	public int getCurrentTab(){
		return mTabHost.getCurrentTab();
	}
	
	
	/**
	 * Añade los tabs de Mi Actividad
	 * @param activity
	 * @param tabHost
	 * @param tabSpec
	 */
    private static void AddTab(ProfileActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec) {
        tabSpec.setContent(new MyTabFactory(activity));
        tabHost.addTab(tabSpec);
    }
    
	
    /**
     * Devuelve los fragmentos correspondientes a "Mi Actividad"
     * @return
     */
    private List<Fragment> getFragments(){
        List<Fragment> fList = new ArrayList<Fragment>();

        HistoryFragment f1 = HistoryFragment.newInstance(user_id, HistoryFragment.ESCANDALOS);
        HistoryFragment f2 = HistoryFragment.newInstance(user_id, HistoryFragment.COMENTARIOS);
        HistoryFragment f3 = HistoryFragment.newInstance(user_id, HistoryFragment.LIKES);
        fList.add(f1);
        fList.add(f2);
        fList.add(f3);

        return fList;
    }

    
    /**
     * Inicializa los tabs
     */
    private void initialiseTabHost() {
        mTabHost = (TabHost) findViewById(R.id.tabhost_profile);
        mTabHost.setup();

        ProfileActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(getResources().getString(R.string.subidos)).setIndicator(getResources().getString(R.string.subidos)));
        ProfileActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(getResources().getString(R.string.comentados)).setIndicator(getResources().getString(R.string.comentados)));
        ProfileActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(getResources().getString(R.string.valorados)).setIndicator(getResources().getString(R.string.valorados)));

        mTabHost.setOnTabChangedListener(this);
    }
	
	
	/**
	 * Muestra el avatar en pantalla completa a través de una animación.
	 * Si se vuelve a pulsar el avatar se vuelve a la configuración inicial
	 * @param thumbView
	 */
	@SuppressLint("NewApi")
	private void zoomImageFromThumb(final View thumbView) {
	    // If there's an animation in progress, cancel it
	    // immediately and proceed with this one.
	    if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }

	    // Load the high-resolution "zoomed-in" image.
	    final ImageView expandedImageView = (ImageView) findViewById(
	            R.id.img_profile_expanded);
	    expandedImageView.setImageDrawable(img_picture.getDrawable());

	    // Calculate the starting and ending bounds for the zoomed-in image.
	    // This step involves lots of math. Yay, math.
	    final Rect startBounds = new Rect();
	    final Rect finalBounds = new Rect();
	    final Point globalOffset = new Point();

	    // The start bounds are the global visible rectangle of the thumbnail,
	    // and the final bounds are the global visible rectangle of the container
	    // view. Also set the container view's offset as the origin for the
	    // bounds, since that's the origin for the positioning animation
	    // properties (X, Y).
	    thumbView.getGlobalVisibleRect(startBounds);
	    findViewById(R.id.rl_profile_screen).getGlobalVisibleRect(finalBounds, globalOffset);
	    startBounds.offset(-globalOffset.x, -globalOffset.y);
	    finalBounds.offset(-globalOffset.x, -globalOffset.y);

	    // Adjust the start bounds to be the same aspect ratio as the final
	    // bounds using the "center crop" technique. This prevents undesirable
	    // stretching during the animation. Also calculate the start scaling
	    // factor (the end scaling factor is always 1.0).
	    float startScale;
	    if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
	        // Extend start bounds horizontally
	        startScale = (float) startBounds.height() / finalBounds.height();
	        float startWidth = startScale * finalBounds.width();
	        float deltaWidth = (startWidth - startBounds.width()) / 2;
	        startBounds.left -= deltaWidth;
	        startBounds.right += deltaWidth;
	    } else {
	        // Extend start bounds vertically
	        startScale = (float) startBounds.width() / finalBounds.width();
	        float startHeight = startScale * finalBounds.height();
	        float deltaHeight = (startHeight - startBounds.height()) / 2;
	        startBounds.top -= deltaHeight;
	        startBounds.bottom += deltaHeight;
	    }

	    // Hide the thumbnail and show the zoomed-in view. When the animation
	    // begins, it will position the zoomed-in view in the place of the
	    // thumbnail.
	    thumbView.setAlpha(0f);
	    view_pantalla_negra.setVisibility(View.VISIBLE);
	    expandedImageView.setVisibility(View.VISIBLE);

	    // Set the pivot point for SCALE_X and SCALE_Y transformations
	    // to the top-left corner of the zoomed-in view (the default
	    // is the center of the view).
	    expandedImageView.setPivotX(0f);
	    expandedImageView.setPivotY(0f);

	    // Construct and run the parallel animation of the four translation and
	    // scale properties (X, Y, SCALE_X, and SCALE_Y).
	    AnimatorSet set = new AnimatorSet();
	    set
	            .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
	                    startBounds.left, finalBounds.left))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
	                    startBounds.top, finalBounds.top))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
	            startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
	                    View.SCALE_Y, startScale, 1f));
	    set.setDuration(mShortAnimationDuration);
	    set.setInterpolator(new DecelerateInterpolator());
	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	            mCurrentAnimator = null;
	        }

	        @Override
	        public void onAnimationCancel(Animator animation) {
	            mCurrentAnimator = null;
	        }
	    });
	    set.start();
	    mCurrentAnimator = set;

	    // Upon clicking the zoomed-in image, it should zoom back down
	    // to the original bounds and show the thumbnail instead of
	    // the expanded image.
	    final float startScaleFinal = startScale;
	    expandedImageView.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View view) {
	            if (mCurrentAnimator != null) {
	                mCurrentAnimator.cancel();
	            }

	            // Animate the four positioning/sizing properties in parallel,
	            // back to their original values.
	            AnimatorSet set = new AnimatorSet();
	            set.play(ObjectAnimator
	                        .ofFloat(expandedImageView, View.X, startBounds.left))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.Y,startBounds.top))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.SCALE_X, startScaleFinal))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.SCALE_Y, startScaleFinal));
	            set.setDuration(mShortAnimationDuration);
	            set.setInterpolator(new DecelerateInterpolator());
	            set.addListener(new AnimatorListenerAdapter() {
	                @Override
	                public void onAnimationEnd(Animator animation) {
	                    thumbView.setAlpha(1f);
	                    view_pantalla_negra.setVisibility(View.GONE);
	                    expandedImageView.setVisibility(View.GONE);
	                    mCurrentAnimator = null;
	                }

	                @Override
	                public void onAnimationCancel(Animator animation) {
	                    thumbView.setAlpha(1f);
	                    expandedImageView.setVisibility(View.GONE);
	                    mCurrentAnimator = null;
	                }
	            });
	            set.start();
	            mCurrentAnimator = set;
	        }
	    });
	    
	    expandedImageView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// Creamos un popup para poder guardar la foto en galería
				final CharSequence[] items = {
					getResources().getString(R.string.guardar_foto_galeria) };
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						// Guardar foto en galería
						if (items[item].equals(getResources().getString(R.string.guardar_foto_galeria))) {	
							new SaveImageTask(mContext).execute(MyApplication.DIRECCION_BUCKET + avatar);			
						}
					}
				});
				builder.show();	
				return false;
			}
		});
	}

	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                           OnTabChangeListener, OnPageChangeListener                               |                           |
	// -----------------------------------------------------------------------------------------------------
	
	
    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        int pos = this.mViewPager.getCurrentItem();
        this.mTabHost.setCurrentTab(pos);
    }

    @Override
        public void onPageSelected(int arg0) {
    }

	@Override
	public void onTabChanged(String tag) {
		int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);	
	}
	

	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                CLASES                                                             |
	// -----------------------------------------------------------------------------------------------------
	

	/**
	 * Muestra la información del usuario
	 * 
	 */
	private class ShowUserInformation extends AsyncTask<Void, Integer, Integer> {

		String username;
		String followers_count;
		String follows_count;

		@Override
		protected void onPreExecute() {
			any_error_user_info= false;	
			txt_username.setText("");
		}
		
		@Override
		protected Integer doInBackground(Void... params) {

			String url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/" + user_id + "/profile/" ;			

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				get.setHeader("content-type", "application/json");
				
				if (MyApplication.logged_user){
					get.setHeader("session-token", MyApplication.session_token);
				}

				// Hacemos la petición al servidor
				response = httpClient.execute(get);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				// Parseamos el json devuelto
				JSONObject respJson = new JSONObject(respStr);
				
				username = respJson.getString("username");
				avatar = respJson.getString("avatar");
				followers_count = respJson.getString("followers_count");
				follows_count = respJson.getString("follows_count");
				is_following = respJson.getBoolean("is_following");
				is_me = respJson.getBoolean("is_me");
				
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error obteniendo información del usuario", ex);
				// Hubo algún error inesperado
				any_error_user_info = true;
			}

			// Si hubo algún error devolvemos 666
			if (any_error_user_info) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			showUserInfo();
			
			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			// No hubo error
			else{		
				// Mostramos avatar, nombre de usuario y nº seguidores
				txt_username.setText(username);		
				img_picture.setImage(MyApplication.DIRECCION_BUCKET + avatar);
				txt_following.setText(follows_count);
				txt_followers.setText(followers_count);
				if (followers_count.equals("1")){
					txt_seguidores.setText(getResources().getString(R.string.seguidor));
				}			
				else{
					txt_seguidores.setText(getResources().getString(R.string.seguidores));
				}
				txt_siguiendo.setText(getResources().getString(R.string.siguiendo));
				
				// Estoy logueado
				if (MyApplication.logged_user){
					
					// Soy el usuario del perfil
					if (is_me){					
						// Mostramos opción de configuracion y ocultamos seguir/siguiendo
						txt_settings.setVisibility(View.VISIBLE);
						ll_seguir_siguiendo.setVisibility(View.GONE);
						// Mostramos botón para editar el avatar
						img_editar_avatar.setVisibility(View.VISIBLE);
					}
					
					// No soy usuario del perfil
					else{				
						// Si estoy siguiendo a ese perfil mostramos "Siguiendo"
						if (is_following){
							showFollowingMenu();
						}
						
						// Si no, mostramos "Seguir"
						else{
							showFollowMenu();
						}
						
						// Mostramos seguir/siguiendo y ocultamos opción de configuración
						txt_settings.setVisibility(View.GONE);
						ll_seguir_siguiendo.setVisibility(View.VISIBLE);
					}
				}
				
				// Soy anónimo
				else{
					// Ocultamos opciones de configuración y seguir/siguiendo
					txt_settings.setVisibility(View.GONE);
					ll_seguir_siguiendo.setVisibility(View.GONE);
				}
			}
		}
	}
	
	
	/**
	 * Sigue/Deja de seguir a un usuario
	 * 
	 */
	private class FollowUnfollowUserTask extends AsyncTask<Void, Integer, Integer> {

		String status;
		private String follow_unfollow;

		public FollowUnfollowUserTask(String follow_unfollow){
			this.follow_unfollow = follow_unfollow;
		}
		
		@Override
		protected void onPreExecute() {
			any_error_follow= false;
		}

		
		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;

			if (follow_unfollow.equals(FOLLOW)){
				url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/follow/" ;		
			}
			
			else{
				url =  MyApplication.SERVER_ADDRESS + "/api/v1/user/unfollow/" ;
			}	
		
			HttpResponse response = null;

			try {
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpPost post = new HttpPost(url);
	            post.setHeader("Content-Type", "application/json");
	            post.setHeader("Session-Token", MyApplication.session_token);
	            
	             JSONObject dato = new JSONObject();	                        
	             dato.put("user_id", user_id);
	             
	             StringEntity entity = new StringEntity(dato.toString(), HTTP.UTF_8);
	             post.setEntity(entity);

				// Hacemos la petición al servidor
				response = httpClient.execute(post);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				// Parseamos el json devuelto
				JSONObject respJson = new JSONObject(respStr);
				
				status = respJson.getString("status");
				
				if (status.equals("error")){
					any_error_follow = true;
				}
							
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error siguiendo/dejando de seguir a un usuario", ex);
				// Hubo algún error inesperado
				any_error_follow = true;
			}

			// Si hubo algún error devolvemos 666
			if (any_error_follow) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}
	
	
	
	/**
	 * Guarda una foto en la galería
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
            	Bitmap bitma = ImageUtils.getBitmapFromURL(args[0]);
            	file = Utils.createProfilePhotoScandalOh(context);
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
	        	
		        // Guardamos la foto en la galería  
	        	Utils.galleryAddPic(file.getAbsolutePath(), context);
				
				// Mostramos un mensaje
				Toast toast = Toast.makeText(context, R.string.foto_guardada_en_este_dispositivo, Toast.LENGTH_SHORT);
				toast.show();
	        }
	    }
	}
	
}
