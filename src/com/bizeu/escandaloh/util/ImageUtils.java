package com.bizeu.escandaloh.util;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class ImageUtils {

	
	/**
	 * Transforma un Bitmap en un array de bytes
	 * @param bmp
	 * @return
	 */
	public static byte[] BitmapToBytes(Bitmap bmp){
		
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
	public static Bitmap BytesToBitmap(byte[] bytes){
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes .length);
		return bitmap;
	}
}
