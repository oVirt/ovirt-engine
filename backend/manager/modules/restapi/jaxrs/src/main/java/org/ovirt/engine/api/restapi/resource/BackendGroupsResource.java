package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.resource.GroupResource;
import org.ovirt.engine.api.resource.GroupsResource;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;

public class BackendGroupsResource extends BackendGroupsResourceBase implements GroupsResource {

    public BackendGroupsResource() {
        super(Group.class, LdapGroup.class, SUB_COLLECTIONS);
    }

    public BackendGroupsResource(String id, BackendDomainResource parent) {
        super(id, parent);
    }

    @Override
    public Groups list() {
        return mapDbGroupsCollection(getGroupsCollection(SearchType.DBUser, getSearchPattern()));
    }

    @Override
    public Response add(Group group) {
        validateParameters(group, "name");
        // somewhat counter-intuitively, the AddUserCommand is used
        // to add groups as well as users
        AddUserParameters newGroup = new AddUserParameters();
        newGroup.setAdGroup(getAdGroup(group));
        return performCreation(VdcActionType.AddUser, newGroup, new GroupIdResolver(newGroup.getAdGroup().getid()));
    }

    @Override
    @SingleEntityResource
    public GroupResource getGroupSubResource(String id) {
        return inject(new BackendGroupResource(id, this));
    }

}
