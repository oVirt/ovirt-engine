package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVnc;

public class VncNativeImpl extends AbstractVnc implements IVnc {

    @Override
    public void invokeClient() {
        StringBuilder configBuilder = new StringBuilder("[virt-viewer]"); //$NON-NLS-1$
        configBuilder.append("\ntype=vnc") //$NON-NLS-1$
                .append("\nhost=").append(getVncHost()) //$NON-NLS-1$
                .append("\nport=").append(getVncPort()) //$NON-NLS-1$
                .append("\npassword=").append(getTicket()) //$NON-NLS-1$
                .append("\ndelete-this-file=1") //$NON-NLS-1$
                .append("\ntitle=").append(getTitle()); //$NON-NLS-1$

        ConsoleModel.makeConsoleConfigRequest("console.vv", "application/x-virt-viewer; charset=UTF-8", configBuilder.toString()); //$NON-NLS-1$ $NON-NLS-2$
    }

}
