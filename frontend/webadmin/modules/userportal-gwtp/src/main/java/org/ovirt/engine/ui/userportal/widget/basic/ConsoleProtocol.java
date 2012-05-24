package org.ovirt.engine.ui.userportal.widget.basic;


public enum ConsoleProtocol {
    SPICE("Spice"), //$NON-NLS-1$
    RDP("Remote Desktop"), //$NON-NLS-1$
    VNC("VNC"); //$NON-NLS-1$

    public String displayName;

    private ConsoleProtocol(String displayName) {
        this.displayName = displayName;
    }
}
