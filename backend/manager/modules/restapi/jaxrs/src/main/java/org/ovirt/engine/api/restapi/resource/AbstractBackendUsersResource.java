package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendUsersResource extends BackendUsersResourceBase
{
    public AbstractBackendUsersResource(String id, BackendDomainResource parent) {
        super(id,parent);
    }

    User lookupUser(Guid guid){
        String id = guid.toString();
        for(LdapUser user: getUsersFromDomain()){
                if(user.getUserId().toString().equals(id)) return mapAdUser(user);
        }
        return notFound();
    }
}
