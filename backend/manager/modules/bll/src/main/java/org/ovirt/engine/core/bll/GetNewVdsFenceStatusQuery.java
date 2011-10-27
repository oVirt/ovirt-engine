package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.*;

public class GetNewVdsFenceStatusQuery<P extends GetNewVdsFenceStatusParameters> extends FencingQueryBase<P> {

    public GetNewVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String UNKNOWN = "unknown";
        Guid id = getParameters().getVdsId();
        VDS tempVar = new VDS();
        tempVar.setvds_id((Guid) ((id != null) ? id : Guid.Empty));
        tempVar.setstorage_pool_id(getParameters().getStoragePoolId());
        tempVar.setManagmentIp(getParameters().getManagementIp());
        tempVar.setPmOptionsMap(getParameters().getFencingOptions());
        tempVar.setpm_type(getParameters().getPmType());
        tempVar.setpm_user(getParameters().getUser());
        tempVar.setpm_password(getParameters().getPassword());
        VDS vds = tempVar;
        FencingExecutor executor = new FencingExecutor(vds, FenceActionType.Status);
        if (executor.FindVdsToFence()) {
            VDSReturnValue returnValue = executor.Fence();
            if (returnValue.getReturnValue() != null) {
                getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
            }
        } else {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setReturnValue(
                    new FenceStatusReturnValue(UNKNOWN, AuditLogDirector
                            .GetMessage(AuditLogType.VDS_ALERT_FENCING_NO_PROXY_HOST)));
        }
    }
}
