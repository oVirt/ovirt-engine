package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.SnapshotNicResource;
import org.ovirt.engine.api.resource.SnapshotNicsResource;
import org.ovirt.engine.api.restapi.types.NicMapper;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class BackendSnapshotNicsResource extends BackendSnapshotElementsResource implements SnapshotNicsResource {

    public BackendSnapshotNicsResource(BackendSnapshotResource parent, String vmId) {
        super(parent, vmId);
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
}
