package org.ovirt.engine.api.restapi.resource.aaa;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.model.SshPublicKeys;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeysResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSSHPublicKeysResource
        extends AbstractBackendCollectionResource<SshPublicKey, UserProfileProperty>
        implements SshPublicKeysResource {

    private final Guid userId;

    public BackendSSHPublicKeysResource(Guid userId) {
        super(SshPublicKey.class, UserProfileProperty.class);
        this.userId = userId;
    }

    @Override
    public SshPublicKeys list() {
        return mapCollection(getBackendCollection(QueryType.GetUserProfilePropertiesByUserId,
                new IdQueryParameters(userId)));
    }

    @Override
    public SshPublicKey addParents(SshPublicKey pubkey) {
        User parent = pubkey.getUser();
        if (parent == null) {
            parent = new User();
            pubkey.setUser(parent);
        }
        parent.setId(userId.toString());
        return pubkey;
    }

    @Override
    public Response add(SshPublicKey pubkey) {
        return performCreate(
                ActionType.AddUserProfileProperty,
                new UserProfilePropertyParameters(UserProfileProperty.builder()
                        .from(map(pubkey))
                        .withUserId(userId)
                        .build()),
                new QueryIdResolver<Guid>(QueryType.GetUserProfileProperty, IdQueryParameters.class));
    }

    @Override
    public SshPublicKeyResource getKeyResource(String id) {
        return inject(new BackendSSHPublicKeyResource(id, this));
    }

    protected SshPublicKeys mapCollection(List<UserProfileProperty> entities) {
        SshPublicKeys collection = new SshPublicKeys();
        List<SshPublicKey> keys = entities.stream()
                .filter(UserProfileProperty::isSshPublicKey)
                .map(this::map)
                .map(this::addLinks)
                .map(this::addParents)
                .collect(Collectors.toList());
        collection.getSshPublicKeys().addAll(keys);
        return collection;
    }
}
