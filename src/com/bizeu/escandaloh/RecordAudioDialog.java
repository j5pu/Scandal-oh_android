package com.bizeu.escandaloh;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Audio.PlayListener;

public class RecordAudioDialog extends Dialog{

	private TextView txt_meter;
	private Button but_reproducir; // Botón de empezar a reproducir
	private Button but_abajo; // Botón de grabar y parar (tanto grabar como reproducir)
	private TextView txt_subir_sin_audio;
	private TextView txt_seg;
	private ImageView img_subir_foto;
	private TextView txt_description;
	private LinearLayout ll_espacio_botones;

	private Timer mTimer;
	private int contador = 20;
	private int contador_play = 0;
	private int ultimo_tiempo_guardado = 0;
	private Handler myHandler ;
	private Runnable myRunnable;
	OnMyDialogResult mDialogResult; 

	public RecordAudioDialog(Context con, Audio record) {
		super(con);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.record_audio);
		
		ll_espacio_botones = (LinearLayout) findViewById(R.id.LL_record_audio_espacio_botones);
		txt_description = (TextView) findViewById(R.id.txt_recordar_descripcion);
		txt_seg = (TextView) findViewById(R.id.txt_seg);
		txt_subir_sin_audio = (TextView) findViewById(R.id.txt_cancelar_foto_audio);
		txt_subir_sin_audio.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				Audio.getInstance().closeAudio();	
				
