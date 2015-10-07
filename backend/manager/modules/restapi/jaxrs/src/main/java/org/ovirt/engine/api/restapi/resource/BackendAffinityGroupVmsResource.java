package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.AffinityGroupVmResource;
import org.ovirt.engine.api.resource.AffinityGroupVmsResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;


public class BackendAffinityGroupVmsResource extends AbstractBackendCollectionResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements AffinityGroupVmsResource {
    private final Guid affinityGroupId;

    public BackendAffinityGroupVmsResource(Guid affinityGroupId) {
        super(Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
        this.affinityGroupId = affinityGroupId;
    }

    @Override
    public Vms list() {
        Vms vms = new Vms();
        AffinityGroup affinityGroup = getEntity();

        if (affinityGroup.getEntityIds() != null) {
            for (int i = 0; i < affinityGroup.getEntityIds().size(); i++) {
                Vm vm = new Vm();
                vm.setId(affinityGroup.getEntityIds().get(i).toString());
                vm.setName(affinityGroup.getEntityNames().get(i));
                vm = addLinks(populate(vm, null));
                // remove vm actions, not relevant to this context
                vm.setActions(null);
                vms.getVms().add(vm);
            }
        }

        return vms;
    }

    @Override
    public Response add(Vm vm) {
        AffinityGroup affinityGroup = getEntity();

        affinityGroup.getEntityIds().add(asGuid(vm.getId()));
        return performAction(VdcActionType.EditAffinityGroup, new AffinityGroupCRUDParameters(affinityGroup.getId(),
                affinityGroup));
    }

    @Override
    protected org.ovirt.engine.core.common.scheduling.AffinityGroup getEntity() {
        return getEntity(org.ovirt.engine.core.common.scheduling.AffinityGroup.class,
                VdcQueryType.GetAffinityGroupById,
                new IdQueryParameters(affinityGroupId),
                affinityGroupId.toString());
    }

    @Override
    public AffinityGroupVmResource getVmResource(String id) {
        return inject(new BackendAffinityGroupVmResource(affinityGroupId, id));
    }
}
