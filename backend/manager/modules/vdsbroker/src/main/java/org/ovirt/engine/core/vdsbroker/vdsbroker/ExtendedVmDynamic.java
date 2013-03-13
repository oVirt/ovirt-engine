package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;

public class ExtendedVmDynamic extends VmDynamic {

    private final VDS host;

    public ExtendedVmDynamic(VDS host) {
        this.host = host;
    }

    @Override
    public void setDisplayIp(String value) {
        if (host.getConsoleAddress() != null) {
            super.setDisplayIp(host.getConsoleAddress());
        } else if (value.startsWith("0")) {
            super.setDisplayIp(host.getHostName());
        } else {
            super.setDisplayIp(value);
        }
    }
}
