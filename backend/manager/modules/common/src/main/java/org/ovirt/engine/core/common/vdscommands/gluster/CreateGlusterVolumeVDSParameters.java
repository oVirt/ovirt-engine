/**
 *
 */
package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

/**
 * VDS Command parameters class for the "Create Volume" action, with the volume object as parameter.
 */
public class CreateGlusterVolumeVDSParameters extends VdsIdVDSCommandParametersBase {
    private GlusterVolumeEntity volume;

    private Version clusterVersion;

    private boolean force;

    public CreateGlusterVolumeVDSParameters(Guid serverId,
            GlusterVolumeEntity volume,
            Version clusterVersion,
            boolean force) {
        super(serverId);
        setVolume(volume);
        setClusterVersion(clusterVersion);
        setForce(force);
    }

    public CreateGlusterVolumeVDSParameters() {
    }

    public GlusterVolumeEntity getVolume() {
        return volume;
    }

    public void setVolume(GlusterVolumeEntity volume) {
        this.volume = volume;
    }

    public Version getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(Version version) {
        this.clusterVersion = version;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
