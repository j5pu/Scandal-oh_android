package com.bizeu.escandaloh.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class ImageUtils {

	
	/**
	 * Transforma un Bitmap en un array de bytes
	 * @param bmp
	 * @return
	 */
	public static byte[] bitmapToBytes(Bitmap bmp){
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		
		return byteArray;
	}
	
	
	/**
	 * Transforma un array de bytes en un Bitmap
	 * @param bytes
	 * @return
	 */
	public static Bitmap bytesToBitmap(byte[] bytes){
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes .length);
		return bitmap;
	}
	
	
	
	
	/**
	 * Crea un Bitmap a partir de una Uri
	 * @param selectedImage
	 * @return
	 */
	public static Bitmap uriToBitmap(Uri selectedImage, Context context) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = context.getContentResolver().openAssetFileDescriptor(
					selectedImage, "r");
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
	 * Crea un File a partir de un bitmap
	 * @param bitm
	 * @return
	 */
	public static File bitmapToFile(Bitmap bitm){
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath();
		File return_file = new File(file_path, "tmp.png");
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(return_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bitm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return return_file;
	}
	
	
	
	
	/**
	 * Obtiene un string a partir de una uri
	 * @param context
	 * @param contentUri
	 * @return
	 */
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try { 
			String[] proj = { MediaStore.Images.Media.DATA };
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
		      cursor.close();
		    }
		 }
	}
}
