package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.SnapshotCdRomResource;
import org.ovirt.engine.api.resource.SnapshotCdRomsResource;
import org.ovirt.engine.api.restapi.types.CdRomMapper;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendSnapshotCdRomsResource extends AbstractBackendCollectionResource<CdRom, Snapshot> implements SnapshotCdRomsResource {

    protected BackendSnapshotResource parent;

    public BackendSnapshotCdRomsResource(BackendSnapshotResource parent) {
        super(CdRom.class, Snapshot.class);
        this.parent = parent;
    }

    @Override
    public CdRoms list() {
        CdRoms cdRoms = new CdRoms();
        if (parent.getSnapshot().isVmConfigurationAvailable()) {
            VM vm = parent.collection.getVmPreview(parent.get());
            cdRoms.getCdRoms().add(CdRomMapper.map(vm, null)); //notice currently only 1 cd-rom per VM supported.
        }
        return cdRoms;
    }

    @Override
    public SnapshotCdRomResource getCdRomSubResource(String id) {
        return new BackendSnapshotCdRomResource(id, this);
    }
}
