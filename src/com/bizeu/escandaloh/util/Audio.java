package com.bizeu.escandaloh.util;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class Audio{

	private MediaRecorder recorder = null;
	private MediaPlayer   mPlayer = null;
	private String path = null;
	private static Audio singleton;
	private static boolean yaCreado = false; // Este atributo nos dice si ya fue creada o no una instancia de esta clase

	

	
	/**
	 * Creates a new audio recording at the given path (relative to root of SD
	 * card).
	 */
	private Audio() {
		this.path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_record_scandaloh.3gp";
	}
	
	
	
	/** 
	 * Cuando alguien quiere una instancia de esta clase llama a este método el
	 * cual se fija si ya hay una instancia creada, si no la hay la crea y
	 * finalmente la devuelve a quien lo solicite. 
	*/
	public static Audio getInstance() {
	   if(yaCreado == false) {
		   singleton = new Audio();
		   yaCreado = true;
	   }
	   return singleton;
   }
	
	
	
	/**
	 * Comienza una grabación
	 * @throws IOException
	 */
	public void start_recording() throws IOException {
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is not mounted.  It is " + state
					+ ".");
		}

		// make sure the directory we plan to store the recording in exists
		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Path to file could not be created.");
		}

		recorder = new MediaRecorder();
		recorder.setMaxDuration(20000);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(path);
		
		try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("WE", "prepare() record audio failed");
        }

		recorder.start();
	}

	
	
	/**
	 * Para de grabar
	 * @throws IOException
	 */
	public void stop_recording() throws IOException {
		recorder.stop();
		recorder.release();
		recorder = null;
	}
	
	
	
	
	/**
	 * Reproduce el audio recién grabado
	 */
	public void startPlaying() {
        mPlayer = new MediaPlayer();
        if (path != null){
            try {
                mPlayer.setDataSource(path);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e("WE", "Error al reproducir el audio");
            }
        }
        else{
        	Log.e("WE","Usar start_recording antes de usar este método");
        }
    }

	
	
	
	/**
	 * Reproduce un audio a partir de una URL
	 * @param uri_audio. URL donde se encuentra el audio
	 */
	public void startPlaying(String uri_audio) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(uri_audio);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.start();
					
				}
			});
        } catch (IOException e) {
            Log.e("WE", "error al reproducir el audio");
        }
    }
	
	
	
	/**
	 * Obtiene la ruta del archivo con el audio
	 * @return
	 */
	public String getPath(){
		return path;
	}
	
	
	
	/**
	 * Cierra y libera el audio
	 */
	public void closeAudio(){
		if (recorder != null){
			recorder.reset();
			recorder.release();
			recorder = null;
		}
		
		if (mPlayer != null){
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
		
		// Eliminamos el archivo creado
		if (path != null){
			File file = new File(path);
			file.delete();
		}
	}
}
