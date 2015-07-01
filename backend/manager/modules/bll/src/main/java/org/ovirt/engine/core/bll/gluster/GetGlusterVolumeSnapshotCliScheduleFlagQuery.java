package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsGroupDao;

public class GetGlusterVolumeSnapshotCliScheduleFlagQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    @Inject
    private VdsGroupDao clusterDao;

    public GetGlusterVolumeSnapshotCliScheduleFlagQuery(P parameters) {
        super(parameters);
    }

    @Override
    public void executeQueryCommand() {
        VDSGroup cluster = clusterDao.get(getParameters().getId());
        getQueryReturnValue().setReturnValue(cluster.isGlusterCliBasedSchedulingOn());
    }
}
