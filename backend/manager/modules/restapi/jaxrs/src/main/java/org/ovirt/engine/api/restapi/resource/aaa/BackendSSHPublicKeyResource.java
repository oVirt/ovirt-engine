package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendSSHPublicKeyResource
        extends AbstractBackendUserProfilePropertyResource<SshPublicKey>
        implements SshPublicKeyResource {

    private final BackendSSHPublicKeysResource parent;

    public BackendSSHPublicKeyResource(String id, BackendSSHPublicKeysResource parent) {
        super(id, SshPublicKey.class, SSH_PUBLIC_KEY);
        this.parent = parent;
    }

    UserProfileProperty getProperty(Guid id) {
        return getEntity(
                UserProfileProperty.class,
                QueryType.GetUserProfileProperty,
                new UserProfilePropertyIdQueryParameters(id, getPropertyType()),
                id.toString(),
                true
        );
    }

    /**
     * Custom implementation required because a new entity is returned (this resource is immutable).
     */
    @Override
    public SshPublicKey update(SshPublicKey update) {
        UserProfileProperty existingEntity = getProperty(guid);
        validateUpdate(update, map(existingEntity));
        UserProfileProperty updated = performAction(
                ActionType.UpdateUserProfileProperty,
                new UserProfilePropertyParameters(map(update, existingEntity)),
                UserProfileProperty.class);
        if (updated == null) {
            // nothing was updated - despite upfront validation the update was illegal
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
        }
        return addParents(addLinks(map(updated)));
    }

    @Override
    protected SshPublicKey addParents(SshPublicKey key) {
        return parent.addParents(key);
    }
}
