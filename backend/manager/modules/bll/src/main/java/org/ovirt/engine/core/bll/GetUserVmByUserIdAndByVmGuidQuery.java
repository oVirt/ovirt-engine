package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetUserVmByUserIdAndByVmGuidQuery<P extends GetUserVmByUserIdAndByVmGuidParameters>
        extends QueriesCommandBase<P> {
    public GetUserVmByUserIdAndByVmGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        /*
         * getQueryReturnValue().setReturnValue(
         * DbFacade.getInstance().GetVmTagsByVmIdAndAdElementId(
         * ((GetUserVmByUserIdAndByVmGuidParameters)
         * getParameters()).getUserId(),
         * ((GetUserVmByUserIdAndByVmGuidParameters)
         * getParameters()).getVmId()));
         */
    }
}
