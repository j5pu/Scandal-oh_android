package com.bizeu.escandaloh;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.bizeu.escandaloh.util.AudioRecorder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class RecordAudioDialog extends Dialog {

	private TextView txt_meter;
	private Button but_aceptar;
	private Button but_cancelar;
	
	// private Activity c;
	private Context c;
	private Dialog d;
	private Button but_record;
	private boolean recording = false;
	private Timer mTimer;
	private int contador = 0;
	private Handler myHandler ;
	private Runnable myRunnable;
	private boolean recorded = false;
	
	private AudioRecorder audio_recorder;
	OnMyDialogResult mDialogResult; // the callback

	public RecordAudioDialog(Context con, AudioRecorder record) {
		super(con);
		audio_recorder = record;
		// this.c = a;
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
			}
		});
		
		but_aceptar = (Button) findViewById(R.id.but_subir_foto_audio);
		but_aceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mDialogResult != null ){
	                mDialogResult.finish("OK");
	            }
	            RecordAudioDialog.this.dismiss();
			}
		});
		
		but_record = (Button) findViewById(R.id.but_record_stop);
		but_record.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!recording){
					if (!recorded){
						recording = true;
						but_record.setText("Parar de grabar");	
						try {
							audio_recorder.start();
						} catch (IOException e) {
							Log.e("WE","error grabando audio");
							e.printStackTrace();
						}
					}
					else{
						audio_recorder.startPlaying();		
					}
				} 
				else{
					recording = false;
					recorded = true;
					but_record.setText("Reproducir");
					try {
						audio_recorder.stop();
					} catch (IOException e) {
						Log.e("WE","Error parando el audio");
						e.printStackTrace();
					}
				}
			}
		});
		
		
		
		// Contador de segundos			
		txt_meter = (TextView) findViewById(R.id.txt_count_time_record);
		
		myRunnable = new Runnable() {
			   public void run() {
				   if (recording){
					   contador++;
					   if (contador < 10){
						   txt_meter.setText("00:0" + contador);
					   }
					   else{
						   txt_meter.setText("00:" + contador);
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
