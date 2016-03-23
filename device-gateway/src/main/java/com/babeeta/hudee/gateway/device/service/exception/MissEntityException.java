package com.babeeta.hudee.gateway.device.service.exception;

/**
 * Exception caused by HTTP response with successfully status code but no
 * expectant entity.
 * 
 * @author Xinyu
 * 
 */
public class MissEntityException extends Exception {

	/**
     * 
     */
	private static final long serialVersionUID = 4600639580369160995L;

	/**
	 * Constructs a MissEntityException without a detail message.
	 */
	public MissEntityException() {
		super();
	}

	/**
	 * Constructs a MissEntityException with a detail message.
	 * 
	 * @param s
	 *            Describes the reason for the exception.
	 */
	public MissEntityException(String s) {
		super(s);
	}
}
