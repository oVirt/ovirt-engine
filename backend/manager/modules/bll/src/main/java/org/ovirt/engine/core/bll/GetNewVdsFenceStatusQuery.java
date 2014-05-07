package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

public class GetNewVdsFenceStatusQuery<P extends GetNewVdsFenceStatusParameters> extends FenceQueryBase<P> {

    private static final String UNKNOWN = "unknown";

    public GetNewVdsFenceStatusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        FenceExecutor executor = new FenceExecutor(getHost());
        VDSFenceReturnValue result = executor.checkStatus();
        if (result.getSucceeded()) {
            getQueryReturnValue().setReturnValue(result.getReturnValue());
        } else {
            handleError(result);
        }
    }

    private VDS getHost() {
        Guid id = getParameters().getVdsId();
        VDS vds = new VDS();
        vds.setId((Guid) ((id != null) ? id : Guid.Empty));
        vds.setStoragePoolId(getParameters().getStoragePoolId());
        vds.getFenceAgents().add(getParameters().getAgent());
        vds.setPmProxyPreferences(getParameters().getPmProxyPreferences());
        return vds;
    }

    private void handleError(VDSFenceReturnValue result) {
        if (!result.isProxyHostFound()) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setReturnValue(new FenceStatusReturnValue(UNKNOWN,
                    AuditLogDirector.getMessage(AuditLogType.VDS_ALERT_FENCE_NO_PROXY_HOST)));
        } else {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setReturnValue(new FenceStatusReturnValue(UNKNOWN,
                    AuditLogDirector.getMessage(AuditLogType.VDS_ALERT_FENCE_STATUS_VERIFICATION_FAILED)));
        }
    }
}
