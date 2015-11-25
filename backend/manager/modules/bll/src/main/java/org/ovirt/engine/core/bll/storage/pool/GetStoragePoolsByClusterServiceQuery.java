package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetStoragePoolsByClusterServiceParameters;

public class GetStoragePoolsByClusterServiceQuery<P extends GetStoragePoolsByClusterServiceParameters> extends QueriesCommandBase<P> {

    public GetStoragePoolsByClusterServiceQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStoragePoolDao()
            .getDataCentersByClusterService(
                getParameters().isSupportsVirtService(),
                getParameters().isSupportsGlusterService()));
    }
}
