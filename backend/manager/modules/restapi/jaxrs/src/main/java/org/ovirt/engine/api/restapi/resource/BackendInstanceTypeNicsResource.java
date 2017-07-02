package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.InstanceTypeNicResource;
import org.ovirt.engine.api.resource.InstanceTypeNicsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeNicsResource extends AbstractBackendNicsResource implements InstanceTypeNicsResource {
    private Guid instanceTypeId;

    public BackendInstanceTypeNicsResource(Guid instanceTypeId) {
        super(instanceTypeId, QueryType.GetTemplateInterfacesByTemplateId);
        this.instanceTypeId = instanceTypeId;
    }

    public Nics list() {
        Nics nics = new Nics();
        List<VmNetworkInterface> entities = getBackendCollection(
            QueryType.GetTemplateInterfacesByTemplateId,
            new IdQueryParameters(instanceTypeId)
        );
        for (VmNetworkInterface entity : entities) {
            Nic nic = populate(map(entity), entity);
            nics.getNics().add(addLinks(nic));
        }
        return nics;
    }

    public Response add(Nic nic) {
        validateParameters(nic, "name");
        return performCreate(
            ActionType.AddVmTemplateInterface,
            new AddVmTemplateInterfaceParameters(instanceTypeId, map(nic)),
            new NicResolver(nic.getName())
        );
    }

    @Override
    public InstanceTypeNicResource getNicResource(String id) {
        return inject(new BackendInstanceTypeNicResource(id, instanceTypeId));
    }

    @Override
    public Nic addParents(Nic nic) {
        InstanceType instanceType = new InstanceType();
        instanceType.setId(instanceTypeId.toString());
        nic.setInstanceType(instanceType);
        return nic;
    }
}
