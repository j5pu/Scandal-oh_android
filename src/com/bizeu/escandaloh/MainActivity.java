package com.bizeu.escandaloh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.services.s3.AmazonS3Client;
import com.applidium.shutterbug.FetchableImageView;
import com.bizeu.escandaloh.adapters.*;
import com.bizeu.escandaloh.dialogs.EnterUrlDialog;
import com.bizeu.escandaloh.dialogs.RecordAudioDialog;
import com.bizeu.escandaloh.dialogs.RecordAudioDialog.OnMyDialogResult;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.notifications.NotificationsActivity;
import com.bizeu.escandaloh.notifications.PushReceiver;
import com.bizeu.escandaloh.settings.SettingsActivity;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.users.ProfileActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.DepthPageTransformer;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
import com.countrypicker.CountryPicker;
import com.countrypicker.CountryPickerListener;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;
import com.parse.ParseAnalytics;

public class MainActivity extends SherlockFragmentActivity implements
		OnClickListener, OnItemSelectedListener {

	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    VARIABLES                                                      |
	// -----------------------------------------------------------------------------------------------------
	
	public static final int NUM_SCANDALS_TO_LOAD = 15;
	public static final int NUM_SCANDALS_TO_LOAD_FIRST_TIME = 10;
	public static final int FROM_CAMERA = 10;
	private static final int CREATE_ESCANDALO = 11;
	public static final int FROM_GALLERY = 12;
	public static final int SHARING = 13;
	public static final int SHOW_PROFILE = 14;
	public static final int FROM_AUDIO = 16;
	public static final int FROM_URL = 17;
	public static final String CATEGORY = "Category";
	public static final String ANGRY = "Denuncia";
	public static final String HAPPY = "Humor";
	public static final String BOTH = "Todas";
	public static final String NORMAL = "Normal";
	private static final String FILTRO_RECIENTES = "-date";
	private static final String FILTRO_COMENTADAS = "-comments_count";
	private static final String FILTRO_VOTADAS = "-votes_count";

	private LinearLayout ll_refresh;
	private LinearLayout ll_take_photo;
	private ImageView img_update_list;
	private LinearLayout ll_lateral_notificaciones;
	private LinearLayout ll_lateral_pais;
	private LinearLayout ll_lateral_busqueda;
	private LinearLayout ll_lateral_ajustes;
	private LinearLayout ll_lateral_mas_recientes;
	private LinearLayout ll_lateral_mas_comentados;
	private LinearLayout ll_lateral_mas_votados;
	private TextView txt_lateral_nombreusuario;
	private TextView txt_lateral_mas_recientes;
	private TextView txt_lateral_mas_votados;
	private TextView txt_lateral_mas_comentados;
	private ProgressBar progress_refresh;
	private LinearLayout ll_menu_lateral;
	private Spinner spinner_categorias;
	DrawerLayout mDrawerLayout;
	private FetchableImageView img_lateral_avatar;
	private TextView txt_action_bar_num_notis;
	private TextView txt_country_selected;
	private TextView txt_num_notifs;
	
	private Uri mImageUri;
	AmazonS3Client s3Client;
	private static Activity acti;
	private Context mContext;
	static ScandalohFragmentPagerAdapter adapter;
	ViewPager pager = null;
	ProgressBar loading;
	private boolean any_error;
	private GetScandalsTask getEscandalosAsync;
	private GetNewScandals getNewEscandalosAsync;
	public static String current_category;
	private boolean getting_escandalos = true;
	private boolean there_are_more_scandals = true;
	public static ArrayList<Scandaloh> escandalos;
	DrawerMenuAdapter mMenuAdapter;
	String[] options;
	ActionBarDrawerToggle mDrawerToggle;
	private boolean no_hay_escandalos;
	private ArrayAdapter<CharSequence> adapter_spinner;
	private String actual_avatar = null; // Usado para saber si el usuario ha cambiado de avatar
	private String meta_next_scandals = null;
    private String actual_filter = FILTRO_RECIENTES ;
	public static boolean activity_is_showing = false;
	private String path_photo_file;

	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS  ACTIVITY                                              |
	// -----------------------------------------------------------------------------------------------------
	
	
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		no_hay_escandalos = false;
		mContext = this;
		acti = this;
		escandalos = new ArrayList<Scandaloh>();

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.lay_pantalla_main));

		// Push notifications Parse
		ParseAnalytics.trackAppOpened(getIntent());
		
		// ACTION BAR
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_main, null);
		actBar.setCustomView(view);

		// Activamos el logo del menu para el menu lateral
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.s_mezcla);

		// Si viene de una notificaci�n push y el usuario est� logueado abrimos la pantalla de notificaciones
		if (getIntent().getAction().equals(PushReceiver.PUSH_NOTIFICATION) && MyApplication.logged_user){
			Intent i = new Intent(MainActivity.this, NotificationsActivity.class);
			startActivity(i);
		}

		loading = (ProgressBar) findViewById(R.id.loading_escandalos);
		img_update_list = (ImageView) findViewById(R.id.img_actionbar_updatelist);
		ll_refresh = (LinearLayout) findViewById(R.id.ll_main_refresh);
		ll_refresh.setOnClickListener(this);
		ll_take_photo = (LinearLayout) findViewById(R.id.ll_main_take_photo);
		ll_take_photo.setOnClickListener(this);
		progress_refresh = (ProgressBar) findViewById(R.id.prog_refresh_action_bar);
		txt_action_bar_num_notis = (TextView) findViewById(R.id.txt_action_bar_num_notis);

		// SPINNER
		spinner_categorias = (Spinner) findViewById(R.id.sp_categorias);
		adapter_spinner = ArrayAdapter.createFromResource(this,
				R.array.array_categorias, R.layout.categoria_spinner);
		adapter_spinner.setDropDownViewResource(R.layout.categoria_spinner_desplegaba);
		spinner_categorias.setAdapter(adapter_spinner);
		spinner_categorias.setOnItemSelectedListener(this);
		
		// VIEWPAGER
		pager = (ViewPager) this.findViewById(R.id.pager);
		adapter = new ScandalohFragmentPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {

				// Si quedan 5 esc�ndalos m�s para llegar al �ltimo y a�n quedan
				// m�s esc�ndalos (si hemos llegado
				// a los �ltimos no se pedir�n m�s): obtenemos los siguientes 
				if (position == adapter.getCount() - (NUM_SCANDALS_TO_LOAD-5)
						&& there_are_more_scandals) {
					// Usamos una llave de paso (s�lo la primera vez entrar�).
					// Cuando se obtengan los 10 esc�ndalos se volver� a abrir
					if (!getting_escandalos) {
						// Cambiamos la imagen de actualizar por un loading
						showLoadingOnMenu();	
						getEscandalosAsync = new GetScandalsTask();
						getEscandalosAsync.execute();
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
			pager.setPageTransformer(true, new DepthPageTransformer());
		}

		// MENU LATERAL
		mDrawerLayout = (DrawerLayout) findViewById(R.id.lay_pantalla_main);
		ll_menu_lateral = (LinearLayout) findViewById(R.id.ll_menu_lateral);
		img_lateral_avatar = (FetchableImageView) findViewById(R.id.img_mLateral_avatar);
		ll_lateral_notificaciones = (LinearLayout) findViewById(R.id.ll_mLateral_notificaciones);
		ll_lateral_pais = (LinearLayout) findViewById(R.id.ll_mLateral_pais);
		ll_lateral_busqueda = (LinearLayout) findViewById(R.id.ll_mLateral_buscar);
		ll_lateral_ajustes = (LinearLayout) findViewById(R.id.ll_mLateral_ajustes);
		ll_lateral_mas_recientes = (LinearLayout) findViewById(R.id.ll_mLateral_mas_recientes);
		ll_lateral_mas_votados = (LinearLayout) findViewById(R.id.ll_mLateral_mas_votados);
		ll_lateral_mas_comentados = (LinearLayout) findViewById(R.id.ll_mLateral_mas_comentados);
		txt_lateral_nombreusuario = (TextView) findViewById(R.id.txt_lateral_nombreusuario);
		txt_lateral_mas_comentados = (TextView) findViewById(R.id.txt_mLateral_mas_comentados);
		txt_lateral_mas_votados = (TextView) findViewById(R.id.txt_mLateral_mas_votados);
		txt_lateral_mas_recientes = (TextView) findViewById(R.id.txt_mLateral_mas_recientes);
		txt_num_notifs = (TextView) findViewById(R.id.txt_mLateral_numNotificaciones);
		txt_country_selected = (TextView) findViewById(R.id.txt_mLateral_countryselected);
		
		// Sombra del menu sobre la pantalla
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		// Menu Lateral: si no toca una opci�n que no haga nada
		ll_menu_lateral.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {		
			}
		});
		
		// Notificaciones
		ll_lateral_notificaciones.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, NotificationsActivity.class);
				startActivity(i);			
			}
		});
		
		// Pais
		Locale loc = new Locale("", MyApplication.code_selected_country);
		txt_country_selected.setText(loc.getDisplayCountry());
		
		ll_lateral_pais.setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) {
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
						
				final CountryPicker countryPicker = CountryPicker.newInstance(getResources().getString(R.string.selecciona_pais));
				countryPicker.setListener(new CountryPickerListener() {

					@Override
					public void onSelectCountry(String name, String code) {
								
						// Si el pa�s seleccionado es distinto al actual
						if (!MyApplication.code_selected_country.equals(code)){
							MyApplication.code_selected_country = code;
							Locale lo = new Locale("", code);
							txt_country_selected.setText(lo.getDisplayCountry());
							SharedPreferences prefs = getBaseContext().getSharedPreferences(
						      		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
							// Guardamos el c�digo del pa�s
						    prefs.edit().putString(MyApplication.CODE_COUNTRY, code).commit();
									
							// Si hay conexi�n
							if (Connectivity.isOnline(mContext)) {
								cancelGetScandals();
								hideLoadingFromMenu();
								// Abrimos llave de hay m�s escandalos
								there_are_more_scandals = true;
								// Quitamos los esc�ndalos actuales
								escandalos.clear();

								pager.setCurrentItem(0);
								adapter.clearFragments();
								adapter = new ScandalohFragmentPagerAdapter(
										getSupportFragmentManager());
										pager.setAdapter(adapter);
								// Obtenemos los 10 primeros esc�ndalos para la categor�a seleccionada
								// Mostramos el progressBar y ocultamos la lista de escandalos
								loading.setVisibility(View.VISIBLE);
								pager.setVisibility(View.GONE);
								getEscandalosAsync = new GetScandalsTask();
								getEscandalosAsync.execute();
							}

							// No hay conexi�n
							else {
								Toast toast = Toast.makeText(mContext,
										R.string.no_dispones_de_conexion, Toast.LENGTH_SHORT);
								toast.show();
							}
						}
					        	
					     // Cerramos el dialog
						countryPicker.dismiss();
					}
				});
						
				countryPicker.show(getSupportFragmentManager(), "COUNTRY_PICKER");		
			}
		});
		
		
		// B�squeda
		ll_lateral_busqueda.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Mostramos la pantalla de busqueda
				Intent i = new Intent(MainActivity.this, SearchActivity.class);
				startActivity(i);
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);		
			}
		});
		
		// Avatar, Nombre de usuario y Perfil
		OnClickListener onClickLisPerfil = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Si est� logueado vamos a la pantalla del perfil
				if (MyApplication.logged_user){
					// Almacenamos el avatar actual del usuario
					actual_avatar = MyApplication.avatar;
					// Mostramos la pantalla de perfil
					Intent i = new Intent(MainActivity.this, ProfileActivity.class);
					i.putExtra(ProfileActivity.USER_ID, ProfileActivity.LOGGED);
					startActivityForResult(i,SHOW_PROFILE);	
				}	
				
				// Si no, vamos a la pantalla de iniciar sesi�n
				else{
					Intent i = new Intent(MainActivity.this,LoginSelectActivity.class);
					startActivity(i);
				}
			}
		};
		
		img_lateral_avatar.setOnClickListener(onClickLisPerfil);
		txt_lateral_nombreusuario.setOnClickListener(onClickLisPerfil);

		// Ajustes
		ll_lateral_ajustes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(i);
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
			}
		});
				
		
		// Filtros
		txt_lateral_mas_recientes.setTypeface(null, Typeface.BOLD);   // Por defecto seleccionado los m�s recientes
		
		ll_lateral_mas_recientes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!actual_filter.equals(FILTRO_RECIENTES)){
	            	actual_filter = FILTRO_RECIENTES;
					txt_lateral_mas_recientes.setTypeface(null, Typeface.BOLD);
					txt_lateral_mas_votados.setTypeface(null, Typeface.NORMAL);
					txt_lateral_mas_comentados.setTypeface(null, Typeface.NORMAL);
	            	// Reiniciamos los esc�ndalos con el nuevo filtro
	                resetScandals();
					// Cerramos el menu
					mDrawerLayout.closeDrawer(ll_menu_lateral);
				}			
			}
		});
		
		ll_lateral_mas_votados.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!actual_filter.equals(FILTRO_VOTADAS)){
					actual_filter = FILTRO_VOTADAS;
					txt_lateral_mas_recientes.setTypeface(null, Typeface.NORMAL);
					txt_lateral_mas_votados.setTypeface(null, Typeface.BOLD);
					txt_lateral_mas_comentados.setTypeface(null, Typeface.NORMAL);
	            	// Reiniciamos los esc�ndalos con el nuevo filtro
	                resetScandals();
					// Cerramos el menu
					mDrawerLayout.closeDrawer(ll_menu_lateral);
				}			
			}
		});
		
		ll_lateral_mas_comentados.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!actual_filter.equals(FILTRO_COMENTADAS)){
					actual_filter = FILTRO_COMENTADAS;
					txt_lateral_mas_recientes.setTypeface(null, Typeface.NORMAL);
					txt_lateral_mas_votados.setTypeface(null, Typeface.NORMAL);
					txt_lateral_mas_comentados.setTypeface(null, Typeface.BOLD);
	            	// Reiniciamos los esc�ndalos con el nuevo filtro
	                resetScandals();
					// Cerramos el menu
					mDrawerLayout.closeDrawer(ll_menu_lateral);
				}			
			}
		});

		// Separaci�n entre esc�ndalos
		pager.setPageMargin(3);
		current_category = HAPPY;

		// Si hay conexi�n: obtenemos los primeros esc�ndalos
		if (Connectivity.isOnline(mContext)) {
			getEscandalosAsync = new GetScandalsTask();
			getEscandalosAsync.execute();
		} else {
			Toast toast = Toast.makeText(mContext,
					getResources().getString(R.string.no_dispones_de_conexion),
					Toast.LENGTH_SHORT);
			toast.show();
			// Quitamos el loading y mostramos los esc�ndalos
			pager.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
		}
	}
	

	
	
	/**
	 * onPostCreate
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	
	

	/**
	 * onConfigurationChanged
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	
	
	
	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
		// Indicamos que la actividad se est� mostrando
		activity_is_showing = true;
	}
	
	
	

	/**
	 * onResume
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Si est� logueado
		if (MyApplication.logged_user) {
			ll_lateral_notificaciones.setVisibility(View.VISIBLE); // Mostramos notificaciones
			txt_lateral_nombreusuario.setText(MyApplication.user_name); // Nombre de usuario
			
			// Avatar
			if (MyApplication.avatar != null){
		        img_lateral_avatar.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_defecto);
			}
			else{
		        img_lateral_avatar.setImageResource(R.drawable.avatar_defecto);
			}
			// Actualizamos el n� de notificaciones
			if (Connectivity.isOnline(mContext)){
				new UpdateNumNotificationsTask().execute();
			}
			
			
		// No est� logueado
		} else {
			// Quitamos el n� de comentarios
			txt_action_bar_num_notis.setText("");
			
			// Actualizamos el menu lateral
			ll_lateral_notificaciones.setVisibility(View.GONE); // Ocultamos notificaciones
			img_lateral_avatar.setImageResource(R.drawable.avatar_defecto); // Avatar por defecto
			txt_lateral_nombreusuario.setText(getResources().getString(R.string.iniciar_sesion)); // Usuario invitado 
		}
		
		// Si ha iniciado/cerrado sesi�n: reiniciamos los esc�ndalos y eliminamos las push que hubiesen
		if (MyApplication.reset_scandals){		
			resetScandals();
			NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(PushReceiver.NOTIFICATION_ID);
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
		// Indicamos que la actividad no se est� mostrando
		activity_is_showing = false;
	}

	
	
	
	/**
	 * onDestroy
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Cancelamos los esc�ndalos que se estuvieran obteniendo
		cancelGetScandals();
	}

	
	
	
	/**
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Le asignamos el bot�n home al men� lateral
		if (item.getItemId() == android.R.id.home) {

			if (mDrawerLayout.isDrawerOpen(ll_menu_lateral)) {
				mDrawerLayout.closeDrawer(ll_menu_lateral);
			} else {
				mDrawerLayout.openDrawer(ll_menu_lateral);
			}
		}		

		return super.onOptionsItemSelected(item);
	}

	
	
	/**
	 * onActivityResult
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Esc�ndalo desde la c�mara
		if (requestCode == FROM_CAMERA) {
			
			if (resultCode == RESULT_OK) {
				
				if (mImageUri != null) {
					
					// Guardamos la foto en la galer�a
					Utils.galleryAddPic(path_photo_file, mContext);

					// Mostramos la pantalla de subir esc�ndalo
					Intent i = new Intent(MainActivity.this, CreateScandalohActivity.class);
					i.putExtra("photo_from", FROM_CAMERA);
					i.putExtra("photoUri", mImageUri.toString());
					startActivityForResult(i, CREATE_ESCANDALO);
					
				} else {
					Toast toast = Toast.makeText(mContext, getResources().getString(R.string.hubo_algun_error_camara),Toast.LENGTH_LONG);
					toast.show();
				}
			}
		}

		// Esc�ndalo desde la galer�a
		else if (requestCode == FROM_GALLERY) {
			if (data != null) {
				// Mostramos la pantalla de subir esc�ndalo
				Uri selectedImageUri = data.getData();
				Intent i = new Intent(MainActivity.this,CreateScandalohActivity.class);
				i.putExtra("photo_from", FROM_GALLERY);
				i.putExtra("photoUri", ImageUtils.getRealPathFromURI(mContext,selectedImageUri));
				startActivityForResult(i, CREATE_ESCANDALO);
			}
		}
		
		// Perfil de usuario
		else if (requestCode == SHOW_PROFILE){
			// Comprobamos si ha cambiado de avatar
			if (actual_avatar != null && MyApplication.avatar != null){
				if (!MyApplication.avatar.equals(actual_avatar)){
					// Lo actualizamos
					updateUserAvatar();
				}
			}
		}
	}
	
	
	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS                                                        |
	// -----------------------------------------------------------------------------------------------------
	

	/**
	 * onClick
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		// Login/Subir escandalo
		case R.id.ll_main_take_photo:

			uploadScandal();
			
			break;

		// Actualizar carrusel: Le decimos al fragmento que actualice los
		// esc�ndalos (y suba el carrusel al primero)
		case R.id.ll_main_refresh:

			// Nos colocamos en el primer escandalo
			pager.setCurrentItem(0);

			// Comprobamos si hay nuevos esc�ndalos s�lo si estamos filtrando por fecha
			if (actual_filter.equals(FILTRO_RECIENTES)){

				// Si no se est�n obteniendo otros esc�ndalos
				if (!getting_escandalos) {

					// Si hay conexi�n
					if (Connectivity.isOnline(mContext)) {

						// Obtenemos los esc�ndalos:
						// Si no hay ninguno mostrado obtenemos los primeros, si hay
						// alguno obtenemos si hay nuevos esc�ndalos subidos
						getting_escandalos = true;

						if (escandalos.size() > 0) {
							getNewEscandalosAsync = new GetNewScandals();
							getNewEscandalosAsync.execute();
						} else {
							no_hay_escandalos = true; // Indicamos que no hay
														// esc�ndalos a�n
							getEscandalosAsync = new GetScandalsTask();
							getEscandalosAsync.execute();
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
			}				
			break;
		}
	}
	
	
	

	/**
	 * Seleccionar opci�n del spinner
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		// Si ha seleccionado una categoria diferente de la que se encuentra actualmente
		if ((pos == 0 && current_category.equals(ANGRY))|| (pos == 1 && current_category.equals(HAPPY))) {
			// Si hay conexi�n
			if (Connectivity.isOnline(mContext)) {
				cancelGetScandals();
				hideLoadingFromMenu();
				// Inhabilitamos el spinner
				spinner_categorias.setClickable(false);
				// Abrimos llave de hay m�s escandalos
				there_are_more_scandals = true;
				// Quitamos los esc�ndalos actuales
				escandalos.clear();

				switch (pos) {
					case 0: // Humor
						if (current_category.equals(ANGRY)) {
							current_category = HAPPY;						
							((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.morado));
						}
						break;
						
					case 1: // Denuncia
						current_category = ANGRY;						
						((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.azul));
						break;
				}

				pager.setCurrentItem(0);
				adapter.clearFragments();
				adapter = new ScandalohFragmentPagerAdapter(
						getSupportFragmentManager());
				pager.setAdapter(adapter);
				// Obtenemos los 10 primeros esc�ndalos para la categor�a seleccionada
				// Mostramos el progressBar y ocultamos la lista de escandalos
				loading.setVisibility(View.VISIBLE);
				pager.setVisibility(View.GONE);
				getEscandalosAsync = new GetScandalsTask();
				getEscandalosAsync.execute();
			}

			// No hay conexi�n
			else {
				Toast toast = Toast.makeText(mContext,
						R.string.no_dispones_de_conexion, Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}
	
	
	
	

	/**
	 * Al no seleccionar nada del spinner
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	

	/**
	 * Cancela si hubiese alguna hebra obteniendo esc�ndalos
	 */
	private void cancelGetScandals() {
		if (getEscandalosAsync != null) {
			if (getEscandalosAsync.getStatus() == AsyncTask.Status.PENDING
					|| getEscandalosAsync.getStatus() == AsyncTask.Status.RUNNING) {
				getEscandalosAsync.cancel(true);
			}
		}

		if (getNewEscandalosAsync != null) {
			if (getNewEscandalosAsync.getStatus() == AsyncTask.Status.PENDING
					|| getNewEscandalosAsync.getStatus() == AsyncTask.Status.RUNNING) {
				getNewEscandalosAsync.cancel(true);
			}
		}
	}
	
	
	
	
	/**
	 * Resetea los esc�ndalos del carrusel
	 */
	private void resetScandals(){
		// Abrimos llave de hay m�s escandalos
		there_are_more_scandals = true;
		// Quitamos los esc�ndalos actuales
		escandalos.clear();
		pager.setCurrentItem(0);
		adapter.clearFragments();
		adapter = new ScandalohFragmentPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		// Obtenemos los 10 primeros esc�ndalos para la categor�a seleccionada
		// Mostramos el progressBar y ocultamos la lista de escandalos
		loading.setVisibility(View.VISIBLE);
		pager.setVisibility(View.GONE);
		getEscandalosAsync = new GetScandalsTask();
		getEscandalosAsync.execute();
		
		// Cerramos llave
		MyApplication.reset_scandals = false;
	}
	
	
	
	
	
	/**
	 * Oculta el bot�n actualizar y muestra el loading en el menu
	 */
	private void showLoadingOnMenu(){
		// Cambiamos la imagen de actualizar por un loading
		progress_refresh.setVisibility(View.VISIBLE);
		img_update_list.setVisibility(View.GONE);
	}
	
	
	
	
	/**
	 * Oculta el loading del menu y muestra el bot�n actualizar
	 */
	private void hideLoadingFromMenu() {
		// Cambiamos el loading del menu por el bot�n de actualizar
		progress_refresh.setVisibility(View.GONE);
		img_update_list.setVisibility(View.VISIBLE); 
	}
	
	
	
	
	/**
	 * Actualiza el already_voted del escandalo (fragmento) que est� actualmente visualiz�ndose
	 * @param already_voted
	 */
	public  static void updateLikesDislikes(int already_voted, int num_likes, int num_dislikes){
		adapter.updateFragmentLike(already_voted, num_likes, num_dislikes);
	}
	
	
	
	/**
	 * Actualiza el �ltimo comentario del escandalo (fragmento) que est� actualmente visualiz�ndose
	 * @param lst_comm
	 */
	public static void updateLastComment(Comment lst_comm){
		adapter.updateLastComment(lst_comm);
	}
	
	
	/**
	 * Actualiza el n�mero de comentarios del escandalo (fragmento) que est� actualmente visualiz�ndose
	 * @param num_comments
	 */
	public static void updateNumComments(int num_comments){
		adapter.updateNumComments(num_comments);
	}
	
	
	
	/**
	 * Actualiza el avatar del usuario en todos los esc�ndalos (fragmentos) 
	 */
	private void updateUserAvatar(){
		adapter.updateUserAvatar();
		adapter.notifyDataSetChanged();
	}
	
	
	
	
	/**
	 * Sube un esc�ndalo
	 */
	public void uploadScandal(){
		// Paramos si hubiera alg�n audio reproduci�ndose
		Audio.getInstance(mContext).releaseResources();

		// Si dispone de conexi�n
		if (Connectivity.isOnline(mContext)) {

			// Si est� logueado
			if (MyApplication.logged_user) {

				// Creamos un menu para elegir entre hacer foto con la c�mara o cogerla de la galer�a
				final CharSequence[] items = {getResources().getString(R.string.hacer_foto_con_camara),
											getResources().getString(R.string.seleccionar_foto_galeria),
											getResources().getString(R.string.subir_audio),
											getResources().getString(R.string.subir_desde_url)
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle(R.string.subir_escandalo);
				builder.setItems(items,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,int item) {

						// SUBIR FOTO CON LA CAMARA
						if (items[item].equals(getResources().getString(R.string.hacer_foto_con_camara))) {

							// Comprobamos disponibilidad de la c�mara
							if (Utils.checkCameraHardware(mContext)) {
								
								// Comprobamos disponibilidad del almacenamiento externo
								if (Utils.isExternalStorageWritable(mContext)){
									
									Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
									
									// Nos aseguramos que hay una actividad para la c�mara
								    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
								    	
								        File photoFile = null;
								        photoFile = Utils.createPhotoScandalOh(mContext);
								        path_photo_file = photoFile.getAbsolutePath();
		        						       
								        if (photoFile != null) {
								        	mImageUri = Uri.fromFile(photoFile);
								            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
								            startActivityForResult(takePictureIntent, FROM_CAMERA);
								        }
								    }
								}
								
								// Almacenamiento externo no disponible
								else{
									Toast toast = Toast.makeText(mContext,R.string.no_se_puede_acceder_al_sistema_de_archivos,Toast.LENGTH_LONG);
									toast.show();
								}											
							}

							// C�mara no disponible
							else {
								Toast toast = Toast.makeText(mContext,R.string.este_dispositivo_no_dispone_camara,Toast.LENGTH_LONG);
								toast.show();
							}
						}

						// SUBIR FOTO DE LA GALERIA
						else if (items[item].equals(getResources().getString(R.string.seleccionar_foto_galeria))) {

							Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(i, FROM_GALLERY);
						}

						// SUBIR AUDIO
						else if (items[item].equals(getResources().getString(R.string.subir_audio))){

							// Mostramos el dialog de grabaci�n de audio
							RecordAudioDialog record_audio = new RecordAudioDialog(mContext, Audio.getInstance(mContext));
							record_audio.setDialogResult(new OnMyDialogResult() {
								public void finish(String result) {
									if (result.equals("OK")) {
										Intent i = new Intent(MainActivity.this, CreateScandalohActivity.class);
										i.putExtra("photo_from", FROM_AUDIO);
										startActivity(i);
									}
								}
							});
							record_audio.setCancelable(false);
							record_audio.show();
						}

						// SUBIR DESDE URL
						else if (items[item].equals(getResources().getString(R.string.subir_desde_url))){

							// Mostramos el dialog de introducir url
							EnterUrlDialog record_audio = new EnterUrlDialog(mContext);
							record_audio.setCancelable(false);
							record_audio.show();
						}
					}
				});
				builder.show();
			}

			// No est� logueado: mostramos un popup preguntando si quiere loguearse
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.debes_iniciar_sesion_para_compartir);		
				builder.setPositiveButton(R.string.iniciar_sesion, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               Intent i = new Intent(MainActivity.this, LoginSelectActivity.class);
			               startActivity(i);
			           }
			       });
				builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.dismiss();
			           }
			       });

				AlertDialog dialog = builder.create();
				dialog.show();
			}
			
		}

		// No dispone de conexi�n
		else {
			Toast toast = Toast.makeText(mContext,
					R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	
	
	
	
	/**
	 * Actualiza el n� de notificaciones
	 */
	public static void updateNumNotifications(){
		((MainActivity) acti).new  UpdateNumNotificationsTask().execute();
	}
	

	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                ADAPTADOR VIEWPAGER                                                |
	// -----------------------------------------------------------------------------------------------------

	public class ScandalohFragmentPagerAdapter extends FragmentStatePagerAdapter {

		// Lista de fragmentos con los esc�ndalos
		List<ScandalFragment> fragments;

		/**
		 * Constructor
		 * 
		 * @param fm Interfaz para interactuar con los fragmentos dentro de una actividad
		 */
		public ScandalohFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fragments = new ArrayList<ScandalFragment>();
		}

	
		/**
		 * Devuelve el fragmento de una posici�n dada
		 * 
		 * @param position Posici�n
		 */
		@Override
		public ScandalFragment getItem(int position) {
			// return ScandalohFragment.newInstance(escandalos.get(position));
			return fragments.get(position);
		}

		/**
		 * Devuelve el n�mero de fragmentos
		 */
		@Override
		public int getCount() {
			return fragments.size();
		}

		/**
		 * getItemPosition
		 */
		@Override
		public int getItemPosition(Object item) {
			ScandalFragment fragment = (ScandalFragment) item;
			if (pager.getCurrentItem() == fragments.indexOf(fragment)) {
				return fragments.indexOf(fragment);
			} else {
				return POSITION_NONE;
			}

		}

		/**
		 * A�ade un fragmento al final de la lista
		 * @param fragment Fragmento a a�adir
		 */
		public void addFragment(ScandalFragment fragment) {
			this.fragments.add(fragment);
		}

		/**
		 * A�ade un fragmento al principio de la lista
		 * @param fragment
		 */
		public void addFragmentAtStart(ScandalFragment fragment) {
			this.fragments.add(0, fragment);
		}

		/**
		 * Modifica un fragmento
		 * @param position
		 * @param fragment
		 */
		public void setFragment(int position, ScandalFragment fragment) {
			this.fragments.set(position, fragment);
		}
		
		
		/**
		 * Actualiza el campo already_voted del fragmento que est� actualmente visualiz�ndose
		 * @param already_voted
		 */
		public void updateFragmentLike(int already_voted, int num_likes, int num_dislikes){
			// Obtenemos el escandalo que est� en pantalla
			Scandaloh scan = escandalos.get(pager.getCurrentItem());
			// Le modificamos el already_voted
			scan.setAlreadyVoted(already_voted);
			scan.setLikes(num_likes);
			scan.setDislikes(num_dislikes);
			// Actualizamos el adaptador con el nuevo fragmento
			ScandalFragment sf2 = ScandalFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}
		
		
		/**
		 * Actualiza el �ltimo comentario del fragmento que est� actualmente visualiz�ndose
		 * @param comment_text
		 */
		public void updateLastComment(Comment comm){
			// Obtenemos el escandalo que est� en pantalla
			Scandaloh scan = escandalos.get(pager.getCurrentItem());
			// Le modificamos el �ltimo comentario
			scan.setLastComment(comm);
			// Actualizamos el adaptador con el nuevo fragmento
			ScandalFragment sf2 = ScandalFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}
		
		
		/**
		 * Actualiza el n� de comentarios del fragmento que est� actualmente visualiz�ndose
		 * @param num_c
		 */
		public void updateNumComments(int num_c){
			// Obtenemos el escandalo que est� en pantalla
			Scandaloh scan = escandalos.get(pager.getCurrentItem());
			// Le modificamos el �ltimo comentario
			scan.setNumComments(num_c);
			// Actualizamos el adaptador con el nuevo fragmento
			ScandalFragment sf2 = ScandalFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}
		
		
		/**
		 * Actualiza el avatar del usuario en todos los esc�ndalos
		 */
		public void updateUserAvatar(){
			 for (int i=0; i<escandalos.size(); i++){
				 
				 // Obtenemos el esc�ndalo
				 Scandaloh scan = escandalos.get(i);
				 
				 // Si soy el usuario del esc�ndalo actualizo mi avatar
				 if (scan.getUser().equals(MyApplication.user_name)){
					 scan.setAvatar(MyApplication.avatar);
				 }
				 
				 // Si soy el usuario del �ltimo comentario actualizo mi avatar
				 Comment cAux = escandalos.get(i).getLastComment();
				 if (cAux != null){
					 if (cAux.getUsername().equals(MyApplication.user_name)){
						 cAux.setAvatar(MyApplication.avatar);
						 scan.setLastComment(cAux);
					 }
				 }
				 
				 ScandalFragment sf2 = ScandalFragment.newInstance(scan);
				 this.fragments.set(i, sf2); 
			 }
		}

		/**
		 * Obtiene un fragmento a partir de una posici�n
		 * @param position
		 * @return
		 */
		public ScandalFragment getFragment(int position) {
			return this.fragments.get(position);
		}
		

		/**
		 * Elimina todos los fragmentos
		 */
		public void clearFragments() {
			this.fragments.clear();
		}
	}

	
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                CLASES                                                             |
	// -----------------------------------------------------------------------------------------------------
	
	
	/**
	 * Obtiene los siguientes 10 esc�ndalos anteriores a partir de uno dado
	 * 
	 */
	private class GetScandalsTask extends AsyncTask<Void, Integer, Integer> {

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
			getting_escandalos = true;
			any_error = false;	
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;
			
			// No hay esc�ndalos: obtenemos los primeros
			if (escandalos.size() == 0){
				
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/?limit=" + NUM_SCANDALS_TO_LOAD;
				
				// HUMOR
				if (current_category.equals(MainActivity.HAPPY)) {
					url += "&category__id=1";
				}
				// DENUNCIA
				else{
					url += "&category__id=2";
				}
						
				url += "&country="+ MyApplication.code_selected_country;
				url += "&order_by=" + actual_filter ;
			}
			
			// Obtenemos los siguientes esc�ndalos
			else{
				
				// Fin del carrusel: meta nulo indica que no hay m�s esc�ndalos
				if (meta_next_scandals.equals("null")){
					there_are_more_scandals = false;
					return 5;
				}
				url = MyApplication.SERVER_ADDRESS + meta_next_scandals;
			}

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getEscandalos = new HttpGet(url);
				getEscandalos.setHeader("content-type", "application/json");
				
				// Si es con usuario le a�adimos el session_token
				if (MyApplication.logged_user){
					getEscandalos.setHeader("Session-Token", MyApplication.session_token);
				}

				// Hacemos la petici�n al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				// Parseamos los esc�ndalos devueltos
				JSONObject respJson = new JSONObject(respStr);

				// Obtenemos el meta
				JSONObject respMetaJson = respJson.getJSONObject("meta");
				meta_next_scandals = respMetaJson.getString("next");

				JSONArray escandalosObject = respJson.getJSONArray("objects");
				
				// Obtenemos los datos de los esc�ndalos
				for (int i = 0; i < escandalosObject.length(); i++) {
					// Hacemos una declaraci�n por cada esc�ndalo
					final Comment last_comment;

					JSONObject escanObject = escandalosObject.getJSONObject(i);

					final String category = escanObject.getString("category");
					final String date = escanObject.getString("date");
					final String id = escanObject.getString("id");
					final String user_id = escanObject.getString("user_id");
					final String img_p = escanObject.getString("img_p"); // Fotos peque�as sin marca de agua
					final String img = escanObject.getString("img");
					final String comments_count = escanObject.getString("comments_count");
					String latitude = escanObject.getString("latitude");
					String longitude = escanObject.getString("longitude");
					final String resource_uri = escanObject.getString("resource_uri");
					final String title = new String(escanObject.getString("title").getBytes("ISO-8859-1"), HTTP.UTF_8);
					final String user = escanObject.getString("user");
					String visits_count = escanObject.getString("visits_count");
					final String sound = escanObject.getString("sound");
					final String username = escanObject.getString("username");
					final String avatar = escanObject.getString("avatar");
					final String social_network = escanObject.getString("social_network");
					final int already_voted = Integer.parseInt(escanObject.getString("already_voted"));
					final int likes = Integer.parseInt(escanObject.getString("likes"));
					final int dislikes = Integer.parseInt(escanObject.getString("dislikes"));
					final int media_type = Integer.parseInt(escanObject.getString("media_type"));
					final String favicon = escanObject.getString("favicon");
					final String source = escanObject.getString("source");
					final String source_name = escanObject.getString("source_name");
					
					// Obtenemos el comentario m�s reciente
					if (!escanObject.isNull("last_comment")){
						JSONObject commentObject = escanObject.getJSONObject("last_comment");
						c_date = commentObject.getString("date");
						c_id = commentObject.getString("id");
						c_photo = commentObject.getString("photo");
						c_resource_uri = commentObject
						.getString("resource_uri");
						c_social_network = commentObject
						.getString("social_network");
						c_text = new String(commentObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
						c_user = commentObject.getString("user");
						c_user_id = commentObject.getString("user_id");
						c_username = commentObject.getString("username");
						c_avatar = commentObject.getString("avatar");

						last_comment = new Comment(c_date, c_id, c_photo,
								c_resource_uri, c_social_network, c_text,
							c_user, c_user_id, c_username, c_avatar);
					}
					else{
						last_comment = null;
					}

					if (escandalos != null && !isCancelled()) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {							
								// A�adimos el escandalo al ArrayList
								Scandaloh escanAux = new Scandaloh(id, user_id, title,category,
										Integer.parseInt(comments_count),resource_uri,
										MyApplication.DIRECCION_BUCKET + img_p,
										MyApplication.DIRECCION_BUCKET + img, sound, username, date,
										avatar, last_comment, social_network,
										already_voted, likes, dislikes, media_type, MyApplication.DIRECCION_BUCKET + favicon, source, source_name);
								escandalos.add(escanAux);
								adapter.addFragment(ScandalFragment.newInstance(escandalos.get(escandalos
												.size() - 1)));
								adapter.notifyDataSetChanged();
							}
						});
					}
				}
			} catch (Exception ex) {
				Log.e("ServicioRest",
						"Error obteniendo esc�ndalos o comentarios", ex);
				// Hubo alg�n error inesperado
				any_error = true;
			}

			// Si hubo alg�n error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el c�digo resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			// Mostramos el bot�n actualizar
			hideLoadingFromMenu();
			
			// Quitamos el loading y mostramos los esc�ndalos
			pager.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);

			// Si hubo alg�n error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}

			// Habilitamos el spinner
			spinner_categorias.setClickable(true);

			// Si hemos llegado aqui porque no hab�an esc�ndalos (y le dio a actualizar), paramos el loading del menu
			if (no_hay_escandalos) {
				hideLoadingFromMenu();
				no_hay_escandalos = false;
			}

			// Ya no se est�n obteniendo esc�ndalos (abrimos la llave)
			getting_escandalos = false;

			adapter.notifyDataSetChanged();
		}
	}

	
	
	
	
	
	/**
	 * Obtiene (si hay) nuevos escandalos
	 * 
	 */
	private class GetNewScandals extends AsyncTask<Void, Integer, Integer> {

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
			getting_escandalos = true;
			any_error = false;
			// Mostramos el loading
			showLoadingOnMenu();
		}

		@Override
		protected Integer doInBackground(Void... params) {
				
			String url = null;
			
			url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/?id__gt=" + escandalos.get(0).getId();
			// HAPPY
			if (current_category.equals(MainActivity.HAPPY)) {
				url += "&category__id=1";
			}
			else{
				url += "&category__id=2";
			}
									
			url += "&country="+ MyApplication.code_selected_country;
			url += "&order_by=" + actual_filter ;

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getEscandalos = new HttpGet(url);
				getEscandalos.setHeader("content-type", "application/json");

				// Hacemos la petici�n al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);
				
				// Parseamos los esc�ndalos devueltos
				JSONObject respJson = new JSONObject(respStr);
				JSONArray escandalosObject = respJson.getJSONArray("objects");

				// Obtenemos los datos de los esc�ndalos
				for (int i = escandalosObject.length() -1; i >= 0; i--) {
					// Hacemos una declaraci�n por cada esc�ndalo
					final Comment last_comment;

					JSONObject escanObject = escandalosObject.getJSONObject(i);

					final String category = escanObject.getString("category");
					final String date = escanObject.getString("date");
					final String id = escanObject.getString("id");
					final String user_id = escanObject.getString("user_id");
					final String img_p = escanObject.getString("img_p"); // Fotos peque�as sin marca de agua
					final String img = escanObject.getString("img");
					final String comments_count = escanObject.getString("comments_count");
					String latitude = escanObject.getString("latitude");
					String longitude = escanObject.getString("longitude");
					final String resource_uri = escanObject.getString("resource_uri");
					final String title = new String(escanObject.getString("title").getBytes("ISO-8859-1"), HTTP.UTF_8);
					final String user = escanObject.getString("user");
					String visits_count = escanObject.getString("visits_count");
					final String sound = escanObject.getString("sound");
					final String username = escanObject.getString("username");
					final String avatar = escanObject.getString("avatar");
					final String social_network = escanObject.getString("social_network");
					final int already_voted = Integer.parseInt(escanObject.getString("already_voted"));
					final int likes = Integer.parseInt(escanObject.getString("likes"));
					final int dislikes = Integer.parseInt(escanObject.getString("dislikes"));
					final int media_type = Integer.parseInt(escanObject.getString("media_type"));
					final String favicon = escanObject.getString("favicon");
					final String source = escanObject.getString("source");
					final String source_name = escanObject.getString("source_name");
					
					// Obtenemos el comentario m�s reciente
					if (!escanObject.isNull("last_comment")){
						JSONObject commentObject = escanObject.getJSONObject("last_comment");
						c_date = commentObject.getString("date");
						c_id = commentObject.getString("id");
						c_photo = commentObject.getString("photo");
						c_resource_uri = commentObject
						.getString("resource_uri");
						c_social_network = commentObject
						.getString("social_network");
						c_text = new String(commentObject.getString("text").getBytes("ISO-8859-1"), HTTP.UTF_8);
						c_user = commentObject.getString("user");
						c_user_id = commentObject.getString("user_id");
						c_username = commentObject.getString("username");
						c_avatar = commentObject.getString("avatar");

						last_comment = new Comment(c_date, c_id, c_photo,
								c_resource_uri, c_social_network, c_text,
							c_user, c_user_id, c_username, c_avatar);
					}
					else{
						last_comment = null;
					}

					if (escandalos != null && !isCancelled()) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {							
								// A�adimos el escandalo al ArrayList
								Scandaloh escanAux = new Scandaloh(id, user_id, title,category,
										Integer.parseInt(comments_count),resource_uri,
										MyApplication.DIRECCION_BUCKET + img_p,
										MyApplication.DIRECCION_BUCKET + img, sound, username, date,
										avatar, last_comment, social_network,
										already_voted, likes, dislikes, media_type, MyApplication.DIRECCION_BUCKET + favicon, source, source_name);
								escandalos.add(0,escanAux);
								adapter.addFragmentAtStart(ScandalFragment.newInstance(escandalos.get(0)));
								adapter.notifyDataSetChanged();
							}
						});
					}
				}
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				// Hubo alg�n error inesperado
				any_error = true;
			}

			// Si hubo alg�n error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el c�digo resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Quitamos el progresbar y mostramos la lista de escandalos
			hideLoadingFromMenu();

			// Si hubo alg�n error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			else{
				pager.setCurrentItem(0);
			}

			// Habilitamos el spinner
			spinner_categorias.setClickable(true);

			// Si hemos llegado aqui porque no hab�an esc�ndalos (y le dio a actualizar), paramos el loading del menu
			if (no_hay_escandalos) {
				hideLoadingFromMenu();
				no_hay_escandalos = false;
			}

			// Ya no se est�n obteniendo esc�ndalos (abrimos la llave)
			getting_escandalos = false;

			adapter.notifyDataSetChanged();
			
			// Indicamos a la actividad que ha terminado de actualizar
			hideLoadingFromMenu();
		}
	}


	
	
	

	/**
	 * Actualiza el n� de notificaciones
	 * 
	 */
	private class UpdateNumNotificationsTask extends AsyncTask<Void, Integer, Integer> {

		String num_notifs;

		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String url = MyApplication.SERVER_ADDRESS + "/api/v1/notification/count/" ;		
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet del = new HttpGet(url);
			del.setHeader("Content-Type", "application/json");
			del.setHeader("Session-Token", MyApplication.session_token);
			HttpResponse response = null;

			try {
				response = httpClient.execute(del);
				String respStr = EntityUtils.toString(response.getEntity());
				num_notifs = respStr.toString();

			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				any_error = true; // Indicamos que hubo un error
			}

			// Si hubo alg�n error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el n� de notificaciones
				try{
					return (Integer.parseInt(num_notifs));
				}
				catch(NumberFormatException exception){
					return 666;
				}
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Si hubo alg�n error
			if (result == 666) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			}

			// No hubo ning�n error extra�o
			else {
				if (result > 0){
					txt_num_notifs.setText(result.toString());
					txt_action_bar_num_notis.setText(result.toString());
				}
				else{
					txt_num_notifs.setText("");
					txt_action_bar_num_notis.setText("");
				}
			}
		}
	}

	

}
