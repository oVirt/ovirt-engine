package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.*;

public abstract class FenceQueryBase<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    protected FenceQueryBase(P parameters) {
        super(parameters);
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
    private void Alert(AuditLogType logType, String reason) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getVdsId());
        alert.addCustomValue("Reason", reason);
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts if power management status failed.
     */
    protected void AlertPowerManagementStatusFailed(String reason) {
        Alert(AuditLogType.VDS_ALERT_FENCE_TEST_FAILED, reason);
    }
}
