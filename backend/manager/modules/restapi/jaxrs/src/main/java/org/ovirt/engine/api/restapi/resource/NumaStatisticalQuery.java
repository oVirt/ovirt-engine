package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.NumaNode;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class NumaStatisticalQuery extends AbstractStatisticalQuery<NumaNode, VdsNumaNode> {

    private static final Statistic MEM_TOTAL   = create("memory.total",       "Total memory",    GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_USED    = create("memory.used",        "Used memory",           GAUGE, BYTES,   INTEGER);
    private static final Statistic MEM_FREE    = create("memory.free",        "Free memory",           GAUGE, BYTES,   INTEGER);
    private static final Statistic CPU_USER    = create("cpu.current.user",   "User CPU usage",    GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_SYS     = create("cpu.current.system", "System CPU usage",      GAUGE, PERCENT, DECIMAL);
    private static final Statistic CPU_IDLE    = create("cpu.current.idle",   "Idle CPU usage",        GAUGE, PERCENT, DECIMAL);

    protected NumaStatisticalQuery(NumaNode parent) {
        this(null, parent);
    }

    protected NumaStatisticalQuery(AbstractBackendResource<NumaNode, VdsNumaNode>.EntityIdResolver<Guid> resolver, NumaNode parent) {
        super(NumaNode.class, parent, resolver);
    }

    @Override
    public List<Statistic> getStatistics(VdsNumaNode entity) {
        NumaNodeStatistics s = entity.getNumaNodeStatistics();
        long memTotal = entity.getMemTotal();
        long memFree = (s==null) ? 0 : s.getMemFree();
        return asList(setDatum(clone(MEM_TOTAL),   memTotal),
                setDatum(clone(MEM_USED),    memTotal-memFree),
                setDatum(clone(MEM_FREE),    memFree),
                setDatum(clone(CPU_USER),    (s==null) ? 0 : s.getCpuUser()),
                setDatum(clone(CPU_SYS),     (s==null) ? 0 : s.getCpuSys()),
                setDatum(clone(CPU_IDLE),    (s==null) ? 0 : s.getCpuIdle()));
    }

    @Override
    public Statistic adopt(Statistic statistic) {
        statistic.setHostNumaNode(clone(parent));
        return statistic;
    }

    private NumaNode clone(NumaNode parent) {
        NumaNode node = new NumaNode();
        node.setId(parent.getId());
        node.setHost(new Host());
        node.getHost().setId(parent.getHost().getId());
        return node;
    }
}
