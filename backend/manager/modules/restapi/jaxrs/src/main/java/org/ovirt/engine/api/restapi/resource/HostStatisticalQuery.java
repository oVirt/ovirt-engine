package org.ovirt.engine.api.restapi.resource;

import java.math.BigDecimal;
import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;


public class HostStatisticalQuery extends AbstractStatisticalQuery<Host, VDS> {

    private final static Statistic MEM_TOTAL   = create("memory.total",       "Total memory",    GAUGE, BYTES,   INTEGER);
    private final static Statistic MEM_USED    = create("memory.used",        "Used memory",           GAUGE, BYTES,   INTEGER);
    private final static Statistic MEM_FREE    = create("memory.free",        "Free memory",           GAUGE, BYTES,   INTEGER);
    private final static Statistic MEM_BUFFERS = create("memory.buffers",     "IO buffers",            GAUGE, BYTES,   INTEGER);
    private final static Statistic MEM_CACHED  = create("memory.cached",      "OS caches",     GAUGE, BYTES,   INTEGER);
    private final static Statistic SWAP_TOTAL  = create("swap.total",         "Total swap",            GAUGE, BYTES,   INTEGER);
    private final static Statistic SWAP_FREE   = create("swap.free",          "Free swap",             GAUGE, BYTES,   INTEGER);
    private final static Statistic SWAP_USED   = create("swap.used",          "Used swap",             GAUGE, BYTES,   INTEGER);
    private final static Statistic SWAP_CACHED = create("swap.cached",        "Swap also in memory",   GAUGE, BYTES,   INTEGER);
    private final static Statistic CPU_KSM     = create("ksm.cpu.current",    "KSM CPU usage",         GAUGE, PERCENT, DECIMAL);
    private final static Statistic CPU_USER    = create("cpu.current.user",   "User CPU usage",    GAUGE, PERCENT, DECIMAL);
    private final static Statistic CPU_SYS     = create("cpu.current.system", "System CPU usage",      GAUGE, PERCENT, DECIMAL);
    private final static Statistic CPU_IDLE    = create("cpu.current.idle",   "Idle CPU usage",        GAUGE, PERCENT, DECIMAL);
    private final static Statistic CPU_LOAD    = create("cpu.load.avg.5m",    "CPU 5 minute load average", GAUGE, PERCENT, DECIMAL);

    protected HostStatisticalQuery(Host parent) {
        this(null, parent);
    }

    protected HostStatisticalQuery(AbstractBackendResource<Host, VDS>.EntityIdResolver entityResolver, Host parent) {
        super(Host.class, parent, entityResolver);
    }

    public List<Statistic> getStatistics(VDS entity) {
        VdsStatistics s = entity.getStatisticsData();
        long mem = entity.getphysical_mem_mb() * Mb;
        long memUsedByCent = mem * s.getusage_mem_percent();
        long swapUsed = s.getswap_total() - s.getswap_free();
        return asList(setDatum(clone(MEM_TOTAL),   mem),
                      setDatum(clone(MEM_USED),    new BigDecimal(memUsedByCent).divide(CENT)),
                      setDatum(clone(MEM_FREE),    s.getmem_available()*Mb),
                      setDatum(clone(MEM_BUFFERS), 0),
                      setDatum(clone(MEM_CACHED),  0),
                      setDatum(clone(SWAP_TOTAL),  s.getswap_total()*Mb),
                      setDatum(clone(SWAP_FREE),   s.getswap_free()*Mb),
                      setDatum(clone(SWAP_USED),   swapUsed*Mb),
                      setDatum(clone(SWAP_CACHED), 0),
                      setDatum(clone(CPU_KSM),     s.getksm_cpu_percent()),
                      setDatum(clone(CPU_USER),    s.getcpu_user()),
                      setDatum(clone(CPU_SYS),     s.getcpu_sys()),
                      setDatum(clone(CPU_IDLE),    s.getcpu_idle()),
                      setDatum(clone(CPU_LOAD),    s.getcpu_load()/100));
    }

    public Statistic adopt(Statistic statistic) {
        statistic.setHost(parent);
        return statistic;
    }
}
