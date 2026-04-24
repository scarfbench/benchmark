package com.ibm.webapi.data;

public class NoConnectivity extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public NoConnectivity() {}
	public NoConnectivity(Throwable cause) { super(cause); }
}
