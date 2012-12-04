package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;


public enum ConsoleProtocol {
    SPICE("Spice", SpiceConsoleModel.class), //$NON-NLS-1$
    RDP("Remote Desktop", RdpConsoleModel.class), //$NON-NLS-1$
    VNC("VNC", VncConsoleModel.class); //$NON-NLS-1$

    public String displayName;
    private final Class<? extends ConsoleModel> model;

    private ConsoleProtocol(String displayName, Class<? extends ConsoleModel> model) {
        this.displayName = displayName;
        this.model = model;
    }

    public boolean isBackedBy(Class<? extends ConsoleModel> model) {
        return this.model.equals(model);
    }

    public static ConsoleProtocol getProtocolByModel(Class<? extends ConsoleModel> model) {
        for (ConsoleProtocol value : values()) {
            if (value.isBackedBy(model)) {
                return value;
            }
        }

        return null;
    }
}
