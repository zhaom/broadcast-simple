package com.babeeta.hudee.core.router.network.dns;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 *
 */
public interface DNSClient {
	/**
	 * resolve domain to address
	 * 
	 * @param serviceDomain
	 *            domain
	 * @return address included ip and host
	 * @throws UnknownHostException
	 */
	InetSocketAddress resolve(String serviceDomain) throws UnknownHostException;
}
