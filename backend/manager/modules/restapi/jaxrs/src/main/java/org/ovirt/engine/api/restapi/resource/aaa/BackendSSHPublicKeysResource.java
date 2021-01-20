package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import java.util.List;

import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.model.SshPublicKeys;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeysResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendSSHPublicKeysResource
        extends AbstractBackendUserProfilePropertiesResource<SshPublicKey>
        implements SshPublicKeysResource {


    public BackendSSHPublicKeysResource(Guid userId) {
        super(userId, SshPublicKey.class, SSH_PUBLIC_KEY);
    }

    @Override
    public SshPublicKeys list() {
        return wrap(getBackendCollection());
    }

    private SshPublicKeys wrap(List<SshPublicKey> backendCollection) {
        SshPublicKeys collection = new SshPublicKeys();
        collection.getSshPublicKeys().addAll(backendCollection);
        return collection;
    }

    @Override
    public SshPublicKey addParents(SshPublicKey key) {
        key.setUser(getParent());
        return key;
    }

    @Override
    public SshPublicKeyResource getKeyResource(String id) {
        return inject(new BackendSSHPublicKeyResource(id, this));
    }
}
