package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmJobDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public class VmAnalyzerFactory {

    private final VdsManager vdsManager;
    private final boolean updateStatistics;

    private Supplier<Map<Integer, VdsNumaNode>> vdsNumaNodesProvider;

    private AuditLogDirector auditLogDirector;
    private ResourceManager resourceManager;

    private VmStaticDao vmStaticDao;
    private VmDynamicDao vmDynamicDao;
    private VmDao vmDao;
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    private VdsDao vdsDao;
    private VmJobDao vmJobDao;
    private VdsNumaNodeDao vdsNumaNodeDao;
    private VmNumaNodeDao vmNumaNodeDao;

    public VmAnalyzerFactory(
            VdsManager vdsManager,
            boolean updateStatistics,
            AuditLogDirector auditLogDirector,
            ResourceManager resourceManager,
            VmStaticDao vmStaticDao,
            VmDynamicDao vmDynamicDao,
            VmDao vmDao,
            VmNetworkInterfaceDao vmNetworkInterfaceDao,
            VdsDao vdsDao,
            VmJobDao vmJobDao,
            VdsNumaNodeDao vdsNumaNodeDao,
            VmNumaNodeDao vmNumaNodeDao) {
        this.vdsManager = vdsManager;
        this.updateStatistics = updateStatistics;
        this.auditLogDirector = auditLogDirector;
        this.resourceManager = resourceManager;
        this.vmStaticDao = vmStaticDao;
        this.vmDynamicDao = vmDynamicDao;
        this.vmDao = vmDao;
        this.vmNetworkInterfaceDao = vmNetworkInterfaceDao;
        this.vdsDao = vdsDao;
        this.vmJobDao = vmJobDao;
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

    protected VmAnalyzer getVmAnalyzer(Pair<VM, VmInternalData> monitoredVm) {
        return new VmAnalyzer(
                monitoredVm.getFirst(),
                monitoredVm.getSecond(),
                updateStatistics,
                vdsManager,
                auditLogDirector,
                resourceManager,
                vmStaticDao,
                vmDynamicDao,
                vmDao,
                vmNetworkInterfaceDao,
                vdsDao,
                vmJobDao,
                vdsNumaNodesProvider,
                vmNumaNodeDao);
    }
}
