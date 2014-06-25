package com.bizeu.escandaloh.notifications;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.bizeu.escandaloh.CoverActivity;
import com.bizeu.escandaloh.MainActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class PushReceiver extends BroadcastReceiver {

	
	// -----------------------------------------------------------------------------------------------------
	// |                                    VARIABLES                                                      |
	// -----------------------------------------------------------------------------------------------------
	
	
	public final static int NOTIFICATION_ID = 5;
	public final static String PUSH_NOTIFICATION = "push_notification";
	
	private String message;
	private String num_notificaciones;
	ArrayList<String> notis = new ArrayList<String>();
	
	
	
	// -----------------------------------------------------------------------------------------------------
	// |                                    MÉTODOS PUSHRECEIVER                                           |
	// -----------------------------------------------------------------------------------------------------
	
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		try {
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			if (json.has("message")){
				JSONArray messagesJson = json.getJSONArray("message");
	            for (int i=0; i<messagesJson.length(); i++){
	                notis.add(messagesJson.getString(i));
	            }
			}
				
			if (json.has("numNotificaciones")){
				num_notificaciones = json.getString("numNotificaciones");
			}	

			// Creamos el resumen que aparecerá abajo cuando la notificación es grande o como texto cuando la 
			// notificación es pequeña
			String summary = null;
			if (num_notificaciones.equals("1")){
				summary = num_notificaciones + " " + context.getResources().getString(R.string.nuevo_aviso);			
			}
			else{
				summary = num_notificaciones + " " + context.getResources().getString(R.string.nuevos_avisos);	
			}
				
			NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.logo_blanco)
		        .setContentTitle(context.getResources().getString(R.string.scandaloh))
				.setContentText(summary)
				.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
				.setTicker(summary)
				.setAutoCancel(true)
				.setLights(0xb21b72, 1000, 2000);
				
			// Configuración para la notificación grande (cuando está la primera de la lista)
		    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
	      
		    inboxStyle.setBigContentTitle(context.getResources().getString(R.string.scandaloh));
		    for (int i=0; i < notis.size(); i++) {
		       inboxStyle.addLine(notis.get(i));
		    }

		    inboxStyle.setSummaryText(summary); // Resumen		      
		    mBuilder.setStyle(inboxStyle);
			
			// Intent al pulsar la notificación
			Intent resultIntent = new Intent(context, CoverActivity.class);
			resultIntent.setAction(PUSH_NOTIFICATION);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			         );
			mBuilder.setContentIntent(resultPendingIntent);
			
			// Mandamos la notificación
			NotificationManager mNotificationManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
			
			// Si tenemos la pantalla del carrusel abierta actualizamos el nº de notificaciones
			if (MainActivity.activity_is_showing){
				MainActivity.updateNumNotifications();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
