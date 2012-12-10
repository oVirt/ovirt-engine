package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmOsType;

public class ConsoleSelectionContext {

    private VmOsType osType;

    private DisplayType defaultDisplayType;

    public ConsoleSelectionContext(VmOsType osType, DisplayType consoleProtocol) {
        super();
        this.osType = osType;
        this.defaultDisplayType = consoleProtocol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultDisplayType == null) ? 0 : defaultDisplayType.hashCode());
        result = prime * result + ((osType == null) ? 0 : osType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConsoleSelectionContext other = (ConsoleSelectionContext) obj;
        if (defaultDisplayType != other.defaultDisplayType)
            return false;
        if (osType != other.osType)
            return false;
        return true;
    }

}
