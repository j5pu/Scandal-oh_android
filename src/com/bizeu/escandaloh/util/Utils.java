package com.bizeu.escandaloh.util;

public class Utils {
	
	/**
	 * Limita un string a 22 caracteres + tres puntos suspensivos
	 * 
	 * @param completo
	 *            String oritinal
	 * @return String con un tamaño máximo de 25 caracteres
	 */
	public static String limitaCaracteres(String completo) {
		String acortado = null;
		if (completo.length() > 25) {
			acortado = completo.substring(0, 22) + "...";
		} else {
			acortado = completo;
		}

		return acortado;
	}

}
