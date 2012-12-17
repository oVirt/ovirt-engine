package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendGroupsResource extends BackendGroupsResourceBase
{
    public AbstractBackendGroupsResource(String id, BackendDomainResource parent) {
        super(id,parent);
    }

    Group lookupGroup(Guid guid){
        String id = guid.toString();
        for(LdapGroup group: getGroupsFromDomain()){
                if(group.getid().toString().equals(id)) return mapAdGroup(group);
        }
        return notFound();
    }
}
