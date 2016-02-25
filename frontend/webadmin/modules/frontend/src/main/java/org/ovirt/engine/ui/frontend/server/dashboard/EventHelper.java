package org.ovirt.engine.ui.frontend.server.dashboard;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.EventDAO;

public class EventHelper {

    /**
     * Looks up the total number of Alerts/Warning/Error events from the Audit log.
     * @param dataSource The {@code DataSource} to use for the queries.
     * @return An {@code InventoryEntitySummary} object containing the event data.
     * @throws SQLException When there is an issue with the queries or database.
     * @throws DashboardDataException Unable to load query properties.
     */
    public static InventoryStatus getEventStatus(DataSource dataSource) throws SQLException, DashboardDataException {
        InventoryStatus result = new InventoryStatus();
        EventDAO dao = new EventDAO(dataSource);
        Map<AuditLogSeverity, Integer> data = dao.getEventStatusCount();
        for (Map.Entry<AuditLogSeverity, Integer> entry : data.entrySet()) {
            switch (entry.getKey()) {
                case ALERT:
                    result.setStatusCount(AuditLogSeverity.ALERT.name().toLowerCase(), entry.getValue());
                    result.setTotalCount(result.getTotalCount() + entry.getValue());
                    break;
                case ERROR:
                    result.setStatusCount(AuditLogSeverity.ERROR.name().toLowerCase(), entry.getValue());
                    result.setTotalCount(result.getTotalCount() + entry.getValue());
                    break;
                case NORMAL:
                    // Do nothing
                    break;
                case WARNING:
                    result.setStatusCount(AuditLogSeverity.WARNING.name().toLowerCase(), entry.getValue());
                    result.setTotalCount(result.getTotalCount() + entry.getValue());
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        return result;
    }
}
