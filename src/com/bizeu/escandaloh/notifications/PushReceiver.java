package com.bizeu.escandaloh.notifications;

import org.json.JSONException;
import org.json.JSONObject;

import com.bizeu.escandaloh.MainActivity;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver {

	final static int NOTIFICATION_ID = 5;
	
	private String message;
	private String photo_id;
	private String num_notificaciones;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			if (json.has("message")){
                message = json.getString("message");
			}
				
			if (json.has("photoID")){
				photo_id = json.getString("photoID");
			}
			
			if (json.has("numNotificaciones")){
				num_notificaciones = json.getString("numNotificaciones");
			}		
			
			// Resumen
			// Creamos el resumen que aparecerá abajo cuando la notificación es grande o como texto cuando la 
			// notificación es pequeña
			String summary = null;
			if (num_notificaciones.equals("1")){
				summary = num_notificaciones + " nuevos aviso.";			
			}
			else{
				summary = num_notificaciones + " nuevos avisos.";
				
			}
			
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(context)
			        .setSmallIcon(R.drawable.logo)
			        .setContentTitle(context.getResources().getString(R.string.scandaloh))
					.setContentText(summary)
					.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
					.setTicker(summary)
					.setAutoCancel(true)
					.setLights(0xb21b72, 3000, 3000);
			
			// Configuración para la notificación grande (cuando está la primera de la lista)
		    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

		    String[] events = new String[6];
		    events[0] = new String("ghghbhbh....");
		    events[1] = new String("This is second line...");
		    events[2] = new String("This is third line...");
		    events[3] = new String("This is 4th line...");
		    events[4] = new String("This is 5th line...");
		    events[5] = new String("This is 6th line...");
	      
		    inboxStyle.setBigContentTitle(context.getResources().getString(R.string.scandaloh));
		    for (int i=0; i < events.length; i++) {
		       inboxStyle.addLine(events[i]);
		    }

		    inboxStyle.setSummaryText(summary); // Resumen		      
		    mBuilder.setStyle(inboxStyle);
			
			// Intent al pulsar la notificación
			Intent resultIntent = new Intent(context, NotificationsActivity.class);

			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager =
			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
