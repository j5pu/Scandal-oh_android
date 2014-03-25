package com.bizeu.escandaloh.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

public class Utils {
	
	/**
	 * Limita un string a un nº de caracteres + tres puntos suspensivos
	 * 
	 * @param completo String original
	 * @param num_caracteres Número de caracteres máximo que podrá contener
	 * @return String con un tamaño máximo de num_caracteres caracteres
	 */
	public static String limitaCaracteres(String completo, int num_caracteres) {
		String acortado = null;
		if (completo.length() > num_caracteres) {
			acortado = completo.substring(0, num_caracteres - 3) + "...";
		} else {
			acortado = completo;
		}

		return acortado;
	}
	
	
	/**
	 * Convierte dp en pixel
	 * @param dp
	 * @param context
	 * @return
	 */
    public static int dpToPx(int dp, Context context){
        context.getResources();
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    
    
    /**
     * Obtiene la fecha actual
     * @return
     */
    public static String getCurrentDate(){  	
    	String format = "dd-MM-yyyy";
    	SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
    	Date date = new Date();
    	return sdf.format(date);
    }

    
    
	/**
	 * Comprueba si el dispositivo dispone de cámara
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
}
