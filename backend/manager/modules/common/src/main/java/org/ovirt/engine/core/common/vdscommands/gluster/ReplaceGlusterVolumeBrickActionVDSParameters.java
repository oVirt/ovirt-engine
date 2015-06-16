package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Replace Gluster volume brick action VDS parameter class with server id, volume name,
 * existing brick dir, new brick dir and forceAction as parameters. <br>
 * This will be used directly by Replace Gluster volume Brick VDS commands <br>
 */
public class ReplaceGlusterVolumeBrickActionVDSParameters extends GlusterVolumeActionVDSParameters {

    private String existingBrickDir;

    private String newBrickDir;

    public ReplaceGlusterVolumeBrickActionVDSParameters(Guid serverId,
            String volumeName,
            String existingBrickDirectory,
            String newBrickDirectory) {
        super(serverId, volumeName, true);
        setExistingBrickDir(existingBrickDirectory);
        setNewBrickDir(newBrickDirectory);
    }

    public ReplaceGlusterVolumeBrickActionVDSParameters() {
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
