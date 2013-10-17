package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskOperation;
import org.ovirt.engine.core.compat.Guid;

/**
 * Replace Gluster volume brick action VDS parameter class with server id, volume name,
 * action, existing brick dir, new brick dir and forceAction as parameters. <br>
 * This will be used directly by Replace Gluster volume Brick VDS commands <br>
 */
public class ReplaceGlusterVolumeBrickActionVDSParameters extends GlusterVolumeActionVDSParameters {

    private GlusterTaskOperation action;

    private String existingBrickDir;

    private String newBrickDir;

    public ReplaceGlusterVolumeBrickActionVDSParameters(Guid serverId,
            String volumeName,
            GlusterTaskOperation action,
            String existingBrickDirectory,
            String newBrickDirectory,
            boolean forceAction) {
        super(serverId, volumeName, forceAction);
        setAction(action);
        setExistingBrickDir(existingBrickDirectory);
        setNewBrickDir(newBrickDirectory);
    }

    public ReplaceGlusterVolumeBrickActionVDSParameters() {
    }

    public GlusterTaskOperation getAction() {
        return action;
    }

    public void setAction(GlusterTaskOperation action) {
        this.action = action;
    }

    public String getExistingBrickDir() {
        return existingBrickDir;
    }

    public void setExistingBrickDir(String existingBrickDir) {
        this.existingBrickDir = existingBrickDir;
    }

    public String getNewBrickDir() {
        return newBrickDir;
    }

    public void setNewBrickDir(String newBrickDir) {
        this.newBrickDir = newBrickDir;
    }

}
