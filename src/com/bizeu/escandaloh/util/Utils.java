package com.bizeu.escandaloh.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class Utils {
	
	
	
	/**
	 * Crea un archivo para una foto en la carpeta de ScandalOh
	 * @param context Contexto
	 * @return File o nulo si hubo algún error creando el archivo
	 */
	public static File createPhotoScandalOh(Context context) {
		
		File photo = null;
		
		// Obtenemos (o creamos) la carpeta ScandalOh
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath() + "/ScandalOh/ScandalOh Images/");
		if (!scandaloh_dir.exists()) {
			scandaloh_dir.mkdirs();
		}
		
		// Creamos el archivo
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "IMG_" + timeStamp + "_";
		try {
			photo = File.createTempFile(imageFileName, ".jpg", scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(context, R.string.hubo_algun_error_creando_archivo, Toast.LENGTH_LONG);
			toast.show();
		}

		return photo;
	}
	
	
	
	
	/**
	 * Crea un archivo para una foto de perfil en la carpeta de ScandalOh
	 * @param context Contexto
	 * @return File o nulo si hubo algún error creando el archivo
	 */
	public static File createProfilePhotoScandalOh(Context context) {
		
		File photo = null;
		
		// Obtenemos (o creamos) la carpeta ScandalOh
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath() + "/ScandalOh/ScandalOh Profile Photos/");
		if (!scandaloh_dir.exists()) {
			scandaloh_dir.mkdirs();
		}
		
		// Creamos el archivo
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "IMG_" + timeStamp + "_";
		try {
			photo = File.createTempFile(imageFileName, ".jpg", scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(context, R.string.hubo_algun_error_creando_archivo, Toast.LENGTH_LONG);
			toast.show();
		}

		return photo;
	}
	
	
	
	
	
	
	/**
	 * Crea un archivo para un audio en la carpeta de ScandalOh
	 * @param context Contexto
	 * @return File o nulo si hubo algún error creando el archivo
	 */
	public static File createAudioScandalOh(Context context) {
		
		File photo = null;
		
		// Obtenemos (o creamos) la carpeta ScandalOh
		File scandaloh_dir = Environment.getExternalStorageDirectory();
		scandaloh_dir = new File(scandaloh_dir.getAbsolutePath() + "/ScandalOh/ScandalOh Audio/");
		if (!scandaloh_dir.exists()) {
			scandaloh_dir.mkdirs();
		}
		
		// Creamos el archivo
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "AUD_" + timeStamp + "_";
		try {
			photo = File.createTempFile(imageFileName, ".3gp", scandaloh_dir);
		} catch (IOException e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(context, R.string.hubo_algun_error_creando_archivo, Toast.LENGTH_LONG);
			toast.show();
		}

		return photo;
	}
	
	
	
	/**
	 * Comprueba si el almacenamiento externo está disponible para lectura y escritura
	 * @param Context contexto
	 * @return true si está disponible, false en caso contrario
	 */
	public static boolean isExternalStorageWritable(Context context) {
	    String state = Environment.getExternalStorageState();
	    
	    // El sistema no está montado porque el dispositivo está en modo almacenamiento masivo
	    if (state.equals(Environment.MEDIA_SHARED)){
	    	Toast toast = Toast.makeText(context,R.string.desconecta_el_modo_multimedia,Toast.LENGTH_LONG);
			toast.show();
			return false;
	    }
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	
	/**
	 * Añade un archivo a la galeria
	 * @param path Path donde se encuentra el archivo
	 * @param context Contexto
	 */
	public static void galleryAddPic(String path, Context context) {
	    File f = new File(path);
	    Uri contentUri = Uri.fromFile(f);
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    mediaScanIntent.setData(contentUri);
	    context.sendBroadcast(mediaScanIntent);
	}
	
	
	
	
	
	
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
     * Convierte de px a dp
     * @param px
     * @param context
     * @return
     */
    public static float pxToDp(float px, Context context)
    {
        return px / context.getResources().getDisplayMetrics().density;
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
	 * Cambia el formato de la fecha
	 * @param old_date Fecha en formato "AAAA-MM-DDTHH:MM:SS"
	 * @return fecha en formato "DD-MM-AAAA"
	 */
	public static String changeDateFormat(String old_date){
        // La fecha tendrá el formato: dd-mm-aaaa
        String date_without_time = (old_date.split("T",2))[0];   
        String year = date_without_time.split("-",3)[0];
        String month = date_without_time.split("-",3)[1];
        String day = date_without_time.split("-",3)[2];
        String final_date = day + "-" + month + "-" + year;
        return final_date;
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
