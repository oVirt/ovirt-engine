package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;

public class EventDao extends BaseDao {

    private static final String SEVERITY = "severity"; //$NON-NLS-1$
    private static final String COUNT = "count"; //$NON-NLS-1$

    /**
     * Query for obtaining the event data needed to populate the event inventory object.
     */
    private static final String AUDIT_LOG_COUNT = "event.audit_log_count"; //$NON-NLS-1$

    public EventDao(DataSource engineDataSource) throws DashboardDataException {
        super(engineDataSource, "EventDAO.properties", EventDao.class); //$NON-NLS-1$
    }

    public Map<AuditLogSeverity, Integer> getEventStatusCount() throws DashboardDataException {
        final Map<AuditLogSeverity, Integer> result = new HashMap<>();

        runQuery(AUDIT_LOG_COUNT, rs -> result.put(AuditLogSeverity.forValue(rs.getInt(SEVERITY)), rs.getInt(COUNT)));

        return result;
    }

}
