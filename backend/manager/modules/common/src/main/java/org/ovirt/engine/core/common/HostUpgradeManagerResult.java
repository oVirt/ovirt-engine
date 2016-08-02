package org.ovirt.engine.core.common;

import java.io.Serializable;
import java.util.Collection;

public class HostUpgradeManagerResult implements Serializable {

    private Collection<String> availablePackages;
    private boolean updatesAvailable;

    public Collection<String> getAvailablePackages() {
        return availablePackages;
    }

    public void setAvailablePackages(Collection<String> availablePackages) {
        this.availablePackages = availablePackages;
    }

    public boolean isUpdatesAvailable() {
        return updatesAvailable;
    }

    public void setUpdatesAvailable(boolean updatesAvailable) {
        this.updatesAvailable = updatesAvailable;
    }
}
