package org.ovirt.engine.ui.userportal.client.protocols;

public enum Protocol {
	SPICE("Spice"), RDP("Remote Desktop");

	public String displayName;
	private Protocol(String displayName) {
		this.displayName = displayName;
	}
}