				if (Audio.getInstance().isPlaying()){
					Audio.getInstance().stopPlaying();
				}
				if( mDialogResult != null ){
	                mDialogResult.finish("CANCELED");
	            }
	            RecordAudioDialog.this.dismiss();
			}
		});
		
		img_subir_foto = (ImageView) findViewById(R.id.img_subir_foto_audio);
		img_subir_foto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Audio.getInstance().isPlaying()){
					Audio.getInstance().stopPlaying();
				}
				if( mDialogResult != null ){
	                mDialogResult.finish("OK");
	            }
	            RecordAudioDialog.this.dismiss();			
			}
		});
		
		but_abajo = (Button) findViewById(R.id.but_recordar_enviar);
		but_abajo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// Si está reproduciendo: paramos de reproducir
				if (Audio.getInstance().isPlaying()){
					
					Audio.getInstance().stopPlaying();
					
					// Acualizamos la interfaz
					changeIUPlayFinised();
				}
				
				// Si no está reproduciendo, comprobamos si está grabando o no
				else if(!Audio.getInstance().isPlaying()){
					
					// Si está grabando: parar de grabar
					if (Audio.getInstance().isRecording()){
						try {
							Audio.getInstance().stop_recording();
						} catch (IOException e) {
							Log.e("WE","Error parando el audio");
							e.printStackTrace();
						}		
											
						// Actualizamos la IU para mostrar el tiempo de grabación y el botón de reproducir
						ll_espacio_botones.setVisibility(View.VISIBLE);
						but_abajo.setText("Grabar");
						img_subir_foto.setVisibility(View.VISIBLE);	
						but_reproducir.setVisibility(View.VISIBLE);
						txt_description.setText("Audio grabado:");
						txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F);
						txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F);
						int recorded_seconds = 20 - contador;
						String recorded_seconds_string;
						if (recorded_seconds < 10){
							recorded_seconds_string = "00:0" + recorded_seconds;
						}
						else{
							recorded_seconds_string = "00:" + recorded_seconds;
						}
						// Almacenamos el tiempo grabado
						ultimo_tiempo_guardado = recorded_seconds;
						txt_meter.setText(recorded_seconds_string);
						contador = 20;
						contador_play = 0;
					}
					
					// No está grabando: comienza a grabar
					else{				
						Log.v("WE","No esta grabando");
						
						// Actualizamos la IU para mostrar el tiempo más grande y quitar el botón de reproducir
						ll_espacio_botones.setVisibility(View.GONE);
						txt_description.setText("Grabando audio...");
						txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 41F);
						txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 61f);
						txt_meter.setText("00:" + Integer.toString(contador));
						but_abajo.setText("Parar");	

						try {
							Audio.getInstance().start_recording();
						} catch (IOException e) {
							Log.e("WE","error grabando audio");
							e.printStackTrace();
						}
						but_reproducir.setVisibility(View.GONE);
						img_subir_foto.setVisibility(View.GONE);
					}
				}
			}
		});
		
		but_reproducir = (Button) findViewById(R.id.but_reproducir);
		but_reproducir.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Reseteamos el contador de reproducción
				contador_play = 0;
					
				// Cambios IU
				ll_espacio_botones.setVisibility(View.VISIBLE);
				txt_description.setText("Reproduciendo...");
				txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 41F);
				txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 61f);
				txt_meter.setText("00:00");	
				// Quitamos el botón de reproducir
				but_reproducir.setVisibility(View.GONE);
				// El boton de abajo ahora pondrá Parar
				but_abajo.setText("Parar");
					
				Audio.getInstance().startPlaying();	
			}
		});
		
		
		
		// Contador de segundos			
		txt_meter = (TextView) findViewById(R.id.txt_count_time_record);
		
		myRunnable = new Runnable() {
			   public void run() {
				   // Está grabando: decrementamos el secundero cada segundo
				   if (Audio.getInstance().isRecording()){
					   contador--;
					   if (contador > 0){
						   if (contador < 10){
							   txt_meter.setText("00:0" + contador);
						   }
						   else{
							   txt_meter.setText("00:" + contador);
						   }   					   
					   }
					   else if (contador == 0){
				    	   try {
								Audio.getInstance().stop_recording();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Hacemos los cambios necesarios a la IU
							ll_espacio_botones.setVisibility(View.VISIBLE);
							but_abajo.setText("Grabar");
							img_subir_foto.setVisibility(View.VISIBLE);	
							but_reproducir.setVisibility(View.VISIBLE);
							txt_description.setText("Audio grabado:");
							txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F);
							txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F);
							int recorded_seconds = 20 - contador;
							String recorded_seconds_string;
							if (recorded_seconds < 10){
								recorded_seconds_string = "00:0" + recorded_seconds;
							}
							else{
								recorded_seconds_string = "00:" + recorded_seconds;
							}
							// Almacenamos el tiempo grabado
							ultimo_tiempo_guardado = recorded_seconds;
							txt_meter.setText(recorded_seconds_string);
							contador = 20;
					   }						  
				   }
				   
				   // Está reproduciendo: incrementamos el secundero cada segundo
				   else if (Audio.getInstance().isPlaying()){
					   contador_play++;
					   
					   // Si aún hay más que reproducir
					   if (contador_play < ultimo_tiempo_guardado + 1){
						   if (contador_play < 10){
							   txt_meter.setText("00:0" + contador_play);
						   }
						   else{
							   txt_meter.setText("00:" + contador_play);
						   }
					   }
					   
					   // Fin de la reproducción
					   else{
						   Log.v("WE","Entra en else malo y contador_play vale: " + contador_play + "y ultimo_tiemop: " + ultimo_tiempo_guardado);
						   Audio.getInstance().stopPlaying();

						   // Cambios IU
						   changeIUPlayFinised();
					   }
				   }
			   }
			};
			
		myHandler = new Handler();

		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateMeter();
			}
		}, 0, 1000);
		
		
		Audio.getInstance().setOnPlayListener(new PlayListener(){
		    @SuppressWarnings("unchecked")	   
		    @Override
		    public void onPlayFinished() {
		    	// Actualizamos la interfaz
		    	changeIUPlayFinised();
		    }
		});
		
	}

	
	/**
	 * Refresca el contador de segundos en la pantalla
	 */
	private void updateMeter() {
		   myHandler.post(myRunnable);
	}
	
    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
       void finish(String result);
    }


    /**
     * Cambia la interfaz de usuario al terminar de reproducir un audio
     */
    private void changeIUPlayFinised(){
    	
    	// Quitamos el hueco en medio
		ll_espacio_botones.setVisibility(View.GONE);
		
		// Actualizamos descripción
		txt_description.setText("Audio grabado:");
		
		// Disminuimos tamaño del tiempo
		txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F);
		txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F);
		
		// Indicamos el tiempo de grabación total en el tiempo
		String recorded_seconds_string;
		if (ultimo_tiempo_guardado < 10){
			recorded_seconds_string = "00:0" + ultimo_tiempo_guardado;
		}
		else{
			recorded_seconds_string = "00:" + ultimo_tiempo_guardado;
		}
		txt_meter.setText(recorded_seconds_string);
		
		// Mostramos el botón de reproducir
		but_reproducir.setVisibility(View.VISIBLE);
		
		// El botón de abajo indicará Grabar
		but_abajo.setText("Grabar");
		
		// Reseteamos contador de reproducción
		contador_play = 0;
    }
    
  
	
	
}
