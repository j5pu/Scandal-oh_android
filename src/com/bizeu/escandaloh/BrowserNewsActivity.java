package com.bizeu.escandaloh;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bizeu.escandaloh.users.LoginSelectActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class BrowserNewsActivity extends SherlockActivity {

	private WebView web;
	private ImageView img_back;
	private ImageView img_forward;
	private ImageView img_cancel;
	private ImageView img_share;
	
	private String url;
	private boolean is_loading = true; // Nos indica si está cargando una web
	
	
	
	/**
	 * onCreate
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.browser_news);
		
		if (getIntent() != null){
			Intent i = getIntent();
			url = i.getStringExtra("source");
			web = (WebView) findViewById(R.id.wb_browser_noticia);
			web.getSettings().setBuiltInZoomControls(true);
			web.setWebViewClient(new MyWebViewClient());

			web.loadUrl(url);
		}	
		
		// Action Bar
		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM| ActionBar.DISPLAY_SHOW_HOME);
		View view = getLayoutInflater().inflate(R.layout.action_bar_browser, null);
		actBar.setCustomView(view);
		actBar.setHomeButtonEnabled(true);
		actBar.setDisplayHomeAsUpEnabled(true);
		actBar.setIcon(R.drawable.logo_blanco);
		
		img_back = (ImageView) findViewById(R.id.img_browser_back);
		img_forward = (ImageView) findViewById(R.id.img_browser_forward);
		img_cancel = (ImageView) findViewById(R.id.img_actionbarsearch_cancel);
		img_share = (ImageView) findViewById(R.id.img_browser_share);
		
		img_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (web.canGoBack()) {
			        web.goBack();
			    }	
			}
		});
		
		img_forward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (web.canGoForward()){
					web.goForward();
				}	
			}
		});
		
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
		
		img_share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent i;
				
				// Logueado: mostramos pantalla de subir escándalo
				if (MyApplication.logged_user){
					i = new Intent(BrowserNewsActivity.this, CreateScandalohActivity.class);
				}
				
				// No logueado: mostramos pantalla de iniciar sesión
				else{
					i = new Intent(BrowserNewsActivity.this, LoginSelectActivity.class);
				}
				
				// Mantamos la url actual
				WebBackForwardList mWebBackForwardList = web.copyBackForwardList();
				String lastUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()).getUrl();
				i.putExtra("photo_from", CoverActivity.FROM_SHARING_TEXT);
				i.putExtra("shareUri", lastUrl);
				startActivity(i);
			}
		});
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
	         img_cancel.setImageResource(R.drawable.cancel);
	     }

	    // Mostramos el icono de refrescar
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	is_loading = false;
	    	img_cancel.setImageResource(R.drawable.recarga_blanca);
	     }
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
