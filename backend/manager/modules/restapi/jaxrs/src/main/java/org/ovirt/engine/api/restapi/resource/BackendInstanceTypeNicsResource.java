package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeNicsResource extends BackendTemplateNicsResource {
    public BackendInstanceTypeNicsResource(Guid parentId) {
        super(parentId);
    }

    @Override
    public Nic addParents(Nic nic) {
        InstanceType parent = new InstanceType();
        parent.setId(parentId.toString());
        nic.setInstanceType(parent);
        return nic;
    }
}
