package org.ovirt.engine.ui.common.uicommon;

import java.util.logging.Logger;

import org.ovirt.engine.ui.uicommonweb.models.vms.IRdp;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;

import com.google.gwt.core.client.GWT;

public class IRdpImpl implements IRdp {
    private static Logger logger = Logger.getLogger(IRdpImpl.class
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
    public void connect() {
        RdpConnector connector = new RdpConnector(getAddress(), getDisconnectedEvent());
        connector.setRedirectDrives(getUseLocalDrives());
        connector.connect();
    }

    public String getRDPCabURL() {
        return GWT.getModuleBaseURL() + "msrdp.cab";//$NON-NLS-1$
    }

    public static boolean isBrowserSupported() {
        ClientAgentType cat = new ClientAgentType();
        logger.finer("Determining whether browser [" + cat.browser//$NON-NLS-1$
                + "], version [" + cat.version + "] on OS [" + cat.os//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "] is supported by spice");//$NON-NLS-1$

        if ((cat.os.equalsIgnoreCase("Windows"))//$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Explorer"))//$NON-NLS-1$
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
