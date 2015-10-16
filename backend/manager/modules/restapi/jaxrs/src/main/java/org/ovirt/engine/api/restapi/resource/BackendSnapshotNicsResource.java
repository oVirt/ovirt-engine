package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.SnapshotNicResource;
import org.ovirt.engine.api.resource.SnapshotNicsResource;
import org.ovirt.engine.api.restapi.types.NicMapper;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class BackendSnapshotNicsResource extends AbstractBackendCollectionResource<Nic, Snapshot> implements SnapshotNicsResource {

    protected BackendSnapshotResource parent;

    public BackendSnapshotNicsResource(BackendSnapshotResource parent) {
        super(Nic.class, Snapshot.class);
        this.parent = parent;
    }

    @Override
    public Nics list() {
        Nics nics = new Nics();
        if (parent.getSnapshot().isVmConfigurationAvailable()) {
            VM vm = parent.collection.getVmPreview(parent.get());
            for (VmNetworkInterface nic : vm.getInterfaces()) {
                nics.getNics().add(NicMapper.map(nic, null));
            }
        }
        return nics;
    }

    @Override
    public SnapshotNicResource getNicResource(String id) {
        return new BackendSnapshotNicResource(id, this);
    }
}
