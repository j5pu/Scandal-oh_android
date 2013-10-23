package com.bizeu.escandaloh;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.bizeu.escandaloh.util.Base64;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateEscandalo extends Activity {

	private ImageView picture;
	private Button but_accept;
	private Button but_cancel;
	private EditText edit_title;
	private RadioGroup radio_category;
	
	Bitmap taken_photo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.create_escandalo);
		
		if (getIntent() != null){
			Intent data = getIntent();
			
			if (data != null){
				if (data.hasExtra("data")) {
					taken_photo = (Bitmap) data.getParcelableExtra("data");
				}
			}
		}
		
		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		radio_category = (RadioGroup) findViewById(R.id.rg_create_escandalo_category);	
		
		picture = (ImageView) findViewById(R.id.img_new_escandalo_photo);
		picture.setImageBitmap(taken_photo);
		
		but_accept = (Button) findViewById(R.id.but_new_escandalo_accept);
		but_accept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String introducido = edit_title.getText().toString();
				if (introducido.equals("")){
					Toast toast = Toast.makeText(getBaseContext(), "Debe introducir primero un título", Toast.LENGTH_SHORT);
					toast.show();
					
				}
				else{
					new SendPhoto().execute();	
				}
		
			}
		});
		
		but_cancel = (Button) findViewById(R.id.but_new_escandalo_cancel);
		but_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();			
			}
		});
		

		

		
		

	}
	
	
	
	private class SendPhoto extends AsyncTask<Void,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(Void... params) {
	    	boolean resul = true;
	 
	        HttpClient httpClient = new DefaultHttpClient();
	        String param = "http://192.168.1.48:8000/test/photo/";
	        HttpPost post = new HttpPost(param);
	        post.setHeader("content-type", "application/json");
	 
		    try{
		        JSONObject dato = new JSONObject();
		 
		        String written_title = edit_title.getText().toString();
		        
		        String category_selected = null ;
		        int id_category_selected = radio_category.getCheckedRadioButtonId();
		        switch(id_category_selected){
		        	case R.id.rb_create_category_happy:
		        		category_selected = "/api/v1/category/1/";
		        		break;
		        	case R.id.rb_create_category_angry:
		        		category_selected = "/api/v1/category/2/";
		        		break;
		        }
		        
		        dato.put("user", "/api/v1/user/2/");        
		        dato.put("category", category_selected);
		        dato.put("title", written_title);
		        
		        //Convertimos la imagen a Base64
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		        taken_photo.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object   
		        byte[] b = baos.toByteArray();
		        String encoded_photo = Base64.encodeBytes(b);
		        dato.put("img", encoded_photo);
		 
		        StringEntity entity = new StringEntity(dato.toString());
		        post.setEntity(entity);
		 
		        HttpResponse resp = httpClient.execute(post);
		        String respStr = EntityUtils.toString(resp.getEntity());
		        
		        Log.v("WE",respStr);
		        guardaTextoImage(respStr);
		        
		        }catch(Exception ex){
			         Log.e("ServicioRest","Error!", ex);
			         resul = false;
			     }
		    
		     return resul;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
	        if (result){
	        	Log.v("WE","foto enviada");
	        	finish();
	        }
	        else{
	        	Log.v("WE","foto no enviada");
	        }
	    }
	}
	
	
	private void guardaTextoImage(String texto){
		   Writer writer;
	        
	        File root = Environment.getExternalStorageDirectory();
	        File outDir = new File(root.getAbsolutePath() + File.separator + "imagen_base64");
	        if (!outDir.isDirectory()) {
	          outDir.mkdir();
	        }
	        try {
	          if (!outDir.isDirectory()) {
	            throw new IOException(
	                "Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
	          }
	          File outputFile = new File(outDir, "imagen_base64.txt");
	          writer = new BufferedWriter(new FileWriter(outputFile));
	          writer.write(texto);

	          writer.close();
	        } catch (IOException e) {
	          Log.w("error en guardar datos imagen", e.getMessage(), e);
	        }       
	}
}
