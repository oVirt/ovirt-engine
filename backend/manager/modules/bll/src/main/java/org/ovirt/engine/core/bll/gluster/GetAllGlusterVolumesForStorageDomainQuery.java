package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

/**
 * Query to fetch gluster volumes that can be used as storage domain
 */
public class GetAllGlusterVolumesForStorageDomainQuery<P extends QueryParametersBase> extends GlusterQueriesCommandBase<P> {

    public GetAllGlusterVolumesForStorageDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(glusterVolumeDao.getVolumesSupportedAsStorageDomain());
        getQueryReturnValue().setSucceeded(true);
    }

}
