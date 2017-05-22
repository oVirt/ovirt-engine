package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetStoragePoolsByClusterServiceParameters;

public class GetStoragePoolsByClusterServiceQuery<P extends GetStoragePoolsByClusterServiceParameters> extends StoragePoolQueryBase<P> {

    public GetStoragePoolsByClusterServiceQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected Object queryDataCenter() {
        return storagePoolDao.getDataCentersByClusterService(getParameters().isSupportsVirtService(),
                getParameters().isSupportsGlusterService());
    }
}
