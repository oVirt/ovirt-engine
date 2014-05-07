package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.compat.Guid;


public class GlusterVolumeProfileParameters extends GlusterVolumeQueriesParameters {
    private static final long serialVersionUID = 1L;

    private boolean nfs;

    public boolean isNfs() {
        return nfs;
    }

    public GlusterVolumeProfileParameters() {

    }
    public GlusterVolumeProfileParameters(Guid clusterId, Guid volumeId) {
        this(clusterId, volumeId, false);
    }

    public GlusterVolumeProfileParameters(Guid clusterId, Guid volumeId, boolean nfs) {
        super(clusterId, volumeId);
        this.nfs = nfs;
    }
}
