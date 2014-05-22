package com.bizeu.escandaloh.util;

import java.io.File;
import java.io.IOException;

import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Audio{

	PlayListener playListener = null;
	private MediaRecorder mRecord;
	private MediaPlayer  mPlayer;
	private boolean is_recording = false;
	private String path;
	private static Audio singleton;
	private static boolean yaCreado = false; // Este atributo nos dice si ya fue creada o no una instancia de esta clase
	private Context mContext;
	
	public void setOnPlayListener(PlayListener playListener) {
        this.playListener = playListener;
    }
	
	public static interface PlayListener {
        void onPlayFinished();
        void onPlayPrepared();
    }

	
	/**
	 * Constructor
	 */
	private Audio(Context context) {

		// Inicializamos el grabador
		mRecord = new MediaRecorder();
		
		// Inicializamos el reproductor 
		mPlayer = new MediaPlayer();
		
		mContext = context;
	}
	
	
	
	/** 
	 * Cuando alguien quiere una instancia de esta clase llama a este método el
	 * cual se fija si ya hay una instancia creada, si no la hay la crea y
	 * finalmente la devuelve a quien lo solicite. 
	*/
	public static Audio getInstance(Context context) {
	   if(yaCreado == false) {
		   singleton = new Audio(context);
		   yaCreado = true;
	   }

	   return singleton;
   }
	
	
	
	/**
	 * Comienza una grabación
	 * @throws IOException
	 */
	public void startRecording() throws IOException {	
		
		mRecord = new MediaRecorder();
		
		File audio_file = Utils.createAudioScandalOh(mContext);
		path = audio_file.getPath();
		
		mRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
		
		// ESTADO Initialized: Indicamos el formato
		mRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		//mRecord.setAudioChannels(1);
		//mRecord.setAudioSamplingRate(8);
		//mRecord.setAudioEncodingBitRate(8);
		
		// ESTADO DataSourceConfigured
		mRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecord.setOutputFile(path);

		try {
			mRecord.prepare();
			
			// ESTADO Prepared: comenzamos a grabar
			mRecord.start();
			is_recording = true;

        } 
		catch (IOException e) {
            Log.e(mContext.getClass().getSimpleName(), "Error MediaRecorder en prepare() o start(): " + e); 
        }		
	}

	
	
	/**
	 * Para de grabar
	 */
	public void stopRecording() {
		// Estado RECORDING: paramos de grabar
		mRecord.stop();
		is_recording = false;
	}
	
	
	
	/**
	 * Indica si está grabando
	 * @return booleano indicando si está trabando (true) o no (false)
	 */
	public boolean isRecording(){
		return is_recording;
	}
	
	

	
	
	/**
	 * Reproduce el audio recién grabado
	 */
	public void startPlaying() {
		
		try {		
			// Estado Idle: indicamos el recurso
			mPlayer.setDataSource(path);
			
			// Estado Initialized: preparamos el reproductor
			mPlayer.prepare();
			
			// Indicamos que el audio está preparado y va a comenzar la reproduccion
			if (playListener != null){
				playListener.onPlayPrepared();
			}
			
			// Estado Prepared: comenzamos la reproducción
			mPlayer.start();
			
			
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
					
				@Override
				public void onCompletion(MediaPlayer mp) {
					// Estado Started: paramos la reproducción
					mp.stop();
					mp.reset();	
					
					// Indicamos que se ha terminado de reproducir
					if (playListener != null){
						playListener.onPlayFinished();
					}			
				}
			});
            
		} catch (IOException e) {
			Log.e("WE", "Error preparando para reproducir el audio");
		}
    }


	
	
	
	/**
	 * Reproduce un audio a partir de una URL
	 * @param uri_audio. URL donde se encuentra el audio
	 */
	public void startPlaying(String uri_audio) {
        
        		try {
        			// Estado Idle: indicamos el recurso
        			mPlayer.setDataSource(uri_audio);
        			
        			mPlayer.setOnPreparedListener(new OnPreparedListener() {
         				
         				@Override
         				public void onPrepared(MediaPlayer mp) {
         					
         					// Indicamos que el audio está preparado y va a comenzar la reproduccion
         					if (playListener != null){
         						playListener.onPlayPrepared();
         					}
         					
         					// Estado Prepared: comenzamos la reproducción
         					mp.start();	
         					mp.setOnCompletionListener(new OnCompletionListener() {
         						
         						@Override
         						public void onCompletion(MediaPlayer mp) {
         							// Estado Started: paramos la reproducción
         							mp.stop();
         							mp.reset();			
         							
         							// Indicamos que ha terminado la reproducción
         							if (playListener != null){
         								playListener.onPlayFinished();
         							}	
         						}
         					});					
         				}
        			 }); 
        			// Estado Initialized: preparamos el reproductor
                    mPlayer.prepareAsync();
 
                 } catch (IOException e) {
                    Log.e("WE", "error al reproducir el audio");
                 }       
    }
	
	
	/**
	 * Para de reproducir un audio
	 */
	public void stopPlaying(){
			mPlayer.stop();
			mPlayer.reset();
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
	 * Borra el último audio grabado
	 */
	public void deleteAudio(){
		try{
			if (path != null){
				File f = new File(path);
				if (f != null){
					f.delete();
				}
			}
		}	
		catch(Exception e){
			Log.e(mContext.getClass().getSimpleName(), "Error borrando archivo de audio");
		}
	}
	
	
	/**
	 * Cierra y libera los recursos
	 */
	public void releaseResources(){
		
		// Liberamos el grabador
		if (mRecord != null){
			if (isRecording()){
				stopRecording();
			}
			mRecord.reset();
			mRecord.release();
			mRecord = null;
		}
		
		// Liberamos el reproductor
		if (mPlayer != null){
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
			
		}
	
		
		yaCreado = false;
	}

}
