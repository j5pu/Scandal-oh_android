package com.bizeu.escandaloh.util;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class Audio{

	PlayListener playListener = null;
	private MediaRecorder mRecord;
	private MediaPlayer  mPlayer;
	private boolean is_recording = false;
	private String path = null;
	private static Audio singleton;
	private static boolean yaCreado = false; // Este atributo nos dice si ya fue creada o no una instancia de esta clase
	
	
	public void setDataDownloadListener(PlayListener dataDownloadListener) {
        this.playListener = dataDownloadListener;
    }
	
	public static interface PlayListener {
        void onPlayFinished();
    }
	
	/**
	 * Constructor
	 */
	private Audio() {
		
		// Inicializamos el grabador
		this.path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_record_scandaloh.3gp";
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try {
				throw new IOException("SD Card is not mounted.  It is " + state
						+ ".");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) {
			try {
				throw new IOException("Path to file could not be created.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		mRecord = new MediaRecorder();
		
		// Inicializamos el reproductor 
		mPlayer = new MediaPlayer();
				
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
		//recorder.setMaxDuration(20000);
		mRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecord.setOutputFile(path);
		try {
			mRecord.prepare();
        } catch (IOException e) {
            Log.e("WE", "prepare() record audio failed");
        }

		mRecord.start();
		is_recording = true;
	}

	
	
	/**
	 * Para de grabar
	 * @throws IOException
	 */
	public void stop_recording() throws IOException {
		mRecord.stop();
		is_recording = false;
	}
	
	
	public boolean isRecording(){
		return is_recording;
	}
	
	
	
	/**
	 * Reproduce el audio recién grabado
	 */
	public void startPlaying() {
        if (path != null){
            try {
                mPlayer.setDataSource(path);
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.stop();
						mp.reset();	
						if (playListener != null){
							playListener.onPlayFinished();
						}
						
					}
				});
            
            } catch (IOException e) {
                Log.e("WE", "Error al reproducir el audio");
            }
        }
        else{
        	Log.e("WE","Debes usar start_recording antes de usar este método");
        }
    }

	
	
	
	/**
	 * Reproduce un audio a partir de una URL
	 * @param uri_audio. URL donde se encuentra el audio
	 */
	public void startPlaying(String uri_audio) {
        
        		try {
        			if (mPlayer.isPlaying()){
        				mPlayer.reset();
        				mPlayer.release();
        			}
        			mPlayer.setDataSource(uri_audio);
        			 mPlayer.setOnPreparedListener(new OnPreparedListener() {
         				
         				@Override
         				public void onPrepared(MediaPlayer mp) {
         					mp.start();	
         					mp.setOnCompletionListener(new OnCompletionListener() {
         						
         						@Override
         						public void onCompletion(MediaPlayer mp) {
         							Log.v("WE","Ha terminado");
         							//mp.stop();
         							mp.reset();
         							if (playListener != null){
         								playListener.onPlayFinished();
         							}
         							
         						}
         					});					
         				}
        			 }); 
                    mPlayer.prepareAsync();
                   
        			                
    
                 } catch (IOException e) {
                    Log.e("WE", "error al reproducir el audio");
                 }
        		
            
            
    }
	
	
	/**
	 * Para de reproducir un audio
	 */
	public void stopPlaying(){
		if (mPlayer.isPlaying()){
			mPlayer.stop();
			mPlayer.reset();
		}
	}
	
	
	public boolean isPlaying(){
		return mPlayer.isPlaying();
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
		if (mRecord != null){
			mRecord.reset();
			mRecord.release();
			mRecord = null;
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
		
		yaCreado = false;
	}
	
	
	

}
