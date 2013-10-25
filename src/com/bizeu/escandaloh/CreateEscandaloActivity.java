package com.bizeu.escandaloh;


import java.io.File;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

public class CreateEscandaloActivity extends Activity {

	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	
	private ImageView picture;
	private Button but_accept;
	private Button but_cancel;
	private EditText edit_title;
	private RadioGroup radio_category;
	
	private String selected_category;
	private String written_title ;
	private Bitmap taken_photo;
	private Uri photoUri;
	private Bitmap bitmap;
	private File photo_file;
	private Uri mImageUri;
	
	
	
	/**
	 * OnCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.create_escandalo);
		
		if (getIntent() != null){
			Intent data = getIntent();
			
			if (data != null){
				mImageUri = Uri.parse(data.getExtras().getString("photoUri"));
				this.getContentResolver().notifyChange(mImageUri, null);
			    ContentResolver cr = this.getContentResolver();
			    try{
			    	taken_photo = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
			        
			    }
			    catch (Exception e)
			    {
			        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
			        Log.d("WE", "Failed to load", e);
			    }
			}
		}
			
		edit_title = (EditText) findViewById(R.id.edit_create_escandalo_title);
		radio_category = (RadioGroup) findViewById(R.id.rg_create_category);	
		
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
					new SendScandalo().execute();	
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
	
	
	
	/**
	 * Sube un escandalo al servidor: foto, categoría y título
	 * @author Alejandro
	 *
	 */
	private class SendScandalo extends AsyncTask<Void,Integer,Boolean> {
		 
		@Override
	    protected Boolean doInBackground(Void... params) {
	    	boolean result = false;
	 
	    	HttpEntity resEntity;
	        String urlString = "http://192.168.1.48:8000/api/v1/photo/";
	        photo_file = new File(mImageUri.getPath());
	        

	        try{
	             HttpClient client = new DefaultHttpClient();
	             HttpPost post = new HttpPost(urlString);
	             
	             // Obtenemos los datos y comprimimos en Multipart para su envío
	             written_title = edit_title.getText().toString();    
	             int id_category_selected = radio_category.getCheckedRadioButtonId();
			     switch(id_category_selected){
			        case R.id.rb_create_category_happy:
			        	selected_category = HAPPY_CATEGORY;
			        	break;
			        case R.id.rb_create_category_angry:
			        	selected_category = ANGRY_CATEGORY;
			        	break;
			     }
			        
			     StringBody categoryBody = new StringBody(selected_category);	        
	             FileBody bin1 = new FileBody(photo_file);
	             StringBody titleBody = new StringBody(written_title);
	             StringBody userBody = new StringBody("/api/v1/user/2/");        
             
	             MultipartEntity reqEntity = new MultipartEntity();
  
	             reqEntity.addPart("img", bin1);
	             reqEntity.addPart("title", titleBody);
	             reqEntity.addPart("category", categoryBody);
	             reqEntity.addPart("user", userBody);
	             	                    
	             post.setEntity(reqEntity);
	             HttpResponse response = client.execute(post);
	             resEntity = response.getEntity();
	             final String response_str = EntityUtils.toString(resEntity);
	             
	             if (resEntity != null) {
	                 Log.i("RESPONSE",response_str);
	                 result = true;
	             }
	        }
	        catch (Exception ex){
	             Log.e("Debug", "error: " + ex.getMessage(), ex);
	        }
	        
	        return result;
	    }

		
		@Override
	    protected void onPostExecute(Boolean result) {
	        if (result){
	        	Log.v("WE","foto enviada");
	        	Intent resultIntent = new Intent();
	        	resultIntent.putExtra("title", written_title);
	        	resultIntent.putExtra("category", selected_category);
	        	setResult(Activity.RESULT_OK, resultIntent);
	        	finish();
	        }
	        else{
	        	Log.v("WE","foto no enviada");
	        }
	    }
	}
	
	


	/*
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
	*/
}
