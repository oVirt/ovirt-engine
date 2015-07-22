package org.ovirt.engine.core.common.action.gluster;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;

/**
 * Command parameters class with a volume option (key and value) as parameter.
 */
public class GlusterVolumeOptionParameters extends GlusterVolumeParameters {
    private static final long serialVersionUID = -7962802152394185854L;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_OPTION_NOT_NULL")
    @Valid
    private GlusterVolumeOptionEntity volumeOption;

    public GlusterVolumeOptionParameters() {
    }

    public GlusterVolumeOptionParameters(GlusterVolumeOptionEntity option) {
        super(option.getVolumeId());
        setVolumeOption(option);
    }

    public GlusterVolumeOptionEntity getVolumeOption() {
        return volumeOption;
    }

    public void setVolumeOption(GlusterVolumeOptionEntity volumeOption) {
        this.volumeOption = volumeOption;
    }
}
