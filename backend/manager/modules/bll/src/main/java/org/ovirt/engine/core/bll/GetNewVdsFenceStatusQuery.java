package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

public class GetNewVdsFenceStatusQuery<P extends GetNewVdsFenceStatusParameters> extends FenceQueryBase<P> {

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
        tempVar.setVdsGroupId(getParameters().getVdsGroupId());
        if (getParameters().getOrder() == FenceAgentOrder.Primary) {
        tempVar.setManagementIp(getParameters().getManagementIp());
        tempVar.setPmOptionsMap(getParameters().getFencingOptions());
        tempVar.setPmType(getParameters().getPmType());
        tempVar.setPmUser(getParameters().getUser());
        tempVar.setPmPassword(getParameters().getPassword());
        }
        else if (getParameters().getOrder() == FenceAgentOrder.Secondary) {
            tempVar.setPmSecondaryIp(getParameters().getManagementIp());
            tempVar.setPmSecondaryOptionsMap(getParameters().getFencingOptions());
            tempVar.setPmSecondaryType(getParameters().getPmType());
            tempVar.setPmSecondaryUser(getParameters().getUser());
            tempVar.setPmSecondaryPassword(getParameters().getPassword());
        }
        tempVar.setPmProxyPreferences(getParameters().getPmProxyPreferences());
        VDS vds = tempVar;
        FenceExecutor executor = new FenceExecutor(vds, FenceActionType.Status);
        if (executor.findProxyHost()) {
            VDSReturnValue returnValue = executor.fence(getParameters().getOrder());
            if (returnValue.getReturnValue() != null) {
                getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
            }
        } else {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setReturnValue(
                    new FenceStatusReturnValue(UNKNOWN, AuditLogDirector
                            .getMessage(AuditLogType.VDS_ALERT_FENCE_NO_PROXY_HOST)));
        }
    }
}
