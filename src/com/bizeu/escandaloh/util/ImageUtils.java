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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

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
	 * Crea un Bitmap a partir de una Uri
	 * 
	 * @param selectedImage
	 * @return
	 */
	
	/*
	public static Bitmap uriToBitmap(Uri selectedImage, Context context) {
		Bitmap bm = null;
		AssetFileDescriptor fileDescriptor = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;

		try {
			fileDescriptor = context.getContentResolver()
					.openAssetFileDescriptor(selectedImage, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
				
			    // Calculate inSampleSize
			    options.inSampleSize = calculateInSampleSize(options, 200, 200);
			    
			    // Decode bitmap with inSampleSize set
			    options.inJustDecodeBounds = false;
			    
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
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}
	
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
	public static Bitmap resizePicture(Bitmap bit, int max_pixels){
		Bitmap resized_bitmap = bit;
		
		float actual_height = bit.getHeight();
		float actual_width = bit.getWidth();
		float new_height = 0;
		float new_width = 0;
		float percentage;
	
		// Portrait
		if (actual_height > actual_width){
			if (actual_height > max_pixels){
				percentage = max_pixels / actual_height;
				new_height = max_pixels;
				new_width = (int) (actual_width * percentage);	
			}
		}
		
		// Landscape
		else{
			if (actual_width > max_pixels){
				percentage = max_pixels / actual_width;
				new_width = max_pixels;
				new_height = actual_width * percentage;
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
	public static File reduceSizeBitmap(Bitmap bitm, int max_kb, Context mContext){
		Bitmap reduced_bitmap = bitm;
		Bitmap resized_bitmap;
		File f = null;
		float tamaño_file_k;
		int compression_factor;
		
		// Escalamos la foto a 700 pixeles (de su lado mayor)
		resized_bitmap = ImageUtils.resizePicture(reduced_bitmap, 700);	
		
		try {
			// Obtenemos el factor de compresión para obtener el máximo de kb
			f = ImageUtils.bitmapToFileTemp(resized_bitmap, mContext, "scandal_picture.jpg");
			tamaño_file_k = (float) f.length() / (MEGABYTE);
			compression_factor = (int) (max_kb * 100 / tamaño_file_k);
			
			// Mientras no lleguemos al máximo de tamaño requerido comprimimos más
			while (tamaño_file_k > max_kb){
				f.delete();	
				reduced_bitmap = ImageUtils.compressBitmapToJpg(resized_bitmap,  compression_factor);
				f = ImageUtils.bitmapToFileTemp(reduced_bitmap, mContext, "scandal_picture.jpg");
				tamaño_file_k = (float) f.length() / (MEGABYTE);
				// Reducimos el factor de compresión
				compression_factor =  (int) ((int) compression_factor / 1.5);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return f;
	}
	
	
	/**
	 * Crea un File en cache a partir de un bitmap 
	 * @param bitm
	 * @param context
	 * @param file_name
	 * @return
	 * @throws IOException 
	 */
	public static File bitmapToFileTemp(Bitmap bitm, Context context, String file_name) throws IOException{
		File f = new File(context.getCacheDir(), "avatar.jpg");
		f.createNewFile();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitm.compress(CompressFormat.JPEG, 100, bos);
		byte[] bitmapdata = bos.toByteArray();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bitmapdata);
		fos.close();
		
		return f;
	}
	
	

	/**
	 * Crea un File a partir de un bitmap
	 * 
	 * @param bitm
	 * @return
	 */
	public static File bitmapToFile(Bitmap bitm, Context context) {
		String file_path = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File return_file = new File(file_path, "tmp.png");
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(return_file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// Mandamos la excepcion a Google Analytics
			EasyTracker easyTracker = EasyTracker.getInstance(context);
			easyTracker.send(MapBuilder.createException(
					new StandardExceptionParser(context, null) 
							.getDescription(Thread.currentThread().getName(), 
									e), // The exception.
					false).build());
		}

		bitm.compress(Bitmap.CompressFormat.PNG, 85, fOut);

		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			// Mandamos la excepcion a Google Analytics
			EasyTracker easyTracker = EasyTracker.getInstance(context);
			easyTracker.send(MapBuilder.createException(
					new StandardExceptionParser(context, null) 
							.getDescription(Thread.currentThread().getName(), 
									e), // The exception.
					false).build());
		}

		return return_file;
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
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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
		File imageFileFolder = new File(
				Environment.getExternalStorageDirectory(), "ScándalOh");
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

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	


}
