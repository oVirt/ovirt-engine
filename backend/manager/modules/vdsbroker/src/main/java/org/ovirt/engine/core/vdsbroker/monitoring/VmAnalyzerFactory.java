package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

public class VmAnalyzerFactory {

    private final VdsManager vdsManager;
    private final boolean updateStatistics;

    private Supplier<Map<Integer, VdsNumaNode>> vdsNumaNodesProvider;

    private AuditLogDirector auditLogDirector;
    private ResourceManager resourceManager;

    private VmDynamicDao vmDynamicDao;
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    private VdsDynamicDao vdsDynamicDao;
    private VdsNumaNodeDao vdsNumaNodeDao;
    private VmNumaNodeDao vmNumaNodeDao;

    public VmAnalyzerFactory(
            VdsManager vdsManager,
            boolean updateStatistics,
            AuditLogDirector auditLogDirector,
            ResourceManager resourceManager,
            VmDynamicDao vmDynamicDao,
            VmNetworkInterfaceDao vmNetworkInterfaceDao,
            VdsDynamicDao vdsDynamicDao,
            VdsNumaNodeDao vdsNumaNodeDao,
            VmNumaNodeDao vmNumaNodeDao) {
        this.vdsManager = vdsManager;
        this.updateStatistics = updateStatistics;
        this.auditLogDirector = auditLogDirector;
        this.resourceManager = resourceManager;
        this.vmDynamicDao = vmDynamicDao;
        this.vmNetworkInterfaceDao = vmNetworkInterfaceDao;
        this.vdsDynamicDao = vdsDynamicDao;
        this.vdsNumaNodeDao = vdsNumaNodeDao;
        this.vmNumaNodeDao = vmNumaNodeDao;
        initProviders();
    }

    private void initProviders() {
        if (updateStatistics) {
            vdsNumaNodesProvider = new MemoizingSupplier<>(() -> {
                return vdsNumaNodeDao
                        .getAllVdsNumaNodeByVdsId(vdsManager.getVdsId()).stream()
                        .collect(toMap(VdsNumaNode::getIndex, Function.identity()));
            });
        }
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
                vdsNumaNodesProvider,
                vmNumaNodeDao,
                vmNetworkInterfaceDao);
    }

}
