package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with Gluster Volume Id, operation, layout and forceAction as parameters. <br>
 * This will be used by Rebalance start, stop and status gluster volume commands. <br>
 */
public class GlusterVolumeRebalanceParameters extends GlusterVolumeParameters {
    private static final long serialVersionUID = 8279638411077880853L;

    private boolean fixLayoutOnly;
    private boolean forceAction;

    public GlusterVolumeRebalanceParameters() {
    }

    public GlusterVolumeRebalanceParameters(Guid volumeId) {
        super(volumeId);
    }

    public GlusterVolumeRebalanceParameters(Guid volumeId, boolean fixLayoutOnly, boolean forceAction) {
        super(volumeId);

        setFixLayoutOnly(fixLayoutOnly);
        setForceAction(forceAction);
    }

    public boolean isFixLayoutOnly() {
        return fixLayoutOnly;
    }

    public void setFixLayoutOnly(boolean fixLayoutOnly) {
        this.fixLayoutOnly = fixLayoutOnly;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

    public boolean isForceAction() {
        return forceAction;
    }
}
