package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVmsRunningOnVDSQuery<P extends GetVmsRunningOnVDSParameters> extends QueriesCommandBase<P> {
    public GetVmsRunningOnVDSQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmDao().getAllRunningByVds(getParameters().getId()));
        // ResourceManager.Instance.GetVmsRunningOnVDS(((GetVmsRunningOnVDSParameters)Parameters).Id);
    }
}
