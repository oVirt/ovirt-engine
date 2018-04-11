package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.di.Injector;

/**
 * This builder is responsible to create the memory volumes for stateless snapshot -
 * it just take the memory volume of the previously active snapshot
 */
public class StatelessSnapshotMemoryImageBuilder implements MemoryImageBuilder {

    private final Snapshot activeSnapshot;

    public StatelessSnapshotMemoryImageBuilder(VM vm) {
        activeSnapshot = getSnapshotDao().get(vm.getId(), SnapshotType.ACTIVE);
    }

    @Override
    public void build() {
        //no op
    }

    @Override
    public Guid getMemoryDiskId() {
        return activeSnapshot.getMemoryDiskId();
    }

    @Override
    public Guid getMetadataDiskId() {
        return activeSnapshot.getMetadataDiskId();
    }

    @Override
    public boolean isCreateTasks() {
        return false;
    }

    protected SnapshotDao getSnapshotDao() {
        return Injector.get(SnapshotDao.class);
    }
}
