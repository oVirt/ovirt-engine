package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with volume id as parameter which extends from GlusterParameters. <br>
 * This will be used by gluster volume profile info command.
 */
public class GlusterVolumeQueriesParameters extends GlusterParameters {

    private static final long serialVersionUID = 199106704417008718L;
    private Guid volumeId;

    public GlusterVolumeQueriesParameters() {
    }

    public GlusterVolumeQueriesParameters(Guid clusterId, Guid volumeId) {
        super(clusterId);
        setVolumeId(volumeId);
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

}
