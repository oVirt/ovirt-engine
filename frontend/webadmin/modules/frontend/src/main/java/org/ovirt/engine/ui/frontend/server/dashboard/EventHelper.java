package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.Map;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.EventDao;

public class EventHelper {

    /**
     * Looks up the total number of Alerts/Warning/Error events from the Audit log.
     * @param dataSource The {@code DataSource} to use for the queries.
     * @return An {@code InventoryEntitySummary} object containing the event data.
     * @throws DashboardDataException Unable to load query properties.
     */
    public static InventoryStatus getEventStatus(DataSource dataSource) throws DashboardDataException {
        InventoryStatus result = new InventoryStatus();
        EventDao dao = new EventDao(dataSource);
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
