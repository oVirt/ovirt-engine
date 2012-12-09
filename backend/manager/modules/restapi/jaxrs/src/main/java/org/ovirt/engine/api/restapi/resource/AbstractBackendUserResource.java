package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.DbUser;

public class AbstractBackendUserResource
    extends AbstractBackendSubResource<User, DbUser> {

    protected AbstractBackendUsersResource parent;

    public AbstractBackendUserResource(String id, AbstractBackendUsersResource parent) {
        super(id, User.class, DbUser.class);
        this.parent = parent;
    }

    public User get() {
        User entity = parent.lookupUser(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(entity);
    }

    AbstractBackendUsersResource getParent() {
        return parent;
    }

    @Override
    protected User doPopulate(User model, DbUser entity) {
        return model;
    }
}
