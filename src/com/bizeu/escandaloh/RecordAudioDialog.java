package com.bizeu.escandaloh;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Audio.PlayListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class RecordAudioDialog extends Dialog{

	private TextView txt_meter;
	private Button but_subir;
	private Button but_cancelar;
	private Button but_reproducir;
	private Button but_record;

	private boolean recording ;
	private Timer mTimer;
	private int contador = 20;
	private Handler myHandler ;
	private Runnable myRunnable;
	private boolean recorded = false;
	OnMyDialogResult mDialogResult; // the callback

	public RecordAudioDialog(Context con, Audio record) {
		super(con);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.record_audio);
		
		but_cancelar = (Button) findViewById(R.id.but_cancelar_foto_audio);
		but_cancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				Audio.getInstance().closeAudio();
			}
		});
		
		but_subir = (Button) findViewById(R.id.but_subir_foto_audio);
		but_subir.setOnClickListener(new View.OnClickListener() {
			
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
		
		but_record = (Button) findViewById(R.id.but_record);
		but_record.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Si no está reproduciendo
				if (!Audio.getInstance().isPlaying()){
					Log.v("WE","No esta reproduciendo");
					// Si está grabando: parar de grabar
					if (Audio.getInstance().isRecording()){
						Log.v("WE","Esta grabando");
						try {
							Audio.getInstance().stop_recording();
						} catch (IOException e) {
							Log.e("WE","Error parando el audio");
							e.printStackTrace();
						}				
						contador = 20;
						but_record.setText("Grabar");
						but_subir.setVisibility(View.VISIBLE);	
						but_reproducir.setVisibility(View.VISIBLE);
						but_subir.setVisibility(View.VISIBLE);
					}
					// No está grabando: comienza a grabar
					else{
						Log.v("WE","No esta grabando");
						txt_meter.setText(Integer.toString(contador));
						but_record.setText("Parar de grabar");	

						try {
							Audio.getInstance().start_recording();
						} catch (IOException e) {
							Log.e("WE","error grabando audio");
							e.printStackTrace();
						}
						but_reproducir.setVisibility(View.GONE);
						but_subir.setVisibility(View.GONE);
					}
				}
				else{
					Log.v("WE","Esta reproduciendo");
				}
			}
		});
		
		but_reproducir = (Button) findViewById(R.id.but_reproducir);
		but_reproducir.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Audio.getInstance().isPlaying()){
					Audio.getInstance().stopPlaying();
					but_reproducir.setText("Reproducir");
					but_record.setVisibility(View.VISIBLE);
				}
				else{
					Audio.getInstance().startPlaying();	
					but_record.setVisibility(View.GONE);
					but_reproducir.setText("Parar");
				}
			}
		});
		
		
		
		// Contador de segundos			
		txt_meter = (TextView) findViewById(R.id.txt_count_time_record);
		
		myRunnable = new Runnable() {
			   public void run() {
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
						   but_record.setText("Reproducir");
						   txt_meter.setText("0");
						   but_subir.setVisibility(View.VISIBLE);	  
						   recording = false;
						   recorded = true;
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
		
		
		Audio.getInstance().setDataDownloadListener(new PlayListener(){
		    @SuppressWarnings("unchecked")	   
		    @Override
		    public void onPlayFinished() {
		    	but_record.setVisibility(View.VISIBLE);
		    	but_reproducir.setText("Reproducir");
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


    
    
    
  
	
	
}
