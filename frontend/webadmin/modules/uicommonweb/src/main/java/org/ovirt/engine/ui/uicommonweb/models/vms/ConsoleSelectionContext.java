package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DisplayType;

public class ConsoleSelectionContext {

    private int osId;

    private DisplayType defaultDisplayType;

    public ConsoleSelectionContext(int osId, DisplayType consoleProtocol) {
        super();
        this.osId = osId;
        this.defaultDisplayType = consoleProtocol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultDisplayType == null) ? 0 : defaultDisplayType.hashCode());
        result = prime * result + osId;
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
        if (osId != other.osId)
            return false;
        return true;
    }

}
