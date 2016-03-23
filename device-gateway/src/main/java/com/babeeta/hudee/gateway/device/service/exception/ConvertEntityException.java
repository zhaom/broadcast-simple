/**
 * 
 */
package com.babeeta.hudee.gateway.device.service.exception;

/**
 * Exception caused by IO exception of convert entity to byte array or string
 * 
 * @author Xinyu
 * 
 */
public class ConvertEntityException extends Exception {
	/**
     * 
     */
	private static final long serialVersionUID = -8223426494721599619L;

	/**
	 * Constructs a ConvertEntityException without a detail message.
	 */
	public ConvertEntityException() {
		super();
	}

	/**
	 * Constructs a ConvertEntityException with a detail message.
	 * 
	 * @param s
	 *            Describes the reason for the exception.
	 */
	public ConvertEntityException(String s) {
		super(s);
	}
}
