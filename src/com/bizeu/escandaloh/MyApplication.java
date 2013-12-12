package com.bizeu.escandaloh;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {
	public static int ALTO_TABS;
	//public static String SERVER_ADDRESS = "http://ec2-23-22-159-14.compute-1.amazonaws.com/" ;
	public static String SERVER_ADDRESS = "http://192.168.1.14:8000/";
	public static String CODE_COUNTRY = "Code country";
	public static String USER_URI = "user_uri";
	public static Boolean logged_user = false;
	public static String code_selected_country = null;
	public static String resource_uri = "";
	public static boolean FIRST_TIME_HAPPY = true;
	public static boolean FIRST_TIME_ANGRY = true;
	public static boolean FIRST_TIME_BOTH = true;
	public static boolean TIMEOUT_PHOTO_SHOWN = false;
	public static boolean PHOTO_CLICKED = false;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
}
