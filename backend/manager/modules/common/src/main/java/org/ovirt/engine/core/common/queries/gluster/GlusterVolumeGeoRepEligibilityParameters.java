package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;


public class GlusterVolumeGeoRepEligibilityParameters extends IdQueryParameters {

    private static final long serialVersionUID = 1L;

    private Guid slaveVolumeId;

    public GlusterVolumeGeoRepEligibilityParameters() {
        super();
    }

    public GlusterVolumeGeoRepEligibilityParameters(Guid masterVolumeId, Guid slaveVolumeId) {
        super(masterVolumeId);
        this.slaveVolumeId = slaveVolumeId;
    }

    public Guid getSlaveVolumeId() {
        return slaveVolumeId;
    }

    public void setSlaveVolumeId(Guid slaveVolumeId) {
        this.slaveVolumeId = slaveVolumeId;
    }
}
