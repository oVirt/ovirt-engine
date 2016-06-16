package org.ovirt.engine.core.builder;

import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ClusterBuilder extends AbstractBuilder<Cluster, ClusterBuilder> {

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private MacPoolBuilder macPoolBuilder;

    private MacPool macPool;

    public ClusterBuilder id(final Guid clusterId) {
        object.setId(clusterId);
        return this;
    }

    public ClusterBuilder name(final String name) {
        object.setName(name);
        return this;
    }

    public ClusterBuilder macPool(final MacPool macPool) {
        object.setMacPoolId(macPool.getId());
        this.macPool = macPool;
        return this;
    }

    @Override
    protected void prePersist() {
        populate();
        if (StringUtils.isEmpty(object.getName())) {
            object.setName(RandomUtils.instance().nextString(10));
        }
        if (Objects.isNull(macPool)) {
            object.setMacPoolId(macPoolBuilder.reset().persist().getId());
        }
    }

    @Override
    protected void preBuild() {
        populate();
    }

    private void populate() {
        if (Guid.isNullOrEmpty(object.getId())) {
            object.setId(Guid.newGuid());
        }
        if (object.getCompatibilityVersion() == null) {
            object.setCompatibilityVersion(Version.getLast());
        }
        if (object.getArchitecture() == null) {
            object.setArchitecture(ArchitectureType.x86_64);
        }
    }

    @Override
    public ClusterBuilder reset() {
        object = new Cluster();
        macPool = null;
        return this;
    }

    @Override
    public ClusterBuilder reset(Cluster object) {
        this.object = object;
        return this;
    }

    @Override
    protected Cluster doPersist() {
        clusterDao.save(object);
        return clusterDao.get(object.getId());
    }
}
