package com.bizeu.escandaloh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
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
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.model.Scandaloh;
import com.bizeu.escandaloh.settings.SettingsActivity;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.bizeu.escandaloh.users.ProfileActivity;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Connectivity;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.ImageUtils;
import com.bizeu.escandaloh.util.Utils;
import com.countrypicker.CountryPicker;
import com.countrypicker.CountryPickerListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class MainActivity extends SherlockFragmentActivity implements
		OnClickListener, OnItemSelectedListener {

	public static final int NUM_SCANDALS_TO_LOAD = 15;
	public static final int NUM_SCANDALS_TO_LOAD_FIRST_TIME = 10;
	public static final int SHOW_CAMERA = 10;
	private static final int CREATE_ESCANDALO = 11;
	public static final int FROM_GALLERY = 12;
	public static final int SHARING = 13;
	public static final int SHOW_PROFILE = 14;
	public static final String CATEGORY = "Category";
	public static final String ANGRY = "Denuncia";
	public static final String HAPPY = "Humor";
	public static final String BOTH = "Todas";
	public static final String NORMAL = "Normal";
	public static final String ENVIAR_COMENTARIO = "Enviar_comentario";
	private static final String FILTRO_RECIENTES = "-date";
	private static final String FILTRO_COMENTADAS = "-comments_count";
	private static final String FILTRO_VOTADAS = "-votes_count";

	private LinearLayout ll_refresh;
	private LinearLayout ll_take_photo;
	private ImageView img_update_list;
	private ImageView img_take_photo;
	private LinearLayout ll_lateral_notificaciones;
	private LinearLayout ll_lateral_pais;
	private LinearLayout ll_lateral_perfil;
	private LinearLayout ll_lateral_ajustes;
	private LinearLayout ll_lateral_login;
	private TextView txt_lateral_nombreusuario;
	private ProgressBar progress_refresh;
	private LinearLayout ll_menu_lateral;
	private TextView txt_code_country;
	private EditText edit_escribir_comentario;
	private Spinner spinner_categorias;
	private ImageView img_send_comment;
	DrawerLayout mDrawerLayout;
	private FetchableImageView img_lateral_avatar;
	private ExpandableListView explist_lateral_filtros;
	
	private Uri mImageUri;
	AmazonS3Client s3Client;
	private Context mContext;
	ScandalohFragmentPagerAdapter adapter;
	ViewPager pager = null;
	ProgressBar loading;
	private boolean any_error;
	private GetScandalsTask getEscandalosAsync;
	private GetNewScandals getNewEscandalosAsync;
	private String category;
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
	private List<String> filter_header;
    private List<String> filter_childs;
    private Map<String, List<String>> filterCollection;
    private String actual_filter = FILTRO_RECIENTES ;

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
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar, null);
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

				// Si quedan 5 escándalos más para llegar al último y aún quedan
				// más escándalos (si hemos llegado
				// a los últimos no se pedirán más): obtenemos los siguientes 10
				if (position == adapter.getCount() - (NUM_SCANDALS_TO_LOAD-5)
						&& there_are_more_scandals) {
					// Usamos una llave de paso (sólo la primera vez entrará).
					// Cuando se obtengan los 10 escándalos se volverá a abrir
					if (!getting_escandalos) {
						getEscandalosAsync = new GetScandalsTask();
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
		ll_lateral_perfil = (LinearLayout) findViewById(R.id.ll_mLateral_profile);
		ll_lateral_ajustes = (LinearLayout) findViewById(R.id.ll_mLateral_ajustes);
		ll_lateral_login = (LinearLayout) findViewById(R.id.ll_mLateral_login);
		txt_lateral_nombreusuario = (TextView) findViewById(R.id.txt_lateral_nombreusuario);
		explist_lateral_filtros = (ExpandableListView) findViewById(R.id.explist_mLateral_filtros);
		
		// Sombra del menu sobre la pantalla
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);

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

		// Avatar
		img_lateral_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MyApplication.logged_user){				
				}
			}
		});
		
		// Perfil
		ll_lateral_perfil.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {		
				// Almacenamos el avatar actual del usuario
				actual_avatar = MyApplication.avatar;
				Intent i = new Intent(MainActivity.this, ProfileActivity.class);
				startActivityForResult(i,SHOW_PROFILE);		
			}
		});

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

		// Login
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
		
		// Pais
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
								hideLoadingFromMenu();
								// Abrimos llave de hay más escandalos
								there_are_more_scandals = true;
								// Quitamos los escándalos actuales
								escandalos.clear();

								pager.setCurrentItem(0);
								adapter.clearFragments();
								adapter = new ScandalohFragmentPagerAdapter(
										getSupportFragmentManager());
								pager.setAdapter(adapter);
								// Obtenemos los 10 primeros escándalos para la categoría seleccionada
								// Mostramos el progressBar y ocultamos la lista de escandalos
								loading.setVisibility(View.VISIBLE);
								pager.setVisibility(View.GONE);
								getEscandalosAsync = new GetScandalsTask();
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
		
		// Filtros
		// Los rellenamos
		filter_header = new ArrayList<String>();
		filter_header.add(getResources().getString(R.string.filtrado_por));
		String[] filter_types = { getResources().getString(R.string.mas_recientes),
				getResources().getString(R.string.mas_votados), 
				getResources().getString(R.string.mas_comentados) };
		filterCollection = new LinkedHashMap<String, List<String>>();
        for (String laptop : filter_header) {
            if (laptop.equals(getResources().getString(R.string.filtrado_por))) {
            	filter_childs = new ArrayList<String>();
                for (String model : filter_types)
                	filter_childs.add(model);
            } 
            filterCollection.put(laptop, filter_childs);
        }
        
        // Asignamos los listeners
        final ExpandableListAdapter expListAdapter = new FilterAdapter(this, filter_header, filterCollection);
        explist_lateral_filtros.setAdapter(expListAdapter);
        explist_lateral_filtros.setOnChildClickListener(new OnChildClickListener() {
 
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final String selected_filter = (String) expListAdapter.getChild(groupPosition, childPosition);
                
                // Más recientes
                if (selected_filter.equals(getResources().getString(R.string.mas_recientes))){
                	actual_filter = FILTRO_RECIENTES;
                }
                // Más votados
                else if (selected_filter.equals(getResources().getString(R.string.mas_votados))){
                	actual_filter = FILTRO_VOTADAS;
                }
                // Más comentados
                else if (selected_filter.equals(getResources().getString(R.string.mas_comentados))){
                	actual_filter = FILTRO_COMENTADAS;
                }
 
                resetScandals();
				// Cerramos el menu
				mDrawerLayout.closeDrawer(ll_menu_lateral);
                return true;
            }
        });

		// Le asignamos la animación al pasar entre escándalos (API 11+)
		//pager.setPageTransformer(true, new ZoomOutPageTransformer());
		// Separación entre escándalos
		pager.setPageMargin(3);
		category = HAPPY;

		// Si hay conexión: obtenemos los primeros escándalos
		if (Connectivity.isOnline(mContext)) {
			getEscandalosAsync = new GetScandalsTask();
			getEscandalosAsync.execute();
		} else {
			Toast toast = Toast.makeText(mContext,
					getResources().getString(R.string.no_dispones_de_conexion),
					Toast.LENGTH_SHORT);
			toast.show();
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
		// Activamos google analytics
		EasyTracker.getInstance(this).activityStart(this);

	}

	/**
	 * onResume
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Si está logueado ocultamos las opción de login  y mostramos su info y la opción de perfil
		if (MyApplication.logged_user) {
			ll_lateral_login.setVisibility(View.GONE);
			ll_lateral_perfil.setVisibility(View.VISIBLE);
			txt_lateral_nombreusuario.setText(MyApplication.user_name);
			if (MyApplication.avatar != null){
				Log.v("WE","Myapplication.avatar: " + MyApplication.avatar);
		        img_lateral_avatar.setImage(MyApplication.DIRECCION_BUCKET + MyApplication.avatar, R.drawable.avatar_mas);
			}
			else{
		        img_lateral_avatar.setImageResource(R.drawable.avatar_mas);
			}
		} else {
			ll_lateral_login.setVisibility(View.VISIBLE);
			ll_lateral_perfil.setVisibility(View.GONE);
			img_lateral_avatar.setImageResource(R.drawable.avatar_mas);
			txt_lateral_nombreusuario.setText(getResources().getString(R.string.invitado));
			img_lateral_avatar.setImageResource(R.drawable.avatar_defecto);		
		}
		
		// Si ha iniciado/cerrado sesión: reiniciamos los escándalos
		if (MyApplication.reset_scandals){		
			resetScandals();
		}
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Escándalo desde la cámara
		if (requestCode == SHOW_CAMERA) {
			if (resultCode == RESULT_OK) {
				if (mImageUri != null) {
					// Guardamos la foto en la galería
					Bitmap bitAux = ImageUtils.uriToBitmap(mImageUri, mContext);
					ImageUtils.saveBitmapIntoGallery(bitAux, mContext);

					// Mostramos la pantalla de subir escándalo
					Intent i = new Intent(MainActivity.this,CreateScandalohActivity.class);
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
										EasyTracker easyTracker = EasyTracker.getInstance(mContext);
										easyTracker.send(MapBuilder.createEvent(
																"Acción UI",
																"Selección realizada",
																"Hacer foto desde la cámara",
																null).build());

										// Si dispone de cámara iniciamos la
										// cámara
										if (Utils.checkCameraHardware(mContext)) {
											Intent takePictureIntent = new Intent(
													"android.media.action.IMAGE_CAPTURE");
											File photo = null;
											photo = createFileTemporary(
													"picture", ".png");
											if (photo != null) {
												mImageUri = Uri.fromFile(photo);
												takePictureIntent.putExtra(
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
				
				// No está logueado: mostramos un popup preguntando si quiere loguearse
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
		
			// Nos colocamos en el primer escandalo
			pager.setCurrentItem(0);
			
			// Comprobamos si hay nuevos escándalos sólo si estamos filtrando por fecha
			if (actual_filter.equals(FILTRO_RECIENTES)){
				
				// Si no se están obteniendo otros escándalos
				if (!getting_escandalos) {
					
					// Si hay conexión
					if (Connectivity.isOnline(mContext)) {
						
						// Obtenemos los escándalos:
						// Si no hay ninguno mostrado obtenemos los primeros, si hay
						// alguno obtenemos si hay nuevos escándalos subidos
						getting_escandalos = true;

						if (escandalos.size() > 0) {
							getNewEscandalosAsync = new GetNewScandals();
							getNewEscandalosAsync.execute();
						} else {
							no_hay_escandalos = true; // Indicamos que no hay
														// escándalos aún
							getEscandalosAsync = new GetScandalsTask();
							getEscandalosAsync.execute();
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
			}				
			break;
		}
	}

	/**
	 * Obtiene los siguientes 10 escándalos anteriores a partir de uno dado
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
			// Cambiamos la imagen de actualizar por un loading
			showLoadingOnMenu();		
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String url = null;
			
			// No hay escándalos: obtenemos los primeros
			if (escandalos.size() == 0){
				
				url = MyApplication.SERVER_ADDRESS + "/api/v1/photo/?limit=" + NUM_SCANDALS_TO_LOAD;
				// HAPPY
				if (category.equals(MainActivity.HAPPY)) {
					url += "&category__id=1";
				}
				else{
					url += "&category__id=2";
				}
						
				url += "&country="+ MyApplication.code_selected_country;
				url += "&order_by=" + actual_filter ;
			}
			
			// Obtenemos los siguientes escándalos
			else{
				// Fin del carrusel: meta nulo indica que no hay más escándalos
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
				
				// Si es con usuario le añadimos el session_token
				if (MyApplication.logged_user){
					getEscandalos.setHeader("Session-Token", MyApplication.session_token);
				}

				// Hacemos la petición al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);

				// Parseamos los escándalos devueltos
				JSONObject respJson = new JSONObject(respStr);

				// Obtenemos el meta
				JSONObject respMetaJson = respJson.getJSONObject("meta");
				meta_next_scandals = respMetaJson.getString("next");

				JSONArray escandalosObject = respJson.getJSONArray("objects");
				
				// Obtenemos los datos de los escándalos
				for (int i = 0; i < escandalosObject.length(); i++) {
					// Hacemos una declaración por cada escándalo
					final Comment last_comment;

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
					
					// Obtenemos el comentario más reciente
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
								// Añadimos el escandalo al ArrayList
								Scandaloh escanAux = new Scandaloh(id, title,
										category, BitmapFactory.decodeResource(getResources(),R.drawable.loading),
										Integer.parseInt(comments_count),resource_uri,
										MyApplication.DIRECCION_BUCKET + img_p,
										MyApplication.DIRECCION_BUCKET + img, sound, username, date,
										avatar, last_comment, social_network,
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
			
			// Mostramos el botón actualizar
			hideLoadingFromMenu();
			
			// Quitamos el loading y mostramos los escándalos
			pager.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);

			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}

			// Habilitamos el spinner
			spinner_categorias.setClickable(true);

			// Si hemos llegado aqui porque no habían escándalos (y le dio a actualizar), paramos el loading del menu
			if (no_hay_escandalos) {
				hideLoadingFromMenu();
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
			if (category.equals(MainActivity.HAPPY)) {
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

				// Hacemos la petición al servidor
				response = httpClient.execute(getEscandalos);
				String respStr = EntityUtils.toString(response.getEntity());
				Log.i("WE", respStr);
				
				// Parseamos los escándalos devueltos
				JSONObject respJson = new JSONObject(respStr);
				JSONArray escandalosObject = respJson.getJSONArray("objects");

				// Obtenemos los datos de los escándalos
				for (int i = escandalosObject.length() -1; i >= 0; i--) {
					// Hacemos una declaración por cada escándalo
					final Comment last_comment;

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
					
					// Obtenemos el comentario más reciente
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
								// Añadimos el escandalo al ArrayList
								Scandaloh escanAux = new Scandaloh(id, title,
										category, BitmapFactory.decodeResource(getResources(),R.drawable.loading),
										Integer.parseInt(comments_count),resource_uri,
										MyApplication.DIRECCION_BUCKET + img_p,
										MyApplication.DIRECCION_BUCKET + img, sound, username, date,
										avatar, last_comment, social_network,
										already_voted, likes, dislikes, media_type, MyApplication.DIRECCION_BUCKET + favicon, source, source_name);
								escandalos.add(0,escanAux);
								adapter.addFragmentAtStart(ScandalohFragment.newInstance(escandalos.get(0)));
								adapter.notifyDataSetChanged();
							}
						});
					}
				}
			} catch (Exception ex) {
				Log.e("ServicioRest", "Error!", ex);
				// Hubo algún error inesperado
				any_error = true;
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
			hideLoadingFromMenu();

			// Si hubo algún error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}
			else{
				pager.setCurrentItem(0);
			}

			// Habilitamos el spinner
			spinner_categorias.setClickable(true);

			// Si hemos llegado aqui porque no habían escándalos (y le dio a actualizar), paramos el loading del menu
			if (no_hay_escandalos) {
				hideLoadingFromMenu();
				no_hay_escandalos = false;
			}

			// Ya no se están obteniendo escándalos (abrimos la llave)
			getting_escandalos = false;

			adapter.notifyDataSetChanged();
			
			// Indicamos a la actividad que ha terminado de actualizar
			hideLoadingFromMenu();
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
				hideLoadingFromMenu();
				// Inhabilitamos el spinner
				spinner_categorias.setClickable(false);
				// Abrimos llave de hay más escandalos
				there_are_more_scandals = true;
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
				getEscandalosAsync = new GetScandalsTask();
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
					new StandardExceptionParser(mContext, null) 
							.getDescription(Thread.currentThread().getName(),
									e), // The exception.
					false).build());
			Toast toast = Toast.makeText(mContext,
					R.string.no_se_puede_acceder_camara, Toast.LENGTH_SHORT);
			toast.show();
		}

		return null;
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
	}
	
	/**
	 * Resetea los escándalos del carrusel
	 */
	private void resetScandals(){
		// Abrimos llave de hay más escandalos
		there_are_more_scandals = true;
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
		getEscandalosAsync = new GetScandalsTask();
		getEscandalosAsync.execute();
		
		// Cerramos llave
		MyApplication.reset_scandals = false;
	}
	
	/**
	 * Oculta el botón actualizar y muestra el loading en el menu
	 */
	private void showLoadingOnMenu(){
		// Cambiamos la imagen de actualizar por un loading
		progress_refresh.setVisibility(View.VISIBLE);
		img_update_list.setVisibility(View.GONE);
	}
	
	/**
	 * Oculta el loading del menu y muestra el botón actualizar
	 */
	private void hideLoadingFromMenu() {
		// Cambiamos el loading del menu por el botón de actualizar
		progress_refresh.setVisibility(View.GONE);
		img_update_list.setVisibility(View.VISIBLE); 
	}
	
	
	
	/**
	 * Actualiza el already_voted del escandalo (fragmento) que esté actualmente visualizándose
	 * @param already_voted
	 */
	public void updateLikesDislikes(int already_voted, int num_likes, int num_dislikes){
		adapter.updateFragmentLike(already_voted, num_likes, num_dislikes);
	}
	
	
	/**
	 * Actualiza el último comentario del escandalo (fragmento) que esté actualmente visualizándose
	 * @param lst_comm
	 */
	public void updateLastComment(Comment lst_comm){
		adapter.updateLastComment(lst_comm);
	}
	
	/**
	 * Actualiza el número de comentarios del escandalo (fragmento) que esté actualmente visualizándose
	 * @param num_comments
	 */
	public void updateNumComments(int num_comments){
		adapter.updateNumComments(num_comments);
	}
	
	/**
	 * Actualiza el avatar del usuario en todos los escándalos (fragmentos) 
	 */
	private void updateUserAvatar(){
		adapter.updateUserAvatar();
		adapter.notifyDataSetChanged();
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
		 * Actualiza el último comentario del fragmento que esté actualmente visualizándose
		 * @param comment_text
		 */
		public void updateLastComment(Comment comm){
			// Obtenemos el escandalo que está en pantalla
			Scandaloh scan = escandalos.get(pager.getCurrentItem());
			// Le modificamos el último comentario
			scan.setLastComment(comm);
			// Actualizamos el adaptador con el nuevo fragmento
			ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}
		
		
		/**
		 * Actualiza el nº de comentarios del fragmento que esté actualmente visualizándose
		 * @param num_c
		 */
		public void updateNumComments(int num_c){
			// Obtenemos el escandalo que está en pantalla
			Scandaloh scan = escandalos.get(pager.getCurrentItem());
			// Le modificamos el último comentario
			scan.setNumComments(num_c);
			// Actualizamos el adaptador con el nuevo fragmento
			ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}
		
		
		/**
		 * Actualiza el avatar del usuario en todos los escándalos
		 */
		public void updateUserAvatar(){
			 for (int i=0; i<escandalos.size(); i++){
				 
				 // Obtenemos el escándalo
				 Scandaloh scan = escandalos.get(i);
				 
				 // Si soy el usuario del escándalo actualizo mi avatar
				 if (scan.getUser().equals(MyApplication.user_name)){
					 scan.setAvatar(MyApplication.avatar);
				 }
				 
				 // Si soy el usuario del último comentario actualizo mi avatar
				 Comment cAux = escandalos.get(i).getLastComment();
				 if (cAux != null){
					 if (cAux.getUsername().equals(MyApplication.user_name)){
						 cAux.setAvatar(MyApplication.avatar);
						 scan.setLastComment(cAux);
					 }
				 }
				 
				 ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
				 this.fragments.set(i, sf2); 
			 }
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
