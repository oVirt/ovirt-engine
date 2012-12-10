package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;


public class VmStatisticalQuery extends AbstractStatisticalQuery<VM, org.ovirt.engine.core.common.businessentities.VM> {

    private static final Statistic MEM_CONFIG   = create("memory.installed",       "Total memory configured", GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_USED     = create("memory.used",            "Memory used (agent)",  GAUGE, BYTES,   INTEGER);
    private static final Statistic CPU_GUEST    = create("cpu.current.guest",      "CPU used by guest",    GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_OVERHEAD = create("cpu.current.hypervisor", "CPU overhead",         GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_TOTAL    = create("cpu.current.total",      "Total CPU used",       GAUGE, PERCENT, DECIMAL);

    protected VmStatisticalQuery(VM parent) {
        this(null, parent);
    }

    protected VmStatisticalQuery(AbstractBackendResource<VM, org.ovirt.engine.core.common.businessentities.VM>.EntityIdResolver<Guid> entityResolver, VM parent) {
        super(VM.class, parent, entityResolver);
    }

    public List<Statistic> getStatistics(org.ovirt.engine.core.common.businessentities.VM entity) {
        VmStatistics s = entity.getStatisticsData();
        long mem = entity.getMemSizeMb() * Mb;
        long memUsedByCent = s.getusage_mem_percent()==null ? 0 : mem * s.getusage_mem_percent();
        Double cpuUser = s.getcpu_user()==null ? 0 : s.getcpu_user();
        Double cpuSys = s.getcpu_sys()==null ? 0 : s.getcpu_sys();
        return asList(setDatum(clone(MEM_CONFIG),   mem),
                      setDatum(clone(MEM_USED),     (int)(memUsedByCent/100)),
                      setDatum(clone(CPU_GUEST),    cpuUser),
                      setDatum(clone(CPU_OVERHEAD), cpuSys),
                      setDatum(clone(CPU_TOTAL),    cpuUser + cpuSys));
    }

    public Statistic adopt(Statistic statistic) {
        statistic.setVm(parent);
        return statistic;
    }
}
