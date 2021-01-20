package org.ovirt.engine.api.restapi.resource.aaa;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;

public abstract class AbstractBackendUserProfilePropertyResource<T extends BaseResource>
        extends AbstractBackendSubResource<T, UserProfileProperty> {

    private final PropertyType propertyType;

    public AbstractBackendUserProfilePropertyResource(String id, Class<T> clazz, PropertyType type) {
        super(id, clazz, UserProfileProperty.class);
        this.propertyType = type;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public T get() {
        return performGet(QueryType.GetUserProfileProperty,
                new UserProfilePropertyIdQueryParameters(guid, getPropertyType()));
    }

    public Response remove() {
        get();
        return performAction(ActionType.RemoveUserProfileProperty, new IdParameters(guid));
    }
}
