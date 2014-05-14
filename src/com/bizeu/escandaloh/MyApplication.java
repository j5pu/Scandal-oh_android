package com.bizeu.escandaloh;

import java.util.ArrayList;
import android.app.Application;
import android.util.Log;
import com.parse.Parse;
import com.parse.PushService;

public class MyApplication extends Application {
	public static int ALTO_TABS;

	public static String SERVER_ADDRESS = "http://host.scandaloh.com"; 	                     // Producción
	//public static String SERVER_ADDRESS = "http://ec2-54-225-46-222.compute-1.amazonaws.com" ; 	 // Desarrollo
	//public static String SERVER_ADDRESS = "http://192.168.1.111:8001"; 	                     // Local
	
	public static String DIRECCION_BUCKET = "http://scandaloh.s3.amazonaws.com/";
	public static String CODE_COUNTRY = "Code country";
	public static String USER_URI = "user_uri";
	public static String SESSION_TOKEN = "session_token";
	public static String USER_NAME = "user_name";
	public static String AUTOPLAY_ACTIVATED = "autoplay_activated";
	public static String NOTIFICATIONS_ACTIVATED = "notifications_activated";
	public static String AVATAR = "avatar";
	public static String SOCIAL_NETWORK = "social_network";
	public static Boolean logged_user = false;
	public static String code_selected_country = null;
	public static String resource_uri = "";
	public static String device_token;
	public static String user_name = "";
	public static String session_token = "";
	public static String avatar = null;
	public static String FLURRY_KEY = "VQ28Y4ZX5QCXQ9NS6TJS";
	public static int social_network;
	public static boolean FIRST_TIME_HAPPY = true;
	public static boolean FIRST_TIME_ANGRY = true;
	public static boolean FIRST_TIME_BOTH = true;
	public static boolean TIMEOUT_PHOTO_SHOWN = false;
	public static boolean PHOTO_CLICKED = false;
	public static boolean reset_scandals = false;
	public static int ACTION_BAR_WITH_TABS_HEIGHT;
	public static ArrayList<String> FILES_TO_DELETE = new ArrayList<String>();
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Parse
		Parse.initialize(this, "d0pwm3XnQrHZCrTmQuuE75SXrzr46001WWfH9uFv", "YGONYEJpspQOod64lA5xwkGaK4V3GgAnq1kHNpD2");
		PushService.setDefaultPushCallback(this, MainActivity.class);


	}
}