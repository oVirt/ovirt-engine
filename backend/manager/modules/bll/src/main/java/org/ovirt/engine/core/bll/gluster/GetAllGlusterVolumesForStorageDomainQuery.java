package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;


/**
 * Query to fetch gluster volumes that can be used as storage domain
 */
public class GetAllGlusterVolumesForStorageDomainQuery<P extends VdcQueryParametersBase> extends GlusterQueriesCommandBase<P> {

    public GetAllGlusterVolumesForStorageDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(glusterVolumeDao.getVolumesSupportedAsStorageDomain());
        getQueryReturnValue().setSucceeded(true);
    }

}
