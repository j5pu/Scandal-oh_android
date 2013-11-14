package com.bizeu.escandaloh;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {
	public static int ALTO_TABS;
	//public static String SERVER_ADDRESS = "http://ec2-23-22-159-14.compute-1.amazonaws.com/" ;
	public static String SERVER_ADDRESS = "http://192.168.1.49:8000/";
	public static String CODE_COUNTRY = "Code country";
	public static String CODE_SELECTED_COUNTRY = null;
	public static String USER_URI = "user_uri";
	public static Boolean LOGGED_USER = false;


	
	@Override
	public void onCreate() {
		super.onCreate();
	}
}
