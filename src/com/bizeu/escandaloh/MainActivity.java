package com.bizeu.escandaloh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.bizeu.escandaloh.adapters.DrawerMenuAdapter;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.settings.SettingsActivity;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.users.RegistrationActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.ImageUtils;
import com.countrypicker.CountryPicker;
import com.countrypicker.CountryPickerListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class MainActivity extends SherlockFragmentActivity implements
		OnClickListener, OnItemSelectedListener {

	public static final int SHOW_CAMERA = 10;
	private static final int CREATE_ESCANDALO = 11;
	public static final int FROM_GALLERY = 12;
	public static final int AVATAR_SHOW_CAMERA = 15;
	public static final int AVATAR_FROM_GALLERY = 14;
	public static final int SHARING = 13;
	public static final int PIC_CROP = 16;
	public static final String CATEGORY = "Category";
	public static final String ANGRY = "Denuncia";
	public static final String HAPPY = "Humor";
	public static final String BOTH = "Todas";
	public static final String NORMAL = "Normal";
	public static final String ENVIAR_COMENTARIO = "Enviar_comentario";

	private LinearLayout ll_refresh;
	private LinearLayout ll_take_photo;
	private ImageView img_update_list;
	private ImageView img_take_photo;
	private LinearLayout ll_lateral_notificaciones;
	private LinearLayout ll_lateral_pais;
	private LinearLayout ll_lateral_ajustes;
	private LinearLayout ll_lateral_login;
	private LinearLayout ll_lateral_registro;
	private TextView txt_lateral_nombreusuario;
	private ProgressBar progress_refresh;
	private LinearLayout ll_menu_lateral;
	private TextView txt_code_country;
	private EditText edit_escribir_comentario;
	private Spinner spinner_categorias;
	private ImageView img_send_comment;
	DrawerLayout mDrawerLayout;
	private FetchableImageView img_lateral_avatar;
	
	private Uri mImageUri;
	AmazonS3Client s3Client;
	private Context mContext;
	ScandalohFragmentPagerAdapter adapter;
	ViewPager pager = null;
	ProgressBar loading;
	private boolean any_error;
	private GetEscandalos getEscandalosAsync;
	private GetNewEscandalos getNewEscandalosAsync;
	// private UpdateNumComments updateNumCommentsAsync;
	private String category;
	private boolean getting_escandalos = true;
	private boolean there_are_more_escandalos = true;
	public static ArrayList<Scandaloh> escandalos;
	DrawerMenuAdapter mMenuAdapter;
	String[] options;
	ActionBarDrawerToggle mDrawerToggle;
	private boolean no_hay_escandalos;
	private ProgressDialog progress;
	private ArrayAdapter<CharSequence> adapter_spinner;
	private String action_bar_type = NORMAL; // NORMAL o ENVIAR_COMENTARIO

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		no_hay_escandalos = false;
		mContext = this;
		escandalos = new ArrayList<Scandaloh>();

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.lay_pantalla_main));

		// ACTION BAR
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_2, null);
		actBar.setCustomView(view);
		// Activamos el logo del menu para el menu lateral
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.logo_blanco);

		loading = (ProgressBar) findViewById(R.id.loading_escandalos);
		img_update_list = (ImageView) findViewById(R.id.img_actionbar_updatelist);
		ll_refresh = (LinearLayout) findViewById(R.id.ll_main_refresh);
		ll_refresh.setOnClickListener(this);
		img_take_photo = (ImageView) findViewById(R.id.img_actionbar_takephoto);
		ll_take_photo = (LinearLayout) findViewById(R.id.ll_main_take_photo);
		ll_take_photo.setOnClickListener(this);
		progress_refresh = (ProgressBar) findViewById(R.id.prog_refresh_action_bar);
		txt_code_country = (TextView) findViewById(R.id.txt_action_bar_codecountry);

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
				updateActionBar(false, null);

				// Si quedan 4 escándalos más para llegar al último y aún quedan
				// más escándalos (si hemos llegado
				// a los últimos no se pedirán más): obtenemos los siguientes 10
				if (position == adapter.getCount() - 5
						&& there_are_more_escandalos) {
					// Usamos una llave de paso (sólo la primera vez entrará).
					// Cuando se obtengan los 10 escándalos se volverá a abrir
					if (!getting_escandalos) {
						getEscandalosAsync = new GetEscandalos();
						getEscandalosAsync.execute();
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		// MENU LATERAL
		mDrawerLayout = (DrawerLayout) findViewById(R.id.lay_pantalla_main);
		ll_menu_lateral = (LinearLayout) findViewById(R.id.ll_menu_lateral);
		img_lateral_avatar = (FetchableImageView) findViewById(R.id.img_mLateral_avatar);
		ll_lateral_notificaciones = (LinearLayout) findViewById(R.id.ll_mLateral_notificaciones);
		ll_lateral_pais = (LinearLayout) findViewById(R.id.ll_mLateral_pais);
		ll_lateral_ajustes = (LinearLayout) findViewById(R.id.ll_mLateral_ajustes);
		ll_lateral_login = (LinearLayout) findViewById(R.id.ll_mLateral_login);
		ll_lateral_registro = (LinearLayout) findViewById(R.id.ll_mLateral_registro);
		txt_lateral_nombreusuario = (TextView) findViewById(R.id.txt_lateral_nombreusuario);
		
		// Sombra del menu sobre la pantalla
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		options = new String[] { getResources().getString(R.string.perfil),
				getResources().getString(R.string.notificaciones),
				getResources().getString(R.string.pais),
				getResources().getString(R.string.ajustes),
				getResources().getString(R.string.danos_tu_opinion) };

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer_blanco, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		img_lateral_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Creamos un popup para elegir entre hacer foto con la cámara o cogerla de la galería
				final CharSequence[] items = {
						getResources()
								.getString(R.string.hacer_foto_con_camara),
						getResources().getString(
								R.string.seleccionar_foto_galeria) };
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle(R.string.avatar);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						// Cámara
						if (items[item].equals(getResources().getString(
								R.string.hacer_foto_con_camara))) {

							// Si dispone de cámara iniciamos la cámara
							if (checkCameraHardware(mContext)) {
								Intent takePictureIntent = new Intent(
										"android.media.action.IMAGE_CAPTURE");
								File photo = null;
								photo = createFileTemporary("picture", ".png");
								if (photo != null) {
									mImageUri = Uri.fromFile(photo);
									takePictureIntent.putExtra(
											MediaStore.EXTRA_OUTPUT, mImageUri);
									startActivityForResult(takePictureIntent,
											AVATAR_SHOW_CAMERA);
									photo.delete();
								}
							}
							// El dispositivo no dispone de cámara
							else {
								Toast toast = Toast
										.makeText(
												mContext,
												R.string.este_dispositivo_no_dispone_camara,
												Toast.LENGTH_LONG);
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

		ll_lateral_ajustes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(i);
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
			}
		});

		ll_lateral_login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this,
						LoginSelectActivity.class);
				startActivity(i);
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
			}
		});

		ll_lateral_registro.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this,
						RegistrationActivity.class);
				startActivity(i);
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
			}
		});
		
		ll_lateral_pais.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
				
				final CountryPicker countryPicker = CountryPicker.newInstance(getResources().getString(R.string.selecciona_pais));
				countryPicker.setListener(new CountryPickerListener() {

					@Override
					public void onSelectCountry(String name, String code) {
						
						// Si el país seleccionado es distinto al actual
						if (!MyApplication.code_selected_country.equals(code)){
							MyApplication.code_selected_country = code;
							txt_code_country.setText(code);
							SharedPreferences prefs = getBaseContext().getSharedPreferences(
				        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
							// Guardamos el código del país
				        	prefs.edit().putString(MyApplication.CODE_COUNTRY, code).commit();
							
							// Si hay conexión
							if (Connectivity.isOnline(mContext)) {
								cancelGetEscandalos();
								refreshFinished();
								// Abrimos llave de hay más escandalos
								there_are_more_escandalos = true;
								// Quitamos los escándalos actuales
								escandalos.clear();

								pager.setCurrentItem(0);
								adapter.clearFragments();
								adapter = new ScandalohFragmentPagerAdapter(
										getSupportFragmentManager());
								pager.setAdapter(adapter);
								// Obtenemos los 10 primeros escándalos para la categoría
								// seleccionada
								// Mostramos el progressBar y ocultamos la lista de escandalos
								loading.setVisibility(View.VISIBLE);
								pager.setVisibility(View.GONE);
								getEscandalosAsync = new GetEscandalos();
								getEscandalosAsync.execute();
							}

							// No hay conexión
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

		// Le asignamos la animación al pasar entre escándalos (API 11+)
		pager.setPageTransformer(true, new ZoomOutPageTransformer());
		// Separación entre escándalos
		pager.setPageMargin(3);
		category = HAPPY;

		// Si hay conexión: obtenemos los 10 primeros escándalos
		if (Connectivity.isOnline(mContext)) {
			getEscandalosAsync = new GetEscandalos();
			getEscandalosAsync.execute();
		} else {
			Toast toast = Toast.makeText(mContext,
					getResources().getString(R.string.no_dispones_de_conexion),
					Toast.LENGTH_SHORT);
			toast.show();
			// Quitamos el progresbar y mostramos la lista de escandalos
			loading.setVisibility(View.GONE);
			pager.setVisibility(View.VISIBLE);
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

		/*
		 * // Actualizamos nº de comentarios si no se están obteniendo otros
		 * escándalos if (!getting_escandalos){ // Si hay conexión if
		 * (Connectivity.isOnline(mContext)){ if (escandalos.size() > 0){
		 * updateNumCommentsAsync = new UpdateNumComments();
		 * updateNumCommentsAsync.execute(); } } }
		 */
		// Activamos google analytics
		EasyTracker.getInstance(this).activityStart(this);

	}

	/**
	 * onResume
	 */
	@Override
	public void onResume() {
		super.onResume();

		// Actualizamos el action bar según esté en modo normal o escritura
		if (action_bar_type.equals(ENVIAR_COMENTARIO)) {
			updateActionBar(true, null);
		} else {
			updateActionBar(false, null);
		}

		// Si está logueado ocultamos las opciones de login y registro y mostramos su nombre en el menu
		// Actualizamos el avatar y el nombre de usuario
		if (MyApplication.logged_user) {
			ll_lateral_login.setVisibility(View.GONE);
			ll_lateral_registro.setVisibility(View.GONE);
			txt_lateral_nombreusuario.setText(MyApplication.user_name);
			Log.v("WE","Myapplication avatar: " + MyApplication.avatar);
			if (MyApplication.avatar != null){
		        img_lateral_avatar.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_mas);
			}
			else{
		        img_lateral_avatar.setImageResource(R.drawable.avatar_mas);
			}
		} else {
			ll_lateral_login.setVisibility(View.VISIBLE);
			ll_lateral_registro.setVisibility(View.VISIBLE);
			img_lateral_avatar.setImageResource(R.drawable.avatar_mas);
			txt_lateral_nombreusuario.setText(getResources().getString(R.string.invitado));
			img_lateral_avatar.setImageResource(R.drawable.avatar_defecto);		
		}
		
		// Si ha iniciado/cerrado sesión: reiniciamos los escándalos
		if (MyApplication.reset_scandals){		
			// Abrimos llave de hay más escandalos
			there_are_more_escandalos = true;
			// Quitamos los escándalos actuales
			escandalos.clear();
			pager.setCurrentItem(0);
			adapter.clearFragments();
			adapter = new ScandalohFragmentPagerAdapter(getSupportFragmentManager());
			pager.setAdapter(adapter);
			// Obtenemos los 10 primeros escándalos para la categoría seleccionada
			// Mostramos el progressBar y ocultamos la lista de escandalos
			loading.setVisibility(View.VISIBLE);
			pager.setVisibility(View.GONE);
			getEscandalosAsync = new GetEscandalos();
			getEscandalosAsync.execute();
			
			// Cerramos llave
			MyApplication.reset_scandals = false;
		}

		// Abrimos la llave para el caso de error del timeout al obtener fotos
		// MyApplication.TIMEOUT_PHOTO_SHOWN = false;

		/*
		 * // Si está logueado mostramos la cámara (con su selector) if
		 * (MyApplication.logged_user){ StateListDrawable states = new
		 * StateListDrawable(); states.addState(new int[]
		 * {android.R.attr.state_pressed
		 * },getResources().getDrawable(R.drawable.camara_pressed));
		 * states.addState(new int[]
		 * {android.R.attr.state_focused},getResources(
		 * ).getDrawable(R.drawable.camara_pressed)); states.addState(new int[]
		 * { },getResources().getDrawable(R.drawable.camara_blanca));
		 * img_take_photo.setImageDrawable(states); } // Si no está logueado
		 * mostramos el símbolo para hacer login else{ StateListDrawable states
		 * = new StateListDrawable(); states.addState(new int[]
		 * {android.R.attr.state_pressed
		 * },getResources().getDrawable(R.drawable.mas_pressed));
		 * states.addState(new int[]
		 * {android.R.attr.state_focused},getResources(
		 * ).getDrawable(R.drawable.mas_pressed)); states.addState(new int[] {
		 * },getResources().getDrawable(R.drawable.mas));
		 * img_take_photo.setImageDrawable(states); }
		 */
	}

	/**
	 * onPause
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * onStop
	 */
	@Override
	public void onStop() {
		super.onStop();
		// Paramos google analytics
		EasyTracker.getInstance(this).activityStop(this);
	}

	/**
	 * onDestroy
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Cancelamos los escándalos que se estuvieran obteniendo
		cancelGetEscandalos();
	}

	/**
	 * onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Le asignamos el botón home al menú lateral
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Escándalo desde la cámara
		if (requestCode == SHOW_CAMERA) {
			if (resultCode == RESULT_OK) {
				if (mImageUri != null) {
					// Guardamos la foto en la galería
					Bitmap bitAux = ImageUtils.uriToBitmap(mImageUri, mContext);
					ImageUtils.saveBitmapIntoGallery(bitAux, mContext);

					// Mostramos la pantalla de subir escándalo
					Intent i = new Intent(MainActivity.this,
							CreateScandalohActivity.class);
					i.putExtra("photo_from", SHOW_CAMERA);
					i.putExtra("photoUri", mImageUri.toString());
					startActivityForResult(i, CREATE_ESCANDALO);
				} else {
					Toast toast = Toast.makeText(mContext, getResources()
							.getString(R.string.hubo_algun_error_camara),
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		}

		// Escándalo desde la galería
		else if (requestCode == FROM_GALLERY) {
			if (data != null) {
				// Mostramos la pantalla de subir escándalo
				Uri selectedImageUri = data.getData();
				Intent i = new Intent(MainActivity.this,
						CreateScandalohActivity.class);
				i.putExtra("photo_from", FROM_GALLERY);
				i.putExtra("photoUri", ImageUtils.getRealPathFromURI(mContext,
						selectedImageUri));
				startActivityForResult(i, CREATE_ESCANDALO);
			}
		}

		// Avatar desde la cámara
		else if (requestCode == AVATAR_SHOW_CAMERA) {
			if (mImageUri != null) {
				if (!performCrop(mImageUri)) {
					Bitmap photo_for_avatar = ImageUtils.uriToBitmap(mImageUri,
							this);
					 new UpdateAvatarTask(this,photo_for_avatar).execute();
				}
			}
		}

		// Avatar desde la galería
		else if (requestCode == AVATAR_FROM_GALLERY) {
			if (data != null) {
				Uri selectedImageUri = data.getData();
				if (!performCrop(selectedImageUri)) {
					String foto_string = ImageUtils.getRealPathFromURI(mContext, selectedImageUri);
					Bitmap photo_for_avatar = BitmapFactory.decodeFile(foto_string);
					new UpdateAvatarTask(this,photo_for_avatar).execute();
				}
			}
		}

		// Crop de la foto
		if (requestCode == PIC_CROP) {
			if (data != null) {
				Bundle extras = data.getExtras();
				Bitmap photo_crop = extras.getParcelable("data");
				new UpdateAvatarTask(this, photo_crop).execute();
			}
		}
	}

	/**
	 * onClick
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		
		// Login/Subir escandalo
		case R.id.ll_main_take_photo:

			// Paramos si hubiera algún audio reproduciéndose
			Audio.getInstance(mContext).releaseResources();

			// Si dispone de conexión
			if (Connectivity.isOnline(mContext)) {
				// Si está logueado
				if (MyApplication.logged_user) {

					// Creamos un menu para elegir entre hacer foto con la
					// cámara o cogerla de la galería
					final CharSequence[] items = {
							getResources().getString(
									R.string.hacer_foto_con_camara),
							getResources().getString(
									R.string.seleccionar_foto_galeria) };
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setTitle(R.string.aniadir_foto);
					builder.setItems(items,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int item) {

									// Cámara
									if (items[item]
											.equals(getResources()
													.getString(
															R.string.hacer_foto_con_camara))) {

										// Mandamos el evento a Google Analytics
										EasyTracker easyTracker = EasyTracker
												.getInstance(mContext);
										easyTracker
												.send(MapBuilder
														.createEvent(
																"Acción UI",
																"Selección realizada",
																"Hacer foto desde la cámara",
																null).build());

										// Si dispone de cámara iniciamos la
										// cámara
										if (checkCameraHardware(mContext)) {
											Intent takePictureIntent = new Intent(
													"android.media.action.IMAGE_CAPTURE");
											File photo = null;
											photo = createFileTemporary(
													"picture", ".png");
											if (photo != null) {
												mImageUri = Uri.fromFile(photo);
												takePictureIntent
														.putExtra(
																MediaStore.EXTRA_OUTPUT,
																mImageUri);
												startActivityForResult(
														takePictureIntent,
														SHOW_CAMERA);
												photo.delete();
											}
										}
										// El dispositivo no dispone de cámara
										else {
											Toast toast = Toast
													.makeText(
															mContext,
															R.string.este_dispositivo_no_dispone_camara,
															Toast.LENGTH_LONG);
											toast.show();
										}
									}

									// Galería
									else if (items[item]
											.equals(getResources()
													.getString(
															R.string.seleccionar_foto_galeria))) {

										// Mandamos el evento a Google Analytics
										EasyTracker easyTracker = EasyTracker
												.getInstance(mContext);
										easyTracker
												.send(MapBuilder
														.createEvent(
																"Acción UI",
																"Selección realizada",
																"Subir foto desde la galería",
																null).build());

										Intent i = new Intent(
												Intent.ACTION_PICK,
												android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
										startActivityForResult(i, FROM_GALLERY);
									}
								}
							});
					builder.show();
				}
				// No está logueado: mostramos un mensaje diciendo que se loguee
				else {
					Toast toast = Toast.makeText(mContext, getResources().getString(R.string.registrate_o_inicia_sesion), Toast.LENGTH_LONG);
					toast.show();
				}
			}
			// No dispone de conexión
			else {
				Toast toast = Toast.makeText(mContext,
						R.string.no_dispones_de_conexion, Toast.LENGTH_LONG);
				toast.show();
			}
			break;

		// Actualizar carrusel: Le decimos al fragmento que actualice los
		// escándalos (y suba el carrusel al primero)
		case R.id.ll_main_refresh:

			// Mandamos el evento a Google Analytics
			EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			easyTracker.send(MapBuilder.createEvent("Acción UI",
					"Boton clickeado", "Actualizar lista escándalos", null)
					.build());

			// Cambiamos la imagen de actualizar por un loading
			progress_refresh.setVisibility(View.VISIBLE);
			img_update_list.setVisibility(View.GONE);

			// Nos colocamos en el primer escandalo
			pager.setCurrentItem(0);

			// Si no se están obteniendo otros escándalos
			if (!getting_escandalos) {
				// Si hay conexión
				if (Connectivity.isOnline(mContext)) {
					// Paramos si se estuviesen actualizando el nº de
					// comentarios
					/*
					 * if (updateNumCommentsAsync != null){ if
					 * (updateNumCommentsAsync.getStatus() ==
					 * AsyncTask.Status.PENDING ||
					 * updateNumCommentsAsync.getStatus() ==
					 * AsyncTask.Status.RUNNING){
					 * updateNumCommentsAsync.cancel(true); } }
					 */

					// Obtenemos los escándalos:
					// Si no hay ninguno mostrado obtenemos los primeros, si hay
					// alguno obtenemos si hay nuevos escándalos subidos
					getting_escandalos = true;

					if (escandalos.size() > 0) {
						getNewEscandalosAsync = new GetNewEscandalos();
						getNewEscandalosAsync.execute();
					} else {
						no_hay_escandalos = true; // Indicamos que no hay
													// escándalos aún
						getEscandalosAsync = new GetEscandalos();
						getEscandalosAsync.execute();
					}
				}

				// No hay conexión
				else {
					Toast toast = Toast.makeText(mContext,
							R.string.no_dispones_de_conexion,
							Toast.LENGTH_SHORT);
					toast.show();
					// Indicamos a la actividad que ha terminado de actualizar
					refreshFinished();
				}
			} else {
				// Indicamos a la actividad que ha terminado de actualizar
				refreshFinished();
			}
			break;
		}
	}

	/**
	 * Obtiene los siguientes 10 escándalos anteriores a partir de uno dado
	 * 
	 */
	private class GetEscandalos extends AsyncTask<Void, Integer, Integer> {

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

			// HAPPY
			if (category.equals(MainActivity.HAPPY)) {
				// Usamos un servicio u otro dependiendo si es el primer listado
				// de escándalos o ya posteriores
				if (escandalos.size() == 0) {
					url = MyApplication.SERVER_ADDRESS
							+ "/api/v1/photo/?limit=10&category__id=1&country="+ MyApplication.code_selected_country;

				} else {
					url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/"
							+ escandalos.get(escandalos.size() - 1).getId()
							+ "/" + MyApplication.code_selected_country
							+ "/previous/?category__id=1";
				}
			}

			// ANGRY
			else if (category.equals(MainActivity.ANGRY)) {
				// Usamos un servicio u otro dependiendo si es el primer listado
				// de escándalos o ya posteriores
				if (escandalos.size() == 0) {
					url = MyApplication.SERVER_ADDRESS
							+ "/api/v1/photo/?limit=10&category__id=2&country="
							+ MyApplication.code_selected_country;
				} else {
					url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/"
							+ escandalos.get(escandalos.size() - 1).getId()
							+ "/" + MyApplication.code_selected_country
							+ "/previous/?category__id=2";
				}
			}

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getEscandalos = new HttpGet(url);
				getEscandalos.setHeader("content-type", "application/json");
				// Si es con usuario le añadimos el session_token
				if (MyApplication.logged_user){
					getEscandalos.setHeader("Session-Token", MyApplication.session_token);
				}


				// Hacemos la petición al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				JSONArray escandalosObject = null;

				// Si es la primera vez obtenemos los escandalos a partir de un
				// JSONObject, si no obtenemos directamente el JSONArray
				// HAPPY
				if (category.equals(MainActivity.HAPPY)) {
					if (escandalos.size() == 0) {
						// Obtenemos el json
						JSONObject respJson = new JSONObject(respStr);
						escandalosObject = respJson.getJSONArray("objects");
					} else {
						escandalosObject = new JSONArray(respStr);
						// Si no hay más escandalos,lo indicamos
						if (escandalosObject.length() == 0) {
							there_are_more_escandalos = false;
						}
					}
				}

				// ANGRY
				else if (category.equals(MainActivity.ANGRY)) {
					if (escandalos.size() == 0) {
						// Obtenemos el json
						JSONObject respJson = new JSONObject(respStr);
						escandalosObject = respJson.getJSONArray("objects");
					} else {
						escandalosObject = new JSONArray(respStr);
						// Si no hay más escandalos,lo indicamos
						if (escandalosObject.length() == 0) {
							there_are_more_escandalos = false;
						}
					}
				}

				/*
				 * // BOTH else if (category.equals(MainActivity.BOTH)) { if
				 * (MyApplication.FIRST_TIME_BOTH){
				 * MyApplication.FIRST_TIME_BOTH = false; // Obtenemos el json
				 * JSONObject respJson = new JSONObject(respStr);
				 * 
				 * escandalosObject = respJson.getJSONArray("objects"); } else{
				 * if (adapter.getCount() == 0){ JSONObject respJson = new
				 * JSONObject(respStr); escandalosObject =
				 * respJson.getJSONArray("objects"); } else{ escandalosObject =
				 * new JSONArray(respStr);
				 * 
				 * // Si no hay más escandalos,lo indicamos if
				 * (escandalosObject.length() == 0){
				 * Log.v("WE","No hay mas boths"); there_are_more_escandalos =
				 * false; } } } }
				 */

				// Obtenemos los datos de los escándalos
				for (int i = 0; i < escandalosObject.length(); i++) {
					// Hacemos una declaración por cada escándalo
					final ArrayList<Comment> array_comments = new ArrayList<Comment>();

					JSONObject escanObject = escandalosObject.getJSONObject(i);

					final String category = escanObject.getString("category");
					final String date = escanObject.getString("date");
					final String id = escanObject.getString("id");
					final String img_p = escanObject.getString("img_p"); // Fotos pequeñas sin marca de agua
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
								
					// Obtenemos los comentarios
					//final String comments = escanObject.getString("last_comment");
					//JSONArray commentsArray = new JSONArray(comments);

					//for (int j = 0; j < commentsArray.length(); j++) {
					if (!escanObject.isNull("last_comment")){
						JSONObject commentObject = escanObject.getJSONObject("last_comment");
						//.getJSONObject(j);
						c_date = commentObject.getString("date");
						c_id = commentObject.getString("id");
						c_photo = commentObject.getString("photo");
						c_resource_uri = commentObject
						.getString("resource_uri");
						c_social_network = commentObject
						.getString("social_network");
						c_text = commentObject.getString("text");
						c_user = commentObject.getString("user");
						c_user_id = commentObject.getString("user_id");
						c_username = commentObject.getString("username");
						c_avatar = commentObject.getString("avatar");

						Comment commentAux = new Comment(c_date, c_id, c_photo,
								c_resource_uri, c_social_network, c_text,
							c_user, c_user_id, c_username, c_avatar);
						array_comments.add(commentAux);
					}
						
					//}

					if (escandalos != null && !isCancelled()) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// Añadimos el escandalo al ArrayList
								Scandaloh escanAux = new Scandaloh(id, title,
										category, BitmapFactory.decodeResource(getResources(),R.drawable.loading),
										Integer.parseInt(comments_count),resource_uri,
										MyApplication.DIRECCION_BUCKET + img_p,
										MyApplication.DIRECCION_BUCKET + img, sound, username, date,
										avatar, array_comments, social_network,
										already_voted, likes, dislikes, media_type, MyApplication.DIRECCION_BUCKET + favicon, source, source_name);
								escandalos.add(escanAux);
								adapter.addFragment(ScandalohFragment.newInstance(escandalos.get(escandalos
												.size() - 1)));
								adapter.notifyDataSetChanged();
							}
						});
					}
				}
			} catch (Exception ex) {
				Log.e("ServicioRest",
						"Error obteniendo escándalos o comentarios", ex);
				// Hubo algún error inesperado
				any_error = true;

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) // Context
																	// and
																	// optional
																	// collection
																	// of
																	// package
																	// names to
																	// be used
																	// in
																	// reporting
																	// the
																	// exception.
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), false).build());

			}

			// Si hubo algún error devolvemos 666
			if (any_error) {
				return 666;
			} else {
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Quitamos el progresbar y mostramos la lista de escandalos
			loading.setVisibility(View.GONE);
			pager.setVisibility(View.VISIBLE);

			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}

			// Habilitamos el spinner
			spinner_categorias.setClickable(true);

			// Si hemos llegado aqui porque no habían escándalos (y le dio a
			// actualizar), paramos el loading del menu
			if (no_hay_escandalos) {
				refreshFinished();
				no_hay_escandalos = false;
			}

			// Ya no se están obteniendo escándalos (abrimos la llave)
			getting_escandalos = false;

			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Obtiene (si hay) nuevos escandalos
	 * 
	 */
	private class GetNewEscandalos extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			// A partir del id más nuevo obtenido (el primero del array)
			String url = null;
			// HAPPY
			if (category.equals(MainActivity.HAPPY)) {
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/"
						+ escandalos.get(0).getId() + "/"
						+ MyApplication.code_selected_country
						+ "/new/?category__id=1";
			}
			// ANGRY
			if (category.equals(MainActivity.ANGRY)) {
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/"
						+ escandalos.get(0).getId() + "/"
						+ MyApplication.code_selected_country
						+ "/new/?category__id=2";
			}
			/*
			 * // BOTH if (category.equals(MainActivity.BOTH)){
			 * Log.v("WE","nuevos both"); url = MyApplication.SERVER_ADDRESS +
			 * "/api/v1/photo/" + escandalos.get(0).getId() + "/" +
			 * MyApplication.code_selected_country+ "/new/"; }
			 */

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getEscandalos = new HttpGet(url);
				getEscandalos.setHeader("content-type", "application/json");

				// Hacemos la petición al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				JSONArray escandalosObject = new JSONArray(respStr);

				for (int i = 0; i < escandalosObject.length(); i++) {
					JSONObject escanObject = escandalosObject.getJSONObject(i);

					final String category = escanObject.getString("category");
					final String date = escanObject.getString("date");
					final String id = escanObject.getString("id");
					final String img_p = escanObject.getString("img_p");
					final String img = escanObject.getString("img");
					final String comments_count = escanObject
							.getString("comments_count");
					String latitude = escanObject.getString("latitude");
					String longitude = escanObject.getString("longitude");
					final String resource_uri = escanObject
							.getString("resource_uri");
					final String title = new String(escanObject.getString(
							"title").getBytes("ISO-8859-1"), HTTP.UTF_8);
					final String user = escanObject.getString("user");
					String visits_count = escanObject.getString("visits_count");
					final String sound = escanObject.getString("sound");
					final String username = escanObject.getString("username");
					final String avatar = escanObject.getString("avatar");

					/*
					 * if (escandalos != null && !isCancelled()){
					 * runOnUiThread(new Runnable() {
					 * 
					 * @Override public void run() { // Añadimos el escandalo al
					 * comienzo Scandaloh escanAux = new Scandaloh(id, title,
					 * category, BitmapFactory.decodeResource(getResources(),
					 * R.drawable.loading), Integer.parseInt(comments_count),
					 * resource_uri, "http://scandaloh.s3.amazonaws.com/" +
					 * img_p, "http://scandaloh.s3.amazonaws.com/" + img, sound,
					 * username, avatar, date); escandalos.add(0,escanAux);
					 * adapter
					 * .addFragmentAtStart(ScandalohFragment.newInstance(escanAux
					 * )); adapter.notifyDataSetChanged(); } }); }
					 */
				}
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				// Hubo algún error inesperado
				any_error = true;

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) // Context
																	// and
																	// optional
																	// collection
																	// of
																	// package
																	// names to
																	// be used
																	// in
																	// reporting
																	// the
																	// exception.
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
				// Devolvemos el código resultado
				return (response.getStatusLine().getStatusCode());
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			// Si hubo algún error inesperado
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}

			// Abrimos la llave
			getting_escandalos = false;

			// Indicamos a la actividad que ha terminado de actualizar
			refreshFinished();
		}
	}

	/**
	 * Sube un comentario
	 * 
	 */
	private class SendCommentTask extends AsyncTask<Void, Integer, Integer> {

		private Context mContext;
		private String pho_id;

		public SendCommentTask(Context context, String photo_id) {
			this.pho_id = photo_id;
			mContext = context;
			any_error = false;
			progress = new ProgressDialog(mContext);
			progress.setTitle(R.string.enviando_comentario);
			progress.setMessage(getResources().getString(
					R.string.espera_por_favor));
			progress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			// Mostramos el ProgressDialog
			progress.show();
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
				String written_comment = edit_escribir_comentario.getText()
						.toString();

				dato.put("user", MyApplication.resource_uri);
				dato.put("photo", "/api/v1/photo/" + pho_id + "/"); // Formato:
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
						new StandardExceptionParser(mContext, null) // Context
																	// and
																	// optional
																	// collection
																	// of
																	// package
																	// names to
																	// be used
																	// in
																	// reporting
																	// the
																	// exception.
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
				if (progress.isShowing()) {
					progress.dismiss();
				}
			} else {
				// Si es codigo 2xx --> OK
				if (result >= 200 && result < 300) {
					// Vaciamos el editext
					edit_escribir_comentario.setText("");

					// Mostramos de nuevo los comentarios (indicamos que si
					// hemos enviado un comentario)
					new GetCommentsTask(mContext, pho_id).execute();
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
					if (progress.isShowing()) {
						progress.dismiss();
					}
				}
			}
		}
	}

	/**
	 * Muestra la lista de comentarios para esa foto
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
		private Context mContext;
		private String phot_id;

		public GetCommentsTask(Context context, String photo_id) {
			phot_id = photo_id;
			mContext = context;
		}

		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet del = new HttpGet(MyApplication.SERVER_ADDRESS
					+ "/api/v1/comment/?photo__id=" + phot_id);
			del.setHeader("content-type", "application/json");
			HttpResponse response = null;

			try {
				response = httpClient.execute(del);
				String respStr = EntityUtils.toString(response.getEntity());
				
				Log.i("WE","com: " + respStr.toString());

				JSONObject respJSON = new JSONObject(respStr);

				// Parseamos el json para obtener los escandalos
				JSONArray escandalosObject = null;

				escandalosObject = respJSON.getJSONArray("objects");
				final ArrayList<Comment> array_comments = new ArrayList<Comment>();

				for (int i = 0; i < escandalosObject.length(); i++) {
					JSONObject escanObject = escandalosObject.getJSONObject(i);

					c_date = escanObject.getString("date");
					c_id = escanObject.getString("id");
					c_photo = escanObject.getString("photo");
					c_resource_uri = escanObject.getString("user");
					c_social_network = escanObject.getString("social_network");
					c_text = new String(escanObject.getString("text").getBytes(
							"ISO-8859-1"), HTTP.UTF_8);
					c_user = escanObject.getString("user");
					c_user_id = escanObject.getString("user_id");
					c_username = escanObject.getString("username");
					c_avatar = escanObject.getString("avatar");
					

					Comment commentAux = new Comment(c_date, c_id, c_photo,
							c_resource_uri, c_social_network, c_text, c_user,
							c_user_id, c_username, c_avatar);
					array_comments.add(commentAux);
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Añadimos el escandalo al ArrayList
						Scandaloh e = escandalos.get(pager.getCurrentItem());
						Scandaloh escanAux = new Scandaloh(e.getId(),
								e.getTitle(), e.getCategory(),
								BitmapFactory.decodeResource(getResources(),
										R.drawable.loading),
								array_comments.size(), e.getResourceUri(),
								e.getRouteImg(), e.getRouteImgBig(),
								e.getUriAudio(), e.getUser(), e.getDate(),
								e.getAvatar(), array_comments, e.getSocialNetwork(),
								e.getAlreadyVoted(), e.getLikes(), e.getDislikes(), e.getMediaType(),
								e.getFavicon(), e.getSource(), e.getSourceName());
						escandalos.set(pager.getCurrentItem(), escanAux);
						adapter.setFragment(pager.getCurrentItem(),
								ScandalohFragment.newInstance(escanAux));
						adapter.notifyDataSetChanged();
						pager.setCurrentItem(pager.getCurrentItem());
					}
				});

			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				any_error = true; // Indicamos que hubo un error

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) // Context
																	// and
																	// optional
																	// collection
																	// of
																	// package
																	// names to
																	// be used
																	// in
																	// reporting
																	// the
																	// exception.
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

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
				progress.dismiss();
			}

			// Si hubo algún error
			if (result == 666) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			}

			// No hubo ningún error extraño
			else {
				/*
				 * // Si es codigo 2xx --> OK if (result >= 200 && result <300){
				 * 
				 * commentsAdapter.notifyDataSetChanged();
				 * 
				 * // Actualizamos el indicador de número de comentarios if
				 * (comments.size() == 1){ num_com.setText(comments.size() +
				 * " comentario"); } else{ num_com.setText(comments.size() +
				 * " comentarios"); }
				 * 
				 * } else{ Toast toast; toast = Toast.makeText(mContext,
				 * getResources
				 * ().getString(R.string.no_se_pudieron_obtener_comentarios),
				 * Toast.LENGTH_LONG); toast.show(); }
				 */
			}
		}
	}

	/**
	 * Envía un avatar nuevo al servidor
	 * 
	 */
	private class UpdateAvatarTask extends AsyncTask<Void, Integer, Integer> {

		private Context mContext;
		private Bitmap photo_avatar;
		private String url_avatar;

		public UpdateAvatarTask(Context context, Bitmap avatar) {
			photo_avatar = avatar;
			mContext = context;
			progress = new ProgressDialog(mContext);
			progress.setTitle(R.string.actualizando_avatar);
			progress.setMessage(getResources().getString(
					R.string.espera_por_favor));
			progress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			// Mostramos el ProgressDialog
			progress.show();
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			HttpEntity resEntity;
			String urlString = MyApplication.SERVER_ADDRESS
					+ MyApplication.resource_uri;
			HttpResponse response = null;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpPut put = new HttpPut(urlString);
				put.setHeader("Session-Token", MyApplication.session_token);
				MultipartEntity reqEntity = new MultipartEntity();
				
				// Creamos un file a partir del bitmap
				File f = new File(mContext.getCacheDir(), "avatar");
				f.createNewFile();

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				photo_avatar.compress(CompressFormat.JPEG, 70 /*ignored for PNG*/, bos);
				byte[] bitmapdata = bos.toByteArray();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(bitmapdata);
				
				FileBody bin1 = new FileBody(f);
				reqEntity.addPart("avatar", bin1);
				put.setEntity(reqEntity);
				response = client.execute(put);
				resEntity = response.getEntity();
				final String response_str = EntityUtils.toString(resEntity);
				
				JSONObject respJSON = new JSONObject(response_str);
				url_avatar = respJSON.getString("avatar");
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

			// Quitamos el ProgressDialog
			if (progress.isShowing()) {
				progress.dismiss();
			}

			// Si hubo algún error mostramos un mensaje
			if (any_error) {
				Toast toast = Toast.makeText(mContext, getResources()
						.getString(R.string.lo_sentimos_hubo),
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				// Si es codigo 2xx --> OK
				if (result >= 200 && result < 300) {
		        	// Guardamos su avatar
					SharedPreferences prefs = getBaseContext().getSharedPreferences(
		        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
		        	prefs.edit().putString(MyApplication.AVATAR, url_avatar).commit();
		        	MyApplication.avatar = url_avatar;
		        	if (url_avatar != null){
		        		img_lateral_avatar.setImage(MyApplication.DIRECCION_BUCKET + url_avatar);
		        		img_lateral_avatar.refreshDrawableState();
		        	}
				} else {
					Toast toast;
					toast = Toast
							.makeText(mContext,getResources().getString(
													R.string.hubo_algun_problema_actualizando_avatar),
									Toast.LENGTH_LONG);
					toast.show();
					// Quitamos el ProgressDialog
					if (progress.isShowing()) {
						progress.dismiss();
					}
				}
			}
		}
	}

	/**
	 * Seleccionar opción del spinner
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		// Si ha seleccionado una categoria diferente de la que se encuentra actualmente
		if ((pos == 0 && category.equals(ANGRY))|| (pos == 1 && category.equals(HAPPY))) {
			// Si hay conexión
			if (Connectivity.isOnline(mContext)) {
				cancelGetEscandalos();
				refreshFinished();
				// Inhabilitamos el spinner
				spinner_categorias.setClickable(false);
				// Abrimos llave de hay más escandalos
				there_are_more_escandalos = true;
				// Quitamos los escándalos actuales
				escandalos.clear();

				switch (pos) {
				case 0: // Humor
					if (category.equals(ANGRY)) {
						// Mandamos el evento a Google Analytics
						EasyTracker easyTracker2 = EasyTracker
								.getInstance(mContext);
						easyTracker2.send(MapBuilder.createEvent("Acción UI",
								"Selección realizada", "Seleccionado humor",
								null).build());
						category = HAPPY;
					}
					break;
				case 1: // Denuncia
					// Mandamos el evento a Google Analytics
					EasyTracker easyTracker3 = EasyTracker
							.getInstance(mContext);
					easyTracker3.send(MapBuilder.createEvent("Acción UI",
							"Selección realizada", "Seleccionado denuncia",
							null).build());
					category = ANGRY;
					break;
				}

				pager.setCurrentItem(0);
				adapter.clearFragments();
				adapter = new ScandalohFragmentPagerAdapter(
						getSupportFragmentManager());
				pager.setAdapter(adapter);
				// Obtenemos los 10 primeros escándalos para la categoría seleccionada
				// Mostramos el progressBar y ocultamos la lista de escandalos
				loading.setVisibility(View.VISIBLE);
				pager.setVisibility(View.GONE);
				getEscandalosAsync = new GetEscandalos();
				getEscandalosAsync.execute();
			}

			// No hay conexión
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
		// TODO Auto-generated method stub

	}

	

	// -----------------------------------------------------------------------------
	// ------------------------------ MÉTODOS --------------------------------------
	// -----------------------------------------------------------------------------

	/**
	 * Crea un archivo temporal en una ruta con un formato específico
	 */
	private File createFileTemporary(String part, String ext) {
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath()
				+ "/ScándalOh/");
		if (!scandaloh_dir.exists()) {
			scandaloh_dir.mkdirs();
		}
		try {
			return File.createTempFile(part, ext, scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(this.getClass().toString(),
					"No se pudo crear el archivo temporal para la foto");
			// Mandamos la excepcion a Google Analytics
			EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			easyTracker.send(MapBuilder.createException(
					new StandardExceptionParser(mContext, null) // Context and
																// optional
																// collection of
																// package names
																// to be used in
																// reporting the
																// exception.
							.getDescription(Thread.currentThread().getName(), // The
																				// name
																				// of
																				// the
																				// thread
																				// on
																				// which
																				// the
																				// exception
																				// occurred.
									e), // The exception.
					false).build());
			Toast toast = Toast.makeText(mContext,
					R.string.no_se_puede_acceder_camara, Toast.LENGTH_SHORT);
			toast.show();
		}

		return null;
	}

	/**
	 * Comprueba si el dispositivo dispone de cámara
	 * 
	 * @param context
	 * @return
	 */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Se llama cuando se ha terminado de actualizar el carrusel
	 */
	public void refreshFinished() {
		// Cambiamos el loading del menu por el botón de actualizar
		progress_refresh.setVisibility(View.GONE);
		img_update_list.setVisibility(View.VISIBLE); 
	}

	/**
	 * Cancela si hubiese alguna hebra obteniendo escándalos
	 */
	private void cancelGetEscandalos() {
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

		/*
		 * if (updateNumCommentsAsync != null){ if
		 * (updateNumCommentsAsync.getStatus() == AsyncTask.Status.PENDING ||
		 * updateNumCommentsAsync.getStatus() == AsyncTask.Status.RUNNING){
		 * updateNumCommentsAsync.cancel(true); } }
		 */
	}

	/**
	 * Actualiza el action bar: muestra el menú con todas las opciones o muestra
	 * un campo para escribir un comentario
	 * 
	 * @param write_mode
	 *            True indica si el action bar será el de escribir comentario
	 */
	public void updateActionBar(boolean write_mode, String id_photo) {

		final String photo_id = id_photo;
		ActionBar actBar = getSupportActionBar();
		// Activamos el logo del menu para el menu lateral
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.logo_blanco);

		// Modo escribir comentarios
		if (write_mode) {
			action_bar_type = ENVIAR_COMENTARIO;
			actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
					| ActionBar.DISPLAY_SHOW_HOME);
			View view = getLayoutInflater().inflate(
					R.layout.action_bar_escribir, null);
			actBar.setCustomView(view);
			// Activamos el logo del menu para el menu lateral
			actBar.setHomeButtonEnabled(true);
			actBar.setDisplayHomeAsUpEnabled(true);
			actBar.setIcon(R.drawable.logo_blanco);

			img_send_comment = (ImageView) findViewById(R.id.img_escandalo_send_comment);
			img_send_comment.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Si hay conexión
					if (Connectivity.isOnline(mContext)) {
						String written_comment;
						written_comment = edit_escribir_comentario.getText()
								.toString();
						// Si ha escrito algo y la longitud es menor de 1000
						// caracteres lo enviamos
						if (!written_comment.equals("")
								&& written_comment.length() < 1001) {
							new SendCommentTask(mContext, photo_id).execute();
						}
					} else {
						Toast toast;
						toast = Toast.makeText(mContext, getResources()
								.getString(R.string.no_dispones_de_conexion),
								Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			});

			edit_escribir_comentario = (EditText) findViewById(R.id.edit_write_comment);
			if (!MyApplication.logged_user) {
				edit_escribir_comentario
						.setHint(R.string.inicia_sesion_para_comentar);
				edit_escribir_comentario.setInputType(InputType.TYPE_NULL);
				edit_escribir_comentario
						.setOnTouchListener(new View.OnTouchListener() {

							@Override
							public boolean onTouch(View v, MotionEvent event) {
								Toast toast = Toast.makeText(mContext, getResources().getString(R.string.registrate_o_inicia_sesion), Toast.LENGTH_SHORT);
								toast.show();
								return false;
							}
						});
			}
			edit_escribir_comentario.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// Si ha llegado al límite de caracteres se lo indicamos
					if (s.length() == 1000) {
						Toast toast = Toast.makeText(mContext, getResources()
								.getString(R.string.ha_llegado_al_limite),
								Toast.LENGTH_LONG);
						toast.show();
					}
				}

				@Override
				public void afterTextChanged(Editable arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub
				}
			});
		}

		// Modo normal
		else {
			action_bar_type = NORMAL;
			actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
					| ActionBar.DISPLAY_SHOW_HOME);
			View view = getLayoutInflater()
					.inflate(R.layout.action_bar_2, null);
			actBar.setCustomView(view);
			// Activamos el logo del menu para el menu lateral
			actBar.setHomeButtonEnabled(true);
			actBar.setDisplayHomeAsUpEnabled(true);
			actBar.setIcon(R.drawable.logo_blanco);

			// Spinner con su categoría seleccionada
			spinner_categorias = (Spinner) findViewById(R.id.sp_categorias);
			spinner_categorias.setAdapter(adapter_spinner);
			spinner_categorias.setOnItemSelectedListener(this);
			if (category.equals(HAPPY)) {
				spinner_categorias.setSelection(0);
			} else {
				spinner_categorias.setSelection(1);
			}
			loading = (ProgressBar) findViewById(R.id.loading_escandalos);
			img_update_list = (ImageView) findViewById(R.id.img_actionbar_updatelist);
			ll_refresh = (LinearLayout) findViewById(R.id.ll_main_refresh);
			ll_refresh.setOnClickListener(this);
			img_take_photo = (ImageView) findViewById(R.id.img_actionbar_takephoto);
			ll_take_photo = (LinearLayout) findViewById(R.id.ll_main_take_photo);
			ll_take_photo.setOnClickListener(this);
			progress_refresh = (ProgressBar) findViewById(R.id.prog_refresh_action_bar);
			txt_code_country = (TextView) findViewById(R.id.txt_action_bar_codecountry);
			
			txt_code_country.setText(MyApplication.code_selected_country);
		}
	}

	/**
	 * Hace crop a una foto
	 * 
	 * @param picUri
	 *            Uri de la foto
	 */
	private boolean performCrop(Uri picUri) {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			cropIntent.setDataAndType(picUri, "image/*");
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			cropIntent.putExtra("outputX", 128);
			cropIntent.putExtra("outputY", 128);
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, PIC_CROP);
			return true;
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			return false;
		}
	}
	
	
	
	/**
	 * Actualiza el already_voted del escandalo (fragmento) que esté actualmente visualizándose
	 * @param already_voted
	 */
	public void updateLikesDislikes(int already_voted, int num_likes, int num_dislikes){
		adapter.updateFragmentLike(already_voted, num_likes, num_dislikes);
	}
	
	
	
	
	
	// ---------------------------------------------------------------------------------------------------------
	
	
	/**
	 * Adaptador del view pager
	 * 
	 */
	public class ScandalohFragmentPagerAdapter extends FragmentStatePagerAdapter {

		// Lista de fragmentos con los escándalos
		List<ScandalohFragment> fragments;

		/**
		 * Constructor
		 * 
		 * @param fm Interfaz para interactuar con los fragmentos dentro de una actividad
		 */
		public ScandalohFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fragments = new ArrayList<ScandalohFragment>();
		}

	
		/**
		 * Devuelve el fragmento de una posición dada
		 * 
		 * @param position Posición
		 */
		@Override
		public ScandalohFragment getItem(int position) {
			// return ScandalohFragment.newInstance(escandalos.get(position));
			return fragments.get(position);
		}

		/**
		 * Devuelve el número de fragmentos
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
			ScandalohFragment fragment = (ScandalohFragment) item;
			if (pager.getCurrentItem() == fragments.indexOf(fragment)) {
				return fragments.indexOf(fragment);
			} else {
				return POSITION_NONE;
			}

		}

		/**
		 * Añade un fragmento al final de la lista
		 * @param fragment Fragmento a añadir
		 */
		public void addFragment(ScandalohFragment fragment) {
			this.fragments.add(fragment);
		}

		/**
		 * Añade un fragmento al principio de la lista
		 * @param fragment
		 */
		public void addFragmentAtStart(ScandalohFragment fragment) {
			this.fragments.add(0, fragment);
		}

		/**
		 * Modifica un fragmento
		 * @param position
		 * @param fragment
		 */
		public void setFragment(int position, ScandalohFragment fragment) {
			this.fragments.set(position, fragment);
		}
		
		/**
		 * Actualiza el campo already_voted del fragmento que esté actualmente visualizándose
		 * @param already_voted
		 */
		public void updateFragmentLike(int already_voted, int num_likes, int num_dislikes){
			// Obtenemos el escandalo que está en pantalla
			Scandaloh scan = escandalos.get(pager.getCurrentItem());
			// Le modificamos el already_voted
			scan.setAlreadyVoted(already_voted);
			scan.setLikes(num_likes);
			scan.setDislikes(num_dislikes);
			// Actualizamos el adaptador con el nuevo fragmento
			ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}

		/**
		 * Obtiene un fragmento a partir de una posición
		 * @param position
		 * @return
		 */
		public ScandalohFragment getFragment(int position) {
			return this.fragments.get(position);
		}
		

		/**
		 * Elimina todos los fragmentos
		 */
		public void clearFragments() {
			this.fragments.clear();
		}
	}
	
	


}
