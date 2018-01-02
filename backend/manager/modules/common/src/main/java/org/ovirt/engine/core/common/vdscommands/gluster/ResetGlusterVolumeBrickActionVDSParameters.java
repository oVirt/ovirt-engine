package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Reset Gluster volume brick action VDS parameter class with server id, volume name,
 * existing brick dir, new brick dir and forceAction as parameters. <br>
 * This will be used directly by Replace Gluster volume Brick VDS commands <br>
 */
public class ResetGlusterVolumeBrickActionVDSParameters extends GlusterVolumeActionVDSParameters {

    private String existingBrickDir;

    public ResetGlusterVolumeBrickActionVDSParameters(Guid serverId,
            String volumeName,
            String existingBrickDirectory) {
        super(serverId, volumeName, true);
        setExistingBrickDir(existingBrickDirectory);
    }

    public ResetGlusterVolumeBrickActionVDSParameters() {
    }

    public String getExistingBrickDir() {
        return existingBrickDir;
    }

    public void setExistingBrickDir(String existingBrickDir) {
        this.existingBrickDir = existingBrickDir;
    }
}
