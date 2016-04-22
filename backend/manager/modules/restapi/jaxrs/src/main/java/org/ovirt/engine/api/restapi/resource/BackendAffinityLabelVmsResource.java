package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.AffinityLabelVmResource;
import org.ovirt.engine.api.resource.AffinityLabelVmsResource;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendAffinityLabelVmsResource extends AbstractBackendAffinityLabelledEntitiesResource<Vm, Vms, VM>
        implements AffinityLabelVmsResource {
    public BackendAffinityLabelVmsResource(String parentId) {
        super(parentId, Vm.class, VM.class, VM::new);
    }

    @Override
    public Response add(Vm entity) {
        return super.add(entity);
    }

    @Override
    protected Vms mapCollection(List<VM> entities) {
        Vms vms = new Vms();
        for (VM vm: entities) {
            Vm vmModel = new Vm();
            vmModel.setId(vm.getId().toString());
            vms.getVms().add(addLinks(vmModel));
        }
        return vms;
    }

    @Override
    public Vms list() {
        Label label = getLabel();
        return mapCollection(label.getVms().stream().map(g -> {
            VM vm = new VM();
            vm.setId(g);
            return vm;
        }).collect(Collectors.toList()));
    }

    @Override
    public AffinityLabelVmResource getVmResource(@PathParam("id") String id) {
        return inject(new BackendAffinityLabelVmResource(parentId, id));
    }
}
