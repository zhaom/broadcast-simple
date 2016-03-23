package com.babeeta.hudee.gateway.device.tunnel;

import com.google.protobuf.MessageLite;

/**
 * Tunnel data package data structures
 * 
 * @param <T>
 *            message object extends from message list of protocol buffer
 */
public class TunnelData<T extends MessageLite> {
	/**
	 * Tag for synchronous request and response between client and server
	 */
	public final int tag;
	/**
	 * Data, message object in byte array
	 */
	public final T obj;
	/**
	 * The enumeration value of message object
	 */
	public final int cmd;

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            Tag for synchronous request and response between client and
	 *            server
	 * @param cmd
	 *            The enumeration value of message object
	 * @param obj
	 *            Data, message object in byte array
	 */
	public TunnelData(int tag, int cmd, T obj) {
		super();
		this.tag = tag;
		this.obj = obj;
		this.cmd = cmd;
	}

}
