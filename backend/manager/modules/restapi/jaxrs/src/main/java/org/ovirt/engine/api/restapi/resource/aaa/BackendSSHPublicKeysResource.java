package org.ovirt.engine.api.restapi.resource.aaa;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.SSHPublicKey;
import org.ovirt.engine.api.model.SSHPublicKeys;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.aaa.SSHPublicKeyResource;
import org.ovirt.engine.api.resource.aaa.SSHPublicKeysResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendSSHPublicKeysResource
        extends AbstractBackendCollectionResource<SSHPublicKey, UserProfile>
        implements SSHPublicKeysResource {

    private Guid userId;

    public BackendSSHPublicKeysResource(Guid userId) {
        super(SSHPublicKey.class, UserProfile.class);
        this.userId = userId;
    }

    @Override
    public SSHPublicKeys list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetUserProfileAsList,
                                                  new IdQueryParameters(userId)));
    }

    @Override
    public SSHPublicKey addParents(SSHPublicKey pubkey) {
        User parent = pubkey.getUser();
        if (parent == null) {
            parent = new User();
            pubkey.setUser(parent);
        }
        parent.setId(userId.toString());
        return pubkey;
    }

    @Override
    public Response add(SSHPublicKey pubkey) {
        validateParameters(pubkey, "content");

        UserProfileParameters params = new UserProfileParameters();
        UserProfile profile = map(pubkey);
        profile.setUserId(userId);
        params.setUserProfile(profile);

        return performAction(VdcActionType.AddUserProfile, params);
    }

    @Override
    public SSHPublicKeyResource getSSHPublicKeySubResource(String id) {
        return inject(new BackendSSHPublicKeyResource(id, userId, this));
    }

    protected SSHPublicKeys mapCollection(List<UserProfile> entities) {
        SSHPublicKeys collection = new SSHPublicKeys();
        for (UserProfile entity : entities) {
            if (!StringUtils.isEmpty(entity.getSshPublicKey())) {
                collection.getSSHPublicKeys().add(addParents(addLinks(map(entity))));
            }
        }
        return collection;
    }
}
