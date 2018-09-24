package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.ValueType;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;


public class VmStatisticalQuery extends AbstractStatisticalQuery<Vm, org.ovirt.engine.core.common.businessentities.VM> {

    private static final Statistic MEM_CONFIG   = create("memory.installed",       "Total memory configured", GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_USED     = create("memory.used",            "Memory used (agent)",  GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_FREE     = create("memory.free",            "Memory free (agent)",  GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_BUFFERED = create("memory.buffered",        "Memory buffered (agent)",  GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_CACHED   = create("memory.cached",          "Memory cached (agent)",  GAUGE, BYTES,   INTEGER);
    private static final Statistic CPU_GUEST    = create("cpu.current.guest",      "CPU used by guest",    GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_OVERHEAD = create("cpu.current.hypervisor", "CPU overhead",         GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_TOTAL    = create("cpu.current.total",      "Total CPU used",       GAUGE, PERCENT, DECIMAL);
    private static final Statistic NETWORK_TOTAL    = create("network.current.total",      "Total network used",       GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_USAGE_HISTORY    = create("cpu.usage.history",      "List of CPU usage history, sorted by date from newest to oldest, at intervals of 30 seconds",       GAUGE, PERCENT, DECIMAL);
    private static final Statistic MEM_USAGE_HISTORY    = create("memory.usage.history",      "List of memory usage history, sorted by date from newest to oldest, at intervals of 30 seconds",       GAUGE, PERCENT, DECIMAL);
    private static final Statistic NETWORK_USAGE_HISTORY    = create("network.usage.history",      "List of network usage history, sorted by date from newest to oldest, at intervals of 30 seconds",       GAUGE, PERCENT, DECIMAL);
    private static final Statistic MIGRATION_PROGRESS = create("migration.progress",      "Migration Progress",       GAUGE, PERCENT, DECIMAL);
    private static final Statistic DISKS_USAGE = create("disks.usage",      "Disk usage, in bytes, per filesystem as JSON (agent)",       GAUGE, NONE, ValueType.STRING);

    protected VmStatisticalQuery(Vm parent) {
        this(null, parent);
    }

    protected VmStatisticalQuery(AbstractBackendResource<Vm, org.ovirt.engine.core.common.businessentities.VM>.EntityIdResolver<Guid> entityResolver, Vm parent) {
        super(Vm.class, parent, entityResolver);
    }

    public List<Statistic> getStatistics(org.ovirt.engine.core.common.businessentities.VM entity) {
        VmStatistics s = entity.getStatisticsData();
        long mem = entity.getMemSizeMb() * Mb;
        long memUsedByCent = s.getUsageMemPercent()==null ? 0 : mem * s.getUsageMemPercent();
        long memFree = entity.getGuestMemoryFree() == null ? 0 : entity.getGuestMemoryFree() * Kb;
        long memBuffered = entity.getGuestMemoryBuffered() == null ? 0 : entity.getGuestMemoryBuffered() * Kb;
        long memCached = entity.getGuestMemoryCached() == null ? 0 : entity.getGuestMemoryCached() * Kb;
        long migrationProgress = entity.getMigrationProgressPercent() != null ? entity.getMigrationProgressPercent() : 0;
        int networkUsagePercent = entity.getUsageNetworkPercent() != null ? entity.getUsageNetworkPercent() : 0;
        String disksUsage = s.getDisksUsage() != null ? s.getDisksUsage() : "";

        Double zero = 0.0;
        Double cpuUser = s.getCpuUser()==null ? zero : s.getCpuUser();
        Double cpuSys = s.getCpuSys()==null ? zero : s.getCpuSys();
        List<Statistic> statistics = asList(setDatum(clone(MEM_CONFIG), mem),
                setDatum(clone(MEM_USED), memUsedByCent / 100),
                setDatum(clone(CPU_GUEST), cpuUser),
                setDatum(clone(CPU_OVERHEAD), cpuSys),
                setDatum(clone(CPU_TOTAL), cpuUser + cpuSys),
                setDatum(clone(MIGRATION_PROGRESS), migrationProgress),
                setDatum(clone(MEM_BUFFERED), memBuffered),
                setDatum(clone(MEM_CACHED), memCached),
                setDatum(clone(MEM_FREE), memFree),
                setDatum(clone(NETWORK_TOTAL), networkUsagePercent),
                setDatum(clone(DISKS_USAGE), disksUsage)
        );

        statistics.add(addHistoryData(clone(CPU_USAGE_HISTORY), entity.getCpuUsageHistory()));
        statistics.add(addHistoryData(clone(MEM_USAGE_HISTORY), entity.getMemoryUsageHistory()));
        statistics.add(addHistoryData(clone(NETWORK_USAGE_HISTORY), entity.getNetworkUsageHistory()));

        return statistics;
    }

    public Statistic adopt(Statistic statistic) {
        statistic.setVm(parent);
        return statistic;
    }

    private Statistic addHistoryData(Statistic statistic, List<Integer> list) {
        if (list != null) {
            List<Integer> cpy = new ArrayList<>(list);
            Collections.reverse(cpy);
            cpy.stream().forEach(i -> setDatum(statistic, i));
        }

        return statistic;
    }
}
