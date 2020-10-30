package org.ovirt.engine.api.restapi.resource.aaa;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSSHPublicKeyResource
        extends AbstractBackendSubResource<SshPublicKey, UserProfileProperty>
        implements SshPublicKeyResource {

    private final BackendSSHPublicKeysResource parent;

    public BackendSSHPublicKeyResource(String id, BackendSSHPublicKeysResource parent) {
        super(id, SshPublicKey.class, UserProfileProperty.class);
        this.parent = parent;
    }

    @Override
    public SshPublicKey get() {
        return performGet(QueryType.GetUserProfileProperty, new IdQueryParameters(guid));
    }

    @Override
    protected SshPublicKey addParents(SshPublicKey pubkey) {
        return parent.addParents(pubkey);
    }

    UserProfileProperty getEntity(Guid id) {
        return getEntity(
                UserProfileProperty.class,
                QueryType.GetUserProfileProperty,
                new IdQueryParameters(id),
                id.toString(),
                true
        );
    }

    @Override
    public SshPublicKey update(SshPublicKey update) {
        UserProfileProperty existingEntity = getEntity(guid);
        validateUpdate(update, map(existingEntity));
        Guid newKeyId = performAction(ActionType.UpdateUserProfileProperty,
                new UserProfilePropertyParameters(map(update, existingEntity)),
                Guid.class);
        return addParents(addLinks(map(getEntity(newKeyId))));
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveUserProfileProperty, new IdParameters(guid));
    }
}
