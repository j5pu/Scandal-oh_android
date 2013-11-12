package com.bizeu.escandaloh;

import android.app.Application;

public class MyApplication extends Application {
	public static int ALTO_TABS;
	public static String SERVER_ADDRESS;
	
	@Override
	public void onCreate() {
		super.onCreate();
		SERVER_ADDRESS = "http://ec2-23-22-159-14.compute-1.amazonaws.com/" ;
		//SERVER_ADDRESS = "http://192.168.1.31:8000/";
	}
}
