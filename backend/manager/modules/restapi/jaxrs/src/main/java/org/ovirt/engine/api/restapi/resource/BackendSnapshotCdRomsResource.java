package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.SnapshotCdromResource;
import org.ovirt.engine.api.resource.SnapshotCdromsResource;
import org.ovirt.engine.api.restapi.types.CdRomMapper;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendSnapshotCdRomsResource
        extends AbstractBackendCollectionResource<Cdrom, Snapshot>
        implements SnapshotCdromsResource {

    protected BackendSnapshotResource parent;

    public BackendSnapshotCdRomsResource(BackendSnapshotResource parent) {
        super(Cdrom.class, Snapshot.class);
        this.parent = parent;
    }

    @Override
    public Cdroms list() {
        Cdroms cdRoms = new Cdroms();
        if (parent.getSnapshot().isVmConfigurationAvailable()) {
            VM vm = parent.collection.getVmPreview(parent.get());
            cdRoms.getCdroms().add(CdRomMapper.map(vm, null)); //notice currently only 1 cd-rom per VM supported.
        }
        return cdRoms;
    }

    @Override
    public SnapshotCdromResource getCdromResource(String id) {
        return new BackendSnapshotCdRomResource(id, this);
    }
}
