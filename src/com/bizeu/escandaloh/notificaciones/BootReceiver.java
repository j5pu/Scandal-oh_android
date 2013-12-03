package com.bizeu.escandaloh.notificaciones;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	private SharedPreferences preferenciasComprobar;
	static AlarmManager alarmaComprobar ;
	int tiempoComprobacion ;

	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		  //Comprobamos que es arrancada desde el sistema (redudante)
	      if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {    	 
	    	  preferenciasComprobar = PreferenceManager.getDefaultSharedPreferences(context);		
	  	      tiempoComprobacion = Integer.parseInt(preferenciasComprobar.getString("tiempoComprobacion", "30"));
	  		
	  	      activaAlarma(context,tiempoComprobacion);	  	 
	      	}
	}
	  		
	  		
	 /**
	  * Detiene la alarma 
	  * @param context contexto
	 */
	  public static void paraAlarma(Context context) {
		  /*
		  alarmaComprobar = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		  Intent i = new Intent(context, DBUpdateCheckService.class);
		  PendingIntent pi = PendingIntent.getService(context, 354, i, PendingIntent.FLAG_NO_CREATE);
	  			
		  if (pi != null){  // nos aseguramos de que esta activa la alarma: comprobamos si existe el pending intent
			  alarmaComprobar.cancel(pi);
	  		}
	  		*/
	  	}
	  		
	  		
	  		
	  		
	  		
	  		
	  		
	  /**
	   * Activa la alarma
	   * @param context contexto
	   * @param tiempoComp tiempo de comprobacion de tweets introducido por el usuario en las preferencias
	   */
	  public static void activaAlarma(Context context, int tiempoComp) {	
		  /*
		  alarmaComprobar = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		  Intent myIntent = new Intent(context, DBUpdateCheckService.class);
	  			
		  PendingIntent pendingIntent = PendingIntent.getService(context, 354, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	  		 
		  Calendar calendar = Calendar.getInstance();
		  calendar.setTimeInMillis(System.currentTimeMillis());
		  calendar.add(Calendar.SECOND, tiempoComp);
		  // Tiempo de comprobación: cada semana
		  // intervalMillis = meses * 2592000 (un mes) * 1000 (ms)
		  alarmaComprobar.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), tiempoComp * 2592000 * 1000 , pendingIntent);	
	  		*/
	  }
		


}

