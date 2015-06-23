package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.SnapshotNicResource;
import org.ovirt.engine.api.resource.SnapshotNicsResource;
import org.ovirt.engine.api.restapi.types.NicMapper;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

import javax.ws.rs.core.Response;

public class BackendSnapshotNicsResource extends AbstractBackendCollectionResource<NIC, Snapshot> implements SnapshotNicsResource {

    protected BackendSnapshotResource parent;

    public BackendSnapshotNicsResource(BackendSnapshotResource parent) {
        super(NIC.class, Snapshot.class);
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
    public SnapshotNicResource getNicSubResource(String id) {
        return new BackendSnapshotNicResource(id, this);
    }

    @Override
    protected Response performRemove(String id) {
        throw new UnsupportedOperationException();
    }
}
