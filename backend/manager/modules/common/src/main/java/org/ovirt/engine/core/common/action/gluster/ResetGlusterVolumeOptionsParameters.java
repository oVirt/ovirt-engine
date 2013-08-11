package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * Command parameters class with a volume id, volume option and force as parameters.
 */
public class ResetGlusterVolumeOptionsParameters extends GlusterVolumeParameters {

    private static final long serialVersionUID = 6574282602574606939L;

    private GlusterVolumeOptionEntity volumeOption;

    private boolean forceAction;

    public ResetGlusterVolumeOptionsParameters() {
    }

    public ResetGlusterVolumeOptionsParameters(Guid volumeId, GlusterVolumeOptionEntity volumeOption, boolean forceAction) {
        super(volumeId);
        setVolumeOption(volumeOption);
        setForceAction(forceAction);
    }

    public GlusterVolumeOptionEntity getVolumeOption() {
        return volumeOption;
    }

    public void setVolumeOption(GlusterVolumeOptionEntity volumeOption) {
        this.volumeOption = volumeOption;
    }

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

}
