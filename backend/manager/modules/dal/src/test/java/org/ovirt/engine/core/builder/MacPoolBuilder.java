package org.ovirt.engine.core.builder;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.springframework.stereotype.Repository;

@Repository
public class MacPoolBuilder extends AbstractBuilder<MacPool, MacPoolBuilder> {

    @Inject
    MacPoolDao macPoolDao;

    public MacPoolBuilder id(final Guid hostId) {
        object.setId(hostId);
        return this;
    }

    @Override
    public MacPoolBuilder reset() {
        object = new MacPool();
        return this;
    }

    @Override
    public MacPoolBuilder reset(MacPool object) {
        this.object = object;
        return this;
    }

    @Override
    protected void preBuild() {
        if (Guid.isNullOrEmpty(object.getId())) {
            object.setId(Guid.newGuid());
        }
    }

    @Override
    protected void prePersist() {
        if (Guid.isNullOrEmpty(object.getId())) {
            object.setId(Guid.newGuid());
        }
    }

    @Override
    protected MacPool doPersist() {
        macPoolDao.save(object);
        return macPoolDao.get(object.getId());
    }
}
