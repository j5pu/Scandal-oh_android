package com.bizeu.escandaloh.util;

public class Utils {
	
	/**
	 * Limita un string a un nº de caracteres + tres puntos suspensivos
	 * 
	 * @param completo String oritinal
	 * @param num_caracteres Número de caracteres máximo que podrá contener
	 * @return String con un tamaño máximo de num_caracteres caracteres
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
