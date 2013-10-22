package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This builder is responsible to create the memory volumes for stateless snapshot -
 * it just take the memory volume of the previously active snapshot
 */
public class StatelessSnapshotMemoryImageBuilder implements MemoryImageBuilder {

    private final String memoryVolumesFromActiveSnapshot;

    public StatelessSnapshotMemoryImageBuilder(VM vm) {
        memoryVolumesFromActiveSnapshot =
                DbFacade.getInstance().getSnapshotDao().get(vm.getId(), SnapshotType.ACTIVE)
                .getMemoryVolume();
    }

    public void build() {
      //no op
    }

    public String getVolumeStringRepresentation() {
        return memoryVolumesFromActiveSnapshot;
    }

    public boolean isCreateTasks() {
        return false;
    }
}
