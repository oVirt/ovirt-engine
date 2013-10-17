package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Gluster volume action VDS parameter class with server id, volume name and forceAction as parameters. <br>
 * This will be used directly by start and stop commands <br>
 */
public class GlusterVolumeActionVDSParameters extends GlusterVolumeVDSParameters {
    private boolean forceAction;

    public GlusterVolumeActionVDSParameters(Guid serverId, String volumeName, boolean forceAction) {
        super(serverId, volumeName);
        setForceAction(forceAction);
    }

    public GlusterVolumeActionVDSParameters() {
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

    public boolean isForceAction() {
        return forceAction;
    }

}
