package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.JSON;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.UserOption;
import org.ovirt.engine.api.model.UserOptions;
import org.ovirt.engine.api.resource.aaa.UserOptionResource;

public class BackendUserOptionResource extends AbstractBackendUserProfilePropertyResource<UserOption>
        implements UserOptionResource {

    private final BackendUserOptionsResource parent;

    public BackendUserOptionResource(String id, BackendUserOptionsResource parent) {
        super(id, UserOption.class, JSON);
        this.parent = parent;
    }

    @Override
    protected UserOption addParents(UserOption option) {
        return parent.addParents(option);
    }

    public static User addUserProperties(User user, UserOptions options) {
        if (user == null) {
            return null;
        }
        List<Property> props = options.getUserOptions().stream()
                .map(BackendUserOptionResource::toProperty)
                .collect(Collectors.toList());
        if (!props.isEmpty()) {
            Properties properties = new Properties();
            properties.getProperties().addAll(props);
            user.setUserOptions(properties);
        }
        return user;
    }

    private static Property toProperty(UserOption option) {
        Property result = new Property();
        result.setName(option.getName());
        result.setValue(option.getContent());
        return result;
    }
}
