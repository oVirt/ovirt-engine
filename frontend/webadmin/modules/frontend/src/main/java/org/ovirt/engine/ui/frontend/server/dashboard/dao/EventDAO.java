package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;

public class EventDAO {
    private static final String SEVERITY = "severity"; //$NON-NLS-1$
    private static final String COUNT = "count"; //$NON-NLS-1$

    /**
     * Query for obtaining the event data needed to populate the event inventory object.
     */
    private static final String AUDIT_LOG_COUNT = "event.audit_log_count"; //$NON-NLS-1$

    private final DataSource engineDataSource;
    private final Properties eventProperties;

    public EventDAO(DataSource engineDataSource) throws DashboardDataException {
        this.engineDataSource = engineDataSource;
        eventProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("EventDAO.properties")) { //$NON-NLS-1$
            eventProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public Map<AuditLogSeverity, Integer> getEventStatusCount() throws SQLException {
        Map<AuditLogSeverity, Integer> result = new HashMap<>();
        try (Connection con = engineDataSource.getConnection();
                PreparedStatement cpuSummary = con.prepareStatement(
                        eventProperties.getProperty(AUDIT_LOG_COUNT));
                ResultSet rs = cpuSummary.executeQuery()) {
            while (rs.next()) {
                result.put(AuditLogSeverity.forValue(rs.getInt(SEVERITY)), rs.getInt(COUNT));
            }
        }
        return result;
    }

}
