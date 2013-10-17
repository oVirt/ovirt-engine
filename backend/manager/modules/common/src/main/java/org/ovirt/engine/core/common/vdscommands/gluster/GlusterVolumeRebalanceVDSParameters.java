package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with volume name, volume option and force as parameters, apart from volume name inherited from
 * {@link GlusterVolumeVDSParameters}. Used by the "Reset gluster volume option" command.
 */
public class GlusterVolumeRebalanceVDSParameters extends GlusterVolumeVDSParameters {

    private boolean fixLayoutOnly;
    private boolean forceAction;

    public GlusterVolumeRebalanceVDSParameters(Guid serverId,
            String volumeName,
            boolean fixLayoutOnly,
            boolean forceAction) {
        super(serverId, volumeName);
        this.fixLayoutOnly = fixLayoutOnly;
        this.forceAction = forceAction;
    }

    public GlusterVolumeRebalanceVDSParameters() {
    }

    public boolean isFixLayoutOnly() {
        return fixLayoutOnly;
    }

    public boolean isForceAction() {
        return forceAction;
    }

}
