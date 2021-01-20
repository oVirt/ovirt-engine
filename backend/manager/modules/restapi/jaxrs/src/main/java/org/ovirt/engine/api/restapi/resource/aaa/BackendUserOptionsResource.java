package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.JSON;

import java.util.List;

import org.ovirt.engine.api.model.UserOption;
import org.ovirt.engine.api.model.UserOptions;
import org.ovirt.engine.api.resource.aaa.UserOptionResource;
import org.ovirt.engine.api.resource.aaa.UserOptionsResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendUserOptionsResource extends AbstractBackendUserProfilePropertiesResource<UserOption>
        implements UserOptionsResource {

    public BackendUserOptionsResource(Guid userId) {
        super(userId, UserOption.class, JSON);
    }

    @Override
    public UserOptions list() {
        return wrap(getBackendCollection());
    }

    private UserOptions wrap(List<UserOption> backendCollection) {
        UserOptions options = new UserOptions();
        options.getUserOptions().addAll(backendCollection);
        return options;
    }

    @Override
    public UserOption addParents(UserOption option) {
        option.setUser(getParent());
        return option;
    }

    @Override
    public UserOptionResource getOptionResource(String id) {
        return inject(new BackendUserOptionResource(id, this));
    }
}
