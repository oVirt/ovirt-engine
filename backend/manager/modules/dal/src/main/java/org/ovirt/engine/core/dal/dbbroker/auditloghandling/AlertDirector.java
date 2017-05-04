package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AuditLogDao;

/**
 * AlertDirector
 */
@Singleton
public class AlertDirector {

    @Inject
    private AuditLogDao auditLogDao;

    /**
     * Removes the alert.
     *
     * @param vdsId
     *            The VDS id.
     * @param type
     *            The type.
     */
    public void removeVdsAlert(Guid vdsId, AuditLogType type) {
        auditLogDao.removeAllOfTypeForVds(vdsId, type.getValue());
    }

    /**
     * Removes the alert
     * @param volumeId
     *            The volume id
     * @param type
     *            The alert type
     */
    public void removeVolumeAlert(Guid volumeId, AuditLogType type) {
        auditLogDao.removeAllOfTypeForVolume(volumeId, type.getValue());
    }

    /**
     * Removes all alerts.
     *
     * @param vdsId
     *            The VDS id.
     * @param removeConfigAlerts
     *            if set to <c>true</c> [remove config alerts].
     */
    public void removeAllVdsAlerts(Guid vdsId, boolean removeConfigAlerts) {
        auditLogDao.removeAllForVds(vdsId, removeConfigAlerts);
    }

    /**
     * Removes the brick down alert.
     *
     * @param brickId
     *            The brick id.
     * @param logtype
     *            The type.
     */
    public void removeAlertsByBrickIdLogType(Guid brickId, AuditLogType logtype) {
        auditLogDao.removeAllofTypeForBrick(brickId, logtype.getValue());
    }
}
