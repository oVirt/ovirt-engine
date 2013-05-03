package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;


public enum ConsoleProtocol {
    SPICE(SpiceConsoleModel.class),
    RDP(RdpConsoleModel.class),
    VNC(VncConsoleModel.class);

    private final Class<? extends ConsoleModel> model;

    private ConsoleProtocol(Class<? extends ConsoleModel> model) {
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
