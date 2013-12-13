package com.bizeu.escandaloh.util;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.bizeu.escandaloh.MyApplication;

public class Screen {

	
	/**
	 * Devuelve el alto de pantalla disponible en píxeles: screen height - (status bar height + action bar height) - tabs height
	 * @return
	 */
	public static int getAvailableHeightScreen(Context context){
		
		int screen_height = 0;
		int available_height = 0;	

		// Screen height
		DisplayMetrics display = context.getResources().getDisplayMetrics();
        screen_height = display.heightPixels;

        // Available height
		available_height = screen_height - getActionBarHeight(context) - getStatusBarHeight(context) - MyApplication.TABS_HEIGHT ;
		
		return available_height;
	}
	
	
	
	/**
	 * Devuelve el alto del status bar
	 * @return
	 */
	public static int getStatusBarHeight(Context context){
		int status_bar_height = 0;
		
		int resourceId = context.getResources().getIdentifier("status_bar_height",
				"dimen", "android");	
		if (resourceId > 0) {
			status_bar_height = context.getResources().getDimensionPixelSize(resourceId);
		}
		
		return status_bar_height;
	}
	
	
	
	
	/**
	 * Devuelve el alto del action bar
	 * @return
	 */
	public static int getActionBarHeight(Context context){
		TypedValue tv = new TypedValue();
		int action_bar_height = 0;
		
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
           if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        	   action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
        }
        else if(context.getApplicationContext().getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true)){
        	action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
        }
        
		return action_bar_height;
	}
	
	
}
