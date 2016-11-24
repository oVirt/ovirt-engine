package org.ovirt.engine.core.builder;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.springframework.stereotype.Repository;

@Repository
public class VdsBuilder extends AbstractBuilder<VDS, VdsBuilder> {

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private VdsStatisticsDao vdsStatisticsDao;

    @Inject
    private VdsDao vdsDao;

    public VdsBuilder id(final Guid hostId) {
        object.setId(hostId);
        return this;
    }

    public VdsBuilder cluster(final Cluster cluster) {
        object.setClusterId(cluster.getId());
        return this;
    }

    public VdsBuilder status(final VDSStatus status) {
        object.setStatus(status);
        return this;
    }

    public VdsBuilder name(final String name) {
        object.setVdsName(name);
        return this;
    }

    public VdsBuilder hostName(final String hostName) {
        object.setHostName(hostName);
        return this;
    }

    public VdsBuilder physicalMemory(final int memoryInMb) {
        object.setPhysicalMemMb(memoryInMb);
        object.setMemAvailable((long)memoryInMb);
        object.setMemFree((long)memoryInMb);
        object.setMemCommited(0);
        return this;
    }

    @Override
    protected void prePersist() {
        if (Guid.isNullOrEmpty(object.getId())) {
            object.setId(Guid.newGuid());
        }

        if (StringUtils.isEmpty(object.getHostName())) {
            object.setHostName(RandomUtils.instance().nextString(10));
        }
    }

    @Override
    protected void preBuild() {
        if (Guid.isNullOrEmpty(object.getId())) {
            object.setId(Guid.newGuid());
        }
    }

    @Override
    public VdsBuilder reset() {
        object = new VDS();
        return this;
    }

    @Override
    public VdsBuilder reset(VDS object) {
        this.object = object;
        return this;
    }

    @Override
    protected VDS doPersist() {
        vdsStaticDao.save(object.getStaticData());
        vdsDynamicDao.save(object.getDynamicData());
        vdsStatisticsDao.save(object.getStatisticsData());
        return vdsDao.get(object.getId());
    }
}
