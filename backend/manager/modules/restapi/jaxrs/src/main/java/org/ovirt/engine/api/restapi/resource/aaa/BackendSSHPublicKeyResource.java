package org.ovirt.engine.api.restapi.resource.aaa;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSSHPublicKeyResource
    extends AbstractBackendSubResource<SshPublicKey, UserProfile>
    implements SshPublicKeyResource {

    private Guid userId;

    private BackendSSHPublicKeysResource parent;

    public BackendSSHPublicKeyResource(String id, Guid userId, BackendSSHPublicKeysResource parent) {
        super(id, SshPublicKey.class, UserProfile.class);
        this.userId = userId;
        this.parent = parent;
    }

    @Override
    public SshPublicKey get() {
        return performGet(VdcQueryType.GetUserProfile, new IdQueryParameters(userId));
    }

    @Override
    protected SshPublicKey addParents(SshPublicKey pubkey) {
        return parent.addParents(pubkey);
    }

    @Override
    public SshPublicKey update(SshPublicKey pubkey) {
        return performUpdate(pubkey,
                new QueryIdResolver<>(VdcQueryType.GetUserProfile, IdQueryParameters.class),
                VdcActionType.UpdateUserProfile,
                new UpdateParametersProvider());
    }

    public class UpdateParametersProvider implements ParametersProvider<SshPublicKey, UserProfile> {
        @Override
        public VdcActionParametersBase getParameters(SshPublicKey model, UserProfile entity) {
            UserProfileParameters params = new UserProfileParameters();
            UserProfile profile = map(model, entity);

            profile.setUserId(userId);
            if (Guid.isNullOrEmpty(profile.getSshPublicKeyId())) {
                profile.setSshPublicKeyId(Guid.newGuid());
            }

            params.setUserProfile(profile);
            return params;
        }
    }

    @Override
    public Response remove() {
        // we cannot just remove UserProfile, because we'll wipe out unrelated fields.
        // Instead, we just clear the public key fields.

        UserProfile entity = getEntity(
                UserProfile.class,
                VdcQueryType.GetUserProfile,
                new IdQueryParameters(userId),
                userId.toString(),
                true
        );

        entity.setSshPublicKeyId(Guid.Empty);
        entity.setSshPublicKey("");

        UserProfileParameters parameters = new UserProfileParameters();
        parameters.setUserProfile(entity);

        return performAction(VdcActionType.UpdateUserProfile, parameters);
    }
}
