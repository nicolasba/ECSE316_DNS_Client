package ca.mcgill.ecse316.dnsclient;

public class InputSyntaxException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InputSyntaxException(String message) {
		
		super("Incorrect input syntax: " + message);
	}

}
