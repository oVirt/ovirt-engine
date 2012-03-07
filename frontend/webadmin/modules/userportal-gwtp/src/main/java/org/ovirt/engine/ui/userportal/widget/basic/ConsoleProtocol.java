package org.ovirt.engine.ui.userportal.widget.basic;

public enum ConsoleProtocol {
    SPICE("Spice"),
    RDP("Remote Desktop");

    public String displayName;

    private ConsoleProtocol(String displayName) {
        this.displayName = displayName;
    }
}
