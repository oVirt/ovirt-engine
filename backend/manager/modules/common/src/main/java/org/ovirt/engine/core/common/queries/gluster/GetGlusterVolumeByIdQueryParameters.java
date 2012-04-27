package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class for the GetGlusterVolumeByIdQuery
 */
public class GetGlusterVolumeByIdQueryParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -4601447036978553847L;
    private Guid volumeId;

    public GetGlusterVolumeByIdQueryParameters(Guid id) {
        setVolumeId(id);
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }
}
