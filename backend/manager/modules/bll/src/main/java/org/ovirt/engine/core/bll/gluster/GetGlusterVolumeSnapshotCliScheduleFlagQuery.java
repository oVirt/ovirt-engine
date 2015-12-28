package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetGlusterVolumeSnapshotCliScheduleFlagQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetGlusterVolumeSnapshotCliScheduleFlagQuery(P parameters) {
        super(parameters);
    }

    @Override
    public void executeQueryCommand() {
        Cluster cluster = clusterDao.get(getParameters().getId());
        getQueryReturnValue().setReturnValue(cluster.isGlusterCliBasedSchedulingOn());
    }
}
