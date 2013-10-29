package com.bizeu.escandaloh.model;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class Cache {

	private static Cache singleton;
	private LruCache<String, Bitmap> mMemoryCache;
	private static boolean yaCreado = false; // Ya fue creada o no una instancia de esta clase
	
	private Cache(Context context){
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

	    // Usamos 1/8 de la memoria disponible para esta memoria cache
	    final int cacheSize = 1024 * 1024 * memClass / 8;

	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
	        @Override
	        protected int sizeOf(String key, Bitmap bitmap) {
	            return (bitmap.getRowBytes() * bitmap.getHeight());
	        }
	    };
	}
		
	public static Cache getInstance(Context context) {
		if(yaCreado == false) {
			singleton = new Cache(context);
			yaCreado = true;
		}
		return singleton;
	}
	
	
	
	/**
	 * Añade una imagen a la cache
	 * @param key: Id de la imagen (usamos el string de la URL)
	 * @param bitmap: Imagen
	 */
	public void aniadeImagenAcache(String key, Bitmap bitmap) {
	    if (obtenImagenDeCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	
	/**
	 * Obtiene una imagen desde cache
	 * @return bitmap: Imagen obtenida
	 */
	public Bitmap obtenImagenDeCache(String key) {
	    return mMemoryCache.get(key);
	}
	

}
