package org.ovirt.engine.api.restapi.resource.aaa;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendUserProfilePropertiesResource<T extends BaseResource>
        extends AbstractBackendCollectionResource<T, UserProfileProperty> {
    private final Guid userId;
    private final PropertyType propertyType;
    private final User parent;

    public AbstractBackendUserProfilePropertiesResource(Guid userId, Class<T> clazz, PropertyType type) {
        super(clazz, UserProfileProperty.class);
        this.userId = userId;
        this.parent = new User();
        parent.setId(userId.toString());
        this.propertyType = type;
    }

    public Response add(T entity) {
        return performCreate(
                ActionType.AddUserProfileProperty,
                new UserProfilePropertyParameters(UserProfileProperty.builder()
                        .from(map(entity))
                        .withUserId(userId)
                        .build()),
                new EntityIdResolver<Guid>() {
                    @Override
                    public UserProfileProperty lookupEntity(Guid id) throws BackendFailureException {
                        return doGetEntity(entityType,
                                QueryType.GetUserProfileProperty,
                                new UserProfilePropertyIdQueryParameters(id,
                                        propertyType),
                                id.toString());
                    }
                });

    }

    protected User getParent() {
        return parent;
    }

    protected List<T> getBackendCollection() {
        return getBackendCollection(
                QueryType.GetUserProfilePropertiesByUserId,
                new UserProfilePropertyIdQueryParameters(userId, propertyType)
        )
                .stream()
                .map(this::map)
                .map(this::addLinks)
                .map(this::addParents)
                .collect(Collectors.toList());
    }
}
