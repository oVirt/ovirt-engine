package org.ovirt.engine.core.common.action.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with Gluster Volume Id parameter. <br>
 * This will be used directly by some commands (e.g. start volume), <br>
 * and inherited by others (e.g. set volume option).
 */
public class GlusterVolumeParameters extends ActionParametersBase {
    private static final long serialVersionUID = -5148741622108406754L;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_ID_NOT_NULL")
    private Guid volumeId;

    public GlusterVolumeParameters() {
    }

    public GlusterVolumeParameters(Guid volumeId) {
        setVolumeId(volumeId);
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public Guid getVolumeId() {
        return volumeId;
    }
}
