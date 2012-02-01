package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

public enum ConsoleProtocol {
    SPICE("Spice"),
    RDP("Remote Desktop");

    public String displayName;

    private ConsoleProtocol(String displayName) {
        this.displayName = displayName;
    }
}
