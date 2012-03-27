/**
 *
 */
package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS Command parameters class for the "Create Volume" action, with the volume object as parameter.
 */
public class CreateGlusterVolumeVDSParameters extends VdsIdVDSCommandParametersBase {
    private GlusterVolumeEntity volume;

    public CreateGlusterVolumeVDSParameters(Guid serverId, GlusterVolumeEntity volume) {
        super(serverId);
        setVolume(volume);
    }

    public GlusterVolumeEntity getVolume() {
        return volume;
    }

    public void setVolume(GlusterVolumeEntity volume) {
        this.volume = volume;
    }
}
