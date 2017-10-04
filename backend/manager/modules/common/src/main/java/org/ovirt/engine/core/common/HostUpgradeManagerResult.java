package org.ovirt.engine.core.common;

import java.io.Serializable;
import java.util.List;

public class HostUpgradeManagerResult implements Serializable {

    private List<String> availablePackages;
    private boolean updatesAvailable;

    public List<String> getAvailablePackages() {
        return availablePackages;
    }

    public void setAvailablePackages(List<String> availablePackages) {
        this.availablePackages = availablePackages;
    }

    public boolean isUpdatesAvailable() {
        return updatesAvailable;
    }

    public void setUpdatesAvailable(boolean updatesAvailable) {
        this.updatesAvailable = updatesAvailable;
    }
}
