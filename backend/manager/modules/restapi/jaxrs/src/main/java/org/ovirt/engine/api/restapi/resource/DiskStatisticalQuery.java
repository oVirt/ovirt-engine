package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StatisticUnit;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Guid;


public class DiskStatisticalQuery extends AbstractStatisticalQuery<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> {

    static final Statistic DATA_READ  = create("data.current.read",  "Read data rate",  GAUGE, BYTES_PER_SECOND, DECIMAL);
    static final Statistic DATA_READ_OPS  = create("data.current.read_ops",  "Read data ops",  GAUGE, StatisticUnit.SECONDS, DECIMAL);
    static final Statistic DATA_WRITE = create("data.current.write", "Write data rate", GAUGE, BYTES_PER_SECOND, DECIMAL);
    static final Statistic DATA_WRITE_OPS = create("data.current.write_ops", "Write data ops", GAUGE, StatisticUnit.SECONDS, DECIMAL);
    static final Statistic READ_LATENCY = create("disk.read.latency", "Read latency", GAUGE, StatisticUnit.SECONDS, DECIMAL);
    static final Statistic WRITE_LATENCY = create("disk.write.latency", "Write latency", GAUGE, StatisticUnit.SECONDS, DECIMAL);
    static final Statistic FLUSH_LATENCY = create("disk.flush.latency", "Flush latency", GAUGE, StatisticUnit.SECONDS, DECIMAL);


    protected DiskStatisticalQuery(Disk parent) {
        this(null, parent);
    }

    protected DiskStatisticalQuery(AbstractBackendResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>.EntityIdResolver<Guid> resolver, Disk parent) {
        super(Disk.class, parent, resolver);
    }

    public List<Statistic> getStatistics(org.ovirt.engine.core.common.businessentities.storage.Disk i) {
        if (i.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage disk = (DiskImage) i;
            return asList(setDatum(clone(DATA_READ), disk.getReadRate()),
                      setDatum(clone(DATA_READ_OPS), disk.getReadOps()),
                      setDatum(clone(DATA_WRITE), disk.getWriteRate()),
                      setDatum(clone(DATA_WRITE_OPS), disk.getWriteOps()),
                      setDatum(clone(READ_LATENCY), disk.getReadLatency()==null ? 0.0 : disk.getReadLatency()),
                      setDatum(clone(WRITE_LATENCY), disk.getWriteLatency()==null ? 0.0 : disk.getWriteLatency()),
                      setDatum(clone(FLUSH_LATENCY), disk.getFlushLatency()==null ? 0.0 : disk.getFlushLatency()));
        }
        return asList(setDatum(clone(DATA_READ), 0), setDatum(clone(DATA_WRITE), 0));
    }

    public Statistic adopt(Statistic statistic) {
        // clone required because LinkHelper unsets the grandparent
         statistic.setDisk(clone(parent));
         return statistic;
     }

    private Disk clone(Disk parent) {
        Disk disk = new Disk();
        disk.setId(parent.getId());
        if (parent.isSetVm()) {
            disk.setVm(new Vm());
            disk.getVm().setId(parent.getVm().getId());
        }
        return disk;
    }
}
