package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;


public class DiskStatisticalQuery extends AbstractStatisticalQuery<Disk, org.ovirt.engine.core.common.businessentities.Disk> {

    private static final Statistic DATA_READ  = create("data.current.read",  "Read data rate",  GAUGE, BYTES_PER_SECOND, DECIMAL);
    private static final Statistic DATA_WRITE = create("data.current.write", "Write data rate", GAUGE, BYTES_PER_SECOND, DECIMAL);

    protected DiskStatisticalQuery(Disk parent) {
        this(null, parent);
    }

    protected DiskStatisticalQuery(AbstractBackendResource<Disk, org.ovirt.engine.core.common.businessentities.Disk>.EntityIdResolver resolver, Disk parent) {
        super(Disk.class, parent, resolver);
    }

    public List<Statistic> getStatistics(org.ovirt.engine.core.common.businessentities.Disk i) {
        if (i.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage disk = (DiskImage) i;
            return asList(setDatum(clone(DATA_READ), disk.getread_rate()),
                      setDatum(clone(DATA_WRITE), disk.getwrite_rate()));
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
        disk.setVm(new VM());
        disk.getVm().setId(parent.getVm().getId());
        return disk;
    }
}
