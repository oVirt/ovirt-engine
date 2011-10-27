package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendGroupsResource extends BackendGroupsResource
{
    public AbstractBackendGroupsResource(String id, BackendDomainResource parent) {
        super(id,parent);
    }

    Group lookupGroup(Guid guid){
        String id = guid.toString();
        for(ad_groups group: getGroupsFromDomain()){
                if(group.getid().toString().equals(id)) return mapAdGroup(group);
        }
        return notFound();
    }
}
