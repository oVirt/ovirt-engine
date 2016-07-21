package org.ovirt.engine.api.restapi.resource.aaa;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.resource.aaa.DomainUserResource;
import org.ovirt.engine.api.resource.aaa.DomainUsersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.api.restapi.resource.ResourceConstants;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to the users that exist in a directory accessible
 * to the engine. Those users may or may not have been added to the engine
 * database and the engine can't modify them, and thus the resource doesn't
 * provide any method to modify the collection.
 */
public class BackendDomainUsersResource
       extends AbstractBackendSubResource<User, DirectoryUser>
       implements DomainUsersResource {

    private BackendDomainResource parent;

    public BackendDomainUsersResource(String id, BackendDomainResource parent) {
        super(id, User.class, DirectoryUser.class);
        this.parent = parent;
    }

    public void setParent(BackendDomainResource parent) {
        this.parent = parent;
    }

    public BackendDomainResource getParent() {
        return parent;
    }

    public Domain getDirectory() {
        return parent.getDirectory();
    }

    @Override
    public DomainUserResource getUserResource(String id) {
        return inject(new BackendDomainUserResource(id, this));
    }

    private String getSearchPattern() {
        String constraint = QueryHelper.getConstraint(httpHeaders, uriInfo, DirectoryUser.class, false);
        StringBuilder sb = new StringBuilder(128);
        sb.append(MessageFormat.format(ResourceConstants.AAA_PRINCIPALS_SEARCH_TEMPLATE, parent.getDirectory().getName(), ""));
        sb.append(StringUtils.isEmpty(constraint)? "allnames=*": constraint);
        return sb.toString();
    }

    private List<DirectoryUser> getDomainUsers() {
        return asCollection(DirectoryUser.class, getEntity(List.class, SearchType.DirectoryUser, getSearchPattern()));
    }

    private Users mapUsers(List<DirectoryUser> entities) {
        Users collection = new Users();
        for (DirectoryUser entity : entities) {
            User user = map(entity);
            user = populate(user, entity);
            user = addLinks(user, true);
            collection.getUsers().add(user);
        }
        return collection;
    }

    @Override
    public Users list() {
        return mapUsers(getDomainUsers());
    }

    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }

}
