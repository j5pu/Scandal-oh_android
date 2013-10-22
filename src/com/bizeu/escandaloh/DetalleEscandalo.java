package com.bizeu.escandaloh;

import com.bizeu.escandaloh.model.Escandalo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DetalleEscandalo extends Activity {

	private TextView titulo;
	private ImageView foto;
	private Escandalo esca;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.escandalo);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    int value = extras.getInt("posicion");
		    esca = MainActivity.escandalos_prueba.get(value);  
		}
		
		titulo = (TextView) findViewById(R.id.txt_titulo);
		foto = (ImageView) findViewById(R.id.img_foto);
		titulo.setText(esca.getTitle());
		foto.setImageBitmap(esca.getPicture());
	}
}
