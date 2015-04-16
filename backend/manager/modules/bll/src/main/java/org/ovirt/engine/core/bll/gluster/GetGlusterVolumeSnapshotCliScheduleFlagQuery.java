package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeSnapshotCliScheduleFlagQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    public GetGlusterVolumeSnapshotCliScheduleFlagQuery(P parameters) {
        super(parameters);
    }

    @Override
    public void executeQueryCommand() {
        VDSGroup cluster = getVdsGroupDao().get(getParameters().getId());
        getQueryReturnValue().setReturnValue(cluster.isGlusterCliBasedSchedulingOn());
    }
}
