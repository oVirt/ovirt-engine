package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicsResource extends AbstractBackendNicsResource implements VmNicsResource {
    private Guid vmId;

    public BackendVmNicsResource(Guid vmId) {
        super(vmId, QueryType.GetVmInterfacesByVmId);
        this.vmId = vmId;
    }

    @Override
    public Nics list() {
        Nics nics = new Nics();
        List<VmNetworkInterface> entities = getBackendCollection(
            QueryType.GetVmInterfacesByVmId,
            new IdQueryParameters(vmId)
        );
        for (VmNetworkInterface entity : entities) {
            Nic nic = populate(map(entity), entity);
            nics.getNics().add(addLinks(nic));
        }
        return nics;
    }

    @Override
    public Response add(Nic nic) {
        validateParameters(nic, "name");
        return performCreate(
            ActionType.AddVmInterface,
            new AddVmInterfaceParameters(vmId, map(nic)),
            new NicResolver(nic.getName())
        );
    }

    @Override
    public VmNicResource getNicResource(String id) {
        return inject(new BackendVmNicResource(id, vmId));
    }

    @Override
    protected Nic addParents(Nic nic) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        nic.setVm(vm);
        return nic;
    }
}
