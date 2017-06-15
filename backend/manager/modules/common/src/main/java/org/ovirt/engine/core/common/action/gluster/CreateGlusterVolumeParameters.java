package org.ovirt.engine.core.common.action.gluster;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;

/**
 * Command parameters for the "Create Volume" action
 */
public class CreateGlusterVolumeParameters extends ActionParametersBase {
    private static final long serialVersionUID = 2015321730118872954L;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_NOT_NULL")
    @Valid
    private GlusterVolumeEntity volume;

    private boolean force;

    public CreateGlusterVolumeParameters() {
    }

    public CreateGlusterVolumeParameters(GlusterVolumeEntity volume) {
        this(volume, false);
    }

    public CreateGlusterVolumeParameters(GlusterVolumeEntity volume, boolean force) {
        setVolume(volume);
        setForce(force);
    }

    public GlusterVolumeEntity getVolume() {
        return volume;
    }

    public void setVolume(GlusterVolumeEntity volume) {
        this.volume = volume;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
