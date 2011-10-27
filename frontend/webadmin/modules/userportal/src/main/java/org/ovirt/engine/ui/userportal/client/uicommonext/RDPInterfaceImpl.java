package org.ovirt.engine.ui.userportal.client.uicommonext;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.ui.uicommon.models.vms.IRdp;
import org.ovirt.engine.ui.uicommon.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.userportal.client.protocols.RdpConnector;
import org.ovirt.engine.ui.userportal.client.util.ClientAgentType;

public class RDPInterfaceImpl implements IRdp {
	private static Logger logger = Logger.getLogger(RDPInterfaceImpl.class
			.getName());
	private String address;
	private String guestID;
	private boolean useLocalDrives;
	
	private Event disconnectedEvent = new Event(RdpConsoleModel.RdpDisconnectedEventDefinition);

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public void setAddress(String value) {
		address = value;
	}

	@Override
	public String getGuestID() {
		return guestID;
	}

	@Override
	public void setGuestID(String value) {
		guestID = value;
	}
	
	@Override
	public boolean getUseLocalDrives() {
		return useLocalDrives;
	}

	@Override
	public void setUseLocalDrives(boolean value) {
	    useLocalDrives = value;
	}

	@Override
	public void Connect() {
		RdpConnector connector = new RdpConnector(getAddress(), getDisconnectedEvent());
		connector.setRedirectDrives(getUseLocalDrives());
		connector.connect();
	}

	public String getRDPCabURL() {
		return GWT.getModuleBaseURL() + "msrdp.cab";
	}

	public static boolean isBrowserSupported() {
		ClientAgentType cat = new ClientAgentType();
		logger.finer("Determining whether browser [" + cat.browser
				+ "], version [" + cat.version + "] on OS [" + cat.os
				+ "] is supported by spice");

		if ((cat.os.equalsIgnoreCase("Windows"))
				&& (cat.browser.equalsIgnoreCase("Explorer"))
				&& (cat.version >= 7.0)) {
			return true;
		}

		return false;
	}

    @Override
    public Event getDisconnectedEvent() {
        return disconnectedEvent;
    }
}
