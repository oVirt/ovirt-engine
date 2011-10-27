package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class GetStorageStatisticsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStorageStatisticsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue = IrsClusterMonitor.Instance.IrsStatus;
    }
}
