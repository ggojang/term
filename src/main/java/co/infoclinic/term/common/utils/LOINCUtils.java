package co.infoclinic.term.common.utils;

/**
 * The LOINC Utils
 */
public final class LOINCUtils {
	public final static String CodeSystem = "LOINC";
	
	private LOINCUtils() {} // Prevent the class from being constructed

	public static String CODE_PATTERN = "^([a-zA-Z \\-\\_\\.\\,]+|LP[\\-0-9]{1,8}|LG[\\-0-9]{1,8}|LA[\\-0-9]{1,8}|LL[\\-0-9]{1,8}|[\\-0-9]{1,10})$"; //20200411 by Yu
}
