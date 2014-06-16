package com.bizeu.escandaloh;

import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.flurry.android.FlurryAgent;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class BrowserActivity extends SherlockActivity {

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    VARIABLES                                                      |
	// -----------------------------------------------------------------------------------------------------
	
	
	private WebView web;
	private ImageView img_back;
	private ImageView img_forward;
	private ImageView img_cancel;
	private ImageView img_share;
	private ProgressBar prog_loading;
	
	private String url;
	private boolean is_loading = true; // Nos indica si está cargando una web
	private Context mContext;
	
		
	// -----------------------------------------------------------------------------------------------------
	// |                                    METODOS  ACTIVITY                                              |
	// -----------------------------------------------------------------------------------------------------
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.browser);
		
		mContext = this;
		
		if (getIntent() != null){
			Intent i = getIntent();
			url = i.getStringExtra("source");
			web = (WebView) findViewById(R.id.wb_browser_noticia);
				
			// Hacemos que funcione todo (instagram, vine, facebook, twitter, ... etc)
			web.setKeepScreenOn(false);
			web.getSettings().setJavaScriptEnabled(true);
			web.getSettings().setDomStorageEnabled(true);
			web.getSettings().setBuiltInZoomControls(true);
			web.setInitialScale(100);
			web.getSettings().setUseWideViewPort(true);
			web.setWebViewClient(new MyWebViewClient());
			web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			web.loadUrl(url);
		}	
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_browser, null);
		actBar.setCustomView(view);
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.s_mezcla);
		
		img_back = (ImageView) findViewById(R.id.img_browser_backk);
		img_forward = (ImageView) findViewById(R.id.img_browser_forward);
		img_cancel = (ImageView) findViewById(R.id.img_browser_cancel);
		img_share = (ImageView) findViewById(R.id.img_browser_compartir);
		prog_loading = (ProgressBar) findViewById(R.id.prog_browser_loading);
		
		// Flecha atrás
		img_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (web.canGoBack()) {
			        web.goBack();
			    }	
			}
		});
		
		// Flecha adelante
		img_forward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (web.canGoForward()){
					web.goForward();
				}	
			}
		});
		
		// Cancelar/recargar págoina
		img_cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Cargando: cancelamos la petición
				if (is_loading){
					web.stopLoading();
				}
				// No está cargando: refrescamos la página
				else{
					WebBackForwardList mWebBackForwardList = web.copyBackForwardList();
					String lastUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()).getUrl();
					web.loadUrl(lastUrl);
				}
			}
		});
		
		// Compartir
		img_share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent i;
				
				// Logueado: mostramos pantalla de subir escándalo
				if (MyApplication.logged_user){
					i = new Intent(BrowserActivity.this, CreateScandalohActivity.class);
				}
				
				// No logueado: mostramos pantalla de iniciar sesión
				else{
					i = new Intent(BrowserActivity.this, LoginSelectActivity.class);
				}
				
				// Mandamos la url actual
				WebBackForwardList mWebBackForwardList = web.copyBackForwardList();
				String lastUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()).getUrl();
				i.putExtra("photo_from", CoverActivity.FROM_SHARING_TEXT);
				i.putExtra("photoUri", lastUrl);
				startActivity(i);
			}
		});
	}
	
	
		
	/**
	 * onStart
	 */
	@Override
	public void onStart() {
		super.onStart();
		// Iniciamos Flurry
		FlurryAgent.onStartSession(mContext, MyApplication.FLURRY_KEY);
	}
	
	
	
	/**
	 * onPause
	 */
	@Override
	public void onPause() {
	    super.onPause();

	    try {
	        Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(web, (Object[]) null);
	    } catch(ClassNotFoundException cnfe) {
	    } catch(NoSuchMethodException nsme) {
	    } catch(InvocationTargetException ite) {
	    } catch (IllegalAccessException iae) {
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
	 * onResume
	 */
	@Override
	public void onResume() {
	    super.onResume();

	    try {
	        Class.forName("android.webkit.WebView").getMethod("onResume", (Class[]) null).invoke(web, (Object[]) null);
	    } catch(ClassNotFoundException cnfe) {
	    } catch(NoSuchMethodException nsme) {
	    } catch(InvocationTargetException ite) {
	    } catch (IllegalAccessException iae) {
	    }
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
	 * Al pulsar el botón back vuelve a la noticia anterior o a la aplicación si no hay historial
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Hay historial?
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
	        web.goBack();
	        return true;
	    }
	    // Si no es el botón back o no hay historial
	    return super.onKeyDown(keyCode, event);
	}
	
	
	
	/**
	 * onSaveInstanceState
	 * Guardamos el estado del webview
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		web.saveState(savedInstanceState);
	}

	/**
	 * onRestoreInstanceState
	 * Restauramos el estado del webview
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		web.restoreState(savedInstanceState);
	}

	/**
	 * onConfigurationChanged
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                CLASES                                                             |
	// -----------------------------------------------------------------------------------------------------
	
	/**
	 * WebViewClient
	 *
	 */
	private class MyWebViewClient extends WebViewClient {
		// No permitimos que llame a otro navegador
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	return false;
	    }
	    
	    // Mostramos el icono de cancelar
	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap facIcon) {
	         is_loading = true;
	         prog_loading.setVisibility(View.VISIBLE);
	         img_cancel.setImageResource(R.drawable.equis_negra);
	     }

	    // Mostramos el icono de refrescar
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	is_loading = false;
	    	prog_loading.setVisibility(View.INVISIBLE) ;
	    	img_cancel.setImageResource(R.drawable.recargar_negro);
	    	
	         if (view.canGoBack()){
	        	 img_back.setImageResource(R.drawable.flecha_atras_negra);
	         }
	         else{
	        	 img_back.setImageResource(R.drawable.flecha_atras_gris);
	         }
	         
	         if (view.canGoForward()){
	        	 img_forward.setImageResource(R.drawable.flecha_adelante_negra);
	         }
	         else{
	        	 img_forward.setImageResource(R.drawable.flecha_adelante_gris);
	         }
	     }
	}
}
