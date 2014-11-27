package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeSnapshotsByVolumeIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    public GetGlusterVolumeSnapshotsByVolumeIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    public void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterVolumeSnapshotDao().getAllByVolumeId(getParameters().getId()));
    }
}
