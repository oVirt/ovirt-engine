package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;

public class ExtendedVmDynamic extends VmDynamic {

    private final VDS host;

    public ExtendedVmDynamic(VDS host) {
        this.host = host;
    }

    @Override
    public void setdisplay_ip(String value) {
        if (value.startsWith("0")) {
            value = host.gethost_name();
        }
        super.setdisplay_ip(value);
    }
}
