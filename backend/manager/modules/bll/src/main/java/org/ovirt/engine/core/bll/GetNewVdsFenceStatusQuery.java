package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

public class GetNewVdsFenceStatusQuery<P extends GetNewVdsFenceStatusParameters> extends FencingQueryBase<P> {

    public GetNewVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String UNKNOWN = "unknown";
        Guid id = getParameters().getVdsId();
        VDS tempVar = new VDS();
        tempVar.setId((Guid) ((id != null) ? id : Guid.Empty));
        tempVar.setStoragePoolId(getParameters().getStoragePoolId());
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
