package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskOperation;
import org.ovirt.engine.core.compat.Guid;

/**
 * Command parameters class with a volume id and existing brick and new brick as parameters.
 */
public class GlusterVolumeReplaceBrickActionParameters extends GlusterVolumeActionParameters {

    private static final long serialVersionUID = -8083235918435906931L;

    private GlusterTaskOperation action;

    private GlusterBrickEntity existingBrick;

    private GlusterBrickEntity newBrick;

    public GlusterVolumeReplaceBrickActionParameters() {
    }

    public GlusterVolumeReplaceBrickActionParameters(Guid volumeId,
            GlusterTaskOperation action,
            GlusterBrickEntity existingBrick,
            GlusterBrickEntity newBrick,
            boolean forceAction) {
        super(volumeId, forceAction);
        setAction(action);
        setExistingBrick(existingBrick);
        setNewBrick(newBrick);
    }

    public GlusterTaskOperation getAction() {
        return action;
    }

    public void setAction(GlusterTaskOperation action) {
        this.action = action;
    }

    public GlusterBrickEntity getExistingBrick() {
        return existingBrick;
    }

    public void setExistingBrick(GlusterBrickEntity existingBrick) {
        this.existingBrick = existingBrick;
    }

    public GlusterBrickEntity getNewBrick() {
        return newBrick;
    }

    public void setNewBrick(GlusterBrickEntity newBrick) {
        this.newBrick = newBrick;
        this.newBrick.setVolumeId(getVolumeId());
    }

}
