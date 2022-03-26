package io.github.contractautomata.care.exceptions;

import java.net.SocketAddress;

public class ContractViolationException extends RuntimeException {

	//remote host causing the violation of the contract, for accountability
	private final SocketAddress remote;

	
	public ContractViolationException(SocketAddress remote) {
		super();
		this.remote = remote;
	}


	public SocketAddress getRemote() {
		return remote;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
