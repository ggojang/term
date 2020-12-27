package co.infoclinic.term.snomedct.utils;

public class ECLSyntaxError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9163245249034405542L;

	public ECLSyntaxError() {}
	
	public ECLSyntaxError (String message) {
		super(message);
	}
	
	public ECLSyntaxError(Throwable cause) {
		super(cause);
	}
	
	public ECLSyntaxError(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ECLSyntaxError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
