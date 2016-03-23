/**
 * 
 */
package com.babeeta.hudee.gateway.device.service.exception;

/**
 * Exception caused by IO exception of consume entity
 * 
 * @author Xinyu
 * 
 */
public class ConsumeEntityException extends Exception {

	/**
     * 
     */
	private static final long serialVersionUID = 8469463960245590236L;

	/**
	 * Constructs a ConsumeEntityException without a detail message.
	 */
	public ConsumeEntityException() {
		super();
	}

	/**
	 * Constructs a ConsumeEntityException with a detail message.
	 * 
	 * @param s
	 *            Describes the reason for the exception.
	 */
	public ConsumeEntityException(String s) {
		super(s);
	}
}
