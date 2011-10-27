package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.businessentities.DiskImage;


public class DiskStatisticalQuery extends AbstractStatisticalQuery<Disk, DiskImage> {

    private static final Statistic DATA_READ  = create("data.current.read",  "Read data rate",  GAUGE, BYTES_PER_SECOND, DECIMAL);
    private static final Statistic DATA_WRITE = create("data.current.write", "Write data rate", GAUGE, BYTES_PER_SECOND, DECIMAL);

    protected DiskStatisticalQuery(Disk parent) {
        this(null, parent);
    }

    protected DiskStatisticalQuery(AbstractBackendResource<Disk, DiskImage>.EntityIdResolver resolver, Disk parent) {
        super(Disk.class, parent, resolver);
    }

    public List<Statistic> getStatistics(DiskImage i) {
        return asList(setDatum(clone(DATA_READ),  i.getread_rate()),
                      setDatum(clone(DATA_WRITE), i.getwrite_rate()));
    }

    public Statistic adopt(Statistic statistic) {
        // clone required because LinkHelper unsets the grandparent
         statistic.setDisk(clone(parent));
         return statistic;
     }

    private Disk clone(Disk parent) {
        Disk disk = new Disk();
        disk.setId(parent.getId());
        disk.setVm(new VM());
        disk.getVm().setId(parent.getVm().getId());
        return disk;
    }
}
