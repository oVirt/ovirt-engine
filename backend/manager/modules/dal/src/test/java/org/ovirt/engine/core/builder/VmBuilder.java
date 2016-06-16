package org.ovirt.engine.core.builder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.springframework.stereotype.Repository;

@Repository
public class VmBuilder extends AbstractBuilder<VM, VmBuilder> {

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmStatisticsDao vmStatisticsDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmDao vmDao;

    public VmBuilder id(final Guid vmId) {
        object.setId(vmId);
        return this;
    }

    public VmBuilder cluster(final Cluster cluster) {
        object.setClusterId(cluster.getId());
        return this;
    }

    public VmBuilder status(final VMStatus status) {
        object.setStatus(status);
        return this;
    }

    public VmBuilder name(final String name) {
        object.setName(name);
        return this;
    }

    public VmBuilder up() {
        object.setStatus(VMStatus.Up);
        return this;
    }

    public VmBuilder down() {
        object.setStatus(VMStatus.Down);
        return this;
    }

    public VmBuilder host(final VDS host) {
        object.setRunOnVds(host.getId());
        object.setClusterId(host.getClusterId());
        return this;
    }

    public VmBuilder pinToHosts(VDS... hosts) {
        final List<Guid> hostIds = Stream.of(hosts).map(VDS::getId).collect(Collectors.toList());
        object.setDedicatedVmForVdsList(hostIds);
        object.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        return this;
    }

    public VmBuilder pinToHost(VDS host) {
        return pinToHosts(host);
    }

    public VmBuilder preferHosts(VDS... hosts) {
        pinToHosts(hosts);
        object.setMigrationSupport(MigrationSupport.MIGRATABLE);
        return this;
    }

    public VmBuilder preferHost(VDS host) {
        return preferHosts(host);
    }

    @Override
    protected void prePersist() {
        if (Guid.isNullOrEmpty(object.getId())) {
            object.setId(Guid.newGuid());
        }
        if (StringUtils.isEmpty(object.getName())) {
            object.setName(RandomUtils.instance().nextString(10));
        }
        if (object.getClusterId() == null) {
            throw new NullPointerException("No cluster, or not persistent cluster specified");
        }
    }

    @Override
    public VmBuilder reset() {
        object = new VM();
        return this;
    }

    @Override
    public VmBuilder reset(VM object) {
        this.object = object;
        return this;
    }

    @Override
    protected VM doPersist() {
        vmStaticDao.save(object.getStaticData());
        vmDynamicDao.save(object.getDynamicData());
        vmStatisticsDao.save(object.getStatisticsData());
        return vmDao.get(object.getId());
    }
}
