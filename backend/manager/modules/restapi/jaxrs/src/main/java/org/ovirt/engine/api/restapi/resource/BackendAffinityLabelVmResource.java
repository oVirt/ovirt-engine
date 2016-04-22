package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.AffinityLabelVmResource;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendAffinityLabelVmResource extends AbstractBackendAffinityLabelledEntityResource<Vm, VM>
        implements AffinityLabelVmResource {
    public BackendAffinityLabelVmResource(String parentId, String id) {
        super(parentId, VM::new, id, Vm.class, VM.class);
    }
}
