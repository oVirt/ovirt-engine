package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with Gluster Volume Id and forceAction as parameters. <br>
 * This will be used by start and stop gluster volume commands. <br>
 */
public class GlusterVolumeActionParameters extends GlusterVolumeParameters {
    private static final long serialVersionUID = -1224829720081853632L;

    private boolean forceAction;

    public GlusterVolumeActionParameters() {
    }

    public GlusterVolumeActionParameters(Guid volumeId, boolean forceAction) {
        super(volumeId);
        setForceAction(forceAction);
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

    public boolean isForceAction() {
        return forceAction;
    }

}
