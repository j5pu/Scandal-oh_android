package com.bizeu.escandaloh.util;

public class Utils {
	
	/**
	 * Limita un string a un n� de caracteres + tres puntos suspensivos
	 * 
	 * @param completo String oritinal
	 * @param num_caracteres N�mero de caracteres m�ximo que podr� contener
	 * @return String con un tama�o m�ximo de num_caracteres caracteres
	 */
	public static String limitaCaracteres(String completo, int num_caracteres) {
		String acortado = null;
		if (completo.length() > num_caracteres) {
			acortado = completo.substring(0, num_caracteres - 3) + "...";
		} else {
			acortado = completo;
		}

		return acortado;
	}

}
