package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetStorageSessionsListQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {
    public GetStorageSessionsListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.GetStorageSessionsList,
                                new VdsIdVDSCommandParametersBase(getParameters().getVdsId())).getReturnValue());
    }
}
