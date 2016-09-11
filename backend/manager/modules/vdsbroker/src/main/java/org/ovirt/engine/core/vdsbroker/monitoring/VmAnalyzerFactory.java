package org.ovirt.engine.core.vdsbroker.monitoring;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

public class VmAnalyzerFactory {

    private final VdsManager vdsManager;
    private final boolean updateStatistics;

    private AuditLogDirector auditLogDirector;
    private ResourceManager resourceManager;

    private VmDynamicDao vmDynamicDao;
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    private VdsDynamicDao vdsDynamicDao;

    public VmAnalyzerFactory(
            VdsManager vdsManager,
            boolean updateStatistics,
            AuditLogDirector auditLogDirector,
            ResourceManager resourceManager,
            VmDynamicDao vmDynamicDao,
            VmNetworkInterfaceDao vmNetworkInterfaceDao,
            VdsDynamicDao vdsDynamicDao) {
        this.vdsManager = vdsManager;
        this.updateStatistics = updateStatistics;
        this.auditLogDirector = auditLogDirector;
        this.resourceManager = resourceManager;
        this.vmDynamicDao = vmDynamicDao;
        this.vmNetworkInterfaceDao = vmNetworkInterfaceDao;
        this.vdsDynamicDao = vdsDynamicDao;
    }

    protected VmAnalyzer getVmAnalyzer(Pair<VmDynamic, VdsmVm> monitoredVm) {
        // the VM that was reported by vdsm
        VdsmVm vdsmVm = monitoredVm.getSecond();

        // VM from the database running on the monitored host, might be null
        VmDynamic dbVmOnMonitoredHost = monitoredVm.getFirst();
        VmDynamic dbVm = dbVmOnMonitoredHost != null ? dbVmOnMonitoredHost : vmDynamicDao.get(vdsmVm.getVmDynamic().getId());

        return new VmAnalyzer(
                dbVm,
                vdsmVm,
                updateStatistics,
                vdsManager,
                auditLogDirector,
                resourceManager,
                vdsDynamicDao,
                vmNetworkInterfaceDao);
    }

}
