package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Command parameters class with a volume id, volume option and force as parameters.
 */
public class ResetGlusterVolumeOptionsParameters extends GlusterVolumeParameters {

    private static final long serialVersionUID = 6574282602574606939L;

    private String volumeOption;

    private boolean forceAction = false;

    public ResetGlusterVolumeOptionsParameters(Guid volumeId, String volumeOption, boolean forceAction) {
        super(volumeId);
        setVolumeOption(volumeOption);
        setForceAction(forceAction);
    }

    public String getVolumeOption() {
        return volumeOption;
    }

    public void setVolumeOption(String volumeOption) {
        this.volumeOption = volumeOption;
    }

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

}
