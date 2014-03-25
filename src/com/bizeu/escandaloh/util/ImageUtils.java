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

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

public class ImageUtils {

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
	
	
	
	/**
	 * Crea un File en cache a partir de un bitmap 
	 * @param bitm
	 * @param context
	 * @param file_name
	 * @return
	 */
	public static File bitmapToFileTemp(Bitmap bitm, Context context, String file_name){
		File f = new File(context.getCacheDir(), "avatar.jpg");
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitm.compress(CompressFormat.JPEG, 100, bos);
		byte[] bitmapdata = bos.toByteArray();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.write(bitmapdata);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
	    BitmapFactory.decodeFile(f.getPath(), options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, 100, 100);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    BitmapFactory.decodeFile(f.getPath(), options);	
	    
	    try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
