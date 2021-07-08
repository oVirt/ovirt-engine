package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.BuiltResponse;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.AffinityGroupVmResource;
import org.ovirt.engine.api.resource.AffinityGroupVmsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
import org.ovirt.engine.core.compat.Guid;


public class BackendAffinityGroupVmsResource
        extends BackendAffinityGroupSubListResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements AffinityGroupVmsResource {

    public BackendAffinityGroupVmsResource(Guid affinityGroupId) {
        super(affinityGroupId, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    public Vms list() {
        Vms vms = new Vms();
        vms.getVms().addAll(listResources(
                AffinityGroup::getVmIds,
                AffinityGroup::getVmEntityNames,
                (id, name) -> {
                    Vm vm = new Vm();
                    vm.setId(id.toString());
                    vm.setName(name);
                    return vm;
                }));

        return vms;
    }

    @Override
    public Response add(Vm vm) {
        Response response = performAction(ActionType.AddVmToAffinityGroup,
                new AffinityGroupMemberChangeParameters(getAffinityGroupId(), asGuid(vm.getId())));
        return BuiltResponse.fromResponse(response).entity(vm).build();
    }

    @Override
    public AffinityGroupVmResource getVmResource(String id) {
        return inject(new BackendAffinityGroupVmResource(getAffinityGroupId(), id));
    }
}
