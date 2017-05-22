package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.constants.QueryConstants;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.dao.SystemStatisticsDao;

public class GetSystemStatisticsQuery<P extends GetSystemStatisticsQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private SystemStatisticsDao systemStatisticsDao;

    public GetSystemStatisticsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private static final char COMMA_DELIMITER = ',';
    public static final String VM_ENTITY_NAME = "VM";
    public static final String HOST_ENTITY_NAME = "HOST";
    public static final String USER_ENTITY_NAME = "USER";
    public static final String ACTIVE_STORAGE_DOMAIN_ENTITY_NAME = "ASD";
    public static final String TOTAL_STORAGE_DOMAIN_ENTITY_NAME = "TSD";
    public static final String USER_ACTIVE_STATUS = "1";


    private Map<String, Integer> getSystemStatistics() {
        Map<String, Integer> res = new HashMap<>();

        // VMs:
        int totalVMs = getTotalVMsStat();
        int activeVMs = getActiveVMsStat();
        int downVMs = (totalVMs - activeVMs) < 0 ? 0 : (totalVMs - activeVMs);

        // Hosts:
        int totalHosts = getTotalHostsStat();
        int activeHosts = getActiveHostsStat();
        int maintenanceHosts = getMaintenanceHostsStat();
        int downHosts =
                (totalHosts - activeHosts - maintenanceHosts) < 0 ? 0 : (totalHosts - activeHosts - maintenanceHosts);

        res.put(QueryConstants.SYSTEM_STATS_TOTAL_VMS_FIELD, totalVMs);
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_VMS_FIELD, activeVMs);
        res.put(QueryConstants.SYSTEM_STATS_DOWN_VMS_FIELD, downVMs);
        res.put(QueryConstants.SYSTEM_STATS_TOTAL_HOSTS_FIELD, totalHosts);
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_HOSTS_FIELD, activeHosts);
        res.put(QueryConstants.SYSTEM_STATS_MAINTENANCE_HOSTS_FIELD, maintenanceHosts);
        res.put(QueryConstants.SYSTEM_STATS_DOWN_HOSTS_FIELD, downHosts);
        res.put(QueryConstants.SYSTEM_STATS_TOTAL_USERS_FIELD, getTotalUsersStat());
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_USERS_FIELD, getActiveUsersStat());
        res.put(QueryConstants.SYSTEM_STATS_TOTAL_STORAGE_DOMAINS_FIELD, getTotalStorageDomainsStat());
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_STORAGE_DOMAINS_FIELD, getActiveStorageDomainsStat());

        return res;
    }

    private int getTotalVMsStat() {
        return systemStatisticsDao.getSystemStatisticsValue(VM_ENTITY_NAME);
    }

    private int getActiveVMsStat() {
        String[] activeVmStatuses = {String.valueOf(VMStatus.Up.getValue()),
                String.valueOf(VMStatus.PoweringUp.getValue()),
                String.valueOf(VMStatus.MigratingTo.getValue()),
                String.valueOf(VMStatus.WaitForLaunch.getValue()),
                String.valueOf(VMStatus.RebootInProgress.getValue()),
                String.valueOf(VMStatus.PoweringDown.getValue()),
                String.valueOf(VMStatus.Paused.getValue()),
                String.valueOf(VMStatus.Unknown.getValue())};
        return systemStatisticsDao.getSystemStatisticsValue(VM_ENTITY_NAME,
                StringUtils.join(activeVmStatuses, COMMA_DELIMITER));
    }

    private int getTotalHostsStat() {
        return systemStatisticsDao.getSystemStatisticsValue(HOST_ENTITY_NAME);
    }

    private int getActiveHostsStat() {
        String[] activeVdsStatuses =
                {String.valueOf(VDSStatus.Up.getValue()),
                        String.valueOf(VDSStatus.PreparingForMaintenance.getValue())};
        return systemStatisticsDao.getSystemStatisticsValue(HOST_ENTITY_NAME,
                StringUtils.join(activeVdsStatuses, COMMA_DELIMITER));
    }

    private int getMaintenanceHostsStat() {
        return systemStatisticsDao.getSystemStatisticsValue(HOST_ENTITY_NAME,
                String.valueOf(VDSStatus.Maintenance.getValue()));
    }

    private int getTotalUsersStat() {
        return systemStatisticsDao.getSystemStatisticsValue(USER_ENTITY_NAME);
    }

    private int getActiveUsersStat() {
        return systemStatisticsDao.getSystemStatisticsValue(USER_ENTITY_NAME, USER_ACTIVE_STATUS);
    }

    private int getTotalStorageDomainsStat() {
        return systemStatisticsDao.getSystemStatisticsValue(TOTAL_STORAGE_DOMAIN_ENTITY_NAME);
    }

    private int getActiveStorageDomainsStat() {
        return systemStatisticsDao.getSystemStatisticsValue(ACTIVE_STORAGE_DOMAIN_ENTITY_NAME,
                String.valueOf(StorageDomainStatus.Active.getValue()));
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getSystemStatistics());
    }
}
