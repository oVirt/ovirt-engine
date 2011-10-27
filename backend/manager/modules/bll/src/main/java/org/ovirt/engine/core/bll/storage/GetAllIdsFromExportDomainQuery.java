package org.ovirt.engine.core.bll.storage;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.vdscommands.GetVmsInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

public class GetAllIdsFromExportDomainQuery<P extends GetAllFromExportDomainQueryParamenters>
        extends QueriesCommandBase<P> {
    public GetAllIdsFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        GetVmsInfoVDSCommandParameters tempVar = new GetVmsInfoVDSCommandParameters(
                getParameters().getStoragePoolId());
        tempVar.setStorageDomainId(getParameters().getStorageDomainId());
        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.GetVmsList, tempVar);
        List<String> ids = Arrays.asList((String[]) retVal.getReturnValue());
        getQueryReturnValue().setReturnValue(ids);
    }
}
