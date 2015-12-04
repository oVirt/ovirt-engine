package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public abstract class FenceQueryBase<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    protected FenceQueryBase(P parameters) {
        super(parameters);
    }

    public FenceQueryBase(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private Guid privateVdsId;

    protected Guid getVdsId() {
        return privateVdsId;
    }

    protected void setVdsId(Guid value) {
        privateVdsId = value;
    }

    private String privateVdsName;

    protected String getVdsName() {
        return privateVdsName;
    }

    protected void setVdsName(String value) {
        privateVdsName = value;
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param reason
     *            The reason.
     */
    private void alert(AuditLogType logType, String reason) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getVdsId());
        alert.addCustomValue("Reason", reason);
        AlertDirector.alert(alert, logType, auditLogDirector);
    }

    /**
     * Alerts if power management status failed.
     */
    protected void alertPowerManagementStatusFailed(String reason) {
        alert(AuditLogType.VDS_ALERT_FENCE_TEST_FAILED, reason);
    }
}
