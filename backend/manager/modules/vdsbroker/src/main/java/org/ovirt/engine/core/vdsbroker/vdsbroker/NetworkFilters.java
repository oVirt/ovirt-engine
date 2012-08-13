package org.ovirt.engine.core.vdsbroker.vdsbroker;

/**
 * The network filters defined by VDSM to be applied for the VM network interfaces.
 */
public enum NetworkFilters {
    NO_MAC_SPOOFING("vdsm-no-mac-spoofing");

    private String filterName;

    private NetworkFilters(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterName() {
        return filterName;
    }
}
