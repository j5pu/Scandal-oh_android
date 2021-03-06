package com.bizeu.escandaloh.dialogs;

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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bizeu.escandaloh.util.Audio;
import com.bizeu.escandaloh.util.Fuente;
import com.bizeu.escandaloh.util.Audio.PlayListener;
import com.mnopi.scandaloh_escandalo_humor_denuncia_social.R;

public class RecordAudioDialog extends Dialog {

	private static final int RECORD_TIME = 60;

	private TextView txt_meter;
	private Button but_reproducir; // Bot�n de empezar a reproducir
	private Button but_abajo; // Bot�n de grabar y parar (tanto grabar como reproducir)
	private TextView txt_aceptar;
	private TextView txt_cancelar;
	private TextView txt_seg;

	private TextView txt_description;
	private LinearLayout ll_espacio_botones;

	private Timer mTimer;
	private int contador = RECORD_TIME;
	private int contador_play = 0;
	private int ultimo_tiempo_guardado = 0;
	private Handler myHandler;
	private Runnable myRunnable;
	OnMyDialogResult mDialogResult;
	private Context mContext;

	/**
	 * Constructor
	 * 
	 * @param con Contexto
	 * @param record Audio
	 */
	public RecordAudioDialog(Context con, Audio record) {
		super(con);
		mContext = con;
	}

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.record_audio_dialog);

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.lay_pantalla_record_audio_dialog));

		ll_espacio_botones = (LinearLayout) findViewById(R.id.lay_pantalla_record_audio_dialog);
		txt_description = (TextView) findViewById(R.id.txt_recordar_descripcion);
		txt_seg = (TextView) findViewById(R.id.txt_seg);

		// Bot�n de cancelar
		txt_cancelar = (TextView) findViewById(R.id.txt_cancelar_foto_audio);
		txt_cancelar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Si estamos reproduciendo audio: lo paramos
				if (Audio.getInstance(mContext).isPlaying()) {
					Audio.getInstance(mContext).stopPlaying();
				}
				
				// Borramos si hubiese alg�n audio grabado
				Audio.getInstance(mContext).deleteAudio();

				// Indicamos a la actividad que ha cancelado
				if (mDialogResult != null) {
					mDialogResult.finish("CANCELED");
				}

				// Cerramos el dialog
				RecordAudioDialog.this.dismiss();
			}
		});

		// Aceptar
		txt_aceptar = (TextView) findViewById(R.id.txt_subir_foto_audio);
		txt_aceptar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Si estamos reproduciendo audio: lo paramos
				if (Audio.getInstance(mContext).isPlaying()) {
					Audio.getInstance(mContext).stopPlaying();
				}

				// Indicamos a la actividad que queremos subir con audio
				if (mDialogResult != null) {
					mDialogResult.finish("OK");
				}

				// Cerramos el dialog
				RecordAudioDialog.this.dismiss();
			}
		});

		// Bot�n inferior de grabar/parar
		but_abajo = (Button) findViewById(R.id.but_changepass_enviar);
		but_abajo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Si est� reproduciendo, indica PARAR: paramos de reproducir
				if (Audio.getInstance(mContext).isPlaying()) {
					Audio.getInstance(mContext).stopPlaying();

					// Acualizamos la interfaz
					changeIUPlayFinised();
				}

				// Si no est� reproduciendo: comprobamos si est� grabando o no
				else if (!Audio.getInstance(mContext).isPlaying()) {

					// Si est� grabando, indica PARAR: paramos de grabar
					if (Audio.getInstance(mContext).isRecording()) {

						Audio.getInstance(mContext).stopRecording();

						// Actualizamos la IU para mostrar el tiempo de
						// grabaci�n y el bot�n de reproducir
						ll_espacio_botones.setVisibility(View.VISIBLE);
						but_abajo.setText(R.string.grabar);
						txt_aceptar.setVisibility(View.VISIBLE);
						txt_cancelar.setVisibility(View.VISIBLE);
						but_reproducir.setVisibility(View.VISIBLE);
						txt_description.setText(R.string.audio_grabado_dospuntos);
						txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35F);
						txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35F);
						int recorded_seconds = RECORD_TIME - contador;
						String recorded_seconds_string;
						if (recorded_seconds < 10) {
							recorded_seconds_string = "00:0" + recorded_seconds;
						} else {
							recorded_seconds_string = "00:" + recorded_seconds;
						}
						// Almacenamos el tiempo grabado
						ultimo_tiempo_guardado = recorded_seconds;
						txt_meter.setText(recorded_seconds_string);
						contador = RECORD_TIME;
						contador_play = 0;
					}

					// No est� grabando, indica GRABAR: comienza a grabar
					else {

						try {
							Audio.getInstance(mContext).startRecording();
						} catch (IOException e) {
							Log.e("WE", "error grabando audio");
							e.printStackTrace();
						}

						// Actualizamos la IU para mostrar el tiempo m�s grande
						// y quitar el bot�n de reproducir
						txt_description.setText(R.string.grabando_audio);
						txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 41F);
						txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 61f);
						txt_meter.setText("00:" + Integer.toString(contador));
						but_abajo.setText(R.string.parar);
						but_reproducir.setVisibility(View.GONE);
						txt_aceptar.setVisibility(View.GONE);
						txt_cancelar.setVisibility(View.GONE);
					}
				}
			}
		});

		// Bot�n de reproducir audio (s�lo aparece si se ha grabado antes)
		but_reproducir = (Button) findViewById(R.id.but_reproducir);
		but_reproducir.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Reseteamos el contador de reproducci�n
				contador_play = 0;

				// Cambios IU
				ll_espacio_botones.setVisibility(View.VISIBLE);
				txt_description.setText(R.string.reproduciendo);
				txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 41F);
				txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 61f);
				txt_meter.setText("00:00");
				// Quitamos el bot�n de reproducir
				but_reproducir.setVisibility(View.GONE);
				// El boton de abajo ahora pondr� Parar
				but_abajo.setText(R.string.parar);

				// Comenzamos a reproducir
				Audio.getInstance(mContext).startPlaying();
			}
		});

		// Contador de segundos
		txt_meter = (TextView) findViewById(R.id.txt_count_time_record);

		myRunnable = new Runnable() {
			public void run() {
				// Est� grabando: decrementamos el secundero cada segundo
				if (Audio.getInstance(mContext).isRecording()) {
					contador--;
					if (contador > 0) {
						if (contador < 10) {
							txt_meter.setText("00:0" + contador);
						} else {
							txt_meter.setText("00:" + contador);
						}
					} else if (contador == 0) {
						Audio.getInstance(mContext).stopRecording();
						// Hacemos los cambios necesarios a la IU
						ll_espacio_botones.setVisibility(View.VISIBLE);
						but_abajo.setText(R.string.grabar);
						txt_aceptar.setVisibility(View.VISIBLE);
						txt_cancelar.setVisibility(View.VISIBLE);
						but_reproducir.setVisibility(View.VISIBLE);
						txt_description
								.setText(R.string.audio_grabado_dospuntos);
						txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35F);
						txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35F);
						int recorded_seconds = RECORD_TIME - contador;
						String recorded_seconds_string;
						if (recorded_seconds < 10) {
							recorded_seconds_string = "00:0" + recorded_seconds;
						} else {
							recorded_seconds_string = "00:" + recorded_seconds;
						}
						// Almacenamos el tiempo grabado
						ultimo_tiempo_guardado = recorded_seconds;
						txt_meter.setText(recorded_seconds_string);
						contador = RECORD_TIME;
					}
				}

				// Est� reproduciendo: incrementamos el secundero cada segundo
				else if (Audio.getInstance(mContext).isPlaying()) {
					contador_play++;

					if (contador_play < 10) {
						txt_meter.setText("00:0" + contador_play);
					} else {
						txt_meter.setText("00:" + contador_play);
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

		Audio.getInstance(mContext).setOnPlayListener(new PlayListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onPlayFinished() {
				// Actualizamos la interfaz
				changeIUPlayFinised();
			}

			@Override
			public void onPlayPrepared() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * Refresca el contador de segundos en la pantalla
	 */
	private void updateMeter() {
		myHandler.post(myRunnable);
	}

	// Callbacks
	public void setDialogResult(OnMyDialogResult dialogResult) {
		mDialogResult = dialogResult;
	}

	public interface OnMyDialogResult {
		void finish(String result);
	}

	/**
	 * Cambia la interfaz de usuario al terminar de reproducir un audio
	 */
	private void changeIUPlayFinised() {

		// Actualizamos descripci�n
		txt_description.setText(R.string.audio_grabado_dospuntos);

		// Disminuimos tama�o del tiempo
		txt_seg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35F);
		txt_meter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35F);

		// Indicamos el tiempo de grabaci�n total en el tiempo
		String recorded_seconds_string;
		if (ultimo_tiempo_guardado < 10) {
			recorded_seconds_string = "00:0" + ultimo_tiempo_guardado;
		} else {
			recorded_seconds_string = "00:" + ultimo_tiempo_guardado;
		}
		txt_meter.setText(recorded_seconds_string);

		// Mostramos el bot�n de reproducir
		but_reproducir.setVisibility(View.VISIBLE);

		// El bot�n de abajo indicar� Grabar
		but_abajo.setText(R.string.grabar);

		// Reseteamos contador de reproducci�n
		contador_play = 0;
	}

}