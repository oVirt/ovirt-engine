package org.ovirt.engine.core.bll.scheduling.utils;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuCores;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuLoad;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuPinning;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

public class HostCpuLoadHelper {

    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    private PendingResourceManager pendingResourceManager;

    private VdsManager hostManager;

    private VDS host;

    private boolean countThreadsAsCores;

    private boolean countPendingResources;

    public HostCpuLoadHelper(VDS host,
            ResourceManager resourceManager,
            VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper) {
        this(host, resourceManager, vdsCpuUnitPinningHelper, null, false);
    }

    public HostCpuLoadHelper(VDS host,
            ResourceManager resourceManager,
            VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper,
            boolean countThreadsAsCores) {
        this(host, resourceManager, vdsCpuUnitPinningHelper, null, countThreadsAsCores);
    }

    public HostCpuLoadHelper(VDS host,
            ResourceManager resourceManager,
            VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper,
            PendingResourceManager pendingResourceManager,
            boolean countThreadsAsCores) {
        super();
        this.host = host;
        this.hostManager = resourceManager.getVdsManager(host.getId());
        this.vdsCpuUnitPinningHelper = vdsCpuUnitPinningHelper;
        this.pendingResourceManager = pendingResourceManager;
        this.countPendingResources = pendingResourceManager != null;
        this.countThreadsAsCores = countThreadsAsCores;
    }

    public VDS getHost() {
        return host;
    }

    public boolean hostStatisticsPresent() {
        if (host.getCpuCores() == null || host.getCpuThreads() == null || host.getUsageCpuPercent() == null) {
            return false;
        }
        return true;
    }

    public int getEffectiveSharedCpuTotalLoad() {
        int pendingLoad = 0;

        if (countPendingResources) {
            pendingLoad = PendingCpuLoad.collectSharedForHost(pendingResourceManager, host.getId());
        }

        return getSharedCpuTotalLoad()
                + pendingLoad
                + getSpmCpuTotalLoad();
    }

    public double getEffectiveSharedCpuLoad() {
        return Math.ceil((double) getEffectiveSharedCpuTotalLoad() / (double) getEffectiveSharedPCpusCount());
    }

    public long getEffectiveSharedPCpusCount() {
        List<VdsCpuUnit> cpuTopology = getEffectiveCpuTopology();
        if (cpuTopology.isEmpty()) {
            return getEffectiveCpuCores();
        }

        return cpuTopology.stream()
                .filter(cpu -> !cpu.isExclusive())
                .count();
    }

    public int getEffectiveVmsSharedCpusCount() {
        int pendingCpusCount = 0;

        if (countPendingResources) {
            pendingCpusCount = PendingCpuCores.collectSharedForHost(pendingResourceManager, host.getId());
        }
        return getVmsSharedCpusCount() + pendingCpusCount;
    }

    private int getSpmCpuTotalLoad() {
        int spmCpuCount = host.getSpmStatus() == VdsSpmStatus.None
                ? 0
                : Config.<Integer> getValue(ConfigValues.SpmVCpuConsumption);

        return spmCpuCount *
                Config.<Integer> getValue(ConfigValues.VcpuConsumptionPercentage);
    }

    private int getSharedCpuTotalLoad() {
        int hostSharedLoad = getCpuTopology().stream()
                .filter(cpu -> !cpu.isExclusive())
                .mapToInt(cpu -> cpu.getCpuUsagePercent())
                .sum();

        // if the topology or statistics per cpu are not available, use the host usage
        if (hostSharedLoad == 0) {
            hostSharedLoad = host.getUsageCpuPercent() * getEffectiveCpuCores();
        }

        return hostSharedLoad;
    }

    private int getVmsSharedCpusCount() {
        return hostManager.getVmsSharedCpusCount();
    }

    private int getEffectiveCpuCores() {
        return SlaValidator.getEffectiveCpuCores(host, countThreadsAsCores);
    }

    private List<VdsCpuUnit> getEffectiveCpuTopology() {
        List<VdsCpuUnit> cpuTopology = getCpuTopology();

        if (countPendingResources) {
            Map<Guid, List<VdsCpuUnit>> vmToPendingDedicatedCpuPinnings =
                    PendingCpuPinning.collectForHost(pendingResourceManager, host.getId());

            vdsCpuUnitPinningHelper.previewPinOfPendingExclusiveCpus(cpuTopology, vmToPendingDedicatedCpuPinnings);
        }
        return cpuTopology;
    }

    private List<VdsCpuUnit> getCpuTopology() {
        return hostManager.getCpuTopology();
    }
}
