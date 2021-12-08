package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;


public class HostStatisticalQuery extends AbstractStatisticalQuery<Host, VDS> {

    private static final Statistic MEM_TOTAL   = create("memory.total",       "Total memory",    GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_USED    = create("memory.used",        "Used memory",           GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_FREE    = create("memory.free",        "Free memory",           GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_SHARED = create("memory.shared",
            "The amount of memory, in bytes, shared among Virtual-Machines on this Host by Kernel Same-page Merging (KSM)",
            GAUGE,
            BYTES,
            INTEGER);
    private static final Statistic SWAP_TOTAL  = create("swap.total",         "Total swap",            GAUGE, BYTES,   INTEGER);
    private static final Statistic SWAP_FREE   = create("swap.free",          "Free swap",             GAUGE, BYTES,   INTEGER);
    private static final Statistic SWAP_USED   = create("swap.used",          "Used swap",             GAUGE, BYTES,   INTEGER);
    private static final Statistic CPU_KSM     = create("ksm.cpu.current",    "KSM CPU usage",         GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_USER    = create("cpu.current.user",   "User CPU usage",    GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_SYS     = create("cpu.current.system", "System CPU usage",      GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_IDLE    = create("cpu.current.idle",   "Idle CPU usage",        GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_LOAD    = create("cpu.load.avg.5m",    "CPU 5 minute load average", GAUGE, PERCENT, DECIMAL);
    private static final Statistic BOOT_TIME   = create("boot.time",          "Boot time of the machine", GAUGE, NONE, INTEGER);

    protected HostStatisticalQuery(Host parent) {
        this(null, parent);
    }

    protected HostStatisticalQuery(AbstractBackendResource<Host, VDS>.EntityIdResolver<Guid> entityResolver, Host parent) {
        super(Host.class, parent, entityResolver);
    }

    public List<Statistic> getStatistics(VDS entity) {
        VdsStatistics s = entity.getStatisticsData();
        // if user queries host statistics before host installation completed, null values are possible (therefore added checks).
        long memTotal = entity.getPhysicalMemMb()==null ? 0 : entity.getPhysicalMemMb() * Mb;
        long memUsed = (s==null || s.getUsageMemPercent()==null) ? 0 : memTotal * s.getUsageMemPercent() / 100;
        List<Statistic> statistics = asList(setDatum(clone(MEM_TOTAL),   memTotal),
                      setDatum(clone(MEM_USED),    memUsed),
                      setDatum(clone(MEM_FREE),    memTotal-memUsed),
                      setDatum(clone(MEM_SHARED),  (s==null || s.getMemShared()==null) ? 0 : s.getMemShared()*Mb),
                      setDatum(clone(SWAP_TOTAL),  (s==null || s.getSwapTotal()==null) ? 0 : s.getSwapTotal()*Mb),
                      setDatum(clone(SWAP_FREE),   (s==null || s.getSwapFree()==null) ? 0 : s.getSwapFree()*Mb),
                      setDatum(clone(SWAP_USED),   getSwapUsed(s)*Mb),
                      setDatum(clone(CPU_KSM),     (s==null || s.getKsmCpuPercent()==null) ? 0 : s.getKsmCpuPercent()),
                      setDatum(clone(CPU_USER),    (s==null || s.getCpuUser()==null) ? 0 : s.getCpuUser()),
                      setDatum(clone(CPU_SYS),     (s==null || s.getCpuSys()==null) ? 0 : s.getCpuSys()),
                      setDatum(clone(CPU_IDLE),    (s==null || s.getCpuIdle()==null) ? 0 : s.getCpuIdle()),
                      setDatum(clone(CPU_LOAD),    (s==null || s.getCpuLoad()==null) ? 0 : s.getCpuLoad()/100),
                      setDatum(clone(BOOT_TIME),   (s==null || s.getBootTime()==null) ? 0 : s.getBootTime()));

        if (s != null && s.getHugePages() != null) {
            s.getHugePages().stream()
                    .filter(page -> page.getFree() != null)
                    .map(this::createHugePagesFree)
                    .forEach(statistics::add);

            s.getHugePages().stream()
                    .filter(page -> page.getTotal() != null)
                    .map(this::createHugePagesTotal)
                    .forEach(statistics::add);
        }

        return statistics;
    }

    private Statistic createHugePagesFree(HugePage page) {
        return setDatum(
                create("hugepages." + page.getSizeKB() + ".free", "Amount of free huge pages of the given size", GAUGE, NONE, INTEGER),
                page.getFree());
    }

    private Statistic createHugePagesTotal(HugePage page) {
        return setDatum(
                create("hugepages." + page.getSizeKB() + ".total", "Amount of total huge pages of the given size", GAUGE, NONE, INTEGER),
                page.getTotal());
    }

    public Statistic adopt(Statistic statistic) {
        statistic.setHost(parent);
        return statistic;
    }

    private long getSwapUsed(VdsStatistics s) {
        if (s==null) {
            return 0;
        } else {
            if (s.getSwapTotal()==null) {
                return 0;
            } else {
                if (s.getSwapFree()==null) {
                    return s.getSwapTotal();
                } else {
                    return s.getSwapTotal() - s.getSwapFree();
                }
            }
        }
    }
}
