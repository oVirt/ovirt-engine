package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

public class GetTagsByVmIdQuery<P extends GetTagsByVmIdParameters> extends
        QueriesCommandBase<P> {
    public GetTagsByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getTagDao()
                        .getAllForVm(getParameters().getVmId()));
    }
}
