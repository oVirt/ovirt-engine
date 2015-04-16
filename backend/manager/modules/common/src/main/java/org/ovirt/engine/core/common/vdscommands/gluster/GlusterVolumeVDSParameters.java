package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameter class with volume name as parameter. <br>
 * This will be used directly by some commands (e.g. start volume), <br>
 * and inherited by others (e.g. set volume option).
 */
public class GlusterVolumeVDSParameters extends VdsIdVDSCommandParametersBase {
    private String volumeName;

    public GlusterVolumeVDSParameters(Guid serverId, String volumeName) {
        super(serverId);
        this.volumeName = volumeName;
    }

    public GlusterVolumeVDSParameters() {
    }

    public String getVolumeName() {
        return volumeName;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("volumeName", volumeName);
    }
}
