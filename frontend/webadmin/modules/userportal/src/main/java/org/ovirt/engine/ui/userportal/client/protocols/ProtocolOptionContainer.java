package org.ovirt.engine.ui.userportal.client.protocols;

public interface ProtocolOptionContainer {
	public void setProtocol(Protocol protocol);
	public Protocol getProtocol();
	
	public void setProtocolMessage(String message);
	public String getProtocolMessage();
	
}
