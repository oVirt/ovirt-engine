package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with volume option as parameter, apart from volume name inherited from
 * {@link GlusterVolumeVDSParameters}. Used by the "set gluster volume option" command.
 */
public class GlusterVolumeOptionVDSParameters extends GlusterVolumeVDSParameters {
    private GlusterVolumeOptionEntity volumeOption;

    public GlusterVolumeOptionVDSParameters(Guid serverId, String volumeName, GlusterVolumeOptionEntity volumeOption) {
        super(serverId, volumeName);
        this.volumeOption = volumeOption;
    }

    public GlusterVolumeOptionVDSParameters() {
    }

    public GlusterVolumeOptionEntity getVolumeOption() {
        return volumeOption;
    }
}
