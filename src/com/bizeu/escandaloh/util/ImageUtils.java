package com.bizeu.escandaloh.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


public class ImageUtils {

	private static float MEGABYTE = (float) 1024.0;
	
	/**
	 * Transforma un Bitmap en un array de bytes (JPEG)
	 * 
	 * @param bmp
	 * @return
	 */
	public static byte[] bitmapToBytes(Bitmap bmp) {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		return byteArray;
	}
	
	
	/**
	 * 
	 * @param bitm
	 * @param height
	 * @param width
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bitm, int height, int width){
		Bitmap bitmap = Bitmap.createScaledBitmap(bitm, width, height, true);
		return bitmap;
	}
	
	
	
	/**
	 * Escala un bitmap
	 * @param bm
	 * @param newHeight
	 * @param newWidth
	 * @return
	 */
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);
	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}
	
	
	
	/**
	 * Comprime un bitmap a JPG con un nivel de compresión
	 * @param bitm
	 * @param quality
	 * @return
	 */
	public static Bitmap compressBitmapToJpg(Bitmap bitm, int quality){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitm.compress(CompressFormat.JPEG, quality, bos);
		
		byte[] array = bos.toByteArray();
		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}

	
	
	/**
	 * Transforma un array de bytes en un Bitmap
	 * 
	 * @param bytes
	 * @return
	 */
	public static Bitmap bytesToBitmap(byte[] bytes) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;
	}

	
	
	/**
	 * Crea un bitmap a partir de una uri
	 * @param selectedImage
	 * @param context
	 * @return
	 */
	public static Bitmap uriToBitmap(Uri selectedImage, Context context) {
		Bitmap bm = null;
		AssetFileDescriptor fileDescriptor = null;
		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			fileDescriptor = context.getContentResolver()
					.openAssetFileDescriptor(selectedImage, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {				
				bm = BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();		
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}
	
	
	
	/**
	 * Escala un bitmap a un máximo de pixeles determinado.
	 * Si está en portrait entonces escala en base a su alto.
	 * Si está en landscape escala en base a su ancho.
	 * @param bit
	 * @param max_pixels
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bit, int max_pixels){
		Bitmap resized_bitmap = bit;
		
		float actual_height = bit.getHeight();
		float actual_width = bit.getWidth();
		float new_height = 0;
		float new_width = 0;
		float percentage;
	
		// Portrait
		if (actual_height >= actual_width){
			if (actual_height > max_pixels){
				percentage = max_pixels / actual_height;
				new_height = max_pixels;
				new_width = actual_width * percentage;	
			}
			else{
				new_height = actual_height;
				new_width = actual_width;
			}
		}
		
		// Landscape
		else{
			if (actual_width > max_pixels){			
				percentage = max_pixels / actual_width;
				new_width = max_pixels;
				new_height = actual_height * percentage;
			}
			else{
				new_height = actual_height;
				new_width = actual_width;
			}
		} 
		
		resized_bitmap = ImageUtils.scaleBitmap(resized_bitmap, (int) new_height, (int) new_width);
		
		return resized_bitmap;
	}
	
	
	
	
	
	/**
	 * Reduce significativamente el tamaño de un bitmap y lo devuelve como File
	 * @param bitm Bitmap original
	 * @param max_kb Tamaño máximo aceptado en kilobytes
	 * @param mContext Contexto
	 * @return File reducido en tamaño
	 */
	public static File reduceBitmapSize(Bitmap bitm, int max_kb, Context mContext){
		Bitmap reduced_bitmap = bitm;
		Bitmap scaled_bitmap;
		File reduced_file = null;
		float tamanio_file;
		int compression_factor;
		
		// Escalamos la foto a 700 pixeles (de su lado mayor)
		scaled_bitmap = ImageUtils.scaleBitmap(reduced_bitmap, 700);	
		
		try {
			// Obtenemos el factor de compresión para obtener el máximo de kb
			reduced_file = ImageUtils.bitmapToFileTemp(scaled_bitmap, 100, mContext);
			tamanio_file = (float) reduced_file.length() / (MEGABYTE);
			compression_factor = (int) (max_kb * 100 / tamanio_file);
			Log.v("WE","Tamanio file: " + tamanio_file);
			
			// Mientras no lleguemos al máximo de tamaño requerido comprimimos más
			while (tamanio_file > max_kb){
				reduced_file.delete();	
				reduced_file = ImageUtils.bitmapToFileTemp(scaled_bitmap, compression_factor, mContext);
				tamanio_file = (float) reduced_file.length() / (MEGABYTE);
				// Reducimos el factor de compresión
				compression_factor =  (int) ((int) compression_factor / 1.5);
				Log.v("WE","Nuevo tamanio file: " + tamanio_file);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return reduced_file;	
	}
	
	
	
	
	

	/**
	 * Reduce significativamente el tamaño de un file a partir de una Uri y lo devuelve como file
	 * @param bitm Bitmap original
	 * @param max_kb Tamaño máximo aceptado en kilobytes
	 * @param mContext Contexto
	 * @return File reducido en tamaño
	 */
	public static File reduceBitmapSize(Uri uri, int max_kb, Context mContext){
		File reduced_file = null;
		int size_original_file = 0;
		float tamanio_file;
		int compression_factor;
		Bitmap original_bitmap;
		Bitmap scaled_bitmap;
				
		ContentResolver cr = mContext.getContentResolver();
		InputStream is = null;
		try {
			is = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			size_original_file = is.available() / 1024 ;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Si el archivo ocupa menos del máximo permitido lo mandamos tal cual
		if (size_original_file <= max_kb){
			byte[] data;
			try {
				data = IOUtils.toByteArray(is);
				reduced_file = File.createTempFile("tmp", "jpg",mContext.getCacheDir());
				FileUtils.writeByteArrayToFile(reduced_file, data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Si ocupa más, entonces escalamos y comprimimos
		else{
			// Obtenemos el bitmap original
			original_bitmap = BitmapFactory.decodeStream(is);
			
			// Escalamos la foto a 700 pixeles (de su lado mayor)
			scaled_bitmap = ImageUtils.scaleBitmap(original_bitmap, 700);	
			
			try {
				// Obtenemos el factor de compresión para obtener el máximo de kb
				reduced_file = ImageUtils.bitmapToFileTemp(scaled_bitmap, 100, mContext);		
				tamanio_file= (float) reduced_file.length() / (MEGABYTE);
				compression_factor = (int) (max_kb * 100 / tamanio_file);
				
				// Mientras no lleguemos al máximo de tamaño requerido comprimimos más
				while (tamanio_file > max_kb){
					reduced_file.delete();	
					reduced_file = ImageUtils.bitmapToFileTemp(scaled_bitmap, compression_factor, mContext);
					tamanio_file = (float) reduced_file.length() / (MEGABYTE);
					// Reducimos el factor de compresión
					compression_factor =  (int) ((int) compression_factor / 1.5);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return reduced_file;
	}
	
	
	
	
	/**
	 * Crea un File en cache a partir de un bitmap 
	 * @param bitm
	 * @param context
	 * @param file_name
	 * @return
	 * @throws IOException 
	 */
	public static File bitmapToFileTemp(Bitmap bitm, int quality, Context context) throws IOException{
		
		File f = File.createTempFile("tmp", "jpg", context.getCacheDir());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitm.compress(CompressFormat.JPEG, quality, bos);
		byte[] bitmapdata = bos.toByteArray();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bitmapdata);
		fos.close();
		
		return f;
	}
	
	


	/**
	 * Obtiene un string a partir de una uri
	 * 
	 * @param context
	 * @param contentUri
	 * @return
	 */
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * Guarda un bitmap como una foto en la galería del dispositivo
	 * 
	 * @param bmp
	 *            Bitmap de la foto a guardar
	 * @param context
	 *            Contexto
	 */
	public static void saveBitmapIntoGallery(Bitmap bmp, Context context) {
		File imageFileFolder = new File(Environment.getExternalStorageDirectory(), "ScandalOh");
		imageFileFolder.mkdirs();
		FileOutputStream out = null;

		// El nombre de la foto se obtiene a partir de la fecha y hora exacta
		// actual
		Calendar c = Calendar.getInstance();
		String date = String.valueOf(c.get(Calendar.MONTH))
				+ String.valueOf(c.get(Calendar.DAY_OF_MONTH))
				+ String.valueOf(c.get(Calendar.YEAR))
				+ String.valueOf(c.get(Calendar.HOUR_OF_DAY))
				+ String.valueOf(c.get(Calendar.MINUTE))
				+ String.valueOf(c.get(Calendar.SECOND));
		File imageFileName = new File(imageFileFolder, "IMG-" + date.toString()
				+ ".jpg");

		try {
			out = new FileOutputStream(imageFileName);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

			Intent mediaScanIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri contentUri = Uri.fromFile(imageFileName);
			mediaScanIntent.setData(contentUri);
			context.sendBroadcast(mediaScanIntent);

			out = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Descarga una foto de internet
	 * @param src
	 * @return Bitmap de la foto o null en caso de error
	 */
	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	/**
	 * Decodifica una Uri en un Bitmap
	 * @param res
	 * @param resId
	 * @param reqWidth
	 * @param reqHeight
	 * @return Bitmap en caso de éxito, nulo en caso de error
	 */
	public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
		
		Bitmap bm = null;
		AssetFileDescriptor fileDescriptor = null;
		
		try{
		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
			fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
		    BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
		    fileDescriptor.close();
		    
		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight); 
		    
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
			fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
		    bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
		    fileDescriptor.close();
		}
		catch (Exception e){
			if (e instanceof FileNotFoundException){
				
			}
			else if (e instanceof IOException){
			}
		}
	
	    return bm;
	}
	
	
	
	
	
	
	/**
	 * Dedofica un String (path) en un Bitmap
	 * @param res
	 * @param resId
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromString(String path, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path,options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}


	
	/**
	 * Calcula en options.inSampleSize en base de dos
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
}
