package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
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

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private ResourceManager resourceManager;

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmJobDao vmJobDao;
    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    public VmAnalyzerFactory(VdsManager vdsManager, boolean updateStatistics) {
        this.vdsManager = vdsManager;
        this.updateStatistics = updateStatistics;
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
                diskDao,
                vmJobDao,
                vdsNumaNodesProvider,
                vmNumaNodeDao);
    }
}
