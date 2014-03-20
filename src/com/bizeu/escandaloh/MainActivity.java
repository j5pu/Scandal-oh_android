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

				// Si quedan 4 esc�ndalos m�s para llegar al �ltimo y a�n quedan
				// m�s esc�ndalos (si hemos llegado
				// a los �ltimos no se pedir�n m�s): obtenemos los siguientes 10
				if (position == adapter.getCount() - 5
						&& there_are_more_escandalos) {
					// Usamos una llave de paso (s�lo la primera vez entrar�).
					// Cuando se obtengan los 10 esc�ndalos se volver� a abrir
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
		txt_lateral_nombreusuario = (TextView) findViewById(R.id.txt_lateral_nombreusuario);
		
		// Sombra del menu sobre la pantalla
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);

		/*options = new String[] { getResources().getString(R.string.perfil),
				getResources().getString(R.string.notificaciones),
				getResources().getString(R.string.pais),
				getResources().getString(R.string.ajustes),
				getResources().getString(R.string.danos_tu_opinion) };
				*/

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
				// Creamos un popup para elegir entre hacer foto con la c�mara o cogerla de la galer�a
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

						// C�mara
						if (items[item].equals(getResources().getString(
								R.string.hacer_foto_con_camara))) {

							// Si dispone de c�mara iniciamos la c�mara
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
							// El dispositivo no dispone de c�mara
							else {
								Toast toast = Toast
										.makeText(
												mContext,
												R.string.este_dispositivo_no_dispone_camara,
												Toast.LENGTH_LONG);
								toast.show();
							}
						}

						// Galer�a
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
							txt_code_country.setText(code);
							SharedPreferences prefs = getBaseContext().getSharedPreferences(
				        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
							// Guardamos el c�digo del pa�s
				        	prefs.edit().putString(MyApplication.CODE_COUNTRY, code).commit();
							
							// Si hay conexi�n
							if (Connectivity.isOnline(mContext)) {
								cancelGetEscandalos();
								refreshFinished();
								// Abrimos llave de hay m�s escandalos
								there_are_more_escandalos = true;
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
								getEscandalosAsync = new GetEscandalos();
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

		// Le asignamos la animaci�n al pasar entre esc�ndalos (API 11+)
		pager.setPageTransformer(true, new ZoomOutPageTransformer());
		// Separaci�n entre esc�ndalos
		pager.setPageMargin(3);
		category = HAPPY;

		// Si hay conexi�n: obtenemos los 10 primeros esc�ndalos
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
		// Activamos google analytics
		EasyTracker.getInstance(this).activityStart(this);

	}

	/**
	 * onResume
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Si est� logueado ocultamos las opci�n de login  y mostramos su nombre en el menu
		// Actualizamos el avatar y el nombre de usuario
		if (MyApplication.logged_user) {
			ll_lateral_login.setVisibility(View.GONE);
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
			img_lateral_avatar.setImageResource(R.drawable.avatar_mas);
			txt_lateral_nombreusuario.setText(getResources().getString(R.string.invitado));
			img_lateral_avatar.setImageResource(R.drawable.avatar_defecto);		
		}
		
		// Si ha iniciado/cerrado sesi�n: reiniciamos los esc�ndalos
		if (MyApplication.reset_scandals){		
			// Abrimos llave de hay m�s escandalos
			there_are_more_escandalos = true;
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
			getEscandalosAsync = new GetEscandalos();
			getEscandalosAsync.execute();
			
			// Cerramos llave
			MyApplication.reset_scandals = false;
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
		// Cancelamos los esc�ndalos que se estuvieran obteniendo
		cancelGetEscandalos();
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
		if (requestCode == SHOW_CAMERA) {
			if (resultCode == RESULT_OK) {
				if (mImageUri != null) {
					// Guardamos la foto en la galer�a
					Bitmap bitAux = ImageUtils.uriToBitmap(mImageUri, mContext);
					ImageUtils.saveBitmapIntoGallery(bitAux, mContext);

					// Mostramos la pantalla de subir esc�ndalo
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

		// Esc�ndalo desde la galer�a
		else if (requestCode == FROM_GALLERY) {
			if (data != null) {
				// Mostramos la pantalla de subir esc�ndalo
				Uri selectedImageUri = data.getData();
				Intent i = new Intent(MainActivity.this,
						CreateScandalohActivity.class);
				i.putExtra("photo_from", FROM_GALLERY);
				i.putExtra("photoUri", ImageUtils.getRealPathFromURI(mContext,
						selectedImageUri));
				startActivityForResult(i, CREATE_ESCANDALO);
			}
		}

		// Avatar desde la c�mara
		else if (requestCode == AVATAR_SHOW_CAMERA) {
			if (mImageUri != null) {
				if (!performCrop(mImageUri)) {
					Bitmap photo_for_avatar = ImageUtils.uriToBitmap(mImageUri,
							this);
					 new UpdateAvatarTask(this,photo_for_avatar).execute();
				}
			}
		}

		// Avatar desde la galer�a
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

			// Paramos si hubiera alg�n audio reproduci�ndose
			Audio.getInstance(mContext).releaseResources();

			// Si dispone de conexi�n
			if (Connectivity.isOnline(mContext)) {
				
				// Si est� logueado
				if (MyApplication.logged_user) {
					
					// Creamos un menu para elegir entre hacer foto con la
					// c�mara o cogerla de la galer�a
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

									// C�mara
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
																"Acci�n UI",
																"Selecci�n realizada",
																"Hacer foto desde la c�mara",
																null).build());

										// Si dispone de c�mara iniciamos la
										// c�mara
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
										// El dispositivo no dispone de c�mara
										else {
											Toast toast = Toast
													.makeText(
															mContext,
															R.string.este_dispositivo_no_dispone_camara,
															Toast.LENGTH_LONG);
											toast.show();
										}
									}

									// Galer�a
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
																"Acci�n UI",
																"Selecci�n realizada",
																"Subir foto desde la galer�a",
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
			break;

		// Actualizar carrusel: Le decimos al fragmento que actualice los
		// esc�ndalos (y suba el carrusel al primero)
		case R.id.ll_main_refresh:

			// Mandamos el evento a Google Analytics
			EasyTracker easyTracker = EasyTracker.getInstance(mContext);
			easyTracker.send(MapBuilder.createEvent("Acci�n UI",
					"Boton clickeado", "Actualizar lista esc�ndalos", null)
					.build());

			// Cambiamos la imagen de actualizar por un loading
			progress_refresh.setVisibility(View.VISIBLE);
			img_update_list.setVisibility(View.GONE);

			// Nos colocamos en el primer escandalo
			pager.setCurrentItem(0);

			// Si no se est�n obteniendo otros esc�ndalos
			if (!getting_escandalos) {
				// Si hay conexi�n
				if (Connectivity.isOnline(mContext)) {
					// Paramos si se estuviesen actualizando el n� de
					// comentarios
					/*
					 * if (updateNumCommentsAsync != null){ if
					 * (updateNumCommentsAsync.getStatus() ==
					 * AsyncTask.Status.PENDING ||
					 * updateNumCommentsAsync.getStatus() ==
					 * AsyncTask.Status.RUNNING){
					 * updateNumCommentsAsync.cancel(true); } }
					 */

					// Obtenemos los esc�ndalos:
					// Si no hay ninguno mostrado obtenemos los primeros, si hay
					// alguno obtenemos si hay nuevos esc�ndalos subidos
					getting_escandalos = true;

					if (escandalos.size() > 0) {
						getNewEscandalosAsync = new GetNewEscandalos();
						getNewEscandalosAsync.execute();
					} else {
						no_hay_escandalos = true; // Indicamos que no hay
													// esc�ndalos a�n
						getEscandalosAsync = new GetEscandalos();
						getEscandalosAsync.execute();
					}
				}

				// No hay conexi�n
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
	 * Obtiene los siguientes 10 esc�ndalos anteriores a partir de uno dado
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
				// de esc�ndalos o ya posteriores
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
				// de esc�ndalos o ya posteriores
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
				// Si es con usuario le a�adimos el session_token
				if (MyApplication.logged_user){
					Log.v("WE","session token " + MyApplication.session_token);
					getEscandalos.setHeader("Session-Token", MyApplication.session_token);
				}


				// Hacemos la petici�n al servidor
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
						// Si no hay m�s escandalos,lo indicamos
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
						// Si no hay m�s escandalos,lo indicamos
						if (escandalosObject.length() == 0) {
							there_are_more_escandalos = false;
						}
					}
				}

				// Obtenemos los datos de los esc�ndalos
				for (int i = 0; i < escandalosObject.length(); i++) {
					// Hacemos una declaraci�n por cada esc�ndalo
					final Comment last_comment;

					JSONObject escanObject = escandalosObject.getJSONObject(i);

					final String category = escanObject.getString("category");
					final String date = escanObject.getString("date");
					final String id = escanObject.getString("id");
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
						"Error obteniendo esc�ndalos o comentarios", ex);
				// Hubo alg�n error inesperado
				any_error = true;

				// Mandamos la excepcion a Google Analytics
				EasyTracker easyTracker = EasyTracker.getInstance(mContext);
				easyTracker.send(MapBuilder.createException(
						new StandardExceptionParser(mContext, null) 
								.getDescription(Thread.currentThread()
										.getName(), // The name of the thread on
													// which the exception
													// occurred.
										ex), false).build());

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
			loading.setVisibility(View.GONE);
			pager.setVisibility(View.VISIBLE);

			// Si hubo alg�n error inesperado mostramos un mensaje
			if (result == 666) {
				Toast toast = Toast.makeText(mContext,
						R.string.lo_sentimos_hubo, Toast.LENGTH_SHORT);
				toast.show();
			}

			// Habilitamos el spinner
			spinner_categorias.setClickable(true);

			// Si hemos llegado aqui porque no hab�an esc�ndalos (y le dio a
			// actualizar), paramos el loading del menu
			if (no_hay_escandalos) {
				refreshFinished();
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
	private class GetNewEscandalos extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			any_error = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			// A partir del id m�s nuevo obtenido (el primero del array)
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

			HttpResponse response = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getEscandalos = new HttpGet(url);
				getEscandalos.setHeader("content-type", "application/json");

				// Hacemos la petici�n al servidor
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
					 * @Override public void run() { // A�adimos el escandalo al
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
				// Hubo alg�n error inesperado
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

			// Si hubo alg�n error inesperado
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
	 * Env�a un avatar nuevo al servidor
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
				any_error = true; // Indicamos que hubo alg�n error

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

			// Si hubo alg�n error mostramos un mensaje
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
	 * Seleccionar opci�n del spinner
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		// Si ha seleccionado una categoria diferente de la que se encuentra actualmente
		if ((pos == 0 && category.equals(ANGRY))|| (pos == 1 && category.equals(HAPPY))) {
			// Si hay conexi�n
			if (Connectivity.isOnline(mContext)) {
				cancelGetEscandalos();
				refreshFinished();
				// Inhabilitamos el spinner
				spinner_categorias.setClickable(false);
				// Abrimos llave de hay m�s escandalos
				there_are_more_escandalos = true;
				// Quitamos los esc�ndalos actuales
				escandalos.clear();

				switch (pos) {
				case 0: // Humor
					if (category.equals(ANGRY)) {
						// Mandamos el evento a Google Analytics
						EasyTracker easyTracker2 = EasyTracker
								.getInstance(mContext);
						easyTracker2.send(MapBuilder.createEvent("Acci�n UI",
								"Selecci�n realizada", "Seleccionado humor",
								null).build());
						category = HAPPY;
					}
					break;
				case 1: // Denuncia
					// Mandamos el evento a Google Analytics
					EasyTracker easyTracker3 = EasyTracker
							.getInstance(mContext);
					easyTracker3.send(MapBuilder.createEvent("Acci�n UI",
							"Selecci�n realizada", "Seleccionado denuncia",
							null).build());
					category = ANGRY;
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
				getEscandalosAsync = new GetEscandalos();
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
		// TODO Auto-generated method stub

	}

	

	// -----------------------------------------------------------------------------
	// ------------------------------ M�TODOS --------------------------------------
	// -----------------------------------------------------------------------------

	/**
	 * Crea un archivo temporal en una ruta con un formato espec�fico
	 */
	private File createFileTemporary(String part, String ext) {
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath()
				+ "/Sc�ndalOh/");
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
	 * Comprueba si el dispositivo dispone de c�mara
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
		// Cambiamos el loading del menu por el bot�n de actualizar
		progress_refresh.setVisibility(View.GONE);
		img_update_list.setVisibility(View.VISIBLE); 
	}

	/**
	 * Cancela si hubiese alguna hebra obteniendo esc�ndalos
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
	 * Actualiza el already_voted del escandalo (fragmento) que est� actualmente visualiz�ndose
	 * @param already_voted
	 */
	public void updateLikesDislikes(int already_voted, int num_likes, int num_dislikes){
		adapter.updateFragmentLike(already_voted, num_likes, num_dislikes);
	}
	
	
	/**
	 * Actualiza el �ltimo comentario del escandalo (fragmento) que est� actualmente visualiz�ndose
	 * @param lst_comm
	 */
	public void updateLastComment(Comment lst_comm){
		adapter.updateLastComment(lst_comm);
	}
	
	
	public void updateNumComments(int num_comments){
		adapter.updateNumComments(num_comments);
	}
	
	
	
	
	
	// ---------------------------------------------------------------------------------------------------------
	
	
	/**
	 * Adaptador del view pager
	 * 
	 */
	public class ScandalohFragmentPagerAdapter extends FragmentStatePagerAdapter {

		// Lista de fragmentos con los esc�ndalos
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
		 * Devuelve el fragmento de una posici�n dada
		 * 
		 * @param position Posici�n
		 */
		@Override
		public ScandalohFragment getItem(int position) {
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
			ScandalohFragment fragment = (ScandalohFragment) item;
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
		public void addFragment(ScandalohFragment fragment) {
			this.fragments.add(fragment);
		}

		/**
		 * A�ade un fragmento al principio de la lista
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
			ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
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
			ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
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
			ScandalohFragment sf2 = ScandalohFragment.newInstance(scan);
			this.fragments.set(pager.getCurrentItem(), sf2);
		}

		/**
		 * Obtiene un fragmento a partir de una posici�n
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
