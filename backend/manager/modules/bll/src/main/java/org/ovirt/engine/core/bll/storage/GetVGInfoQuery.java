package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetVGInfoQuery<P extends VGQueryParametersBase> extends QueriesCommandBase<P> {
    public GetVGInfoQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                (java.util.ArrayList<LUNs>) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.GetVGInfo,
                                new GetVGInfoVDSCommandParameters(getParameters().getVdsId(), getParameters()
                                        .getVGId())).getReturnValue());
    }
}
