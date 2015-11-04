package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class UpdateHostNicVfsConfigParameters extends VfsConfigBaseParameters {

    private static final long serialVersionUID = 702645639103829096L;

    private int numOfVfs;

    private boolean allNetworksAllowed;

    public UpdateHostNicVfsConfigParameters() {
    }

    public UpdateHostNicVfsConfigParameters(Guid nicId, int numOfVfs, boolean allNetworksAllowed) {
        super(nicId);
        this.numOfVfs = numOfVfs;
        this.allNetworksAllowed = allNetworksAllowed;
    }

    public int getNumOfVfs() {
        return numOfVfs;
    }

    public boolean getAllNetworksAllowed() {
        return allNetworksAllowed;
    }

    public void setNumOfVfs(int numOfVfs) {
        this.numOfVfs = numOfVfs;
    }

    public void setAllNetworksAllowed(boolean allNetworksAllowed) {
        this.allNetworksAllowed = allNetworksAllowed;
    }
}
