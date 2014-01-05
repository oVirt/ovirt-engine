package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Objects;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }

        ExtendedVmDynamic other = (ExtendedVmDynamic) obj;
        return Objects.equals(host, other.host);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(host);
        return result;
    }
}
