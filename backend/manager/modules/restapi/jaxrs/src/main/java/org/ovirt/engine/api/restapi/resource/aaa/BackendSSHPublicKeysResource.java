package org.ovirt.engine.api.restapi.resource.aaa;

import java.util.List;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.model.SshPublicKeys;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeysResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSSHPublicKeysResource
        extends AbstractBackendCollectionResource<SshPublicKey, UserProfile>
        implements SshPublicKeysResource {

    private Guid userId;

    public BackendSSHPublicKeysResource(Guid userId) {
        super(SshPublicKey.class, UserProfile.class);
        this.userId = userId;
    }

    @Override
    public SshPublicKeys list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetUserProfileAsList,
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
        validateParameters(pubkey, "content");

        UserProfileParameters params = new UserProfileParameters();
        UserProfile profile = map(pubkey);
        profile.setUserId(userId);
        params.setUserProfile(profile);

        return performAction(VdcActionType.AddUserProfile, params);
    }

    @Override
    public SshPublicKeyResource getKeyResource(String id) {
        return inject(new BackendSSHPublicKeyResource(id, userId, this));
    }

    protected SshPublicKeys mapCollection(List<UserProfile> entities) {
        SshPublicKeys collection = new SshPublicKeys();
        for (UserProfile entity : entities) {
            if (!StringUtils.isEmpty(entity.getSshPublicKey())) {
                collection.getSshPublicKeys().add(addParents(addLinks(map(entity))));
            }
        }
        return collection;
    }
}
