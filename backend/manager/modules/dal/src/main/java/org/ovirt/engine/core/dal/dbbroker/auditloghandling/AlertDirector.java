package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * AlertDirector
 */
public final class AlertDirector {

    /**
     * Alerts the specified audit logable.
     *
     * @param auditLogable
     *            The audit logable.
     * @param logType
     *            Type of the log.
     */
    public static void Alert(AuditLogableBase auditLogable, AuditLogType logType, AuditLogDirector auditLogDirector) {
        auditLogDirector.log(auditLogable, logType);
    }

    public static void Alert(AuditLogableBase auditLogable, AuditLogType logType, AuditLogDirector auditLogDirector, String message) {
        auditLogDirector.log(auditLogable, logType, message);
    }

    public static void AddVdsAlert(Guid vdsId, AuditLogType type, AuditLogDirector auditLogDirector) {
        AddVdsAlert(vdsId, type, auditLogDirector, new AuditLogableBase());
    }

    /**
     * Adds an alert
     * @param vdsId
     * @param type
     */
    public static void AddVdsAlert(Guid vdsId, AuditLogType type, AuditLogDirector auditLogDirector, AuditLogableBase alert) {
        alert.setVdsId(vdsId);
        AlertDirector.Alert(alert, type, auditLogDirector);
    }
    /**
     * Removes the alert.
     *
     * @param vdsId
     *            The VDS id.
     * @param type
     *            The type.
     */
    public static void RemoveVdsAlert(Guid vdsId, AuditLogType type) {
        DbFacade.getInstance().getAuditLogDao().removeAllOfTypeForVds(vdsId, type.getValue());
    }

    /**
     * Removes all alerts.
     *
     * @param vdsId
     *            The VDS id.
     * @param removeConfigAlerts
     *            if set to <c>true</c> [remove config alerts].
     */
    public static void RemoveAllVdsAlerts(Guid vdsId, boolean removeConfigAlerts) {
        DbFacade.getInstance().getAuditLogDao().removeAllForVds(vdsId, removeConfigAlerts);
    }
}
